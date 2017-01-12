package org.mikhailv.ntnuitennis.data;

/**
 * Created by MikhailV on 06.01.2017.
 */

public final class Globals
{
    public static class Sizes
    {
        public static final int DAY = 15;
        public static final int WEEK = 8;
    }
    public static String MY_NAME = "Mikhail";
    public static final String TAG_LOG = "MIKHAILS_LOG"; // temp



    public static Week getCurrentWeek()
    {
        // temporary stub
        if (s_current == null) {
            s_current = new WeekImpl();
            for (int i = 0; i < Sizes.WEEK; ++i) {
                DayImpl d = new DayImpl("Day #" + i);
                if (i % 2 == 0) {
                    Slot s1 = new SlotImpl(4).setName(0, "Guang").setName(1, "Mikhail").setLevel("N+*").setExpired(true);
                    Slot s2 = new SlotImpl(6).setLevel("M").setName(0, "Guang_1").setName(1, "Guang_2");
                    d.setSlot(9, s1).setSlot(11, s2);
                }
                s_current.setDay(i, d);
            }
        }
        return s_current;
    }
    private static WeekImpl s_current;
}
