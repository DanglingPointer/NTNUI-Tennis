package org.mikhailv.ntnuitennis.data;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

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

    public static final String MY_NAME = "Mikhail";
    public static final String TAG_LOG = "MIKHAILS_LOG"; // temp
    public static final String FILE_NAME = "config";
    public static final String HOME_URL = "http://org.ntnu.no/tennisgr/"; // temp, see strings.xml


    public static Week getCurrentWeek()
    {
        // temporary stub
        if (s_current == null) {
            s_current = new WeekImpl();
            for (int i = 0; i < Sizes.WEEK; ++i) {
                DayImpl d = new DayImpl("Day #" + i);
                if (i % 2 == 0) {
                    Slot s1 = new SlotImpl(4).setName(0, "Guang").setName(1, "Mikhail").setLevel("N+*")
                            .setLink(HOME_URL + "timeinfo.php?spilletid=20170126T13:00:00&timeid=188&lang=no")
                            .setAttendLink(HOME_URL + "http://org.ntnu.no/tennisgr/timeinfo.php?spilletid=20170126T13:00:00&timeid=188&leggtilvikarid=619&lang=no");
                    Slot s2 = new SlotImpl(6).setLevel("M").setName(0, "Guang_1").setName(1, "Guang_2")
                            .setLink(HOME_URL + "timeinfo.php?spilletid=20170125T14:00:00&timeid=206&lang=no")
                            .setAttendLink(HOME_URL + "http://org.ntnu.no/tennisgr/timeinfo.php?spilletid=20170125T14:00:00&timeid=206&fjernvikarid=619&lang=no");
                    d.setSlot(9, s1).setSlot(11, s2);
                }
                s_current.setDay(i, d);
            }
        }
        return s_current;
    }
    private static WeekImpl s_current;

    /**
     * List with notifications preferences.
     * Creates new if config file is absent or outdated. Otherwise loads from config-file
     */
    public static List<HourInfo> getHoursInfo(Context context)
    {
        if (s_hours == null) {
            List<HourInfo> newHours = getCurrentWeek().getHours();
            List<HourInfo> oldHours;
            try {
                FileInputStream fileIn = context.openFileInput(FILE_NAME);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                oldHours = (ArrayList<HourInfo>)in.readObject();
                in.close();
                fileIn.close();

                if (oldHours.size() != newHours.size())
                    throw new Exception();
                for (int i = 0; i < oldHours.size(); ++i) {
                    HourInfo newHour = newHours.get(i);
                    HourInfo oldHour = oldHours.get(i);
                    if (!newHour.getDay().equals(oldHour.getDay()) ||
                            !newHour.getLvl().equals(oldHour.getLvl()) ||
                            newHour.getTime() != oldHour.getTime())
                        throw new Exception();
                }
                s_hours = oldHours;
            } catch (Exception e) {
                // something went wrong or config-file is outdated
                s_hours = newHours;
            }
        }
        return s_hours;
    }
    public static void discardHoursInfoCHanges()
    {
        s_hours = null;
    }
    public static void saveHoursInfo(Context context)
    {
        if (s_hours == null) return;
        try {
            FileOutputStream fileOut = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(s_hours);
            out.close();
            fileOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static List<HourInfo> s_hours;
}
