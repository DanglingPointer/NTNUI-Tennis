package org.mikhailv.ntnuitennis.net;

import org.mikhailv.ntnuitennis.data.SlotDetailsInfo;
import org.mikhailv.ntnuitennis.data.Week;

/**
 * Created by MikhailV on 21.01.2017.
 */
public interface NetworkCallbacks
{
    void onProgressChanged(int progress);

    void onPreDownload();

    void onTableFetched(Week week, Exception e);

    void onSlotFetched(SlotDetailsInfo slotInfo, Exception e);

    void onAuthenticateFinished(Exception e);

    void onDownloadCanceled();
}
