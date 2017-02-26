package org.mikhailv.ntnuitennis.services;

import android.content.Context;
import android.util.Log;

import org.mikhailv.ntnuitennis.TennisApp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mikhailv.ntnuitennis.AppManager.TAG_LOG;

/**
 * Created by MikhailV on 24.02.2017.
 */

/**
 * This class downloads and checks a slot in the current thread. It should be used in an IntentService
 */
public class SlotChecker
{
    private static final int READ_TIMEOUT = 10000;
    private static final int CONNECT_TIMEOUT = 15000;
    private static final int BUFFER_SIZE = 40000;

    private String m_cookies;
    /**
     * Reads cookies from the cookie-file
     */
    public SlotChecker(Context context)
    {
        String cookieString = TennisApp.readCookies(context);
        Log.d(TAG_LOG, "SlotChecker(): cookies from file: " + cookieString);

        if (cookieString == null || cookieString.isEmpty()
                || !cookieString.contains("NTNUITennis_brukernavn"))
            return;

        m_cookies = cookieString;
    }
    /**
     * If no cookies-file or some of the cookies are expired or there are not enough cookies,
     * returns false
     */
    public boolean isCookiesOK()
    {
        return m_cookies != null;
    }
    /**
     * Checks whether the session at 'url' has available positions
     */
    public boolean checkSlotAvailability(String url)
    {
        Log.d(TAG_LOG, "SlotChecker.checkSlotAvailability() called");
        InputStream inStream = null;
        HttpURLConnection conn = null;
        String rawResult = null;
        try {
            URL link = new URL(url);
            conn = (HttpURLConnection)link.openConnection();
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.setRequestProperty("Cookie", m_cookies);

            conn.connect();
            int response = conn.getResponseCode();
            if (response != HttpURLConnection.HTTP_OK)
                throw new IOException("HTTP error: " + response);
            inStream = conn.getInputStream();

            if (inStream != null) {
                InputStreamReader reader = new InputStreamReader(inStream, "ISO-8859-1");
                char[] buffer = new char[BUFFER_SIZE];

                int lastRead = 0, offset = 0;
                while (lastRead != -1 && offset < BUFFER_SIZE) {
                    lastRead = reader.read(buffer, offset, BUFFER_SIZE - offset);
                    offset += lastRead;
                }
                if (offset != -1)
                    rawResult = new String(buffer, 0, offset);
                inStream.close();
            }
        }
        catch (Exception e) {
            Log.d(TAG_LOG, "SlotChecker caught an exception: " + e.toString());
            e.printStackTrace();
            return false;
        }
        finally {
            if (conn != null)
                conn.disconnect();
        }
        return parse(rawResult);
    }
    private boolean parse(String rawHtml)
    {
        int openingTagIndex = rawHtml.indexOf("<table>");
        rawHtml = rawHtml.substring(openingTagIndex);

        List<String> links = new ArrayList<>();

        Matcher m = Pattern.compile("href=\"([^\"]+)\"").matcher(rawHtml);
        while (m.find()) {
            links.add(m.group(1));
        }
        for (String link : links) {
            if (link.contains("bekrefteid") || link.contains("leggtilvikarid"))
                return true;
        }
        return false;
    }
}
