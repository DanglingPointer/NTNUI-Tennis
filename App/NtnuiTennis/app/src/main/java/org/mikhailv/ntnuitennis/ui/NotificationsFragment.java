/*
 * MIT License
 *
 * Copyright (c) 2017-2018 Mikhail Vasilyev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.mikhailv.ntnuitennis.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.mikhailv.ntnuitennis.R;
import org.mikhailv.ntnuitennis.TennisApp;
import org.mikhailv.ntnuitennis.data.SessionInfo;

import java.util.List;

/**
 * Created by MikhailV on 18.01.2017.
 */

public class NotificationsFragment extends Fragment
{
    private static final String SAVED_CHECKED = "NotificationsFragment.checked";

    private HourAdapter m_adapter;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_notifications, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.menu_not_save_btn:
                TennisApp.getManager(getActivity()).saveHoursInfo(m_adapter.getData());
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        RecyclerView recyclerView = (RecyclerView)root.findViewById(R.id.notifications_recycler_view);
        if (savedInstanceState == null) {
            m_adapter = new HourAdapter();
        }
        else {
            boolean[] savedChecked = savedInstanceState.getBooleanArray(SAVED_CHECKED);
            m_adapter = new HourAdapter(savedChecked);
        }
        recyclerView.setAdapter(m_adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return root;
    }
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putBooleanArray(SAVED_CHECKED, m_adapter.getChecked());
    }
    //----------------------------------------------------------------------------------------------

    class HourAdapter extends RecyclerView.Adapter<HourHolder>
    {
        final List<SessionInfo> m_data;
        final boolean[] m_checked;
        /**
         * Sets m_checked according to m_data
         */
        public HourAdapter()
        {
            m_data = TennisApp.getManager(getContext()).getHoursInfo();
            m_checked = new boolean[m_data.size()];
            for (int i = 0; i < m_data.size(); ++i) {
                m_checked[i] = m_data.get(i).isChecked();
            }
        }
        /**
         * Sets m_data according to m_checked
         */
        public HourAdapter(boolean[] checked)
        {
            m_data = TennisApp.getManager(getContext()).getHoursInfo();
            m_checked = checked;
            for (int i = 0; i < m_checked.length; ++i) {
                m_data.get(i).setChecked(m_checked[i]);
            }
        }
        @Override
        public HourHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View root = LayoutInflater.from(getActivity()).inflate(R.layout.item_hour, parent, false);
            return new HourHolder(root, this);
        }
        @Override
        public void onBindViewHolder(HourHolder holder, int position)
        {
            holder.bind(m_data.get(position));
        }
        @Override
        public int getItemCount()
        {
            return m_data.size();
        }
        public List<SessionInfo> getData()
        {
            return m_data;
        }
        public void setCheckedAt(int position, boolean isChecked)
        {
            m_checked[position] = isChecked;
        }
        public boolean[] getChecked()
        {
            return m_checked;
        }
    }

}

class HourHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener
{
    final private CheckBox m_checkBox;
    final private TextView m_lvlText;
    private SessionInfo m_data;
    final private NotificationsFragment.HourAdapter m_adapter;

    public HourHolder(View root, NotificationsFragment.HourAdapter adapter)
    {
        super(root);
        m_adapter = adapter;
        m_lvlText = (TextView)root.findViewById(R.id.item_hour_text_lvl);
        m_checkBox = (CheckBox)root.findViewById(R.id.item_hour_checkbox);
        m_checkBox.setOnCheckedChangeListener(this);
    }
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        m_data.setChecked(isChecked);
        m_adapter.setCheckedAt(getAdapterPosition(), isChecked);
    }
    public void bind(SessionInfo data)
    {
        m_data = data;
        m_lvlText.setText(data.getLvl());
        m_checkBox.setText(data.getDate() + ", " + data.getHour() + ":00");
        m_checkBox.setChecked(data.isChecked());
    }
}
