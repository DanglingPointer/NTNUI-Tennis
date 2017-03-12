package org.mikhailv.ntnuitennis.ui;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.mikhailv.ntnuitennis.R;
import org.mikhailv.ntnuitennis.TennisApp;
import org.mikhailv.ntnuitennis.data.DBManager;
import org.mikhailv.ntnuitennis.data.Slot;
import org.mikhailv.ntnuitennis.data.SlotDetailsInfo;
import org.mikhailv.ntnuitennis.data.Week;
import org.mikhailv.ntnuitennis.net.NetworkCallbacks;

import java.util.List;

import static org.mikhailv.ntnuitennis.AppManager.DAY_SIZE;
import static org.mikhailv.ntnuitennis.AppManager.INIT_HOUR;

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
    private SwipeRefreshLayout m_swiper;

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
                if (TennisApp.getManager(getActivity()).decrementWeek())
                    m_callbacks.updateData();
                return true;
            case R.id.menu_btn_next:
                TennisApp.getManager(getActivity()).incrementWeek();
                m_callbacks.updateData();
                return true;
            case R.id.menu_btn_login:
                m_callbacks.onLogInPressed();
                return true;
            case R.id.menu_btn_about:
                startActivity(AboutActivity.newIntent(getActivity()));
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
        }
        else {
            boolean[] expanded = savedInstanceState.getBooleanArray(SAVED_EXPANDED);
            m_adapter = new SlotAdapter(getActivity(), dayIndex, expanded);
        }
        recyclerView.setAdapter(m_adapter);

        m_progressBar = (ProgressBar)root.findViewById(R.id.day_progress_bar);
        m_progressBar.setProgress(0);

        m_dateText = (TextView)root.findViewById(R.id.day_text_date);
        m_dateText.setText(TennisApp.getManager(getActivity()).getCurrentWeek().getDay(dayIndex).getDate());

        m_swiper = (SwipeRefreshLayout)root.findViewById(R.id.swiperefresh);
        m_swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh() { m_callbacks.updateData(); }
        });
        m_swiper.setColorSchemeResources(R.color.darkGreen);
        m_swiper.setProgressBackgroundColorSchemeResource(R.color.lightGreen);

        return root;
    }
    @Override
    public void onResume()
    {
        super.onResume();
        m_adapter.notifyDataSetChanged();
    }
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        boolean[] expanded = m_adapter.getExpanded();
        outState.putBooleanArray(SAVED_EXPANDED, expanded);
    }
    //-------------------------Network callbacks----------------------------------------------------
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
    public void onTableFetched(Week data, Exception e)
    {
        m_swiper.setRefreshing(false);
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
        String message = (e == null) ?
                getResources().getString(R.string.login_toast) : e.getMessage();
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        if (e == null)
            m_callbacks.updateData();
    }
    @Override
    public void onDownloadCanceled()
    {
        m_swiper.setRefreshing(false);
        m_progressBar.setVisibility(View.GONE);
        Toast.makeText(getActivity(), "Download canceled", Toast.LENGTH_SHORT).show();
    }
}

class SlotAdapter extends RecyclerView.Adapter<SlotHolder>
{
    private final FragmentActivity m_context;
    private final DBManager m_db;
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
        m_db = new DBManager(context);
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
        Slot s = TennisApp.getManager(m_context).getCurrentWeek().getDay(m_dayIndex).getSlot(hour);
        String link = s.getLink();
        holder.bind(s, hour, link != null && m_db.containsLink(link));
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
    private ImageView m_imageChecked;

    private Slot m_slotData;
    private final SlotAdapter m_adapter;

    public SlotHolder(View root, SlotAdapter adapter)
    {
        super(root);
        m_adapter = adapter;

        m_hourText = (TextView)root.findViewById(R.id.slot_text_hour);
        m_reservedText = (TextView)root.findViewById(R.id.slot_text_reserved);

        m_imageChecked = (ImageView)root.findViewById(R.id.slot_image_checked);
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
                }
                else {
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
    public void bind(Slot slot, int hour, boolean alarmSet)
    {
        m_hourText.setText(hour + ":00");
        boolean expanded = m_adapter.getExpandedAt(getAdapterPosition());

        if (expanded) {
            m_expandBtn.setImageResource(R.drawable.ic_collapse);
        }
        else {
            m_expandBtn.setImageResource(R.drawable.ic_expand);
        }
        m_slotData = slot;

        boolean isSession = m_slotData.getLevel() != null;
        m_detailsBtn.setEnabled(!m_slotData.isExpired() && isSession);
        m_expandBtn.setEnabled(isSession);
        m_detailsBtn.setText(slot.getLevel());

        if (isSession) {
            m_imageChecked.setImageResource(alarmSet ? R.drawable.ic_notification_set
                    : R.drawable.ic_notification_not_set);
        }
        else {
            m_imageChecked.setImageResource(0);
        }

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
        }
        else {
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
        }
        else {
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
        }
        else {
            m_detailsBtn.setTextColor(c.getResources().getColor(color));
        }
    }
}
