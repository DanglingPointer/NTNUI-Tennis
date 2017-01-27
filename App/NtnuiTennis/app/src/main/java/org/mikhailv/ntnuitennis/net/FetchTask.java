package org.mikhailv.ntnuitennis.net;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

/**
 * Created by MikhailV on 21.01.2017.
 */

abstract class FetchTask extends AsyncTask<String, Integer, Object>
{
    protected static final int BUFFER_SIZE = 20000;
    protected static final int MAX_READ = 2000;
    protected static final int READ_TIMEOUT = 10000;
    protected static final int CONNECT_TIMEOUT = 15000;

    private NetworkCallbacks m_callbacks;
    private Exception m_exception;

    public FetchTask(NetworkCallbacks callbacks)
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
            String rawData = download(new URL(params[0]));
            result = parse(rawData);
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

    protected abstract String download(URL link) throws IOException;

    protected abstract Object parse(String rawData) throws ParseException;
}
