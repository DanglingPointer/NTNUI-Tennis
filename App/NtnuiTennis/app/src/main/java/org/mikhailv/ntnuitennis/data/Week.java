package org.mikhailv.ntnuitennis.data;

import static org.mikhailv.ntnuitennis.data.Globals.Sizes;

/**
 * Created by MikhailV on 06.01.2017.
 */

public class Week
{
    public static Week getCurrent()
    {
        // temporary stub
        if (s_current == null) {
            s_current = new Week();
            for (int i = 0; i < Sizes.WEEK; ++i)
                s_current.setDay(i, new Day("Day #" + i));
        }
        return s_current;
    }
    private static Week s_current;

    private final Day[] m_days = new Day[Sizes.WEEK];

    public Week()
    { }
    public Day getDay(int index)
    {
        if (index < 0 || index >= Sizes.WEEK)
            return null;
        return m_days[index];
    }
    public void setDay(int index, Day day)
    {
        if (index >= 0 && index < Sizes.WEEK)
            m_days[index] = day;
    }
}
