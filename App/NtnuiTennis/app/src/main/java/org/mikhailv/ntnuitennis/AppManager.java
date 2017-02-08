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
    int DAY_SIZE = 15;
    int WEEK_SIZE = 8;
    int INIT_HOUR = 8;

    String TAG_LOG = "MIKHAILS_LOG"; // temp

    interface Credentials
    {
        String getPassword();

        String getEmail();

        String getLanguage();
    }

    String getTableURL();

    void incrementWeek();

    boolean decrementWeek();

    void saveCredentials(String email, String password, String lang);

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

    void discardHoursInfoChanges();

    /**
     * Writes to file if any changes made
     */
    void saveHoursInfo();
}

