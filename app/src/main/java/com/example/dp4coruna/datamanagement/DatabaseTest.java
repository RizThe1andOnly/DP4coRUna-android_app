package com.example.dp4coruna.datamanagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.dp4coruna.location.LocationObjectData;

/**
 * Created by Mitch on 2016-05-13.
 */
public class DatabaseTest extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "mylist.db";
    public static final String locTable = "mylist_data";
   // public static final String sensorTable = "SENSORDATA";
   // public static final String APTable = "WIFI ACCESS POINTS";



    public static final String COL1 = "ID";
    public static final String COL2 = "ITEM1";

   public static final String COL3="ITEM2";
   public static final String COL5 = "ITEM3";

   // String CREATE_SENSORTABLE = "CREATE TABLE " + sensorTable + " (ID INTEGER," +
     //       COL2 + " TEXT );";

    String createLocTable = "CREATE TABLE " + locTable + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + COL2 +
           "TEXT );";

   // String CREATE_APTABLE = "CREATE TABLE " + APTable + " (ID INTEGER, " + COL3
     //       + "TEXT );";


    public DatabaseTest(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(createLocTable);
       // db.execSQL(CREATE_SENSORTABLE);
       // db.execSQL(CREATE_APTABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + locTable);
     //   db.execSQL("DROP TABLE IF EXISTS " + sensorTable);
     //   db.execSQL("DROP TABLE IF EXISTS " + APTable);

        onCreate(db);
    }

    public boolean addData(String item1, String type) {


        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        //If you are inserting location data, add it to locTable


            contentValues.put(COL2, item1);
            long result = db.insert(locTable, null, contentValues);


            //if date as inserted incorrectly it will return -1
            if (result == -1) {
                return false;
            } else {
                return true;
            }


/*
        else if (type.equals("SENSORDATA")) { //If you are adding sensor data then add to the sensor data table


            Log.d("IN SENSORDATA: ","I AM HERE ADDING "+item1+ " to SENSOR TABLE");
            contentValues.put(COL5, item1);
           long result = db.insert(sensorTable, null, contentValues);


            //if date as inserted incorrectly it will return -1
            if (result == -1) {
                return false;
            } else {
                return true;
            }

        }

        //If not location data or sensor data, add to the wifi access points table

       contentValues.put(COL3,item1);
        long result = db.insert(APTable, null, contentValues);

        if(result == -1)
            return false;
        else{
            return true;
        }



  */



    }
    public Cursor getListContents(String tableType){



        SQLiteDatabase db = this.getWritableDatabase();



         Cursor data = db.rawQuery("SELECT * FROM " + locTable, null);


        /*
        else if (tableType.equals("SENSORDATA")) {
            data = db.rawQuery("SELECT * FROM " + sensorTable, null);
        }else
         data = db.rawQuery("SELECT * FROM " + APTable, null);



         */
        return data;
    }
}