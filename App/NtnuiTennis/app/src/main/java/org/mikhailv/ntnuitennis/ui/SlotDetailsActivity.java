package org.mikhailv.ntnuitennis.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.mikhailv.ntnuitennis.R;
import org.mikhailv.ntnuitennis.data.Globals;
import org.mikhailv.ntnuitennis.net.NetworkCallbacks;
import org.mikhailv.ntnuitennis.net.NetworkFragment;

/**
 * Created by MikhailV on 22.01.2017.
 */

public class SlotDetailsActivity extends AppCompatActivity implements NetworkCallbacks
{
    public static final String EXTRA_URL = "SlotDetailsActivity.URL";
    private static final String SAVED_TEXT = "SlotDetailsActivity.Text";

    private ProgressBar m_progress;
    private TextView m_text;
    private NetworkFragment m_networker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticity_slot_details);

        m_networker = NetworkFragment.addInstance(getSupportFragmentManager());

        m_progress = (ProgressBar)findViewById(R.id.activity_slot_progress);
        m_text = (TextView)findViewById(R.id.activity_slot_text);
        m_text.setMovementMethod(new ScrollingMovementMethod());

        if (savedInstanceState == null) {
            Log.d(Globals.TAG_LOG, "onCreate() called without savedInstanceState");
            String link = (String)getIntent().getSerializableExtra(EXTRA_URL);
            m_networker.downloadSlot(link);
        } else {
            Log.d(Globals.TAG_LOG, "onCreate() called with savedInstanceState");
            m_text.setText(savedInstanceState.getCharSequence(SAVED_TEXT));
            if (savedInstanceState.getCharSequence(SAVED_TEXT) == null)
                Log.d(Globals.TAG_LOG, "no text saved");
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(SAVED_TEXT, m_text.getText());
    }
    @Override
    protected void onStop()
    {
        super.onStop();
        m_networker.cancelDownload();
    }
    /**
     * Network callbacks
     */
    @Override
    public void onProgressChanged(int progress)
    {
        m_progress.setProgress(progress);
    }
    @Override
    public void onPreExecute()
    {
        m_progress.setVisibility(View.VISIBLE);
        m_progress.setProgress(0);
    }
    @Override
    public void onTableFetched(Exception e)
    {
        // Who cares?
    }
    @Override
    public void onSlotFetched(String htmlPage, Exception e)
    {
        if (e != null) {
            Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (Build.VERSION.SDK_INT >= 24){
                m_text.setText(Html.fromHtml(htmlPage, Html.FROM_HTML_MODE_COMPACT));
            }
            else {
                m_text.setText(Html.fromHtml(htmlPage));
            }

            m_progress.setVisibility(View.GONE);
        }
    }
    @Override
    public void onDownloadCanceled()
    {
        Toast.makeText(this, "Download canceled", Toast.LENGTH_SHORT).show();
        m_progress.setVisibility(View.GONE);
    }
}
