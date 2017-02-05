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
