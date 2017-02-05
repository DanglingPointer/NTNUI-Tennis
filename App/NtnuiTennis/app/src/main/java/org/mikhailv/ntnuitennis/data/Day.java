package org.mikhailv.ntnuitennis.data;

import static org.mikhailv.ntnuitennis.AppManager.DAY_SIZE;
import static org.mikhailv.ntnuitennis.AppManager.INIT_HOUR;

/**
 * Created by MikhailV on 06.01.2017.
 */

public interface Day
{
    String getDate();

    Slot getSlot(int hour);
}

class DayImpl implements Day
{
    private final Slot[] m_slots = new Slot[DAY_SIZE];
    private String m_date;

    public DayImpl(String date)
    {
        m_date = date;
    }
    public String getDate()
    {
        return m_date;
    }
    public Slot getSlot(int hour)
    {
        int index = hour - INIT_HOUR;
        if (index < 0 || index >= DAY_SIZE)
            return null;
        return m_slots[index];
    }
    public DayImpl setSlot(int hour, Slot slot)
    {
        m_slots[hour - INIT_HOUR] = slot;
        return this;
    }
}
