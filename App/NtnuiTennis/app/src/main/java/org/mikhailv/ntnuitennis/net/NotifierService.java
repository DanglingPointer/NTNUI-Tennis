package org.mikhailv.ntnuitennis.net;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import org.mikhailv.ntnuitennis.data.DBManager;
import org.mikhailv.ntnuitennis.data.SessionInfo;
import org.mikhailv.ntnuitennis.ui.PagerActivity;
import org.mikhailv.ntnuitennis.ui.SlotDetailsActivity;

import java.util.Calendar;
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

    private static int m_notificationId = 0;

    public static void setAlarm(Context context)
    {
        Log.d(TAG_LOG, "alarm set");
        PendingIntent pi = PendingIntent.getService(context, 0,
                new Intent(context, NotifierService.class), 0);
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() /*+ INTERVALL*/, INTERVALL, pi);
    }
    public static void cancelAlarm(Context context)
    {
        Log.d(TAG_LOG, "alarm canceled");
        PendingIntent pi = PendingIntent.getService(context, 0,
                new Intent(context, NotifierService.class), 0);
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        pi.cancel();
    }
    public static boolean isAlarmOn(Context context)
    {
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
        // Checking internet connection
        NetworkInfo netInfo = ((ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        if (netInfo == null || !netInfo.isConnected()) {
            Log.d(TAG_LOG, "NotifierService.onHandleIntent(): no connection");
            return;
        }

        // Reading data from db
        DBManager db = new DBManager(this);
        List<SessionInfo.ShortForm> sessions = db.getAllTuples();
        long currentDate = Calendar.getInstance().getTimeInMillis();
        for (Iterator<SessionInfo.ShortForm> it = sessions.iterator(); it.hasNext(); ) {
            SessionInfo.ShortForm entry = it.next();
            if (entry.getDate() < currentDate) {
                it.remove();
                db.deleteTuple(entry.getLink());
            }
        }
        if (sessions.size() == 0) {
            cancelAlarm(this);
            Log.d(TAG_LOG, "NotifierService.onHandleIntent(): everything is expired");
            return;
        }

        // Checking if cookies are expired
        SlotChecker fetcher = new SlotChecker(this);
        if (!fetcher.isCookiesOK()) {
            fireExpiredNotification();
            return;
        }

        // Fire notifications and update db
        for (SessionInfo.ShortForm entry : sessions) {
            if (fetcher.checkSlotAvailability(entry.getLink())) {
                db.deleteTuple(entry.getLink());
                fireSlotNotification(entry.getLink(), entry.getInfo());
            }
        }

        // Cancel service alarm if db is empty
        if (db.getTableSize() == 0) {
            cancelAlarm(this);
        }
    }
    private void fireSlotNotification(String link, String info)  // TODO
    {
        int id = link.hashCode();
        Log.d(TAG_LOG, "id = " + id);
        PendingIntent pi = PendingIntent.getActivity(
                this, id, SlotDetailsActivity.newIntent(this, link, 0), PendingIntent.FLAG_UPDATE_CURRENT);
        Notification note =  new NotificationCompat.Builder(this)
                .setTicker("blablabla")                                     // temp
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle("Available tennis session")
                .setContentText("Available spot in " + info + " tennis session.")
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setVibrate(new long[] { 500, 500 })        // TODO
                .setLights(Color.GREEN, 3000, 3000)         // TODO: doesn't work
                .build();
        NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        nm.notify(id, note);
    }
    private void fireExpiredNotification()   // TODO
    {
        PendingIntent pi = PendingIntent.getActivity(
                this, 0, new Intent(this, PagerActivity.class), PendingIntent.FLAG_ONE_SHOT);
        Notification note =  new NotificationCompat.Builder(this)
                .setTicker("blablabla")
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle("Login info expired")
                .setContentText("Press to renew your login information automatically.")
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();
        NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        nm.notify(0, note);
    }
}
