package org.mikhailv.ntnuitennis.net;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

/**
 * Created by MikhailV on 31.01.2017.
 */

public class FetchTableTask extends FetchTask
{
    public FetchTableTask(NetworkCallbacks callbacks)
    {
        super(callbacks);
    }
    @Override
    protected String download(URL link) throws IOException
    {
        return null;
    }
    @Override
    protected Object parse(String rawData) throws ParseException
    {
        return null;
    }
    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
    }
    @Override
    protected void onPostExecute(Object o)
    {
        super.onPostExecute(o);
    }
    @Override
    protected void onProgressUpdate(Integer... values)
    {
        super.onProgressUpdate(values);
    }
    @Override
    protected void onCancelled()
    {
        super.onCancelled();
    }
}
