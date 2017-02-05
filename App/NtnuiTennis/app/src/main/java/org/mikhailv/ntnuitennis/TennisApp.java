package org.mikhailv.ntnuitennis;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import org.mikhailv.ntnuitennis.data.Globals;
import org.mikhailv.ntnuitennis.data.HourInfo;
import org.mikhailv.ntnuitennis.data.Week;
import org.mikhailv.ntnuitennis.net.NetworkFragment;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.mikhailv.ntnuitennis.data.Globals.TAG_LOG;

/**
 * Created by MikhailV on 04.02.2017.
 */

public class TennisApp extends Application
{
    private static AppManagerImpl s_manager;
    public static AppManager getManager(Context context)
    {
        if (s_manager == null)
            s_manager = new AppManagerImpl(context);
        else
            s_manager.setContext(context);
        return s_manager;
    }
    public static void clearManagerContext()
    {
        if (s_manager != null)
            s_manager.setContext(null);
    }

    private static class AppManagerImpl implements AppManager
    {
        private static final String NOTIFICATIONS_FILE = "config";
        private static final String CREDENTIALS_FILE = "creds";


        private Context m_context;
        private NetworkFragment m_networker;
        private Week m_week;
        private CredentialsImpl m_credentials;
        private List<HourInfo> m_hours;

        public AppManagerImpl(Context context)
        {
            m_context = context;
            m_networker = null;
            m_credentials = null;
        }
        /**
         * Should be used with null as argument to clear context
         */
        public void setContext(Context context)
        {
            m_context = context;
        }
        @Override
        public String getHomeURL()
        {
            String url;
            if (m_credentials != null && m_credentials.lang != null)
                url = "http://org.ntnu.no/tennisgr/index.php?lang=" + m_credentials.lang;
            else
                url = "http://org.ntnu.no/tennisgr/";
            return url;
        }
        /**
         * Writes credentials both to cash and to file
         */
        @Override
        public void saveCredentials(String email, String password, String lang)
        {
            if (m_credentials == null)
                m_credentials = new CredentialsImpl();
            m_credentials.email = email;
            m_credentials.password = password;
            m_credentials.lang = lang;
            try {
                FileOutputStream fileOut = m_context.openFileOutput(CREDENTIALS_FILE, Context.MODE_PRIVATE);
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(m_credentials);
                out.close();
                fileOut.close();
            } catch (Exception e) {
                Log.d(TAG_LOG, "Cannot write credentials :(");
                e.printStackTrace();
            }
        }
        /**
         * Sets NetworkFragment and calls authenticate() if first time
         */
        @Override
        public void setNetworker(NetworkFragment networker)
        {
            if (m_networker == null) {
                m_networker = networker;
                m_networker.authenticate(getCredentials());
            } else {
                m_networker = networker;
            }
        }
        /**
         * Reads credentials from file into cash, and returns it. Credentials fields will be
         * uninitialized if no file exists
         */
        private Credentials getCredentials()
        {
            if (m_credentials == null) {
                try {
                    FileInputStream fileIn = m_context.openFileInput(CREDENTIALS_FILE);
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    m_credentials = (CredentialsImpl)in.readObject();
                    in.close();
                    fileIn.close();
                } catch (Exception e) {
                    Log.d(TAG_LOG, "no credentials file found :(");
                    // credentials file is non-existing (first time)
                    m_credentials = new CredentialsImpl();
                }
            }
            return m_credentials;
        }
        @Override
        public Week getCurrentWeek()
        {
            return m_week;
        }
        @Override
        public void setCurrentWeek(Week week)
        {
            m_week = week;
        }
        @Override
        public List<HourInfo> getHoursInfo()
        {
            if (m_hours == null) {
                List<HourInfo> newHours = getCurrentWeek().getHours();
                List<HourInfo> oldHours;
                try {
                    FileInputStream fileIn = m_context.openFileInput(NOTIFICATIONS_FILE);
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
                    m_hours = oldHours;
                } catch (Exception e) {
                    // something went wrong or config-file is outdated
                    m_hours = newHours;
                }
            }
            return m_hours;
        }
        @Override
        public void discardHoursInfoChanges()
        {
            m_hours = null;
        }
        @Override
        public void saveHoursInfo()
        {
            if (m_hours == null) return;
            try {
                FileOutputStream fileOut = m_context.openFileOutput(NOTIFICATIONS_FILE, Context.MODE_PRIVATE);
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(m_hours);
                out.close();
                fileOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        private static class CredentialsImpl implements AppManager.Credentials, Serializable
        {
            String password;
            String email;
            String lang;

            @Override
            public String getPassword()
            {
                return password;
            }
            @Override
            public String getEmail()
            {
                return email;
            }
            @Override
            public String getLanguage()
            {
                return lang;
            }
        }
    }


}
