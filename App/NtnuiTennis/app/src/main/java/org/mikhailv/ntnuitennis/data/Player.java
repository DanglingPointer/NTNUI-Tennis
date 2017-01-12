package org.mikhailv.ntnuitennis.data;

/**
 * Created by MikhailV on 12.01.2017.
 */

public class Player
{
    private String m_name;
    private boolean m_attending;
    private boolean m_dropIn;

    Player(String name, boolean attending, boolean dropIn)
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