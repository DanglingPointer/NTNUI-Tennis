package org.mikhailv.ntnuitennis.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.mikhailv.ntnuitennis.R;
import org.mikhailv.ntnuitennis.data.Globals;
import org.mikhailv.ntnuitennis.data.Slot;
import org.mikhailv.ntnuitennis.data.SlotDetailsInfo;
import org.mikhailv.ntnuitennis.net.NetworkCallbacks;
import org.mikhailv.ntnuitennis.net.NetworkFragment;

import java.net.MalformedURLException;
import java.net.URL;

public class PagerActivity extends AppCompatActivity implements DayFragment.Callbacks,
                                                                NetworkCallbacks
{
    private ViewPager m_viewPager;
    private NetworkFragment m_networker;
    private DayFragment[] m_fragments = new DayFragment[Globals.Sizes.WEEK];

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
                return Globals.Sizes.WEEK;
            }
        });
        m_networker = NetworkFragment.addInstance(getSupportFragmentManager());
    }
    /**
     * Fragment callbacks
     */
    public void onNotificationsPressed()
    {
        startActivity(new Intent(this, NotificationsActivity.class));
    }
    public void onSlotDetailsPressed(int day, Slot slot)
    {
        Intent i = new Intent(this, SlotDetailsActivity.class);
        i.putExtra(SlotDetailsActivity.EXTRA_URL, slot.getLink());
        startActivity(i);
    }
    public void onAttendPressed(int day, Slot slot)
    {
        if (m_networker != null)
            m_networker.downloadSlot(slot.getAttendLink());
    }
    public void updateData()
    {
        if (m_networker != null)
            m_networker.downloadTable(Globals.HOME_URL);
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
    public void onPreExecute()
    {
        DayFragment currentFragment = m_fragments[m_viewPager.getCurrentItem()];
        if (currentFragment != null && currentFragment.isAdded())
            currentFragment.onPreExecute();
    }
    @Override
    public void onTableFetched(Exception e)
    {
        DayFragment currentFragment = m_fragments[m_viewPager.getCurrentItem()];
        if (currentFragment != null && currentFragment.isAdded())
            currentFragment.onTableFetched(e);
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
}
