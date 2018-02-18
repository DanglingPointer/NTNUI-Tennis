/*
 * MIT License
 *
 * Copyright (c) 2017-2018 Mikhail Vasilyev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.mikhailv.ntnuitennis.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.mikhailv.ntnuitennis.AppManager;
import org.mikhailv.ntnuitennis.R;
import org.mikhailv.ntnuitennis.data.Slot;
import org.mikhailv.ntnuitennis.data.SlotDetailsInfo;
import org.mikhailv.ntnuitennis.data.Week;
import org.mikhailv.ntnuitennis.net.NetworkCallbacks;
import org.mikhailv.ntnuitennis.net.NetworkFragment;

import static org.mikhailv.ntnuitennis.AppManager.WEEK_SIZE;

public class PagerActivity extends AppCompatActivity implements DayFragment.Callbacks,
                                                                NetworkCallbacks
{
    private static final String SAVED_POSITION = "PagerActivity.SAVED_POSITION";
    private static final int REQUEST_SLOT_DETAILS = 0;
    private static final int REQUEST_NOTIFICATIONS = 1;

    private ViewPager m_viewPager;
    private NetworkFragment m_networker;
    private DayFragment[] m_fragments = new DayFragment[WEEK_SIZE];

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);
        FragmentManager manager = getSupportFragmentManager();
        m_viewPager = (ViewPager)findViewById(R.id.activity_view_pager);
        m_viewPager.setAdapter(new FragmentStatePagerAdapter(manager)
        {
            @Override
            public Fragment getItem(int position)
            {
                DayFragment f = DayFragment.newInstance(position);
                return m_fragments[position] = f;
            }
            @Override
            public int getCount()
            {
                return WEEK_SIZE;
            }
        });
        if (savedInstanceState != null) {
            int position = savedInstanceState.getInt(SAVED_POSITION);
            m_viewPager.setCurrentItem(position);
        }
        m_networker = NetworkFragment.addInstance(getSupportFragmentManager());
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        m_networker.downloadTable();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_POSITION, m_viewPager.getCurrentItem());
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode != RESULT_OK)
            return;

        if (requestCode == REQUEST_SLOT_DETAILS && data != null) {
            m_viewPager.setCurrentItem(SlotDetailsActivity.decodePagerPosition(data));
        } else if (requestCode == REQUEST_NOTIFICATIONS && data != null) {
            m_viewPager.setCurrentItem(NotificationsActivity.decodePagerPosition(data));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    /**
     * Fragment callbacks
     */
    @Override
    public void onNotificationsPressed()
    {
        Intent i = NotificationsActivity.newIntent(this, m_viewPager.getCurrentItem());
        startActivityForResult(i, REQUEST_NOTIFICATIONS);
    }
    @Override
    public void onSlotDetailsPressed(int day, Slot slot)
    {
        Intent i = SlotDetailsActivity.newIntent(this, slot.getLink(), m_viewPager.getCurrentItem());
        startActivityForResult(i, REQUEST_SLOT_DETAILS);
    }
    @Override
    public void onLogInPressed()
    {
        if (m_networker != null){
            m_networker.authenticate(new AppManager.Credentials()
            {
                @Override
                public String getPassword() { return null; }
                @Override
                public String getEmail() { return null; }
                @Override
                public String getLanguage() { return null; }
            });
        }
    }
    @Override
    public void updateData()
    {
        if (m_networker != null)
            m_networker.downloadTable();
    }
    @Override
    public void eraseMe(DayFragment me)
    {
        for (int i = 0; i < m_fragments.length; ++i) {
            if (m_fragments[i] == me)
                m_fragments[i] = null;
        }
    }
    /**
     * Forward network callbacks to the current fragment
     */
    @Override
    public void onProgressChanged(int progress)
    {
        DayFragment currentFragment = m_fragments[m_viewPager.getCurrentItem()];
        if (currentFragment != null && currentFragment.isAdded())
            currentFragment.onProgressChanged(progress);
    }
    @Override
    public void onPreDownload()
    {
        DayFragment currentFragment = m_fragments[m_viewPager.getCurrentItem()];
        if (currentFragment != null && currentFragment.isAdded())
            currentFragment.onPreDownload();
    }
    @Override
    public void onTableFetched(Week week, Exception e)
    {
        for (DayFragment fr : m_fragments) {
            if (fr != null && fr.isAdded())
                fr.onTableFetched(week, e);
        }
    }
    @Override
    public void onSlotFetched(SlotDetailsInfo slotData, Exception e)
    {
        DayFragment currentFragment = m_fragments[m_viewPager.getCurrentItem()];
        if (currentFragment != null && currentFragment.isAdded())
            currentFragment.onSlotFetched(slotData, e);
    }
    @Override
    public void onDownloadCanceled()
    {
        DayFragment currentFragment = m_fragments[m_viewPager.getCurrentItem()];
        if (currentFragment != null && currentFragment.isAdded())
            currentFragment.onDownloadCanceled();
    }
    @Override
    public void onAuthenticateFinished(Exception e)
    {
        DayFragment currentFragment = m_fragments[m_viewPager.getCurrentItem()];
        if (currentFragment != null && currentFragment.isAdded())
            currentFragment.onAuthenticateFinished(e);
    }
}
