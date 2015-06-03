package edu.ucla.csm117.bluetoothattendance;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by matthew on 6/1/15.
 */
public class HistoryDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "RosterHistory";
    private static final String DICTIONARY_TABLE_NAME = "People";
    private static final String DICTIONARY_TABLE_CREATE =
            "CREATE TABLE " + DICTIONARY_TABLE_NAME + " (" +
                    "Timestamp" + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "Person" + " TEXT, " +
                    "EventTime" + " DATATIME," +
                    "EventHost" + " TEXT);";

    HistoryDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int x, int y) {
        db.execSQL("DROP TABLE " + DICTIONARY_TABLE_NAME + ";");
        db.execSQL(DICTIONARY_TABLE_CREATE);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DICTIONARY_TABLE_CREATE);
    }
}