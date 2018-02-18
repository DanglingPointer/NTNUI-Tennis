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
