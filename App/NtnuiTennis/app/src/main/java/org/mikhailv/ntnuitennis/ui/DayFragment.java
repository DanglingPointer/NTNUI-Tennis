package org.mikhailv.ntnuitennis.ui;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.mikhailv.ntnuitennis.R;
import org.mikhailv.ntnuitennis.TennisApp;
import org.mikhailv.ntnuitennis.data.Slot;
import org.mikhailv.ntnuitennis.data.SlotDetailsInfo;
import org.mikhailv.ntnuitennis.data.Week;
import org.mikhailv.ntnuitennis.net.NetworkCallbacks;

import java.util.List;

import static org.mikhailv.ntnuitennis.AppManager.DAY_SIZE;
import static org.mikhailv.ntnuitennis.AppManager.INIT_HOUR;
import static org.mikhailv.ntnuitennis.AppManager.TAG_LOG;

/**
 * Created by MikhailV on 07.01.2017.
 */

public class DayFragment extends Fragment implements NetworkCallbacks
{
    public interface Callbacks
    {
        void onNotificationsPressed();

        void onSlotDetailsPressed(int day, Slot slot);

        void onLogInPressed();

        void updateData();

        void eraseMe(DayFragment me);
    }

    private static final String ARG_DAY = "DayFragment.day";
    private static final String SAVED_EXPANDED = "DayFragment.expanded";

    private SlotAdapter m_adapter;
    private ProgressBar m_progressBar;
    private TextView m_dateText;
    private Callbacks m_callbacks;

    public static DayFragment newInstance(int day)
    {
        DayFragment f = new DayFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DAY, day);
        f.setArguments(args);
        return f;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_pager, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) // stub
    {
        switch (item.getItemId()) {
            case R.id.menu_btn_refresh:
                m_callbacks.updateData();
                return true;
            case R.id.menu_btn_prev:
                return true;
            case R.id.menu_btn_next:
                return true;
            case R.id.menu_btn_login:
                m_callbacks.onLogInPressed();
                return true;
            case R.id.menu_btn_about:
                return true;
            case R.id.menu_btn_notifications:
                m_callbacks.onNotificationsPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        m_callbacks = (Callbacks)context;
    }
    @Override
    public void onDetach()
    {
        super.onDetach();
        m_callbacks.eraseMe(this);
        m_callbacks = null;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);

        View root = inflater.inflate(R.layout.fragment_day, container, false);
        RecyclerView recyclerView = (RecyclerView)root.findViewById(R.id.day_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        int dayIndex = getArguments().getInt(ARG_DAY);
        if (savedInstanceState == null) {
            m_adapter = new SlotAdapter(getActivity(), dayIndex);
            Log.d(TAG_LOG, "onCreateView called without saved state");
        } else {
            boolean[] expanded = savedInstanceState.getBooleanArray(SAVED_EXPANDED);
            m_adapter = new SlotAdapter(getActivity(), dayIndex, expanded);
            Log.d(TAG_LOG, "onCreateView called with saved state");
        }
        recyclerView.setAdapter(m_adapter);

        m_progressBar = (ProgressBar)root.findViewById(R.id.day_progress_bar);
        m_progressBar.setProgress(0);

        m_dateText = (TextView)root.findViewById(R.id.day_text_date);
        m_dateText.setText(TennisApp.getManager(getActivity()).getCurrentWeek().getDay(dayIndex).getDate());

        return root;
    }
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        boolean[] expanded = m_adapter.getExpanded();
        outState.putBooleanArray(SAVED_EXPANDED, expanded);
    }
    /**
     * Network callbacks
     */
    @Override
    public void onProgressChanged(int progress)
    {
        Log.d(TAG_LOG, "Table Progress = " + progress);
        m_progressBar.setProgress(progress);
    }
    @Override
    public void onPreDownload()
    {
        m_progressBar.setVisibility(View.VISIBLE);
        m_progressBar.setProgress(0);
    }
    @Override
    public void onTableFetched(Week data, Exception e)
    {
        m_progressBar.setVisibility(View.GONE);
        if (e != null) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        TennisApp.getManager(getActivity()).setCurrentWeek(data);
        m_adapter.notifyDataSetChanged();
        m_dateText.setText(TennisApp.getManager(getActivity())
                .getCurrentWeek().getDay(m_adapter.getDayIndex()).getDate());
    }
    @Override
    public void onSlotFetched(SlotDetailsInfo data, Exception e)
    {
        m_progressBar.setVisibility(View.GONE);
    }
    @Override
    public void onAuthenticateFinished(Exception e)
    {
        m_progressBar.setVisibility(View.GONE);
        if (e != null)
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onDownloadCanceled()
    {
        m_progressBar.setVisibility(View.GONE);
        Toast.makeText(getActivity(), "Download canceled", Toast.LENGTH_SHORT).show();
    }
}

class SlotAdapter extends RecyclerView.Adapter<SlotHolder>
{
    private FragmentActivity m_context;
    private int m_dayIndex;
    private boolean[] m_expanded;

    public SlotAdapter(FragmentActivity context, int dayIndex)
    {
        this(context, dayIndex, new boolean[DAY_SIZE]);
    }
    public SlotAdapter(FragmentActivity context, int dayIndex, boolean[] savedState)
    {
        m_context = context;
        m_dayIndex = dayIndex;
        m_expanded = savedState;
    }
    @Override
    public SlotHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(m_context);
        View rootView = inflater.inflate(R.layout.item_slot, parent, false);
        return new SlotHolder(rootView, this);
    }
    @Override
    public void onBindViewHolder(SlotHolder holder, int position)
    {
        int hour = position + INIT_HOUR;
        holder.bind(TennisApp.getManager(m_context).getCurrentWeek().getDay(m_dayIndex).getSlot(hour), hour);
    }
    @Override
    public int getItemCount()
    {
        return DAY_SIZE;
    }
    public boolean getExpandedAt(int position)
    {
        return m_expanded[position];
    }
    public void setExpandedAt(int position, boolean expanded)
    {
        m_expanded[position] = expanded;
    }
    public boolean[] getExpanded()
    {
        return m_expanded;
    }
    void onSlotDetailsPressed(Slot slot)
    {
        ((DayFragment.Callbacks)m_context).onSlotDetailsPressed(m_dayIndex, slot);
    }
    public int getDayIndex()
    {
        return m_dayIndex;
    }
}

class SlotHolder extends RecyclerView.ViewHolder
{
    private TextView m_hourText;
    private TextView m_reservedText;

    private ImageButton m_expandBtn;
    private Button m_detailsBtn;

    private Slot m_slotData;
    private final SlotAdapter m_adapter;

    public SlotHolder(View root, SlotAdapter adapter)
    {
        super(root);
        m_adapter = adapter;

        m_hourText = (TextView)root.findViewById(R.id.slot_text_hour);
        m_reservedText = (TextView)root.findViewById(R.id.slot_text_reserved);

        m_expandBtn = (ImageButton)root.findViewById(R.id.slot_btn_expand);
        m_detailsBtn = (Button)root.findViewById(R.id.slot_btn_details);

        m_expandBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int position = getAdapterPosition();
                boolean expanded = m_adapter.getExpandedAt(position);
                if (expanded) {
                    m_expandBtn.setImageResource(R.drawable.ic_expand);
                    expanded = false;
                } else {
                    m_expandBtn.setImageResource(R.drawable.ic_collapse);
                    expanded = true;
                }
                configReservedText(expanded);
                m_adapter.setExpandedAt(position, expanded);
            }
        });
        m_detailsBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                m_adapter.onSlotDetailsPressed(m_slotData);
            }
        });
    }
    public void bind(Slot slot, int hour)
    {
        m_hourText.setText(hour + ":00");
        boolean expanded = m_adapter.getExpandedAt(getAdapterPosition());

        if (expanded) {
            m_expandBtn.setImageResource(R.drawable.ic_collapse);
        } else {
            m_expandBtn.setImageResource(R.drawable.ic_expand);
        }
        m_slotData = slot;

        m_detailsBtn.setEnabled(!m_slotData.isExpired() && m_slotData.getLevel() != null);
        m_expandBtn.setEnabled(m_slotData.getLevel() != null);
        m_detailsBtn.setText(slot.getLevel());

        setExpiredBackground(slot.isExpired());
        setNoavailableTextColor(slot.hasAvailable());
        configReservedText(expanded);
    }
    /**
     * Sets text if expanded
     */
    private void configReservedText(boolean expanded)
    {
        List<String> names = m_slotData.getAttending();
        if (names != null && expanded) {
            String text = TextUtils.join("\n", names);
            m_reservedText.setText(text);
            m_reservedText.setVisibility(View.VISIBLE);
        } else {
            m_reservedText.setText(null);
            m_reservedText.setVisibility(View.GONE);
        }
    }
    /**
     * Sets dark background color for buttons
     */
    private void setExpiredBackground(boolean expired)
    {
        int color = expired ? R.color.green : R.color.lightGreen;
        Context c = m_detailsBtn.getContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            m_detailsBtn.setBackgroundColor(c.getColor(color));
            m_expandBtn.setBackgroundColor(c.getColor(color));
        } else {
            m_detailsBtn.setBackgroundColor(c.getResources().getColor(color));
            m_expandBtn.setBackgroundColor(c.getResources().getColor(color));
        }
    }
    /**
     * Sets gray text color if no available places
     */
    private void setNoavailableTextColor(boolean available)
    {
        int color = available ? R.color.extraDarkGreen : R.color.gray;

        Context c = m_detailsBtn.getContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            m_detailsBtn.setTextColor(c.getColor(color));
        } else {
            m_detailsBtn.setTextColor(c.getResources().getColor(color));
        }
    }
}
