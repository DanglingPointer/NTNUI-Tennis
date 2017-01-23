package org.mikhailv.ntnuitennis.net;

import android.util.Log;
import android.util.Xml;

import org.mikhailv.ntnuitennis.data.Globals;
import org.mikhailv.ntnuitennis.data.SlotDetailsInfo;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
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
    protected SlotDetailsInfo parse(String rawData) throws ParseException
    {
        SlotParser parser;
        try {
            int openingTagIndex = rawData.indexOf("<table>");
            int closingTagIndex = rawData.indexOf("</table>");
            rawData = rawData.substring(openingTagIndex, closingTagIndex + 8);
            parser = new SlotParser(rawData);
            parser.parse();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(Globals.TAG_LOG, e.toString());
            throw new ParseException(e.toString(), 0);
        }
        return parser;
    }
    @Override
    protected void onPreExecute()
    {
        if (getCallbacks() != null) {
            getCallbacks().onPreExecute();
        }
    }
    @Override
    protected void onPostExecute(SlotDetailsInfo s)
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

    private static class SlotParser implements SlotDetailsInfo
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
        public void parse() throws XmlPullParserException, IOException
        {
            if (m_parser.nextTag() != XmlPullParser.START_TAG)
                throw new XmlPullParserException("Malformed raw html table");
            int depth = 1;

            boolean newLine = false;

            int what = INFO;
            while (depth != 0) {
                switch (m_parser.next()) {
                    case XmlPullParser.START_TAG:
                        ++depth;
                        if (m_parser.getAttributeCount() == 2 &&
                                m_parser.getAttributeName(1).equals("rowspan")) {
                            newLine = true;
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
                        break;
                    case XmlPullParser.TEXT:
                        if (depth > 2) {
                            String text = m_parser.getText().trim();
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
                            if (newLine) {
                                newLine = false;
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
        }
        @Override
        public int getInfoSize()
        {
            return m_rest.size() - 1;
        }
        @Override
        public int getRegularsCount() // NB! includes unoccupied places
        {
            return m_regulars.size() - 1;
        }
        @Override
        public int getSubstitutesCount()
        {
            return m_substitutes.size() - 1;
        }
        @Override
        public String getInfoTitle()
        {
            return m_rest.get(0).get(0);
        }
        @Override
        public String[] getInfoLine(int row)
        {
            List<String> line = m_rest.get(row + 1);
            return line.toArray(new String[line.size()]);
        }
        @Override
        public String getRegularsTitle()
        {
            return m_regulars.get(0).get(0);
        }
        @Override
        public String[] getRegularsLine(int row) // row starts from 0, one line for each spot/player
        {
            List<String> line = m_regulars.get(row + 1);
            return line.toArray(new String[line.size()]);
        }
        @Override
        public String getSubstitutesTitle()
        {
            return m_substitutes.get(0).get(0);
        }
        @Override
        public String[] getSubstitutesLine(int row)
        {
            List<String> line = m_substitutes.get(row + 1);
            return line.toArray(new String[line.size()]);
        }
    }
}

//01-23 23:12:34.649 14349-19546/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Timeinformasjon_
//01-23 23:12:34.649 14349-19546/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Dato:_Torsdag 26/1_
//01-23 23:12:34.649 14349-19546/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Klokkeslett:_13:00-14:00_
//01-23 23:12:34.649 14349-19546/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Sted:_Øya_
//01-23 23:12:34.649 14349-19546/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Bane:_A_
//01-23 23:12:34.649 14349-19546/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Nivå:_Nybegynner_
//01-23 23:12:34.649 14349-19546/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Trener/instruktør:_Odin �stvedt ( 948 46 408)_(kommer)_
//01-23 23:12:34.649 14349-19546/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Ukens tema:_Ballkontroll_
//01-23 23:12:34.649 14349-19546/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: ~~~~~~~~~~~~~~~~
//01-23 23:12:34.649 14349-19546/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Faste spillere (6 stk.)_
//01-23 23:12:34.649 14349-19546/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Margareth B._(kommer ikke)_
//01-23 23:12:34.649 14349-19546/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Nora Elisabeth S._(kommer ikke)_
//01-23 23:12:34.649 14349-19546/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Peter Michael �._(kommer)_
//01-23 23:12:34.649 14349-19546/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Stina S._(kommer)_
//01-23 23:12:34.649 14349-19546/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Fast ledig plass_
//01-23 23:12:34.649 14349-19546/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Fast ledig plass_
//01-23 23:12:34.649 14349-19546/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: ~~~~~~~~~~~~~~~~
//01-23 23:12:34.649 14349-19546/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Vikarer (0 stk.)_
//01-23 23:12:34.649 14349-19546/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: -_
//
//
//01-23 23:13:05.299 14349-20432/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Timeinformasjon_
//01-23 23:13:05.299 14349-20432/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Dato:_Tirsdag 24/1_
//01-23 23:13:05.299 14349-20432/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Klokkeslett:_10:00-11:00_
//01-23 23:13:05.299 14349-20432/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Sted:_Øya_
//01-23 23:13:05.299 14349-20432/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Bane:_A_
//01-23 23:13:05.299 14349-20432/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Nivå:_Nybegynner+_
//01-23 23:13:05.299 14349-20432/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Ukens tema:_
//01-23 23:13:05.299 14349-20432/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: ~~~~~~~~~~~~~~~~
//01-23 23:13:05.299 14349-20432/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Faste spillere (4 stk.)_
//01-23 23:13:05.299 14349-20432/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Eirik H._(kommer ikke)_
//01-23 23:13:05.299 14349-20432/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Erik Log R._(kommer ikke)_
//01-23 23:13:05.299 14349-20432/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Marvel L._(kommer ikke)_
//01-23 23:13:05.299 14349-20432/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Fast ledig plass_
//01-23 23:13:05.299 14349-20432/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: ~~~~~~~~~~~~~~~~
//01-23 23:13:05.299 14349-20432/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Vikarer (4 stk.)_
//01-23 23:13:05.299 14349-20432/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Ole S._
//01-23 23:13:05.299 14349-20432/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Vince C._
//01-23 23:13:05.299 14349-20432/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Truls P._
//01-23 23:13:05.299 14349-20432/org.mikhailv.ntnuitennis D/MIKHAILS_LOG: Renate B._