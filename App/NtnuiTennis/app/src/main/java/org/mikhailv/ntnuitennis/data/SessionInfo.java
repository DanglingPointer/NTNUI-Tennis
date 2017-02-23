package org.mikhailv.ntnuitennis.data;

/**
 * Created by MikhailV on 08.01.2017.
 */

import android.util.Log;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Scanner;

import static org.mikhailv.ntnuitennis.AppManager.TAG_LOG;

/**
 * Used for notifications page
 */
public class SessionInfo implements Serializable
{
    public interface ShortForm
    {
        String getLink();

        long getDate();

        String getInfo();
    }

    private String m_day;
    private String m_lvl;
    private String m_link;
    private int m_hour;
    private boolean m_checked;

    SessionInfo(String day, String level, int hour, String link)
    {
        m_day = day;
        m_lvl = level;
        m_hour = hour;
        m_checked = false;
        m_link = link;
    }
    //----DB methods--------------------------------------------------------------------------------
    public ShortForm getShortForm()
    {
        Scanner in = new Scanner(m_day).useDelimiter("[^0-9]+");
        int day = in.nextInt();
        int month = in.nextInt();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        Calendar c = new GregorianCalendar(year, month, day, m_hour, 0, 0);
        final long date = c.getTimeInMillis();
        return new ShortForm()
        {
            @Override
            public String getLink() { return m_link; }
            @Override
            public long getDate() { return date; }
            @Override
            public String getInfo() { return m_lvl; }
        };
    }
    public String getLink()
    {
        return m_link;
    }
    //----GUI methods-------------------------------------------------------------------------------
    public String getDate()
    {
        return m_day;
    }
    public String getLvl()
    {
        return m_lvl;
    }
    public int getHour()
    {
        return m_hour;
    }
    public boolean isChecked()
    {
        return m_checked;
    }
    public void setChecked(boolean checked)
    {
        m_checked = checked;
    }
}
