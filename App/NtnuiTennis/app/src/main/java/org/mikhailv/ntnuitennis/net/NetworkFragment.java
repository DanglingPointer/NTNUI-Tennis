package org.mikhailv.ntnuitennis.net;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.mikhailv.ntnuitennis.data.Globals;
import org.mikhailv.ntnuitennis.data.SlotDetailsInfo;

import java.net.CookieHandler;
import java.net.CookieManager;

/**
 * Created by MikhailV on 21.01.2017.
 */

public class NetworkFragment extends Fragment implements NetworkCallbacks
{
    private static final String TAG = "NetworkFragment.Tag";

    private NetworkCallbacks m_callbacks;
    private NetworkTask m_worker;

    static CookieManager cookieManager;

    static {
        cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
    }

    public static NetworkFragment addInstance(FragmentManager fm)
    {
        NetworkFragment nf = (NetworkFragment)fm.findFragmentByTag(TAG);
        if (nf == null) {
            nf = new NetworkFragment();
            fm.beginTransaction().add(nf, TAG).commit();
        }
        return nf;
    }
    /**
     * Lifecycle methods
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        authenticate(Globals.HOME_URL, "mikail.vasilyev@gmail.com", "1234", "no");
    }
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        m_callbacks = (NetworkCallbacks)context;
    }
    @Override
    public void onDetach()
    {
        super.onDetach();
        m_callbacks = null;
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (m_worker != null && m_worker.getStatus() == AsyncTask.Status.RUNNING) {
            m_worker.cancel(true);
            m_worker.freeCallbacks();
        }
    }
    /**
     * Network commands
     */
    public void downloadTable(String homeAddress)
    {
        if (m_worker == null || m_worker.getStatus() != AsyncTask.Status.RUNNING) {

            // TODO: create FetchTableTask and execute
            throw new UnsupportedOperationException();
        }
    }
    public void downloadSlot(String slotAddress)
    {
        if (m_worker == null || m_worker.getStatus() != AsyncTask.Status.RUNNING) {
            m_worker = new FetchSlotTask(this);
            m_worker.execute(slotAddress);
        }
    }
    public void authenticate(String homeURL, String email, String password, String lang)
    {
        if (m_worker == null || m_worker.getStatus() != AsyncTask.Status.RUNNING) {
            m_worker = new AuthenticateTask(this, email, password, lang);
            m_worker.execute(homeURL);
        }
    }
    public void cancelDownload()
    {
        if (m_worker != null && m_worker.getStatus() == AsyncTask.Status.RUNNING)
            m_worker.cancel(true);
    }
    /**
     * Internal callbacks from AsyncTasks.
     * Necessary because the fragment might be reattached while a task is executing
     */
    @Override
    public void onProgressChanged(int progress)
    {
        if (m_callbacks != null)
            m_callbacks.onProgressChanged(progress);
    }
    @Override
    public void onPreDownload()
    {
        if (m_callbacks != null)
            m_callbacks.onPreDownload();
    }
    @Override
    public void onTableFetched(Exception e)
    {
        if (m_callbacks != null)
            m_callbacks.onTableFetched(e);
    }
    @Override
    public void onSlotFetched(SlotDetailsInfo slotInfo, Exception e)
    {
        if (m_callbacks != null)
            m_callbacks.onSlotFetched(slotInfo, e);
    }
    @Override
    public void onDownloadCanceled()
    {
        if (m_callbacks != null)
            m_callbacks.onDownloadCanceled();
    }
}
