package org.mikhailv.ntnuitennis;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.mikhailv.ntnuitennis.data.DBManager;
import org.mikhailv.ntnuitennis.data.SessionInfo;
import org.mikhailv.ntnuitennis.data.TableBuilder;
import org.mikhailv.ntnuitennis.data.Week;
import org.mikhailv.ntnuitennis.net.NetworkFragment;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
        s_manager.setContext(null);
    }

    private static class AppManagerImpl implements AppManager
    {
        private static final String NOTIFICATIONS_FILE = "config";
        private static final String CREDENTIALS_FILE = "creds";      // use SharedPreferences instead??


        private Context m_context;
        private NetworkFragment m_networker;
        private Week m_week;
        private CredentialsImpl m_credentials;
        private int m_weekNumber;

        public AppManagerImpl(Context context)
        {
            m_context = context;
            m_networker = null;
            m_credentials = null;
            m_weekNumber = 0;
        }
        @Override
        public void incrementWeek()
        {
            ++m_weekNumber;
        }
        @Override
        public boolean decrementWeek()
        {
            if (m_weekNumber > 0) {
                --m_weekNumber;
                return true;
            }
            return false;
        }
        /**
         * Should be used with null as argument to clear context
         */
        public void setContext(Context context)
        {
            m_context = context;
        }
        @Override
        public String getTableURL()
        {
            String url = "http://org.ntnu.no/tennisgr/index.php";
            if (m_credentials != null && m_credentials.lang != null) {
                url = Uri.parse(url).buildUpon()
                        .appendQueryParameter("lang", m_credentials.lang)
                        .appendQueryParameter("uke", "" + m_weekNumber)
                        .toString();
                Log.d(TAG_LOG, url);
            }
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
            }
            catch (Exception e) {
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
            }
            else {
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
                }
                catch (Exception e) {
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
            if (m_week == null) {
                TableBuilder builder = new TableBuilder(INIT_HOUR, WEEK_SIZE, DAY_SIZE);
                m_week = builder.getWeek(); // empty week
            }
            return m_week;
        }
        @Override
        public void setCurrentWeek(Week week)
        {
            m_week = week;
        }
        /**
         * Loads content of DB, compares with current week and modifies the latter if necessary.
         * Does not change any internal state.
         * Does not delete expired tracked sessions, that will be done in service
         */
        @Override
        public List<SessionInfo> getHoursInfo()
        {
            DBManager db = new DBManager(m_context);
            List<SessionInfo.ShortForm> trackedSessions = db.getAllTuples();
            List<SessionInfo> currentSessions = getCurrentWeek().getHours();

            for (SessionInfo.ShortForm trackedSession : trackedSessions) {
                for (SessionInfo currentSession : currentSessions) {
                    if (trackedSession.getLink().equals(currentSession.getLink())) {
                        currentSession.setChecked(true);
                        break;
                    }
                }
            }
            return currentSessions;
        }
        /**
         * Updates DB according to checked sessions
         */
        @Override
        public void saveHoursInfo(List<SessionInfo> sessions)
        {
            DBManager db = new DBManager(m_context);
            List<SessionInfo.ShortForm> trackedSessions = db.getAllTuples();
            List<String> trackedURLs = new ArrayList<>();
            for (SessionInfo.ShortForm session : trackedSessions)
                trackedURLs.add(session.getLink());

            for (SessionInfo session : sessions) {
                if (session.isChecked() && !trackedURLs.contains(session.getLink()))
                    db.insertTuple(session.getShortForm());
                else if (!session.isChecked() && trackedURLs.contains(session.getLink()))
                    db.deleteTuple(session.getLink());
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
