package org.mikhailv.ntnuitennis;

import android.content.Context;

import org.mikhailv.ntnuitennis.data.HourInfo;
import org.mikhailv.ntnuitennis.data.Week;
import org.mikhailv.ntnuitennis.net.NetworkFragment;

import java.util.List;

/**
 * Created by MikhailV on 04.02.2017.
 */

public interface AppManager
{
    // can be obtained  through a static function that takes Context as argument

    int DAY_SIZE = 15;
    int WEEK_SIZE = 8;
    int INIT_HOUR = 8;
    String HOME_URL = "http://org.ntnu.no/tennisgr/";

    interface Credentials
    {
        String getPassword();

        String getEmail();

        void setCredentials(String password, String email);
    }

    Credentials getCredentials();

    /**
     * Authenticates the first time a networker is set
     */
    void setNetworker(NetworkFragment networker);

    Week getCurrentWeek();

    void setCurrentWeek(Week week);

    /**
     * Reads from file the first time
     */
    List<HourInfo> getHoursInfo();

    void discardHoursInfoCHanges();

    /**
     * Writes to file if any changes made
     */
    void saveHoursInfo();
}

