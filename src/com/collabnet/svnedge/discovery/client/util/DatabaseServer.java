package com.collabnet.svnedge.discovery.client.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * The main Database Server to cache the list of servers found.
 * 
 * TODO: Still need to be fully implemented.
 * 
 * @author Marcello de Sales (marcello.desales@gmail.com)
 * 
 */
public class DatabaseServer extends SQLiteOpenHelper {

    private static final String TAG = "SVNEDGE_DB";
    private static final String DB_NAME = "svnedge-servers.db";
    private static final int DB_VERSION = 1;
    
    private static final String TABLE_NAME = "server";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_URL = "url";
    private static final String COLUMN_CPU = "cpu";
    private static final String COLUMN_OS = "os";
    private static final String COLUMN_FOUND_ON = "found_on";

    public DatabaseServer(Context context, String name, CursorFactory factory, int version) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN_ID + " integer not null primary key autoincrement, " +
            COLUMN_URL + " varchar not null, " +
            COLUMN_CPU + " varchar not null, " +
            COLUMN_OS + " varchar not null, " +
            COLUMN_FOUND_ON + " long not null)";
        Log.d(TAG, "onCreate=" + sql);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub

    }

}
