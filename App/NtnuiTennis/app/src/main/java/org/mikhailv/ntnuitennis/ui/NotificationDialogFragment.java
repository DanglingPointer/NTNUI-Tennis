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

import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import org.mikhailv.ntnuitennis.R;
import org.mikhailv.ntnuitennis.TennisApp;
import org.mikhailv.ntnuitennis.data.DBManager;
import org.mikhailv.ntnuitennis.data.SessionInfo;
import org.mikhailv.ntnuitennis.services.NotifierService;

import java.util.List;

/**
 * Created by MikhailV on 05.03.2017.
 */

public class NotificationDialogFragment extends DialogFragment implements
                                                               DialogInterface.OnClickListener
{
    public static NotificationDialogFragment newInstance(String link)
    {
        Bundle args = new Bundle();
        args.putString(ARG_LINK, link);
        NotificationDialogFragment fragment = new NotificationDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private static final String ARG_LINK = "NotificationDialogFragment.ARG_LINK";
    private static final String SAVED_CHECKED = "NotificationDialogFragment.SAVED_CHECKED";

    private CheckBox m_checkbox;
    private DBManager m_db;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        View root = LayoutInflater.from(getActivity())
                .inflate(R.layout.fragment_dialog_notification, null);
        String link = getArguments().getString(ARG_LINK);

        m_db = new DBManager(getActivity());
        m_checkbox = (CheckBox)root.findViewById(R.id.dialog_notifications_checkbox);

        if (savedInstanceState == null) {
            boolean checked = m_db.containsLink(link);
            m_checkbox.setChecked(checked);
        }
        else {
            boolean checked = savedInstanceState.getBoolean(SAVED_CHECKED);
            m_checkbox.setChecked(checked);
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.menu_notifications)
                .setView(root)
                .setPositiveButton(R.string.menu_not_save, this)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); }
                })
                .create();
    }
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_CHECKED, m_checkbox.isChecked());
    }
    /**
     * Save-button handler. Writes to DB, turns alarm on/off and invalidates OptionsMenu in Activity
     */
    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        String link = getArguments().getString(ARG_LINK);
        boolean isAlarmOn = NotifierService.isAlarmOn(getContext());
        boolean isChecked = m_checkbox.isChecked();
        boolean inDB = m_db.containsLink(link);

        if (isChecked && !inDB) {
            List<SessionInfo> allHours = TennisApp.getManager(getActivity()).getCurrentWeek().getHours();
            SessionInfo.ShortForm shortInfo = null;
            for (SessionInfo fullInfo : allHours) {
                if (fullInfo.getLink().equals(link)) {
                    shortInfo = fullInfo.getShortForm();
                    break;
                }
            }
            if (shortInfo != null) {
                m_db.insertTuple(shortInfo);
                if (!isAlarmOn)
                    NotifierService.setAlarm(getActivity());
            }
        }
        else if (!isChecked && inDB) {
            m_db.deleteTuple(link);
            if (isAlarmOn && m_db.getTableSize() == 0)
                NotifierService.cancelAlarm(getActivity());
        }
        getActivity().invalidateOptionsMenu();
    }
}
