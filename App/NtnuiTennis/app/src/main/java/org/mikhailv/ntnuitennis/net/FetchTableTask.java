package org.mikhailv.ntnuitennis.net;

import android.util.Log;
import android.util.Xml;

import org.mikhailv.ntnuitennis.data.Globals;
import org.mikhailv.ntnuitennis.data.TableBuilder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.StringReader;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

import static org.mikhailv.ntnuitennis.data.Globals.TAG_LOG;

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
            rawHtml = rawHtml.replaceAll("&nbsp;", " ").replaceAll("&Oslash;", "Ø")
                    .replaceAll("&oslash;", "ø").replaceAll("&Aring;", "Å")
                    .replaceAll("&aring;", "å").replaceAll("&AElig;", "Æ")
                    .replaceAll("&aelig;", "æ").replaceAll("&#9990", "");

            TableBuilder builder = new TableBuilder(8, Globals.Sizes.WEEK, Globals.Sizes.DAY);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(rawHtml));

            if (parser.nextTag() != XmlPullParser.START_TAG)
                throw new XmlPullParserException("Malformed raw html table");

            int depth = 1;
            int day = -2;       // day = index of last <td> or <th>
            int hour = -1;      // any illegal value will do
            Tag tag = null;

            while (depth != 0) {

                switch (parser.next()) {

                    case XmlPullParser.START_TAG:

                        String name = parser.getName();
                        ++depth;
                        if (name.equals("td"))
                            ++day;

                        tag = new Tag();
                        tag.name = parser.getName();
                        for (int i = 0; i < parser.getAttributeCount(); ++i)
                            tag.attributes
                                    .put(parser.getAttributeName(i), parser.getAttributeValue(i));

                        String link = tag.attributes.get("href");
                        if (link != null) {
                            builder.addLink(day, hour, link.substring(1));
                        }

                        String clazz = tag.attributes.get("class");
                        if (clazz != null) {
                            if (clazz.equals("expired"))
                                builder.addExpired(day, hour, true);
                            else if (clazz.equals("fulltime"))
                                builder.addHasAvailable(day, hour, false);
                        }

                        String tip = tag.attributes.get("title");
                        if (tip != null) {
                            tip.replaceAll("&lt;strong&gt;", "")
                                    .replaceAll("&lt;/strong&gt;", "")
                                    .replaceAll("&lt;br/&gt;", "\n")
                                    .replaceAll("&lt;i&gt;", "")
                                    .replaceAll("&lt;/i&gt;", "");
                            builder.addNames(day, hour, Arrays.asList(tip.split("\n")));
                        }
                        break;

                    case XmlPullParser.END_TAG:

                        --depth;
                        if (tag!= null && tag.name.equals("tr")) {
                            day = 0;
                        }
                        break;

                    case XmlPullParser.TEXT:

                        if (depth < 3 || tag == null || tag.name.equals("tr"))
                            break;

                        String text = parser.getText().trim();

                        if (tag.name.equals("th") && day >= 0) {
                            builder.addDayName(text);

                        } else if (tag.name.equals("td") && day == -1) {
                            Scanner in = new Scanner(text).useDelimiter("[^0-9]+");
                            hour = in.nextInt();

                        } else if (tag.name.equals("a")) {
                            builder.addLevel(day, hour, text);
                        }

                        break;
                }
            }
            return builder.getWeek();

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG_LOG, e.toString());
            throw new ParseException(e.toString(), 0);
        }
    }
    @Override
    protected void onPostExecute(Object o)
    {
        if (getCallbacks() != null)
            getCallbacks().onTableFetched(getException());
    }
    private static class Tag
    {
        public String name;
        public Map<String, String> attributes = new HashMap<>();
    }
}
