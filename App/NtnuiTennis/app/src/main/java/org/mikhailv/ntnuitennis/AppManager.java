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

package org.mikhailv.ntnuitennis;

import org.mikhailv.ntnuitennis.data.SessionInfo;
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
    List<SessionInfo> getHoursInfo();

    /**
     * Writes to file if any changes made
     */
    void saveHoursInfo(List<SessionInfo> sessions);
}

