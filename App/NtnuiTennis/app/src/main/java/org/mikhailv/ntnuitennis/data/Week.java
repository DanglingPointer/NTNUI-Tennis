package org.mikhailv.ntnuitennis.data;

import java.util.ArrayList;
import java.util.List;

import static org.mikhailv.ntnuitennis.data.Globals.Sizes;

/**
 * Created by MikhailV on 06.01.2017.
 */

public interface Week
{
    Day getDay(int index);

    List<HourInfo> getHours();
}

class WeekImpl implements Week
{
    private final Day[] m_days = new Day[Sizes.WEEK];

    public WeekImpl()
    { }
    public Day getDay(int index)
    {
        if (index < 0 || index >= Sizes.WEEK)
            return null;
        return m_days[index];
    }
    public Week setDay(int index, Day day)
    {
        m_days[index] = day;
        return this;
    }
    public List<HourInfo> getHours()
    {
        List<HourInfo> newHours = new ArrayList<>();
        Slot slot;
        for (Day d : m_days) {
            for (int hour = 8; hour < Sizes.DAY + 8; ++hour) {
                slot = d.getSlot(hour);
                if (slot.getLevel() != null)
                    newHours.add(new HourInfo(d.getDate(), slot.getLevel(), hour));
            }
        }
        return newHours;
    }
}
