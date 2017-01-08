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
            for (int i = 0; i < Sizes.WEEK; ++i) {
                Slot s = new Slot(4);
                s.setName(0, "Guang");
                s.setName(1, "Mikhail");
                s.setLevel("N+*");

                Day d = new Day("Day #" + i);
                d.setSlot(9, s);
                s_current.setDay(i, d);
            }
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
