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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by MikhailV on 06.01.2017.
 */

public interface Slot
{
    String getLink();

    String getLevel();

    boolean isExpired();

    boolean hasAvailable();

    List<String> getAttending();
}

class SlotImpl implements Slot
{
    private List<String> m_reserved;
    private boolean m_expired;
    private boolean m_hasAvailable;
    private String m_lvl;
    private String m_link;

    public SlotImpl()
    {
        m_reserved = new ArrayList<>();
        m_expired = false;
        m_hasAvailable = true;
    }
    public String getLink()
    {
        return m_link;
    }
    public SlotImpl setLink(String link)
    {
        m_link = link;
        return this;
    }
    public String getLevel()
    {
        return m_lvl;
    }
    public SlotImpl setLevel(String lvl)
    {
        m_lvl = lvl;
        return this;
    }
    public boolean isExpired()
    {
        return m_expired;
    }
    public SlotImpl setExpired(boolean expired)
    {
        m_expired = expired;
        return this;
    }
    public boolean hasAvailable()
    {
        return m_hasAvailable;
    }
    public SlotImpl setAvailable(boolean hasAvailable)
    {
        m_hasAvailable = hasAvailable;
        return this;
    }
    public List<String> getAttending()
    {
        return m_reserved;
    }
    public SlotImpl addName(String name)
    {
        m_reserved.add(name);
        return this;
    }
}
