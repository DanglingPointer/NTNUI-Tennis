package org.mikhailv.ntnuitennis.net;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import org.mikhailv.ntnuitennis.AppManager;
import org.mikhailv.ntnuitennis.TennisApp;
import org.mikhailv.ntnuitennis.data.SlotDetailsInfo;
import org.mikhailv.ntnuitennis.data.Week;
import org.mikhailv.ntnuitennis.ui.LoginDialogFragment;

import java.net.CookieHandler;
import java.net.CookieManager;

import static org.mikhailv.ntnuitennis.AppManager.TAG_LOG;

/**
 * Created by MikhailV on 21.01.2017.
 */

public class NetworkFragment extends Fragment implements NetworkCallbacks
{
    private static final String TAG = "NetworkFragment.Tag";
    private static final int REQUEST_LOGIN = 0;
    private static final String TAG_LOGIN = "NetworkFragment.TAG_LOGIN";

    static CookieManager cookieManager;

    static {
        cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
    }

    /**
     * Creates a new instance if none exists, and attaches it using the fragment manager
     */
    public static NetworkFragment addInstance(FragmentManager fm)
    {
        NetworkFragment nf = (NetworkFragment)fm.findFragmentByTag(TAG);
        if (nf == null) {
            nf = new NetworkFragment();
            fm.beginTransaction().add(nf, TAG).commit();
        }
        return nf;
    }

    private NetworkCallbacks m_callbacks;
    private NetworkTask m_fetchWorker;
    private NetworkTask m_authWorker;

    //-----------------Lifecycle methods------------------------------------------------------------
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
        TennisApp.getManager(context).setNetworker(this); // might call authenticate()
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
        if (m_fetchWorker != null) {
            if (m_fetchWorker.getStatus() == AsyncTask.Status.RUNNING)
                m_fetchWorker.cancel(true);
            m_fetchWorker.freeCallbacks();
        }
        if (m_authWorker != null) {
            if (m_authWorker.getStatus() == AsyncTask.Status.RUNNING)
                m_authWorker.cancel(true);
            m_authWorker.freeCallbacks();
        }
    }
    /**
     * Called to authenticate user after she has entered credentials
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode == REQUEST_LOGIN) {
            // Try authenticating with the new credentials
            AppManager.Credentials creds = LoginDialogFragment.decodeCredentials(data);
            authenticate(creds);
        }
    }
    //-----------------Network-commands-------------------------------------------------------------
    /**
     * Downloads and parses the main page. Returns a fully initialized Week object using
     * onTableFetched(). Changes progress during the download.
     */
    public void downloadTable()
    {
        if (m_fetchWorker == null || m_fetchWorker.getStatus() != AsyncTask.Status.RUNNING) {
            AppManager am = TennisApp.getManager(getActivity());
            m_fetchWorker = new FetchTableTask(this);
            m_fetchWorker.execute(am.getTableURL());
        }
    }
    /**
     * Downloads and parses a session page. Sets progress during the download, and calls
     * onSlotFetched() at the end.
     */
    public void downloadSlot(String slotAddress)
    {
        if (m_fetchWorker == null || m_fetchWorker.getStatus() != AsyncTask.Status.RUNNING) {
            m_fetchWorker = new FetchSlotTask(this);
            m_fetchWorker.execute(slotAddress);
        }
    }
    /**
     * If 'credentials' has null-fields, we launch the login-dialog first, otherwise we authenticate
     * user (using a POST request) and save the received cookie. No html is downloaded, and no
     * progress set during authentication. At the end onAuthenticateFinished() is called
     */
    public void authenticate(AppManager.Credentials credentials)
    {
        String email = credentials.getEmail();
        String password = credentials.getPassword();
        String lang = credentials.getLanguage();

        Log.d(TAG_LOG, "email = " + email + ", passw = " + password + ", lang = " + lang);

        if (email == null || password == null || lang == null) {
            // open user login prompt
            FragmentManager fm = getFragmentManager();
            LoginDialogFragment dialog = new LoginDialogFragment();
            dialog.setTargetFragment(this, REQUEST_LOGIN);
            dialog.show(fm, TAG_LOGIN);
            return;
        }

        if (m_authWorker == null || m_authWorker.getStatus() != AsyncTask.Status.RUNNING) {
            m_authWorker = new AuthenticateTask(this, email, password, lang);
            m_authWorker.execute(TennisApp.getManager(getActivity()).getTableURL());
        }
    }
    public void cancelDownload()
    {
        if (m_fetchWorker != null && m_fetchWorker.getStatus() == AsyncTask.Status.RUNNING)
            m_fetchWorker.cancel(true);
    }
    //-----------------Internal callbacks from AsyncTasks-------------------------------------------
    // Necessary because the fragment might be reattached while a task is executing
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
    /**
     * If 'e' is not null, the result is invalid
     */
    @Override
    public void onTableFetched(Week week, Exception e)
    {
        if (m_callbacks != null)
            m_callbacks.onTableFetched(week, e);
    }
    /**
     * If 'e' is not null, the result is invalid
     */
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
    /**
     * If 'e' is not null, the result is invalid
     */
    @Override
    public void onAuthenticateFinished(Exception e)
    {
        Log.d(TAG_LOG, "Authentication finished");
        if (m_callbacks != null)
            m_callbacks.onAuthenticateFinished(e);
    }
}
