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

package org.mikhailv.ntnuitennis.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import org.mikhailv.ntnuitennis.R;
import org.mikhailv.ntnuitennis.data.DBManager;
import org.mikhailv.ntnuitennis.data.SessionInfo;
import org.mikhailv.ntnuitennis.ui.PagerActivity;
import org.mikhailv.ntnuitennis.ui.SlotDetailsActivity;

import java.util.Iterator;
import java.util.List;

import static org.mikhailv.ntnuitennis.AppManager.TAG_LOG;

/**
 * Created by MikhailV on 24.02.2017.
 */

public class NotifierService extends IntentService
{
    private static final String TAG = "NotifierService";
    private static final int INTERVALL = 1000 * 60; // 1 minute
    private static final int WEEK_MILLIS = 7 * 24 * 3600 * 1000;

    public static void setAlarm(Context context)
    {
        Log.d(TAG_LOG, "NotifierService.setAlarm() called");
        PendingIntent pi = PendingIntent.getService(context, 0,
                new Intent(context, NotifierService.class), 0);
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime(), INTERVALL, pi);
    }
    public static void cancelAlarm(Context context)
    {
        Log.d(TAG_LOG, "NotifierService.cancelAlarm() called");
        PendingIntent pi = PendingIntent.getService(context, 0,
                new Intent(context, NotifierService.class), 0);
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        pi.cancel();
    }
    public static boolean isAlarmOn(Context context)
    {
        Log.d(TAG_LOG, "NotifierService.isAlarmOn() called");
        PendingIntent pi = PendingIntent.getService(context, 0,
                new Intent(context, NotifierService.class),
                PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    public NotifierService()
    {
        super(TAG);
    }
    @Override
    protected void onHandleIntent(Intent intent)
    {
        Log.d(TAG_LOG, "NotifierService.onHandleIntent() called");
        // Checking internet connection
        NetworkInfo netInfo = ((ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        if (netInfo == null || !netInfo.isConnected()) {
            Log.d(TAG_LOG, "NotifierService.onHandleIntent() aborted: no connection");
            return;
        }

        // Reading data from db and removing expired
        DBManager db = new DBManager(this);
        List<SessionInfo.ShortForm> sessions = db.getAllTuples();
        long currentDate = System.currentTimeMillis();
        for (Iterator<SessionInfo.ShortForm> it = sessions.iterator(); it.hasNext(); ) {
            SessionInfo.ShortForm entry = it.next();
            if (entry.getDate() < currentDate) {    // expired
                it.remove();
                db.deleteTuple(entry.getLink());
            }
        }
        if (sessions.size() == 0) {
            Log.d(TAG_LOG, "NotifierService aborted and canceled: everything is expired");
            cancelAlarm(this);
            return;
        }

        // Checking if cookies are expired
        SlotChecker fetcher = new SlotChecker(this);
        if (!fetcher.isCookiesOK()) {
            fireNoCookiesNotification();
            return;
        }

        // Fire notifications and update db
        for (SessionInfo.ShortForm entry : sessions) {
            if (entry.getDate() < currentDate + WEEK_MILLIS
                    && fetcher.checkSlotAvailability(entry.getLink())) {
                db.deleteTuple(entry.getLink());
                fireSlotNotification(entry.getLink(), entry.getInfo());
            }
        }

        // Cancel service alarm if db is now empty
        if (db.getTableSize() == 0) {
            cancelAlarm(this);
        }
    }
    private int getResourceColor(int colorId)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return getColor(colorId);
        else
            return getResources().getColor(colorId);
    }
    private void fireSlotNotification(String link, String info)
    {
        int id = link.hashCode();
        PendingIntent pi = PendingIntent.getActivity(
                this, id, SlotDetailsActivity.newIntent(this, link, 0), PendingIntent.FLAG_UPDATE_CURRENT);
        Resources r = getResources();
        Notification note = new NotificationCompat.Builder(this)
                .setTicker(r.getString(R.string.notification_ticker))
                .setContentTitle(r.getString(R.string.notification_title))
                .setContentText(r.getString(R.string.notification_text, info))
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(r, R.mipmap.ic_launcher))
                .setColor(getResourceColor(R.color.green))
                .setContentIntent(pi)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)  // vibro, sound, lights
                .build();
        NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        nm.notify(id, note);
    }
    private void fireNoCookiesNotification()
    {
        PendingIntent pi = PendingIntent.getActivity(
                this, 0, new Intent(this, PagerActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
        Resources r = getResources();
        Notification note = new NotificationCompat.Builder(this)
                .setTicker(r.getString(R.string.notification_ticker))
                .setContentTitle(r.getString(R.string.notification_cookie_title))
                .setContentText(r.getString(R.string.notification_cookie_text))
                .setSmallIcon(R.drawable.ic_login_notification)
                .setLargeIcon(BitmapFactory.decodeResource(r, R.mipmap.ic_launcher))
                .setColor(getResourceColor(R.color.lightGreen))
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();
        NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        nm.notify(0, note);
    }
}
