package com.example.databasetest;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String TABLE_NAME = "sensor data";
    private static final String COL1 = "ID";
    private static final String COL2 = "name";

    //EDIT:  Change these columns to be fields of the Location Object

    public DatabaseHelper(Context context){

        super(context,TABLE_NAME,null, 1 );

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createTable = "CREATE TABLE" + TABLE_NAME + " COL 1 " + COL1 + "COL 2 " + COL2+ "TEXT";

        db.execSQL(createTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

        db.execSQL("DROP IF TABLE EXISTS" + TABLE_NAME);
        onCreate(db);

    }

    public boolean addData(String item){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(COL2,item);
        //EDIT:  Adding location Objects to the database

        Log.d(TAG, "addData:  Adding "+item + " to "+TABLE_NAME);

        long result = db.insert(TABLE_NAME, null, contentValues);

        if(result==-1){

            return false;
        }
        else
            return true;


    }



}
