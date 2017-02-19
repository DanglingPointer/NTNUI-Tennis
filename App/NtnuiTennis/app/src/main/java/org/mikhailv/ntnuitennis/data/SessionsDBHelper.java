package org.mikhailv.ntnuitennis.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by MikhailV on 18.02.2017.
 */


final class LinksTable
{
    public static final String NAME = "sessions";
    public static final String COL_LINK = "link";
    public static final String COL_DATE = "date";
}

class SessionsDBHelper extends SQLiteOpenHelper
{
    private static final int VERSION = 1;
    private static final String DB_FILENAME = "sessionsBase.db";

    private static final String SQL_CREATE_TABLE = "CREATE TABLE " + LinksTable.NAME + " ( "
            + LinksTable.COL_LINK + " VARCHAR(100) NOT NULL, "
            + LinksTable.COL_DATE + " LONG NOT NULL, "
            + "PRIMARY KEY (" + LinksTable.COL_LINK + ") );";
    private static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + LinksTable.NAME + ";";

    public SessionsDBHelper(Context c)
    {
        super(c, DB_FILENAME, null, VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(SQL_CREATE_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(SQL_DELETE_TABLE);
        onCreate(db);
    }
    public static ContentValues getContentValues(String link, long date)
    {
        ContentValues tuple = new ContentValues(2);
        tuple.put(LinksTable.COL_LINK, link);
        tuple.put(LinksTable.COL_DATE, date);
        return tuple;
    }
}
