package org.mikhailv.ntnuitennis.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import org.mikhailv.ntnuitennis.R;

import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Created by MikhailV on 26.02.2017.
 */

public class AboutActivity extends AppCompatActivity
{
    public static Intent newIntent(Context context)
    {
        return new Intent(context, AboutActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new AboutFragment())
                .commit();
    }

    public static class AboutFragment extends PreferenceFragmentCompat
    {
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
        {
            inflater.inflate(R.menu.menu_about, menu);
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item)
        {
            switch (item.getItemId()){
                case R.id.menu_about_back_btn:
                    getActivity().finish();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }
        @Override
        public void onCreatePreferences(Bundle bundle, String s)
        {
            setPreferencesFromResource(R.xml.fragment_about, s);

            String versionName;
            try {
                PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                versionName = pInfo.versionName;
            }
            catch (PackageManager.NameNotFoundException e) {
                versionName = "";
            }

            findPreference("version").setSummary(versionName);
            findPreference("author").setSummary("Mikhail Vasilyev");
        }
    }
}
