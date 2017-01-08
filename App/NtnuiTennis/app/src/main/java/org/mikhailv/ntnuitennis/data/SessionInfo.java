package org.mikhailv.ntnuitennis.data;

/**
 * Created by MikhailV on 08.01.2017.
 */

/**
 * Used for notifications page
 */
public class SessionInfo
{
    private String m_day;
    private String m_lvl;
    private int m_hour;

    SessionInfo(String day, String level, int hour)
    {
        m_day = day;
        m_lvl = level;
        m_hour = hour;
    }
    public String getDay()
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
}
