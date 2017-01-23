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
            rawData = parser.parse();
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
        private static final int INFO = 0;
        private static final int REGULARS = 1;
        private static final int SUBSTITUTES = 2;

        private XmlPullParser m_parser;

        private List<List<String>> m_regulars;
        private List<List<String>> m_substitutes;
        private List<List<String>> m_rest;

        public SlotParser(String rawHtml) throws XmlPullParserException
        {
            rawHtml = rawHtml.replaceAll("&nbsp;", " ").replaceAll("&Oslash;", "Ø")
                    .replaceAll("&oslash;", "ø").replaceAll("&Aring;", "Å")
                    .replaceAll("&aring;", "å").replaceAll("&AElig;", "Æ")
                    .replaceAll("&aelig;", "æ").replaceAll("&#9990", "");

            m_regulars = new ArrayList<>();
            m_regulars.add(new ArrayList<String>());

            m_substitutes = new ArrayList<>();
            m_substitutes.add(new ArrayList<String>());

            m_rest = new ArrayList<>();

            m_parser = Xml.newPullParser();
            m_parser.setInput(new StringReader(rawHtml));
        }
        public String parse() throws XmlPullParserException, IOException
        {
            if (m_parser.nextTag() != XmlPullParser.START_TAG)
                throw new XmlPullParserException("Malformed raw html table");
            int depth = 1;

            StringBuilder builder = new StringBuilder();
            boolean addNewLine = false;

            int what = INFO;
            while (depth != 0) {
                switch (m_parser.next()) {
                    case XmlPullParser.START_TAG:
                        ++depth;
                        if (m_parser.getAttributeCount() == 2 &&
                                m_parser.getAttributeName(1).equals("rowspan")) {
                            addNewLine = true;
                            ++what;
                        }
                        if (depth == 2) {
                            switch (what) {
                                case INFO:
                                    m_rest.add(new ArrayList<String>());
                                    break;
                                case REGULARS:
                                    m_regulars.add(new ArrayList<String>());
                                    break;
                                case SUBSTITUTES:
                                    m_substitutes.add(new ArrayList<String>());
                                    break;
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        --depth;
                        if (depth == 1) {
                            builder.append('\n');
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if (depth > 2) {
                            String text = m_parser.getText().trim();
                            builder.append(text).append(' ');
                            switch (what) {
                                case INFO:
                                    m_rest.get(m_rest.size() - 1).add(text);
                                    break;
                                case REGULARS:
                                    m_regulars.get(m_regulars.size() - 1).add(text);
                                    break;
                                case SUBSTITUTES:
                                    m_substitutes.get(m_substitutes.size() - 1).add(text);
                                    break;
                            }
                            if (addNewLine) {
                                builder.append('\n');
                                addNewLine = false;
                                switch (what) {
                                    case REGULARS:
                                        m_regulars.add(new ArrayList<String>());
                                        break;
                                    case SUBSTITUTES:
                                        m_substitutes.add(new ArrayList<String>());
                                        break;
                                }
                            }
                        }
                        break;
                }
            }
            for (List<String> line : m_rest) {
                StringBuilder sb = new StringBuilder();
                for (String entry : line)
                    sb.append(entry).append(' ');
                Log.d(Globals.TAG_LOG, sb.toString());
            }
            for (List<String> line : m_regulars) {
                StringBuilder sb = new StringBuilder();
                for (String entry : line)
                    sb.append(entry).append(' ');
                Log.d(Globals.TAG_LOG, sb.toString());
            }
            for (List<String> line : m_substitutes) {
                StringBuilder sb = new StringBuilder();
                for (String entry : line)
                    sb.append(entry).append(' ');
                Log.d(Globals.TAG_LOG, sb.toString());
            }
            return builder.toString();
        }
        public List<String> getData()
        {
            return null;
        }
    }
}
