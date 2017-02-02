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
