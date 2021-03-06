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

import android.util.Xml;

import org.mikhailv.ntnuitennis.data.TableBuilder;
import org.mikhailv.ntnuitennis.data.Week;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.StringReader;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static org.mikhailv.ntnuitennis.AppManager.DAY_SIZE;
import static org.mikhailv.ntnuitennis.AppManager.INIT_HOUR;
import static org.mikhailv.ntnuitennis.AppManager.WEEK_SIZE;

/**
 * Created by MikhailV on 31.01.2017.
 */

class FetchTableTask extends FetchTask
{
    public FetchTableTask(NetworkCallbacks callbacks)
    {
        super(callbacks);
    }
    @Override
    protected Object parse(String rawHtml) throws ParseException
    {
        try {
            int openingTagIndex = rawHtml.indexOf("<table>");
            int closingTagIndex = rawHtml.indexOf("</table>");
            rawHtml = rawHtml.substring(openingTagIndex, closingTagIndex + "</table>".length());

            rawHtml = rawHtml.replace("&nbsp;", " ").replace("&Oslash;", "Ø")
                    .replace("&oslash;", "ø").replace("&Aring;", "Å")
                    .replace("&aring;", "å").replace("&AElig;", "Æ")
                    .replace("&aelig;", "æ").replace("&#9990", "");

            TableBuilder builder = new TableBuilder(INIT_HOUR, WEEK_SIZE, DAY_SIZE);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(rawHtml));

            if (parser.nextTag() != XmlPullParser.START_TAG)
                throw new XmlPullParserException("Malformed raw html table");

            int depth = 1;
            int day = -2;       // day = index of last <td> or <th>
            int hour = -1;      // any invalid value will do
            Tag tag = null;

            while (depth != 0) {

                switch (parser.next()) {

                    case XmlPullParser.START_TAG:

                        String name = parser.getName();
                        ++depth;
                        if (name.equals("td") || name.equals("th"))
                            ++day;

                        tag = new Tag();
                        tag.name = name;
                        for (int i = 0; i < parser.getAttributeCount(); ++i) {
                            tag.attributes
                                    .put(parser.getAttributeName(i), parser.getAttributeValue(i));
                        }

                        String link = tag.attributes.get("href");
                        if (link != null) {
                            builder.addLink(day, hour, BASE_URL + link.substring(1).replace("&amp;", "&"));
                        }

                        String clazz = tag.attributes.get("class");
                        if (clazz != null) {
                            if (clazz.equals("expired"))
                                builder.addExpired(day, hour, true);
                            else if (clazz.equals("fulltime") || clazz.equals("fulldropintime"))
                                builder.addHasAvailable(day, hour, false);
                        }

                        String tip = tag.attributes.get("title");
                        if (tip != null) {
                            tip = tip.replace("<strong>", "")
                                    .replace("</strong>", "")
                                    .replace("<br/>", "\n")
                                    .replace("<i>", "")
                                    .replace("</i>", "");
                            builder.addNames(day, hour, Arrays.asList(tip.split("\n")));
                        }

                        break;

                    case XmlPullParser.END_TAG:

                        --depth;
                        if (parser.getName().equals("tr")) {
                            day = -2;
                        }
                        tag = null;

                        break;

                    case XmlPullParser.TEXT:

                        if (depth < 3 || tag == null)
                            break;

                        String text = parser.getText().trim();

                        if (tag.name.equals("th") && day >= 0) {
                            builder.addDayName(text);
                        }
                        else if (tag.name.equals("td") && day == -1) {
                            Scanner in = new Scanner(text).useDelimiter("[^0-9]+");
                            hour = in.nextInt();
                        }
                        else if (tag.name.equals("a") || tag.name.equals("span")) { // span for expired hours
                            builder.addLevel(day, hour, text);
                        }

                        break;
                }
            }
            return builder.getWeek();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ParseException(e.toString(), 0);
        }
    }
    @Override
    protected void onPostExecute(Object o)
    {
        if (getCallbacks() != null)
            getCallbacks().onTableFetched((Week)o, getException());
    }
    private static class Tag
    {
        public String name;
        public Map<String, String> attributes = new HashMap<>();
    }
}
