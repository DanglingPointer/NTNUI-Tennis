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
