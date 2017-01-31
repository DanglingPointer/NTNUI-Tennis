package org.mikhailv.ntnuitennis.net;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

/**
 * Created by MikhailV on 21.01.2017.
 */

public abstract class NetworkTask extends AsyncTask<String, Integer, Object>
{
    protected static final int READ_TIMEOUT = 10000;
    protected static final int CONNECT_TIMEOUT = 15000;

    private NetworkCallbacks m_callbacks;
    private Exception m_exception;

    public NetworkTask(NetworkCallbacks callbacks)
    {
        m_callbacks = callbacks;
    }
    public void freeCallbacks()
    {
        m_callbacks = null;
    }
    public Exception getException()
    {
        return m_exception;
    }
    @Override
    protected Object doInBackground(String... params)
    {
        if (isCancelled() || params == null || params.length < 1)
            return null;

        Object result = null;
        try {
            result = download(new URL(params[0]));
        } catch (ParseException e) {
            m_exception = e;
            e.printStackTrace();
        } catch (IOException e) {
            m_exception = e;
            e.printStackTrace();
            result = null;
        }
        return result;
    }
    protected NetworkCallbacks getCallbacks()
    {
        return m_callbacks;
    }

    protected abstract Object download(URL link) throws IOException, ParseException;
}
