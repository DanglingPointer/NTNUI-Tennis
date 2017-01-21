package org.mikhailv.ntnuitennis.net;

/**
 * Created by MikhailV on 21.01.2017.
 */
public interface NetworkCallbacks
{
    void onProgressChanged(int progress);

    void onPreExecute();

    void onTableFetched();

    void onSlotFetched(String htmlPage);

    void onDownloadCanceled();
}
