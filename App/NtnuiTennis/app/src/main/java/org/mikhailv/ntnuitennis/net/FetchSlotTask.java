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

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mikhailv.ntnuitennis.data.SlotDetailsInfo;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.mikhailv.ntnuitennis.AppManager.TAG_LOG;

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
    protected Object parse(String rawData) throws ParseException
    {
        SlotParser parser;
        try {
            parser = new SlotParser();
            parser.parse(rawData, BASE_URL);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ParseException(e.toString(), 0);
        }
        return parser;
    }
    @Override
    protected void onPostExecute(Object s)
    {
        if (getCallbacks() != null)
            getCallbacks().onSlotFetched((SlotDetailsInfo)s, getException());
    }

    private static class SlotParser implements SlotDetailsInfo, Serializable
    {
        private static final int IND_INFO  = 0;      // session info
        private static final int IND_REGS  = 1;      // regular players
        private static final int IND_SUBST = 2;      // substitutes
        private static final int TITLE     = 0;

        private final List<List<List<String>>> m_data = new ArrayList<>(3);
        private String m_link;

        public SlotParser() throws XmlPullParserException
        {
            m_link = null;

            m_data.add(IND_INFO, new ArrayList<List<String>>());
            m_data.add(IND_REGS, new ArrayList<List<String>>());
            m_data.add(IND_SUBST, new ArrayList<List<String>>());
        }
        /**
         * One-shot
         */
        public void parse(String rawHtml, String baseURL) throws IOException, NullPointerException
        {
            Document doc = Jsoup.parse(rawHtml, baseURL);
            Element content = doc.getElementById("content");

            int what = IND_INFO;
            List<String> currentLine = null;

            // 1 line = 1 <tr> element
            Elements lines = content.getElementsByTag("tr");
            for (Element line : lines) { // tr
                currentLine = new ArrayList<>();

                // 1 sentence = 1 <th> or 1 <td> element
                Elements row = line.getElementsByTag("th");
                if (row.isEmpty()) row = line.getElementsByTag("td");
                for (Element text : row) { // td

                    currentLine.add(text.text().replace("|", ""));
                    if (text.hasAttr("rowspan")) {
                        m_data.get(++what).add(currentLine);
                        currentLine = new ArrayList<>();
                    }
                } // td
                m_data.get(what).add(currentLine);
            } // tr

            Elements urls = content.getElementsByTag("a");
            for (Element url : urls) {
                String link = url.attr("abs:href");
                if (link.contains("leggtilvikarid") || link.contains("fjernvikarid")
                    || link.contains("bekrefteid") || link.contains("kommerikkeid")) {
                    m_link = link;
                    break;
                }
            }
        }
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
            if (m_data.get(IND_SUBST).size() > 0)
                return m_data.get(IND_SUBST).get(TITLE).get(0);
            return null;
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
        @Override
        public String getAttendingLink()
        {
            return m_link;
        }
    }
}