package org.mikhailv.ntnuitennis.data;

import static org.mikhailv.ntnuitennis.data.Globals.Sizes;

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
    private final Slot[] m_slots = new Slot[Sizes.DAY];
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
        int index = hour - 8;
        if (index < 0 || index >= Sizes.DAY)
            return null;
        return m_slots[index];
    }
    public DayImpl setSlot(int hour, Slot slot)
    {
        m_slots[hour - 8] = slot;
        return this;
    }
}
