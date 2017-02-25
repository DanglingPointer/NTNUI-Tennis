package org.mikhailv.ntnuitennis.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.mikhailv.ntnuitennis.AppManager;
import org.mikhailv.ntnuitennis.R;
import org.mikhailv.ntnuitennis.data.SlotDetailsInfo;
import org.mikhailv.ntnuitennis.data.Week;
import org.mikhailv.ntnuitennis.net.NetworkCallbacks;
import org.mikhailv.ntnuitennis.net.NetworkFragment;

import java.io.Serializable;

/**
 * Created by MikhailV on 22.01.2017.
 */

public class SlotDetailsActivity extends AppCompatActivity implements NetworkCallbacks
{
    public static Intent newIntent(Context context, String infoLink, int pagerPosition)
    {
        Intent i = new Intent(context, SlotDetailsActivity.class);
        i.putExtra(EXTRA_URL_INFO, infoLink);
        i.putExtra(EXTRA_PAGER_POSITION, pagerPosition);
        return i;
    }
    public static int decodePagerPosition(Intent i)
    {
        return i.getIntExtra(EXTRA_PAGER_POSITION, 0);
    }

    private static final String EXTRA_URL_INFO = "SlotDetailsActivity.URL_INFO";
    private static final String EXTRA_PAGER_POSITION = "SlotDetailsActivity.PAGER_POSITION";
    private static final String SAVED_DATA = "SlotDetailsActivity.Data";

    private ProgressBar m_progress;
    private LinearLayout m_rootView;
    private Button m_attendBtn;

    private NetworkFragment m_networker;
    private SlotDetailsInfo m_data;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_slot_details, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.menu_back_btn:
                finish();
                return true;
            case R.id.menu_refresh_btn:
                String link = getIntent().getStringExtra(EXTRA_URL_INFO);
                m_networker.downloadSlot(link);
                return true;
            case R.id.menu_login_btn:
                m_networker.authenticate(new AppManager.Credentials()
                {
                    @Override
                    public String getPassword() { return null; }
                    @Override
                    public String getEmail() { return null; }
                    @Override
                    public String getLanguage() { return null; }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticity_slot_details);

        m_data = null;
        m_networker = NetworkFragment.addInstance(getSupportFragmentManager());
        m_progress = (ProgressBar)findViewById(R.id.activity_slot_progress);

        m_attendBtn = (Button)findViewById(R.id.activity_slot_attend_btn);
        m_attendBtn.setEnabled(false);
        m_attendBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (m_data != null) {
                    m_networker.downloadSlot(m_data.getAttendingLink());
                }
            }
        });
        m_rootView = (LinearLayout)findViewById(R.id.activity_slot_linear_layout);

        if (savedInstanceState == null) {
            String link = getIntent().getStringExtra(EXTRA_URL_INFO);
            m_networker.downloadSlot(link);
            m_progress.setVisibility(View.VISIBLE); // onPreDownload wouldn't work yet at this point
        }
        else {
            m_data = (SlotDetailsInfo)savedInstanceState.getSerializable(SAVED_DATA);
            createLayout(m_data);
        }

        Intent i = new Intent();
        i.putExtra(EXTRA_PAGER_POSITION, getIntent().getIntExtra(EXTRA_PAGER_POSITION, 0));
        setResult(RESULT_OK, i);
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

        if (right != null && (right.contains("not ") || right.contains(" ikke"))
                && countOccurrences(' ', right) < 3)
            rightTextView.setTextColor(Color.RED);

        m_rootView.addView(infoLine);
    }
    private int countOccurrences(char of, CharSequence in)
    {
        int count = 0;
        for (int i = 0; i < in.length(); ++i) {
            if (in.charAt(i) == of)
                ++count;
        }
        return count;
    }
    //-----------Network callbacks------------------------------------------------------------------
    @Override
    public void onProgressChanged(int progress)
    {
        m_progress.setProgress(progress);
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
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
        else {
            createLayout(slotData);
            m_data = slotData;
            m_progress.setVisibility(View.GONE);

            if (m_data.getAttendingLink() != null) {
                m_attendBtn.setEnabled(true);

                if (m_data.getAttendingLink().contains("kommerikke")
                        || m_data.getAttendingLink().contains("fjern"))
                    m_attendBtn.setText(R.string.slot_btn_attend_not);
                else
                    m_attendBtn.setText(R.string.slot_btn_attend);
            }
            else {
                m_attendBtn.setEnabled(false);
            }
        }
    }
    @Override
    public void onDownloadCanceled()
    {
        Toast.makeText(this, "Download canceled", Toast.LENGTH_SHORT).show();
        m_progress.setVisibility(View.GONE);
    }
    @Override
    public void onAuthenticateFinished(Exception e)
    {
        m_progress.setVisibility(View.GONE);
        if (e != null)
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        else {
            String link = getIntent().getStringExtra(EXTRA_URL_INFO);
            m_networker.downloadSlot(link);
        }
    }
}
