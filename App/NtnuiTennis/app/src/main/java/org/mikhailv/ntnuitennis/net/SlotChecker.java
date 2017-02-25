package org.mikhailv.ntnuitennis.net;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.mikhailv.ntnuitennis.TennisApp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpCookie;
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

    private List<HttpCookie> m_cookies;
    /**
     * Reads cookies from the cookie-file
     */
    public SlotChecker(Context context)
    {
        List<String> cookieStrings = TennisApp.readCookies(context);
        if (cookieStrings == null || cookieStrings.size() == 0)
            return;

        m_cookies = new ArrayList<>();
        for (String cookieString : cookieStrings) {
            List<HttpCookie> cookies = HttpCookie.parse(cookieString);
            for (HttpCookie cookie : cookies) {
                if (cookie.hasExpired()) {
                    m_cookies = null;
                    return;
                }
                m_cookies.add(cookie);
            }
        }
    }
    /**
     * If no cookies-file or some of the cookies are expired, returns false
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

            conn.setRequestProperty("Cookie", TextUtils.join(";", m_cookies));
            Log.d(TAG_LOG, "Coockies: " + TextUtils.join(";", m_cookies));

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
            Log.d(TAG_LOG, e.toString());
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
            Log.d(TAG_LOG, "SlotChecker found link: "+ link);
            if (link.contains("bekrefteid") || link.contains("leggtilvikarid"))
                return true;
        }
        return false;
    }
}
