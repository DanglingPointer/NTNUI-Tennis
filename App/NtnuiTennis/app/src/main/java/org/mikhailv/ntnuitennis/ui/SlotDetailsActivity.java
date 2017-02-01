package org.mikhailv.ntnuitennis.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.mikhailv.ntnuitennis.R;
import org.mikhailv.ntnuitennis.data.SlotDetailsInfo;
import org.mikhailv.ntnuitennis.data.Week;
import org.mikhailv.ntnuitennis.net.NetworkCallbacks;
import org.mikhailv.ntnuitennis.net.NetworkFragment;

import java.io.Serializable;

import static org.mikhailv.ntnuitennis.data.Globals.TAG_LOG;

/**
 * Created by MikhailV on 22.01.2017.
 */

public class SlotDetailsActivity extends AppCompatActivity implements NetworkCallbacks
{
    // Should take day and hour and find links through the right Slot in Globals
    public static Intent newIntent(Context context, String infoLink, String attendLink)
    {
        Intent i = new Intent(context, SlotDetailsActivity.class);
        i.putExtra(EXTRA_URL_ATTEND, attendLink);
        i.putExtra(EXTRA_URL_INFO, infoLink);
        return i;
    }

    private static final String EXTRA_URL_INFO = "SlotDetailsActivity.URL_INFO";
    private static final String EXTRA_URL_ATTEND = "SlotDetailsActivity.URL_ATTEND";
    private static final String SAVED_DATA = "SlotDetailsActivity.Data";

    private ProgressBar m_progress;
    private LinearLayout m_rootView;
    private Button m_attendBtn;

    private NetworkFragment m_networker;
    private SlotDetailsInfo m_data;
    private String m_attendLink;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticity_slot_details);

        m_attendLink = getIntent().getStringExtra(EXTRA_URL_ATTEND);
        m_data = null;
        m_networker = NetworkFragment.addInstance(getSupportFragmentManager());
        m_progress = (ProgressBar)findViewById(R.id.activity_slot_progress);

        m_attendBtn = (Button)findViewById(R.id.activity_slot_attend_btn);
        m_attendBtn.setEnabled(m_attendLink != null);
        if (m_attendLink != null) {
            if (m_attendLink.contains("kommerikke") || m_attendLink.contains("fjern"))
                m_attendBtn.setText(R.string.slot_btn_attend_not);
            else
                m_attendBtn.setText(R.string.slot_btn_attend);
        }
        m_attendBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                m_networker.downloadSlot(m_attendLink);
            }
        });
        m_rootView = (LinearLayout)findViewById(R.id.activity_slot_linear_layout);

        if (savedInstanceState == null) {
            String link = getIntent().getStringExtra(EXTRA_URL_INFO);
            m_networker.downloadSlot(link);
            m_progress.setVisibility(View.VISIBLE); // onPreDownload wouldn't work yet at this point
        } else {
            m_data = (SlotDetailsInfo)savedInstanceState.getSerializable(SAVED_DATA);
            createLayout(m_data);
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVED_DATA, (Serializable)m_data);
    }
    @Override
    protected void onStop()
    {
        super.onStop();
        m_networker.cancelDownload();
    }
    //-----------Helpers----------------------------------------------------------------------------
    private void createLayout(SlotDetailsInfo data)
    {
        int childrenCount = m_rootView.getChildCount();
        if (childrenCount > 1) {
            m_rootView.removeViewsInLayout(1, childrenCount - 1);
        }

        addTitleLine(data.getInfoTitle());

        for (int row = 0; row < data.getInfoSize(); ++row) {
            addDataLine(data.getInfoLine(row), false);
        }

        addTitleLine(data.getRegularsTitle());

        for (int row = 0; row < data.getRegularsCount(); ++row) {
            addDataLine(data.getRegularsLine(row), true);
        }

        addTitleLine(data.getSubstitutesTitle());

        for (int row = 0; row < data.getSubstitutesCount(); ++row) {
            addDataLine(data.getSubstitutesLine(row), true);
        }
    }
    private void addTitleLine(String text)
    {
        View titleLineView = LayoutInflater.from(this).inflate(R.layout.slot_title_line, m_rootView, false);
        TextView titleText = (TextView)titleLineView.findViewById(R.id.slot_title_text);
        titleText.setText(text);
        m_rootView.addView(titleLineView);
    }
    private void addDataLine(String[] line, boolean italic)
    {
        View infoLine = LayoutInflater.from(this).inflate(R.layout.slot_info_line, m_rootView, false);
        TextView leftTextView = (TextView)infoLine.findViewById(R.id.slot_info_text_left);
        TextView rightTextView = (TextView)infoLine.findViewById(R.id.slot_info_text_right);
        if (italic)
            rightTextView.setTypeface(rightTextView.getTypeface(), Typeface.ITALIC);

        String left = line[0];
        String right = (line.length > 1) ? line[1] : null;

        if (line.length > 2) {
            for (int i = 2; i < line.length; ++i)
                right += ('\n' + line[i]);
        }
        leftTextView.setText(left);
        rightTextView.setText(right);

        m_rootView.addView(infoLine);
    }
    //-----------Network callbacks------------------------------------------------------------------
    @Override
    public void onProgressChanged(int progress)
    {
        m_progress.setProgress(progress);
        Log.d(TAG_LOG, "Progress: " + progress);
    }
    @Override
    public void onPreDownload()
    {
        m_progress.setVisibility(View.VISIBLE);
        m_progress.setProgress(0);
    }
    @Override
    public void onTableFetched(Week week, Exception e)
    {
        // Who cares?
    }
    @Override
    public void onSlotFetched(SlotDetailsInfo slotData, Exception e)
    {
        if (e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            createLayout(slotData);
            m_data = slotData;
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
