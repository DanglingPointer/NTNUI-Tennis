package org.mikhailv.ntnuitennis.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MikhailV on 29.01.2017.
 */

public class TableBuilder
{
    private final SlotImpl[][] m_slotData;
    private final List<String> m_days;
    private final int START_HOUR;
    private final int WEEK_SIZE;
    private final int DAY_SIZE;

    public TableBuilder(int initHour, int weekSize, int daySize)
    {
        START_HOUR = initHour;
        WEEK_SIZE = weekSize;
        DAY_SIZE = daySize;
        m_slotData = new SlotImpl[WEEK_SIZE][DAY_SIZE];
        m_days = new ArrayList<String>(Globals.Sizes.WEEK);
    }
    public TableBuilder addNames(int day, int hour, List<String> names)
    {
        SlotImpl s = get(day, hour);
        for (String name : names)
            s.addName(name);
        return this;
    }
    public TableBuilder addLink(int day, int hour, String link)
    {
        get(day, hour).setLink(link);
        return this;
    }
    public TableBuilder addLevel(int day, int hour, String lvl)
    {
        get(day, hour).setLevel(lvl);
        return this;
    }
    public TableBuilder addExpired(int day, int hour, boolean expired)
    {
        get(day, hour).setExpired(expired);
        return this;
    }
    public TableBuilder addHasAvailable(int day, int hour, boolean hasAvailable)
    {
        get(day, hour).setAvailable(hasAvailable);
        return this;
    }
    public TableBuilder addDayName(String name)
    {
        m_days.add(name);
        return this;
    }
    public Week getWeek()
    {
        WeekImpl week = new WeekImpl();
        for (int dayIndex = 0; dayIndex < Globals.Sizes.WEEK; ++dayIndex) {
            DayImpl day = new DayImpl(m_days.get(dayIndex));
            for (int slotIndex = 0; slotIndex < Globals.Sizes.DAY; ++slotIndex) {
                Slot s = m_slotData[dayIndex][slotIndex];
                if (s == null)
                    s = new SlotImpl();
                day.setSlot(slotIndex + START_HOUR, s);
            }
            week.setDay(dayIndex, day);
        }
        return week;
    }
    public void reset()
    {
        for (int day = 0; day < WEEK_SIZE; ++day) {
            for (int slot = 0; slot < DAY_SIZE; ++slot)
                m_slotData[day][slot] = null;
        }
        m_days.clear();
    }
    private SlotImpl get(int day, int hour)
    {
        if (day < 0 || day >= WEEK_SIZE || hour < START_HOUR || hour >= START_HOUR + DAY_SIZE)
            return null;
        SlotImpl slot;
        if (m_slotData[day][hour - START_HOUR] == null) {
            slot = new SlotImpl();
            m_slotData[day][hour - START_HOUR] = slot;
        } else {
            slot = m_slotData[day][hour - START_HOUR];
        }
        return slot;
    }
}
