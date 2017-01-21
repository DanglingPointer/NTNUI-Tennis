package org.mikhailv.ntnuitennis.net;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.net.URL;

/**
 * Created by MikhailV on 21.01.2017.
 */

public class NetworkFragment extends Fragment implements NetworkCallbacks
{
    private static final String TAG = "NetworkFragment.Tag";

    private NetworkCallbacks m_callbacks;
    private FetchTask m_worker;

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
    public void downloadTable(URL homeAddress)
    {
        if (m_worker == null || m_worker.getStatus() != AsyncTask.Status.RUNNING) {

            // TODO: create FetchTableTask and execute
        }
    }
    public void downloadSlot(URL slotAddress)
    {
        if (m_worker == null || m_worker.getStatus() != AsyncTask.Status.RUNNING) {

            // TODO: create FetchSlotTask and execute
        }
    }
    public void authenticate(URL homeAddress, String email, String password, String lang)
    {
        if (m_worker == null || m_worker.getStatus() != AsyncTask.Status.RUNNING) {

            // TODO: create AuthenticateTask and execute
        }
    }
    /**
     * Callbacks from AsyncTasks
     */
    @Override
    public void onProgressChanged(int progress)
    {
        if (m_callbacks != null)
            m_callbacks.onProgressChanged(progress);
    }
    @Override
    public void onPreExecute()
    {
        if (m_callbacks != null)
            m_callbacks.onPreExecute();
    }
    @Override
    public void onTableFetched()
    {
        if (m_callbacks != null)
            m_callbacks.onTableFetched();
    }
    @Override
    public void onSlotFetched(String htmlPage)
    {
        if (m_callbacks != null)
            m_callbacks.onSlotFetched(htmlPage);
    }
    @Override
    public void onDownloadCanceled()
    {
        if (m_callbacks != null)
            m_callbacks.onDownloadCanceled();
    }
}
