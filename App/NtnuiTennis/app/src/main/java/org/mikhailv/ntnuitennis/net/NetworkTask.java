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

package org.mikhailv.ntnuitennis.net;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

import static org.mikhailv.ntnuitennis.AppManager.TAG_LOG;

/**
 * Created by MikhailV on 21.01.2017.
 */

abstract class NetworkTask extends AsyncTask<String, Integer, Object>
{
    protected static final int READ_TIMEOUT = 10000;
    protected static final int CONNECT_TIMEOUT = 15000;
    protected static final String BASE_URL = "http://org.ntnu.no/tennisgr/";

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
            if (!isCancelled())
                result = download(new URL(params[0]));
        } catch (Exception e) {
            m_exception = e;
            Log.d(TAG_LOG, "NetworkTask caught an exception: " + e.toString());
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
