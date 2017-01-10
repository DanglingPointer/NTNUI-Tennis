package org.mikhailv.ntnuitennis.data;

import java.util.ArrayList;
import java.util.List;

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
                Day d = new Day("Day #" + i);
                if (i % 2 == 0) {
                    Slot s1 = new Slot(4).setName(0, "Guang").setName(1, "Mikhail").setLevel("N+*").setExpired(true);
                    Slot s2 = new Slot(6).setLevel("M").setName(0, "Guang_1").setName(1, "Guang_2");
                    d.setSlot(9, s1).setSlot(11, s2);
                }
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
    public Week setDay(int index, Day day)
    {
        if (index >= 0 && index < Sizes.WEEK)
            m_days[index] = day;
        return this;
    }
    public List<SessionInfo> getSessionsInfo()
    {
        List<SessionInfo> info = new ArrayList<>();
        for (Day d : m_days) {
            for (int hour = 8; hour < Sizes.DAY + 8; ++hour) {
                Slot slot;
                if ((slot = d.getSlot(hour)) != null)
                    info.add(new SessionInfo(d.getDate(), slot.getLevel(), hour));
            }
        }
        return info;
    }
}
