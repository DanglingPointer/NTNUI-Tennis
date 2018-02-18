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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by MikhailV on 18.01.2017.
 */

public class NotificationsActivity extends AppCompatActivity
{
    private static final String EXTRA_PAGER_POSITION = "NotificationsActivity.PAGER_POSITION";
    public static Intent newIntent(Context context, int pagerPosition)
    {
        Intent i = new Intent(context, NotificationsActivity.class);
        i.putExtra(EXTRA_PAGER_POSITION, pagerPosition);
        return i;
    }
    public static int decodePagerPosition(Intent i)
    {
        return i.getIntExtra(EXTRA_PAGER_POSITION, 0);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new NotificationsFragment())
                .commit();

        Intent i = new Intent();
        i.putExtra(EXTRA_PAGER_POSITION, getIntent().getIntExtra(EXTRA_PAGER_POSITION, 0));
        setResult(RESULT_OK, i);
    }
}
