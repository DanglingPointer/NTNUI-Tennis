package org.mikhailv.ntnuitennis.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.mikhailv.ntnuitennis.R;
import org.mikhailv.ntnuitennis.data.Globals;
import org.mikhailv.ntnuitennis.data.Slot;
import org.mikhailv.ntnuitennis.data.Week;

/**
 * Created by MikhailV on 07.01.2017.
 */

public class DayFragment extends Fragment
{
    private static final String ARG_DAY = "DayFragment.day";
    private static final String SAVED_EXPANDED = "DayFragment.expanded"; // expanded slots

    private SlotAdapter m_adapter;

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
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_day, container, false);
        RecyclerView recyclerView = (RecyclerView)root.findViewById(R.id.day_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        int dayIndex = getArguments().getInt(ARG_DAY);
        if (savedInstanceState == null) {
            m_adapter = new SlotAdapter(getActivity(), dayIndex);
        } else {
            boolean[] expanded = savedInstanceState.getBooleanArray(SAVED_EXPANDED);
            m_adapter = new SlotAdapter(getActivity(), dayIndex, expanded);
        }
        recyclerView.setAdapter(m_adapter);

        TextView dateText = (TextView)root.findViewById(R.id.day_text_date);
        dateText.setText(Week.getCurrent().getDay(dayIndex).getDate());

        setHasOptionsMenu(true);
        return root;
    }
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        boolean[] expanded = m_adapter.getExpanded();
        outState.putBooleanArray(SAVED_EXPANDED, expanded);
    }
}

class SlotAdapter extends RecyclerView.Adapter<SlotHolder>
{
    private FragmentActivity m_context;
    private int m_dayIndex;
    private SlotHolder[] m_slotHolders;
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
        if (m_savedState != null && m_savedState[position] != null) {
            expanded = m_savedState[position];
            m_savedState[position] = null; // invalidate state
        }
        holder.bind(Week.getCurrent().getDay(m_dayIndex).getSlot(hour), hour, expanded);
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
}

class SlotHolder extends RecyclerView.ViewHolder
{
    private TextView m_hourText;
    private TextView m_reservedText;

    private Button m_attendBtn;
    private ImageButton m_expandBtn;
    private Button m_detailsBtn;

    private boolean m_expanded;
    private Slot m_slotData;
    private RecyclerView.Adapter<SlotHolder> m_adapter;

    public SlotHolder(View root, RecyclerView.Adapter<SlotHolder> adapter)
    {
        super(root);
        m_adapter = adapter;

        m_hourText = (TextView)root.findViewById(R.id.slot_text_hour);
        m_reservedText = (TextView)root.findViewById(R.id.slot_text_reserved);

        m_attendBtn = (Button)root.findViewById(R.id.slot_btn_attend);
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
                setReservedText();
            }
        });
        m_detailsBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // TODO: launch a SlotActivity here
            }
        });
        m_attendBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // TODO: send http request to take an available spot
                m_adapter.notifyItemChanged(getAdapterPosition());
            }
        });
    }
    public void bind(Slot slot, int hour, boolean expanded)
    {
        m_hourText.setText(hour + ":00");

        if ((m_slotData = slot) == null) {
            setReservedText();
            m_detailsBtn.setText(null);
            m_expandBtn.setImageResource(R.drawable.ic_expand);
            setBtnsEnabled(false);
            return;
        }

        setBtnsEnabled(true);

        if (m_expanded = expanded) {
            m_expandBtn.setImageResource(R.drawable.ic_collapse);
        } else {
            m_expandBtn.setImageResource(R.drawable.ic_expand);
        }
        setReservedText();
        m_attendBtn.setEnabled(slot.hasAvailable() && !slot.isMeAttending());
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
        m_attendBtn.setEnabled(enabled);
        m_expandBtn.setEnabled(enabled);
        m_detailsBtn.setEnabled(enabled);
    }
    /**
     * Sets text if expanded
     */
    private void setReservedText()
    {
        if (m_slotData != null && m_expanded) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < m_slotData.getSize(); ++i) {
                sb.append(m_slotData.getName(i));
                if (i < m_slotData.getSize() - 1) sb.append(", ");
            }
            m_reservedText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            m_reservedText.setText(sb.toString());
        } else {
            m_reservedText.setText(null);
            m_reservedText.setHeight(0);
        }
    }
}
