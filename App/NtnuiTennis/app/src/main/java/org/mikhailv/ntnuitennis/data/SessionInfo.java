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

package org.mikhailv.ntnuitennis.data;

/**
 * Created by MikhailV on 08.01.2017.
 */

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Scanner;

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
        int month = in.nextInt() - 1;
        int year = new GregorianCalendar().get(Calendar.YEAR);
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
