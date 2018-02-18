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

package org.mikhailv.ntnuitennis.data;

/**
 * Created by MikhailV on 23.01.2017.
 */
public interface SlotDetailsInfo
{
    int getInfoSize();

    int getRegularsCount() // NB! includes unoccupied places
    ;

    int getSubstitutesCount();

    String getInfoTitle();

    String[] getInfoLine(int row);

    String getRegularsTitle();

    String[] getRegularsLine(int row) // row starts from 0, one line for each spot/player
    ;

    String getAttendingLink();

    String getSubstitutesTitle();

    String[] getSubstitutesLine(int row);
}
