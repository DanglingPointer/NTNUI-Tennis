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
import org.mikhailv.ntnuitennis.data.Globals;
import org.mikhailv.ntnuitennis.data.HourInfo;

import java.util.List;

/**
 * Created by MikhailV on 18.01.2017.
 */

public class NotificationsFragment extends Fragment
{
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
                Globals.saveHoursInfo(getContext());
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
        recyclerView.setAdapter(new HourAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return root;
    }
    @Override
    public void onStop()
    {
        super.onStop();
        Globals.discardHoursInfoCHanges();
    }
    class HourAdapter extends RecyclerView.Adapter<HourHolder>
    {
        @Override
        public HourHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View root = LayoutInflater.from(getActivity()).inflate(R.layout.item_hour, parent, false);
            return new HourHolder(root);
        }
        @Override
        public void onBindViewHolder(HourHolder holder, int position)
        {
            List<HourInfo> data = Globals.getHoursInfo(getContext());
            holder.bind(data.get(position));
        }
        @Override
        public int getItemCount()
        {
            return Globals.getHoursInfo(getContext()).size();
        }
    }

}

class HourHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener
{
    private CheckBox m_checkBox;
    private TextView m_lvlText;
    private HourInfo m_data;

    public HourHolder(View root)
    {
        super(root);
        m_lvlText = (TextView)root.findViewById(R.id.item_hour_text_lvl);
        m_checkBox = (CheckBox)root.findViewById(R.id.item_hour_checkbox);
        m_checkBox.setOnCheckedChangeListener(this);
    }
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        m_data.setChecked(isChecked);
    }
    public void bind(HourInfo data)
    {
        m_data = data;
        m_lvlText.setText(data.getLvl());
        m_checkBox.setText(data.getDay() + ", " + data.getTime() + ":00");
        m_checkBox.setChecked(data.getChecked());
    }
}
