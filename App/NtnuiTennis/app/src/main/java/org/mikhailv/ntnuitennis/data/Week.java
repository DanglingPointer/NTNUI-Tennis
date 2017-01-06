package org.mikhailv.ntnuitennis.data;

import static org.mikhailv.ntnuitennis.data.Constants.Sizes;

/**
 * Created by MikhailV on 06.01.2017.
 */

public class Week
{
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
