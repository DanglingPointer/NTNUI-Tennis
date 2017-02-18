package org.mikhailv.ntnuitennis.data;

import java.util.ArrayList;
import java.util.List;

import static org.mikhailv.ntnuitennis.AppManager.DAY_SIZE;
import static org.mikhailv.ntnuitennis.AppManager.INIT_HOUR;
import static org.mikhailv.ntnuitennis.AppManager.WEEK_SIZE;

/**
 * Created by MikhailV on 06.01.2017.
 */

public interface Week
{
    Day getDay(int index);

    List<SessionInfo> getHours();
}

class WeekImpl implements Week
{
    private final Day[] m_days = new Day[WEEK_SIZE];

    public WeekImpl()
    { }
    public Day getDay(int index)
    {
        if (index < 0 || index >= WEEK_SIZE)
            return null;
        return m_days[index];
    }
    public Week setDay(int index, Day day)
    {
        m_days[index] = day;
        return this;
    }
    public List<SessionInfo> getHours()
    {
        List<SessionInfo> newHours = new ArrayList<>();
        Slot slot;
        for (Day d : m_days) {
            for (int hour = INIT_HOUR; hour < DAY_SIZE + INIT_HOUR; ++hour) {
                slot = d.getSlot(hour);
                if (slot.getLevel() != null && !slot.isExpired()) {
                    SessionInfo hi = new SessionInfo(d.getDate(), slot.getLevel(), hour, slot.getLink());
                    newHours.add(hi);
                }
            }
        }
        return newHours;
    }
}
