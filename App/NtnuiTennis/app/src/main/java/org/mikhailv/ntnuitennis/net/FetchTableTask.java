package org.mikhailv.ntnuitennis.net;

import android.util.Log;
import android.util.Xml;

import org.mikhailv.ntnuitennis.data.Globals;
import org.mikhailv.ntnuitennis.data.TableBuilder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.StringReader;
import java.text.ParseException;
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

            TableBuilder tb = new TableBuilder(8, Globals.Sizes.WEEK, Globals.Sizes.DAY);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(rawHtml));

            if (parser.nextTag() != XmlPullParser.START_TAG)
                throw new XmlPullParserException("Malformed raw html slot table");

            int depth = 1;

            int trIndex = -1, tdIndex = -1;
            Stack<String> tags = new Stack<>();
            while (depth != 0) {
                switch (parser.next()) {
                    case XmlPullParser.START_TAG:
                        ++depth;
                        tags.push(parser.getName());
                        if (tags.peek().equals("tr"))
                            ++trIndex;
                        else if (tags.peek().equals("td"))
                            ++tdIndex;

                        break;
                    case XmlPullParser.END_TAG:
                        --depth;
                        String lastTag = tags.pop();
                        if (lastTag.equals("tr"))
                            tdIndex = 0;

                        break;
                    case XmlPullParser.TEXT:
                        if (tags.size()==0 || tags.peek().equals("tr"))
                            break;

                        break;
                }
            }


            return tb.getWeek();
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
}
