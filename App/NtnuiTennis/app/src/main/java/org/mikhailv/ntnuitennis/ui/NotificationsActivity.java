package org.mikhailv.ntnuitennis.ui;

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
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        FragmentManager fm = getSupportFragmentManager();
        Fragment checkListFragment = fm.findFragmentById(R.id.notifications_container);
        if (checkListFragment == null){
            checkListFragment = new NotificationsFragment();
            fm.beginTransaction().add(R.id.notifications_container, checkListFragment).commit();
        }
    }
}
