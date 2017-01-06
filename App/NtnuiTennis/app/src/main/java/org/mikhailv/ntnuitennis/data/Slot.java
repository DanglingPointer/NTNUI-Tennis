package org.mikhailv.ntnuitennis.data;

/**
 * Created by MikhailV on 06.01.2017.
 */

public class Slot
{
    private static class Player
    {
        Player(String name, Boolean attending)
        {
            this.name = name;
            this.attending = attending;
        }
        String name;
        Boolean attending;
    }

    private String m_lvl;
    private Player[] m_players;

    public Slot(int size)
    {
        m_players = new Player[size];
    }
    public int getSize()
    {
        return m_players.length;
    }
    public String getLevel()
    {
        return m_lvl;
    }
    public void setLevel(String lvl)
    {
        m_lvl = lvl;
    }
    public String getPlayerName(int i)
    {
        if (i < 0 || m_players.length <= i || m_players[i] == null)
            return null;
        return m_players[i].name;
    }
    public Boolean getPlayerStatus(int i)
    {
        if (i < 0 || m_players.length <= i || m_players[i] == null)
            return null;
        return m_players[i].attending;
    }
    public void setPlayer(int i, String name, Boolean status)
    {
        if (i < 0 || m_players.length <= i) return;
        if (m_players[i] == null)
            m_players[i] = new Player(name, status);
        else {
            m_players[i].name = name;
            m_players[i].attending = status;
        }
    }
}


