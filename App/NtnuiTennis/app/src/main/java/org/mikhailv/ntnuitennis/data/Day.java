/*
 * MIT License
 *
 * Copyright (c) 2017-2018 Mikhail Vasilyev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
