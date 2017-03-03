package org.mikhailv.ntnuitennis.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
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
        static final String COL_INFO = "info";
    }
    //----------------------------------------------------------------------------------------------

    private final static class Query
    {
        static final String CREATE_TABLE = "CREATE TABLE " + SessionsTable.NAME + " ( "
                + SessionsTable.COL_LINK + " VARCHAR(100) NOT NULL, "
                + SessionsTable.COL_DATE + " LONG NOT NULL, "
                + SessionsTable.COL_INFO + " VARCHAR(16), "
                + "PRIMARY KEY (" + SessionsTable.COL_LINK + ") );";

        static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + SessionsTable.NAME + ";";

        static final String TABLE_SIZE = "SELECT COUNT(*) FROM " + SessionsTable.NAME + ";";
    }
    //----------------------------------------------------------------------------------------------

    private static class SessionsDBHelper extends SQLiteOpenHelper
    {
        private static final int VERSION = 3;
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
    private static final Object s_lock = new Object();
    private final SQLiteDatabase m_db;

    public DBManager(Context context)
    {
        synchronized (s_lock) {
            m_db = new SessionsDBHelper(context.getApplicationContext()).getWritableDatabase();
        }
    }
    public void insertTuple(SessionInfo.ShortForm session)
    {
        synchronized (s_lock) {
            ContentValues tuple = getContentValues(session);
            m_db.insert(SessionsTable.NAME, null, tuple);
        }
    }
    public void deleteTuple(String link)
    {
        synchronized (s_lock) {
            // WHERE link = 'http://blablabla'
            m_db.delete(SessionsTable.NAME, SessionsTable.COL_LINK + " = ?", new String[] { link });
        }
    }
    public List<SessionInfo.ShortForm> getAllTuples()
    {
        synchronized (s_lock) {
            List<SessionInfo.ShortForm> tuples = new ArrayList<>();

            try (Cursor cursor = m_db.query(
                    SessionsTable.NAME, // FROM
                    null,               // SELECT *
                    null,               // WHERE
                    null,               // WHERE args
                    null,               // GROUP BY
                    null,               // HAVING
                    null                // ORDER BY
            )) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    SessionInfo.ShortForm tuple = getSessionShortForm(cursor);
//                    Log.d(TAG_LOG, "Tuple: Link = " + tuple.getLink()
//                            + " Info = " + tuple.getInfo() + " Date = " + tuple.getDate());
                    tuples.add(tuple);
                    cursor.moveToNext();
                }
                return tuples;
            }
        }
    }
    public boolean containsLink(String link)
    {
        synchronized (s_lock) {
            try (Cursor cursor = m_db.query(
                    SessionsTable.NAME,                         // FROM
                    new String[] { SessionsTable.COL_LINK },    // SELECT
                    SessionsTable.COL_LINK + " = ?",            // WHERE
                    new String[] { link },                      // WHERE args
                    null,                                       // GROUP BY
                    null,                                       // HAVING
                    null                                        // ORDER BY
            )) {
                return cursor.moveToFirst();
            }
        }
    }
    public int getTableSize()
    {
        synchronized (s_lock) {
            try (Cursor cursor = m_db.rawQuery(Query.TABLE_SIZE, null)) {
                cursor.moveToFirst();
                return cursor.getInt(0);
            }
        }
    }
    private ContentValues getContentValues(SessionInfo.ShortForm session)
    {
        ContentValues tuple = new ContentValues(3);
        tuple.put(SessionsTable.COL_LINK, session.getLink());
        tuple.put(SessionsTable.COL_DATE, session.getDate());
        tuple.put(SessionsTable.COL_INFO, session.getInfo());
        return tuple;
    }
    private SessionInfo.ShortForm getSessionShortForm(Cursor c)
    {
        final String link = c.getString(c.getColumnIndex(SessionsTable.COL_LINK));
        final long date = c.getLong(c.getColumnIndex(SessionsTable.COL_DATE));
        final String info = c.getString(c.getColumnIndex(SessionsTable.COL_INFO));
        return new SessionInfo.ShortForm()
        {
            @Override
            public String getLink() { return link; }
            @Override
            public long getDate() { return date; }
            @Override
            public String getInfo() { return info; }
        };
    }
}