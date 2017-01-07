package org.mikhailv.ntnuitennis.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.mikhailv.ntnuitennis.R;
import org.mikhailv.ntnuitennis.data.Globals;

public class PagerActivity extends AppCompatActivity
{
    private ViewPager m_viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);
        FragmentManager manager = getSupportFragmentManager();
        m_viewPager = (ViewPager)findViewById(R.id.activity_view_pager);
        m_viewPager.setAdapter(new FragmentPagerAdapter(manager)
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
    }
}
