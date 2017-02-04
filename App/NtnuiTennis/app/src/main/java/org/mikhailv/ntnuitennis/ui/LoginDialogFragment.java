package org.mikhailv.ntnuitennis.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.mikhailv.ntnuitennis.AppManager;
import org.mikhailv.ntnuitennis.R;
import org.mikhailv.ntnuitennis.TennisApp;

/**
 * Created by MikhailV on 04.02.2017.
 */

public class LoginDialogFragment extends DialogFragment
{
    private static final String EXTRA_EMAIL = "LoginDialogFragment.extraEmail";
    private static final String EXTRA_PASSWORD = "LoginDialogFragment.extraPassword";
    private static final String EXTRA_LANG = "LoginDialogFragment.extraLang";

    private static final String SAVED_EMAIL = "LoginDialogFragment.savedEmail";
    private static final String SAVED_PASSWORD = "LoginDialogFragment.savedPassword";
    private static final String SAVED_LANG = "LoginDialogFragment.savedLang";

    public static AppManager.Credentials decodeCredentials(Intent i)
    {
        final String email = i.getStringExtra(EXTRA_EMAIL);
        final String password = i.getStringExtra(EXTRA_PASSWORD);
        final String lang = i.getStringExtra(EXTRA_LANG);

        return new AppManager.Credentials()
        {
            @Override
            public String getPassword() { return password; }
            @Override
            public String getEmail() { return email; }
            @Override
            public String getLanguage() { return lang; }
        };
    }

    private EditText m_passwordEditText;
    private EditText m_emailEditText;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        View root = LayoutInflater.from(getActivity())
                .inflate(R.layout.fragment_dialog_login, null);

        m_passwordEditText = (EditText)root.findViewById(R.id.password_edit_text);
        m_emailEditText = (EditText)root.findViewById(R.id.email_edit_text);

        if (savedInstanceState != null) {
            m_passwordEditText.setText(savedInstanceState.getString(SAVED_PASSWORD));
            m_emailEditText.setText(savedInstanceState.getString(SAVED_EMAIL));
            // TODO: extract language
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.credentials_dialog_title)
                .setView(root)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (getTargetFragment() == null)
                            return;

                        String email = m_emailEditText.getText().toString();
                        String password = m_passwordEditText.getText().toString();
                        String lang = "no"; // TODO

                        TennisApp.getManager(getActivity()).saveCredentials(email, password, lang);

                        Intent i = new Intent();
                        i.putExtra(EXTRA_PASSWORD, password);
                        i.putExtra(EXTRA_EMAIL, email);
                        i.putExtra(EXTRA_LANG, lang);

                        getTargetFragment().onActivityResult(
                                getTargetRequestCode(), Activity.RESULT_OK, i);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .create();
    }
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_PASSWORD, m_passwordEditText.getText().toString());
        outState.putString(SAVED_EMAIL, m_emailEditText.getText().toString());
        // TODO: save language
    }
}
