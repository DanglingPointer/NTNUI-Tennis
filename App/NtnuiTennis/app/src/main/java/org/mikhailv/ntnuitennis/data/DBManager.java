package org.mikhailv.ntnuitennis.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static org.mikhailv.ntnuitennis.AppManager.TAG_LOG;

/**
 * Created by MikhailV on 18.02.2017.
 */

public class DBManager
{
    private final static class SessionsTable implements BaseColumns
    {
        static final String NAME = "sessions";
        static final String COL_LINK = "link";
        static final String COL_DATE = "date";
    }
    //----------------------------------------------------------------------------------------------

    private final static class Query
    {
        static final String CREATE_TABLE = "CREATE TABLE " + SessionsTable.NAME + " ( "
                + SessionsTable.COL_LINK + " VARCHAR(100) NOT NULL, "
                + SessionsTable.COL_DATE + " LONG NOT NULL, "
                + "PRIMARY KEY (" + SessionsTable.COL_LINK + ") );";

        static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + SessionsTable.NAME + ";";
    }
    //----------------------------------------------------------------------------------------------

    private static class SessionsDBHelper extends SQLiteOpenHelper
    {
        private static final int VERSION = 1;
        private static final String DB_FILENAME = "sessionsBase.db";


        public SessionsDBHelper(Context c)
        {
            super(c, DB_FILENAME, null, VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(Query.CREATE_TABLE);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            db.execSQL(Query.DELETE_TABLE);
            onCreate(db);
        }
    }
    //----------------------------------------------------------------------------------------------

    private SQLiteDatabase m_db;

    public DBManager(Context context)
    {
        m_db = new SessionsDBHelper(context.getApplicationContext()).getWritableDatabase();
    }
    public void insertTuple(SessionInfo.ShortForm session)
    {
        ContentValues tuple = getContentValues(session);
        long rowID = m_db.insert(SessionsTable.NAME, null, tuple);
        Log.d(TAG_LOG, "DB: Inserted row " + rowID);
    }
    public void deleteTuple(String link)
    {
        // WHERE link = 'http://blablabla'
        int rows = m_db.delete(SessionsTable.NAME,
                SessionsTable.COL_LINK + " = ?", new String[] { link });
        Log.d(TAG_LOG, "DB: Deleted " + rows + " rows");
    }
    public List<SessionInfo.ShortForm> getAllTuples()
    {
        List<SessionInfo.ShortForm> tuples = new ArrayList<>();

        try (Cursor cursor = m_db.query(
                SessionsTable.NAME, // table
                null,               // SELECT *
                null,               // WHERE
                null,               // WHERE args
                null,               // GROUP BY
                null,               // HAVING
                null                // ORDER BY
        )) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                tuples.add(getSessionShortForm(cursor));
                cursor.moveToNext();
            }
            return tuples;
        }
    }

    private ContentValues getContentValues(SessionInfo.ShortForm session)
    {
        ContentValues tuple = new ContentValues(2);
        tuple.put(SessionsTable.COL_LINK, session.getLink());
        tuple.put(SessionsTable.COL_DATE, session.getLink());
        return tuple;
    }
    private SessionInfo.ShortForm getSessionShortForm(Cursor c)
    {
        final String link = c.getString(c.getColumnIndex(SessionsTable.COL_LINK));
        final long date = c.getLong(c.getColumnIndex(SessionsTable.COL_DATE));
        return new SessionInfo.ShortForm()
        {
            @Override
            public String getLink() { return link; }
            @Override
            public long getDate() { return date; }
        };
    }
}