package org.mikhailv.ntnuitennis.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import org.mikhailv.ntnuitennis.R;

/**
 * Created by MikhailV on 18.01.2017.
 */

public class NotificationsActivity extends AppCompatActivity
{
    private static final String EXTRA_PAGER_POSITION = "NotificationsActivity.PAGER_POSITION";
    public static Intent newIntent(Context context, int pagerPosition)
    {
        Intent i = new Intent(context, NotificationsActivity.class);
        i.putExtra(EXTRA_PAGER_POSITION, pagerPosition);
        return i;
    }
    public static int decodePagerPosition(Intent i)
    {
        return i.getIntExtra(EXTRA_PAGER_POSITION, 0);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        FragmentManager fm = getSupportFragmentManager();
        Fragment checkListFragment = fm.findFragmentById(R.id.notifications_container);
        if (checkListFragment == null) {
            checkListFragment = new NotificationsFragment();
            fm.beginTransaction().add(R.id.notifications_container, checkListFragment).commit();
        }

        Intent i = new Intent();
        i.putExtra(EXTRA_PAGER_POSITION, getIntent().getIntExtra(EXTRA_PAGER_POSITION, 0));
        setResult(RESULT_OK, i);
    }
}
