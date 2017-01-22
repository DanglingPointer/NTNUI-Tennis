package org.mikhailv.ntnuitennis.net;

import android.net.ParseException;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.URL;

/**
 * Created by MikhailV on 21.01.2017.
 */

abstract class FetchTask extends AsyncTask<URL, Integer, String>
{
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
    protected String doInBackground(URL... params)
    {
        if (isCancelled() || params == null || params.length < 1)
            return null;

        String result = null;
        try {
            result = download(params[0]);
            result = parse(result);
        } catch (ParseException e) {
            m_exception = e;
        } catch (IOException e) {
            m_exception = e;
            result = null;
        }
        return result;
    }
    protected NetworkCallbacks getCallbacks()
    {
        return m_callbacks;
    }

    protected abstract String download(URL link) throws IOException;

    protected abstract String parse(String rawData) throws ParseException;
}
