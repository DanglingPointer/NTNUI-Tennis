package org.mikhailv.ntnuitennis.data;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by MikhailV on 06.01.2017.
 */

public interface Slot
{
    String getLink();

    String getAttendLink();

    int getSize();

    String getLevel();

    boolean isExpired();

    String getAttendingAt(int i);

    List<String> getAttending();

    boolean hasAvailable();

    boolean isMeAttending();

    void getPlayers(@NonNull List<Player> dropIns, @NonNull List<Player> nonDropIns);
}

class SlotImpl implements Slot
{
    private List<Player> m_players;
    private String[] m_reserved;
    private boolean m_expired;
    private String m_lvl;
    private String m_link;
    private String m_attendLink;

    public SlotImpl(int size)
    {
        m_reserved = new String[size];
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
    @Override
    public String getAttendLink()
    {
        return m_attendLink;
    }
    public SlotImpl setAttendLink(String link)
    {
        m_attendLink = link;
        return this;
    }
    public int getSize()
    {
        return m_reserved.length;
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
    public String getAttendingAt(int i)
    {
        if (i < 0 || m_reserved.length <= i || m_reserved[i] == null)
            return null;
        return m_reserved[i];
    }
    public List<String> getAttending()
    {
        int firstNullIndex;
        for (firstNullIndex = 0; firstNullIndex < m_reserved.length; ++firstNullIndex)
            if (m_reserved[firstNullIndex] == null)
                break;
        String[] reservedOnly = new String[firstNullIndex];
        for (int i = 0; i < firstNullIndex; ++i)
            reservedOnly[i] = m_reserved[i];
        return Arrays.asList(reservedOnly);
    }
    public SlotImpl setName(int i, String name)
    {
        m_reserved[i] = name;
        return this;
    }
    public boolean hasAvailable()
    {
        for (String name : m_reserved)
            if (name == null)
                return true;
        return false;
    }
    public boolean isMeAttending()
    {
        for (String name : m_reserved)
            if (name != null && name.equals(Globals.MY_NAME))
                return true;
        return false;
    }
    public void getPlayers(@NonNull List<Player> dropIns, @NonNull List<Player> nonDropIns)
    {
        for (Player p : m_players)
            if (p.isDropIn())
                dropIns.add(p);
            else
                nonDropIns.add(p);
    }
    public SlotImpl addPlayer(Player player)
    {
        if (m_players == null)
            m_players = new ArrayList<>();
        m_players.add(player);
        return this;
    }
}
