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
import org.mikhailv.ntnuitennis.net.NetworkCallbacks;
import org.mikhailv.ntnuitennis.net.NetworkFragment;

public class PagerActivity extends AppCompatActivity implements DayFragment.Callbacks,
                                                                NetworkCallbacks
{
    private ViewPager m_viewPager;
    private NetworkFragment m_networker;

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
                return DayFragment.newInstance(position);
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

    }
    public void updateData()
    {

    }
    /**
     * Network callbacks
     */
    @Override
    public void onProgressChanged(int progress)
    {

    }
    @Override
    public void onPreExecute()
    {

    }
    @Override
    public void onTableFetched(Exception e)
    {

    }
    @Override
    public void onSlotFetched(String htmlPage, Exception e)
    {

    }
    @Override
    public void onDownloadCanceled()
    {

    }
}
