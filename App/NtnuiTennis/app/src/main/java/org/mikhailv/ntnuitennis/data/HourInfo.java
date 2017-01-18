package org.mikhailv.ntnuitennis.data;

/**
 * Created by MikhailV on 08.01.2017.
 */

import java.io.Serializable;

/**
 * Used for notifications page
 */
public class HourInfo implements Serializable
{
    private String m_day;
    private String m_lvl;
    private int m_time;
    private boolean m_checked;

    HourInfo(String day, String level, int hour)
    {
        m_day = day;
        m_lvl = level;
        m_time = hour;
        m_checked = false;
    }
    public String getDay()
    {
        return m_day;
    }
    public String getLvl()
    {
        return m_lvl;
    }
    public int getTime()
    {
        return m_time;
    }
    public boolean getChecked()
    {
        return m_checked;
    }
    public void setChecked(boolean checked)
    {
        m_checked = checked;
    }
}
