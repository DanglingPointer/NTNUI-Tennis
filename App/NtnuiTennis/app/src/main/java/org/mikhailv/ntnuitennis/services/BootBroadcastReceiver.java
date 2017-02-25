package org.mikhailv.ntnuitennis.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.mikhailv.ntnuitennis.data.DBManager;

/**
 * Created by MikhailV on 25.02.2017.
 */

public class BootBroadcastReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        DBManager db = new DBManager(context);
        if (db.getTableSize() > 0) {
            NotifierService.setAlarm(context);
        }
    }
}
