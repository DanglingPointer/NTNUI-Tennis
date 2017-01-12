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

    List<SessionInfo> getSessionsInfo();
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
