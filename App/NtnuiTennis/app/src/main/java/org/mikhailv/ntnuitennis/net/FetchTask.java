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

import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by MikhailV on 31.01.2017.
 */

abstract class FetchTask extends NetworkTask
{
    protected static final int INIT_BUFFER_SIZE = 1024 * 40;
    protected static final int MAX_READ = 4000;

    public FetchTask(NetworkCallbacks callbacks)
    {
        super(callbacks);
    }
    @Override
    protected Object download(URL link) throws IOException, ParseException
    {
        InputStream inStream = null;
        HttpURLConnection conn = null;
        String rawResult = null;
        try {
            conn = (HttpURLConnection)link.openConnection();
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            CookieManager cm = NetworkFragment.cookieManager;
            List<HttpCookie> cookies = cm.getCookieStore().getCookies();
            if (cookies.size() > 0) {
                conn.setRequestProperty("Cookie", TextUtils.join(";", cookies));
            }

            conn.connect();
            int response = conn.getResponseCode();
            if (response != HttpURLConnection.HTTP_OK)
                throw new IOException("HTTP error: " + response);
            inStream = conn.getInputStream();
            if (inStream != null) {
                InputStreamReader reader = new InputStreamReader(inStream, "ISO-8859-1");

                int bufferSize = INIT_BUFFER_SIZE;
                char[] buffer = new char[bufferSize];

                int lastRead = 0, offset = 0;
                while (lastRead != -1 && !isCancelled()) {
                    publishProgress(offset * 100 / bufferSize);
                    lastRead = reader.read(buffer, offset, Math.min(MAX_READ, bufferSize - offset));
                    offset += lastRead;
                    if (offset == bufferSize) {
                        bufferSize += INIT_BUFFER_SIZE;
                        buffer = Arrays.copyOf(buffer, bufferSize);
                    }
                }
                if (offset != -1)
                    rawResult = new String(buffer, 0, offset);
                inStream.close();
            }
            return parse(rawResult);
        }
        finally {
            if (conn != null)
                conn.disconnect();
        }
    }

    protected abstract Object parse(String rawHtml) throws ParseException;

    @Override
    protected void onPreExecute()
    {
        if (getCallbacks() != null)
            getCallbacks().onPreDownload();
    }
    @Override
    protected void onProgressUpdate(Integer... values)
    {
        if (getCallbacks() != null && values != null && values.length > 0)
            getCallbacks().onProgressChanged(values[0]);
    }
    @Override
    protected void onCancelled()
    {
        if (getCallbacks() != null)
            getCallbacks().onDownloadCanceled();
    }
}
