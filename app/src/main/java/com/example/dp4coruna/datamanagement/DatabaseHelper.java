package com.example.dp4coruna.datamanagement;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.example.dp4coruna.location.LocationObject;
import com.example.dp4coruna.location.SensorReader;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String TABLE_NAME = "sensor data";
    private static final String COL1 = "buildingName";
    private static final String COL2 = "roomName";
    private static final String COL3 = "roomNumber";
    private static final String COL4 = "soundLevel";
    private static final String COL5 = "geomagneticLevel";


    //GPS Location Strings:

    private static final String COL6 = "streetAddress";
    private static final String COL7 ="city";
    private static final String COL8 = "state";
    private static final String COL9 = "country";



    //EDIT:  Change these columns to be fields of the Location Object

    public DatabaseHelper(Context context){

        super(context,TABLE_NAME,null, 1 );

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String hello = "hello";

        String createTable ="create table TABLE_NAME (id INTEGER PRIMARY KEY, txt TEXT)";


        db.execSQL(createTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);

    }

    public boolean addData(LocationObject locationObject, SensorReader sr, Context context){


        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        //Adding location fields to the database (!!! all three lines below)
//        String buildingName = locationObject.buildingName;
//        String roomName = locationObject.roomName;
//        String roomNumber = locationObject.roomNumber;

        String city = locationObject.getCity();
        String state = locationObject.getState();

        //Adding sensor data fields into the database
        //(!!!)Double soundLevel = sr.getSoundLevel();
        //(!!!)Double geoMagneticLevel = sr.getGeoMagneticField();

        //(!!!)sr.getLightLevel(context);

        //(!!! all three lines below)
//        contentValues.put(COL1,buildingName);
//        contentValues.put(COL2,roomName);
//        contentValues.put(COL3,roomNumber);
        //(!!!)contentValues.put(COL4,soundLevel);
        //(!!!)contentValues.put(COL5,geoMagneticLevel);
        contentValues.put(COL6,locationObject.getCity());


        //EDIT:  Adding location Objects to the database

        //(!!! all three lines below)
//        Log.d(TAG, "addData:  Adding "+ buildingName+ " to "+TABLE_NAME);
//        Log.d(TAG,"addData:  Adding "+roomName + " to "+TABLE_NAME);
//        Log.d(TAG,"addData:  Adding "+roomNumber + "to " + TABLE_NAME);
        //(!!!)Log.d(TAG, "addData:  Adding "+ soundLevel+ " to "+TABLE_NAME);
        //(!!!)Log.d(TAG, "addData:  Adding "+geoMagneticLevel + " to "+TABLE_NAME);


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

    @SuppressLint("Recycle")
    public Cursor getListContents(){

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor data = db.rawQuery("SELECT * FROM "+ TABLE_NAME,null);


        return data;

    }



}
