package org.mikhailv.ntnuitennis.data;

import android.net.Uri;
import android.support.annotation.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by MikhailV on 06.01.2017.
 */

public class Slot
{
    private List<Player> m_players;
    private String[] m_reserved;
    private boolean m_expired;
    private String m_lvl;
    private URL m_link;

    public Slot(int size)
    {
        m_reserved = new String[size];
    }
    public URL getLink()
    {
        return m_link;
    }
    public Slot setLink(URL link)
    {
        m_link = link;
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
    public Slot setLevel(String lvl)
    {
        m_lvl = lvl;
        return this;
    }
    public boolean isExpired()
    {
        return m_expired;
    }
    public Slot setExpired(boolean expired)
    {
        m_expired = expired;
        return this;
    }
    public String getReservedAt(int i)
    {
        if (i < 0 || m_reserved.length <= i || m_reserved[i] == null)
            return null;
        return m_reserved[i];
    }
    public List<String> getReserved()
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
    public Slot setName(int i, String name)
    {
        if (!(i < 0 || m_reserved.length <= i))
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
    public Slot addPlayer(Player player)
    {
        if (m_players == null)
            m_players = new ArrayList<>();
        m_players.add(player);
        return this;
    }

    public static class Player
    {
        private String m_name;
        private boolean m_attending;
        private boolean m_dropIn;

        public Player(String name, boolean attending, boolean dropIn)
        {
            this.m_name = name;
            this.m_attending = attending;
            this.m_dropIn = dropIn;
        }
        public String getName()
        {
            return m_name;
        }
        public boolean isAttending()
        {
            return m_attending;
        }
        public boolean isDropIn()
        {
            return m_dropIn;
        }
    }
}


