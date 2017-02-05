package org.mikhailv.ntnuitennis.net;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by MikhailV on 27.01.2017.
 */

class AuthenticateTask extends NetworkTask
{
    private static final String EMAIL_KEY = "email";
    private static final String PASSWORD_KEY = "password";
    private static final String LANG_KEY = "lang";
    private static final String REMEMBER_KEY = "rememberme";

    private static final String COOKIES_HEADER = "Set-Cookie";
    private static final String ENCODING = "UTF-8";

    private String m_postArgs;

    public AuthenticateTask(NetworkCallbacks callbacks, String email, String password, String lang)
    {
        super(callbacks);
        StringBuilder sb = new StringBuilder();

        try {
            sb.append(URLEncoder.encode(EMAIL_KEY, ENCODING)).append('=')
                    .append(URLEncoder.encode(email, ENCODING)).append('&');
            sb.append(URLEncoder.encode(PASSWORD_KEY, ENCODING)).append("=")
                    .append(URLEncoder.encode(password, ENCODING)).append('&');
            sb.append(URLEncoder.encode(LANG_KEY, ENCODING)).append("=")
                    .append(URLEncoder.encode(lang, ENCODING)).append('&');
            sb.append(URLEncoder.encode(REMEMBER_KEY, ENCODING)).append("=")
                    .append(URLEncoder.encode("on", ENCODING));
        } catch (UnsupportedEncodingException e) {}

        m_postArgs = sb.toString();
    }
    @Override
    protected Object download(URL link) throws IOException
    {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection)link.openConnection();
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.connect();

            OutputStream os = conn.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(os, ENCODING);
            writer.write(m_postArgs);
            writer.flush();

            writer.close();
            os.close();
            publishProgress(50);

            int response = conn.getResponseCode();
            if (response != HttpURLConnection.HTTP_OK)
                throw new IOException("Http error: " + response);

            List<String> cookiesHeader = conn.getHeaderFields().get(COOKIES_HEADER);

            if (cookiesHeader == null)
                throw new IOException("No cookies received from server");

            NetworkFragment.cookieManager.getCookieStore().removeAll();
            for (String cookieString : cookiesHeader) {
                List<HttpCookie> cookies = HttpCookie.parse(cookieString);
                for (HttpCookie cookie : cookies)
                    NetworkFragment.cookieManager.getCookieStore().add(null, cookie);
            }

        } finally {
            if (conn != null)
                conn.disconnect();
        }
        return null;
    }
    @Override
    protected void onPreExecute()
    {
        if (getCallbacks() != null)
            getCallbacks().onPreDownload();
    }
    @Override
    protected void onPostExecute(Object o)
    {
        if (getCallbacks() != null)
            getCallbacks().onAuthenticateFinished();
    }
    @Override
    protected void onProgressUpdate(Integer... values)
    {
        if (getCallbacks() != null)
            getCallbacks().onProgressChanged(values[0]);
    }
    @Override
    protected void onCancelled()
    {
        if (getCallbacks() != null)
            getCallbacks().onDownloadCanceled();
    }
}
