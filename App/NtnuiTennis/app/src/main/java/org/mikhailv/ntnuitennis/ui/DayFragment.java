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
import org.mikhailv.ntnuitennis.data.Globals;
import org.mikhailv.ntnuitennis.data.Slot;
import org.mikhailv.ntnuitennis.data.SlotDetailsInfo;
import org.mikhailv.ntnuitennis.net.NetworkCallbacks;

import java.util.List;

/**
 * Created by MikhailV on 07.01.2017.
 */

public class DayFragment extends Fragment implements NetworkCallbacks
{
    public interface Callbacks
    {
        void onNotificationsPressed();

        void onSlotDetailsPressed(int day, Slot slot);

        void onAttendPressed(int day, Slot slot);

        void updateData();

        void eraseMe(DayFragment me);
    }

    private static final String ARG_DAY = "DayFragment.day";
    private static final String SAVED_EXPANDED = "DayFragment.expanded";

    private SlotAdapter m_adapter;
    private ProgressBar m_progressBar;
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
            case R.id.menu_btn_settings:
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
    @Override
    public void onResume()
    {
        super.onResume();
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
            Log.d(Globals.TAG_LOG, "onCreateView called without saved state");
        } else {
            boolean[] expanded = savedInstanceState.getBooleanArray(SAVED_EXPANDED);
            m_adapter = new SlotAdapter(getActivity(), dayIndex, expanded);
            Log.d(Globals.TAG_LOG, "onCreateView called with saved state");
        }
        recyclerView.setAdapter(m_adapter);

        m_progressBar = (ProgressBar)root.findViewById(R.id.day_progress_bar);
        TextView dateText = (TextView)root.findViewById(R.id.day_text_date);
        dateText.setText(Globals.getCurrentWeek().getDay(dayIndex).getDate());

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
        m_progressBar.setProgress(progress);
    }
    @Override
    public void onPreDownload()
    {
        m_progressBar.setVisibility(View.VISIBLE);
        m_progressBar.setProgress(0);
    }
    @Override
    public void onTableFetched(Exception e)
    {
        // TODO: add Week to arguments, and forward it to the Manager
        m_progressBar.setVisibility(View.GONE);
        m_adapter.notifyDataSetChanged();
    }
    @Override
    public void onSlotFetched(SlotDetailsInfo data, Exception e)
    {
        m_progressBar.setVisibility(View.GONE);
        m_callbacks.updateData(); // refresh table after finished 'attend'
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
    private final SlotHolder[] m_slotHolders;
    private Boolean[] m_savedState;

    public SlotAdapter(FragmentActivity context, int dayIndex)
    {
        m_context = context;
        m_dayIndex = dayIndex;
        m_slotHolders = new SlotHolder[Globals.Sizes.DAY];
    }
    public SlotAdapter(FragmentActivity context, int dayIndex, boolean[] savedState)
    {
        this(context, dayIndex);
        m_savedState = new Boolean[savedState.length];
        for (int i = 0; i < savedState.length; ++i)
            m_savedState[i] = savedState[i];
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
        int hour = position + 8;
        boolean expanded = false;
        if (m_slotHolders[position] != null) {
            // rebinding after scrolling/updates
            expanded = m_slotHolders[position].isExpanded();
        } else if (m_savedState != null && m_savedState[position] != null) {
            // rebinding when recreating the fragment
            expanded = m_savedState[position];
            m_savedState[position] = null; // invalidate state
        }
        holder.bind(Globals.getCurrentWeek().getDay(m_dayIndex).getSlot(hour), hour, expanded);
        m_slotHolders[position] = holder;
    }
    @Override
    public int getItemCount()
    {
        return Globals.Sizes.DAY;
    }
    /**
     * Returns state (expanded/collapsed) of all bound SlotHolder's
     */
    public boolean[] getExpanded()
    {
        boolean[] temp = new boolean[Globals.Sizes.DAY];
        for (int i = 0; i < Globals.Sizes.DAY; ++i) {
            SlotHolder sh = m_slotHolders[i];
            temp[i] = (sh == null) ? false : sh.isExpanded();
        }
        return temp;
    }
    void onSlotDetailsPressed(Slot slot)
    {
        ((DayFragment.Callbacks)m_context).onSlotDetailsPressed(m_dayIndex, slot);
    }
}

class SlotHolder extends RecyclerView.ViewHolder
{
    private TextView m_hourText;
    private TextView m_reservedText;

    private ImageButton m_expandBtn;
    private Button m_detailsBtn;

    private boolean m_expanded;
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
                if (m_expanded) {
                    m_expandBtn.setImageResource(R.drawable.ic_expand);
                    m_expanded = false;
                } else {
                    m_expandBtn.setImageResource(R.drawable.ic_collapse);
                    m_expanded = true;
                }
                configReservedText();
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
    public void bind(Slot slot, int hour, boolean expanded)
    {
        m_hourText.setText(hour + ":00");

        if (m_expanded = expanded) {
            m_expandBtn.setImageResource(R.drawable.ic_collapse);
        } else {
            m_expandBtn.setImageResource(R.drawable.ic_expand);
        }
        m_slotData = slot;

        setBtnsEnabled(m_slotData.getLink() != null && !m_slotData.isExpired());
        m_detailsBtn.setText(slot.getLevel());
        if (slot.isExpired())
            setExpiredBackground();
        if (!slot.hasAvailable())
            setNoavailableTextColor();

        configReservedText();
    }
    public boolean isExpanded()
    {
        return m_expanded;
    }

    /**
     * Enables/disables all buttons
     */
    private void setBtnsEnabled(boolean enabled)
    {
        m_expandBtn.setEnabled(enabled);
        m_detailsBtn.setEnabled(enabled);
    }
    /**
     * Sets text if expanded
     */
    private void configReservedText()
    {
        List<String> names = m_slotData.getAttending();
        if (names != null && m_expanded) {
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
    private void setExpiredBackground()
    {
        Context c = m_detailsBtn.getContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            m_detailsBtn.setBackgroundColor(c.getColor(R.color.darkGreen));
            m_expandBtn.setBackgroundColor(c.getColor(R.color.darkGreen));
        } else {
            m_detailsBtn.setBackgroundColor(c.getResources().getColor(R.color.darkGreen));
            m_expandBtn.setBackgroundColor(c.getResources().getColor(R.color.darkGreen));
        }
    }
    /**
     * Sets gray text color
     */
    private void setNoavailableTextColor()
    {
        Context c = m_detailsBtn.getContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            m_detailsBtn.setTextColor(c.getColor(R.color.gray));
        } else {
            m_detailsBtn.setTextColor(c.getResources().getColor(R.color.gray));
        }
    }
}
