package org.mikhailv.ntnuitennis.net;

import android.os.AsyncTask;

import org.mikhailv.ntnuitennis.data.Slot;
import org.mikhailv.ntnuitennis.data.SlotDetailsInfo;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

/**
 * Created by MikhailV on 21.01.2017.
 */

abstract class FetchTask extends AsyncTask<String, Integer, SlotDetailsInfo>
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
    protected SlotDetailsInfo doInBackground(String... params)
    {
        if (isCancelled() || params == null || params.length < 1)
            return null;

        SlotDetailsInfo result = null;
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

    protected abstract SlotDetailsInfo parse(String rawData) throws ParseException;
}
