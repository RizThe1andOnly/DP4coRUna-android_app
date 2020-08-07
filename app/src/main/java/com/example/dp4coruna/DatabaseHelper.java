package com.example.databasetest;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.Sensor;
import android.location.Location;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String TABLE_NAME = "sensor data";
    private static final String COL1 = "buildingName";
    private static final String COL2 = "roomName";
    private static final String COL3 = "roomNumber";
    private static final String COL4 = "soundLevel";
    private static final String COL5 = "geomagneticLevel";

    //EDIT:  Change these columns to be fields of the Location Object

    public DatabaseHelper(Context context){

        super(context,TABLE_NAME,null, 1 );

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createTable = "CREATE TABLE" + TABLE_NAME + " COL 1: " + COL1 + "COL 2: " + COL2, + "COL3: "+COL3,+"COL 4 "+COL4 + "COL5 "+COL5+"TEXT" ;

        db.execSQL(createTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

        db.execSQL("DROP IF TABLE EXISTS" + TABLE_NAME);
        onCreate(db);

    }

    public boolean addData(LocationObject locationObject, SensorReader sr,Context context){


        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        //Adding location fields to the database
        String buildingName = locationObject.buildingName;
        String roomName = locationObject.roomName;
        String roomNumber = locationObject.roomNumber;

        //Adding sensor data fields into the database
        Double soundLevel = sr.getSoundLevel();
        Double geoMagneticLevel = sr.getGeoMagneticField();

        sr.getLightLevel(context);


        contentValues.put(COL1,buildingName);
        contentValues.put(COL2,roomName);
        contentValues.put(COL3,roomNumber);
        contentValues.put(COL4,soundLevel);
        contentValues.put(COL5,geoMagneticLevel);


        //EDIT:  Adding location Objects to the database

        Log.d(TAG, "addData:  Adding "+ buildingName+ " to "+TABLE_NAME);
        Log.d(TAG,"addData:  Adding "+roomName + " to "+TABLE_NAME);
        Log.d(TAG,"addData:  Adding "+roomNumber + "to " + TABLE_NAME);
        Log.d(TAG, "addData:  Adding "+ soundLevel+ " to "+TABLE_NAME);
        Log.d(TAG, "addData:  Adding "+geoMagneticLevel + " to "+TABLE_NAME);


        long result = db.insert(TABLE_NAME, null, contentValues);

        if(result==-1){

            return false;

            //Did not add to the database
        }
        else {
            return true;


            //Successfully added to the database
        }
    }



}
