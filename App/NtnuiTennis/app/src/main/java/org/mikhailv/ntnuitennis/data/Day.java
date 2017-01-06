package org.mikhailv.ntnuitennis.data;

import static org.mikhailv.ntnuitennis.data.Constants.Sizes;

/**
 * Created by MikhailV on 06.01.2017.
 */

public class Day
{
    private final Slot[] m_slots = new Slot[Sizes.DAY];
    private String m_date;

    public Day(String date)
    {
        m_date = date;
    }
    public String getDate()
    {
        return m_date;
    }
    public void setDate(String date)
    {
        m_date = date;
    }
    public Slot getSlot(int hour)
    {
        int index = hour - 8;
        if (index < 0 || index >= Sizes.DAY)
            return null;
        return m_slots[index];
    }
    public void setSlot(int hour, Slot slot)
    {
        int index = hour - 8;
        if (index >= 0 && index < Sizes.DAY)
            m_slots[index] = slot;
    }
}
