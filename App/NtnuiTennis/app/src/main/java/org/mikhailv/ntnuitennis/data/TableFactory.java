package org.mikhailv.ntnuitennis.data;

import android.content.Context;

import java.util.List;
import java.util.Map;

/**
 * Created by MikhailV on 12.01.2017.
 */

public class TableFactory
{
//    private Context m_context;
//
//    public TableFactory(Context c)
//    {
//        m_context = c;
//    }
    public Slot newSlot(int size, String link, String attendLink, String lvl, boolean expired,
                        List<String> names, List<Player> players)
    {
        if (size < 0) return null;

        SlotImpl s = new SlotImpl(size);
        s.setLink(link).setAttendLink(attendLink).setLevel(lvl).setExpired(expired);
        if (names != null && names.size() <= size) {
            for (int i = 0; i < names.size(); ++i)
                s.setName(i, names.get(i));
        }
        if (players != null) {
            for (Player p : players)
                s.addPlayer(p);
        }
        return s;
    }
    public Day newDay(String date, Map<Integer, Slot> slots)
    {
        DayImpl d = new DayImpl(date);
        for (Integer hour : slots.keySet()) {
            int index = hour - 8;
            Slot s = slots.get(hour);
            if (s != null && index >= 0 && index < Globals.Sizes.DAY)
                d.setSlot(hour, s);
        }
        return d;
    }
    public Week newWeek(List<Day> days)
    {
        WeekImpl w = new WeekImpl();
        for (int i = 0; i < days.size() && i < Globals.Sizes.WEEK; ++i) {
            w.setDay(i, days.get(i));
        }
        return w;
    }
}
