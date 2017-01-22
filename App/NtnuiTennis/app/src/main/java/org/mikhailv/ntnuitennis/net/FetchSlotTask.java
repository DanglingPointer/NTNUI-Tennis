package org.mikhailv.ntnuitennis.net;

import android.util.Log;
import android.util.Xml;

import org.mikhailv.ntnuitennis.data.Globals;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by MikhailV on 21.01.2017.
 */

/**
 * Downloads and parses tml-pages for individual training sessions, and handles attend/substitute
 */
class FetchSlotTask extends FetchTask
{
    private static final int MAX_READ = 50000;

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
        try {
            int openingTagIndex = rawData.indexOf("<table>");
            int closingTagIndex = rawData.indexOf("</table>");
            rawData = rawData.substring(openingTagIndex, closingTagIndex + 8);
            SlotParser parser = new SlotParser(rawData);
            parser.parse();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(Globals.TAG_LOG, e.toString());
            throw new ParseException(e.toString(), 0);
        }
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

    private static class SlotParser
    {
        private XmlPullParser m_parser;
        private List<String> m_data;

        public SlotParser(String rawHtml) throws XmlPullParserException
        {
            rawHtml = rawHtml.replaceAll("&nbsp;", " ").replaceAll("&Oslash;", "Ø")
                    .replaceAll("&oslash;", "ø").replaceAll("&Aring;", "Å")
                    .replaceAll("&aring;", "å").replaceAll("&AElig;", "Æ")
                    .replaceAll("&aelig;", "æ");
            m_data = new ArrayList<>();
            m_parser = Xml.newPullParser();
            m_parser.setInput(new StringReader(rawHtml));
        }
        public void parse() throws XmlPullParserException, IOException
        {
            if (m_parser.nextTag() != XmlPullParser.START_TAG)
                throw new XmlPullParserException("Malformed raw html table");
            int depth = 1;

            while (depth != 0) {
                switch (m_parser.next()) {
                    case XmlPullParser.START_TAG:
                        ++depth;
                        break;
                    case XmlPullParser.END_TAG:
                        --depth;
                        break;
                    case XmlPullParser.TEXT:
                        if (depth >= 3) {
                            String text = m_parser.getText();
                            m_data.add(text);
                        }
                        break;
                    default:
                        break;
                }
            }
            for (String entry : m_data) {
                Log.d(Globals.TAG_LOG, entry);
            }
        }
        public List<String> getData()
        {
            return m_data;
        }
    }
}
