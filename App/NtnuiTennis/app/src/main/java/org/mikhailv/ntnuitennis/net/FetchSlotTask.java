package org.mikhailv.ntnuitennis.net;

import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import org.mikhailv.ntnuitennis.data.SlotDetailsInfo;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringReader;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.mikhailv.ntnuitennis.data.Globals.TAG_LOG;

/**
 * Created by MikhailV on 21.01.2017.
 */

/**
 * Downloads and parses html-pages for individual training sessions, and handles attend/substitute
 */
class FetchSlotTask extends FetchTask
{
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
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            CookieManager cm = NetworkFragment.cookieManager;
            if (cm.getCookieStore().getCookies().size() > 0) {
                conn.setRequestProperty("Cookie",
                        TextUtils.join(";",  cm.getCookieStore().getCookies()));
            }

            conn.connect();
            int response = conn.getResponseCode();
            if (response != HttpURLConnection.HTTP_OK)
                throw new IOException("HTTP error: " + response);
            inStream = conn.getInputStream();
            if (inStream != null) {
                InputStreamReader reader = new InputStreamReader(inStream, "UTF-8");
                char[] buffer = new char[BUFFER_SIZE]; // 2^15=32768

                int lastRead = 0, offset = 0;
                while (lastRead != -1 && offset < BUFFER_SIZE && !isCancelled()) {
                    publishProgress(offset * 100 / 16000);
                    lastRead = reader.read(buffer, offset, Math.min(MAX_READ, BUFFER_SIZE - offset));
                    offset += lastRead;
                }
                if (offset != -1)
                    result = new String(buffer, 0, offset + 1); // last read decrements offset
                inStream.close();
            }
        } finally {
            if (conn != null)
                conn.disconnect();
        }
        return result;
    }
    @Override
    protected Object parse(String rawData) throws ParseException
    {
        Log.i(TAG_LOG, rawData);
        SlotParser parser;
        try {
            int openingTagIndex = rawData.indexOf("<table>");
            int closingTagIndex = rawData.indexOf("</table>");
            rawData = rawData.substring(openingTagIndex, closingTagIndex + 8);
            parser = new SlotParser(rawData);
            parser.parse();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG_LOG, e.toString());
            throw new ParseException(e.toString(), 0);
        }
        return parser;
    }
    @Override
    protected void onPreExecute()
    {
        if (getCallbacks() != null) {
            getCallbacks().onPreDownload();
        }
    }
    @Override
    protected void onPostExecute(Object s)
    {
        if (getCallbacks() != null)
            getCallbacks().onSlotFetched((SlotDetailsInfo)s, getException());
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

    private static class SlotParser implements SlotDetailsInfo, Serializable
    {
        private static final int IND_INFO = 0;      // session info
        private static final int IND_REGS = 1;      // regular players
        private static final int IND_SUBST = 2;     // substitutes
        private static final int TITLE = 0;

        private XmlPullParser m_parser;
        private List<List<List<String>>> m_data = new ArrayList<>(3);

        public SlotParser(String rawHtml) throws XmlPullParserException
        {
            rawHtml = rawHtml.replaceAll("&nbsp;", " ").replaceAll("&Oslash;", "Ø")
                    .replaceAll("&oslash;", "ø").replaceAll("&Aring;", "Å")
                    .replaceAll("&aring;", "å").replaceAll("&AElig;", "Æ")
                    .replaceAll("&aelig;", "æ").replaceAll("&#9990", "");

            m_data.add(IND_INFO, new ArrayList<List<String>>());
            m_data.add(IND_REGS, new ArrayList<List<String>>());
            m_data.add(IND_SUBST, new ArrayList<List<String>>());

            m_parser = Xml.newPullParser();
            m_parser.setInput(new StringReader(rawHtml));
        }
        /**
         * One-shot
         */
        public void parse() throws XmlPullParserException, IOException, NullPointerException
        {
            if (m_parser.nextTag() != XmlPullParser.START_TAG)
                throw new XmlPullParserException("Malformed raw html slot table");

            boolean newLine = false;
            int depth = 1;
            int what = IND_INFO;
            List<String> currentLine = null;

            while (depth != 0) {
                switch (m_parser.next()) {
                    case XmlPullParser.START_TAG:
                        ++depth;
                        if (m_parser.getName().equals("tr")) {
                            currentLine = new ArrayList<>();
                        } else if (m_parser.getAttributeCount() == 2 &&
                                m_parser.getAttributeName(1).equals("rowspan")) {
                            newLine = true;
                            ++what;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        --depth;
                        if (m_parser.getName().equals("tr")) {
                            m_data.get(what).add(currentLine);
                            currentLine = null; // just in case
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if (depth > 2) {
                            String text = m_parser.getText().trim();
                            currentLine.add(text);
                        }
                        if (newLine) {
                            newLine = false;
                            m_data.get(what).add(currentLine);
                            currentLine = new ArrayList<>();
                        }
                        break;
                }
            }
            m_parser = null; // allow parser to be collected by GC
//            test();
        }
        //        void test() // temp
//        {
//            Log.d(TAG_LOG, "Info title: " + getInfoTitle());
//            for (int lineNumber = 0; lineNumber < getInfoSize(); lineNumber++) {
//                StringBuilder sb = new StringBuilder();
//                String[] line = getInfoLine(lineNumber);
//                for (String entry : line)
//                    sb.append(entry).append(' ');
//                Log.d(TAG_LOG, sb.toString());
//            }
//            Log.d(TAG_LOG, "Regulars title: " + getRegularsTitle());
//            for (int lineNumber = 0; lineNumber < getRegularsCount(); lineNumber++) {
//                StringBuilder sb = new StringBuilder();
//                String[] line = getRegularsLine(lineNumber);
//                for (String entry : line)
//                    sb.append(entry).append(' ');
//                Log.d(TAG_LOG, sb.toString());
//            }
//            Log.d(TAG_LOG, "Subst title: " + getSubstitutesTitle());
//            for (int lineNumber = 0; lineNumber < getSubstitutesCount(); lineNumber++) {
//                StringBuilder sb = new StringBuilder();
//                String[] line = getSubstitutesLine(lineNumber);
//                for (String entry : line)
//                    sb.append(entry).append(' ');
//                Log.d(TAG_LOG, sb.toString());
//            }
//        }
        @Override
        public int getInfoSize() // row count, doesn't include title
        {
            return m_data.get(IND_INFO).size() - 1;
        }
        @Override
        public int getRegularsCount() // NB! includes unoccupied places
        {
            return m_data.get(IND_REGS).size() - 1;
        }
        @Override
        public int getSubstitutesCount()
        {
            return m_data.get(IND_SUBST).size() - 1;
        }
        @Override
        public String getInfoTitle()
        {
            return m_data.get(IND_INFO).get(TITLE).get(0);
        }
        @Override
        public String getRegularsTitle()
        {
            return m_data.get(IND_REGS).get(TITLE).get(0);
        }
        @Override
        public String getSubstitutesTitle()
        {
            return m_data.get(IND_SUBST).get(TITLE).get(0);
        }
        @Override
        public String[] getInfoLine(int row)
        {
            List<String> line = m_data.get(IND_INFO).get(row + 1);
            return line.toArray(new String[line.size()]);
        }
        @Override
        public String[] getRegularsLine(int row) // row starts from 0, one line for each spot/player
        {
            List<String> line = m_data.get(IND_REGS).get(row + 1);
            return line.toArray(new String[line.size()]);
        }
        @Override
        public String[] getSubstitutesLine(int row)
        {
            List<String> line = m_data.get(IND_SUBST).get(row + 1);
            return line.toArray(new String[line.size()]);
        }
    }
}