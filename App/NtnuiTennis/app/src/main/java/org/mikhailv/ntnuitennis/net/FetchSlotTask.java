package org.mikhailv.ntnuitennis.net;

import android.net.ParseException;
import android.util.Log;

import org.mikhailv.ntnuitennis.data.Globals;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by MikhailV on 21.01.2017.
 */

/**
 * Downloads and parses tml-pages for individual training sessions, and handles attend/substitute
 */
class FetchSlotTask extends FetchTask
{
    private static final int MAX_READ = 25000;

    public FetchSlotTask(NetworkCallbacks callbacks)
    {
        super(callbacks);
    }
    @Override
    protected String download(URL link) throws IOException
    {
        InputStream inStream = null;
        HttpURLConnection conn = null;
        String result = null;
        try {
            conn = (HttpURLConnection)link.openConnection();
            conn.setReadTimeout(3000);
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            int response = conn.getResponseCode();
            if (response != HttpURLConnection.HTTP_OK)
                throw new IOException("HTTP error: " + response);
            inStream = conn.getInputStream();
            if (inStream != null) {
                InputStreamReader reader = new InputStreamReader(inStream, "UTF-8");
                char[] buffer = new char[MAX_READ]; // 2^15=32768, charCount = 24938

                int lastRead = 0, offset = 0;
                while (lastRead != -1 && offset < MAX_READ && !isCancelled()) {
                    publishProgress(offset * 100 / MAX_READ);
                    lastRead = reader.read(buffer, offset, MAX_READ - offset);
                    offset += lastRead;
                }
                if (offset != -1)
                    result = new String(buffer, 0, offset);
            }
        } finally {
            if (inStream != null)
                inStream.close();
            if (conn != null)
                conn.disconnect();
        }
        return result;
    }
    @Override
    protected String parse(String rawData) throws ParseException
    {
        // TODO: extract xml-table from xml-page
        return rawData;
    }
    @Override
    protected void onPreExecute()
    {
        if (getCallbacks() != null) {
            getCallbacks().onPreExecute();
        }
    }
    @Override
    protected void onPostExecute(String s)
    {
        if (getCallbacks() != null)
            getCallbacks().onSlotFetched(s, getException());
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
