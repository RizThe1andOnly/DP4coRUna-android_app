package com.example.dp4coruna.datamanagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.dp4coruna.location.LocationObject;
import com.example.dp4coruna.location.LocationObjectData;
import com.example.dp4coruna.location.WiFiAccessPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Akshay on 2020-08-14.
 */
public class AppDatabase extends SQLiteOpenHelper {

    private static final int NUMBER_OF_FEATURES = 6;
    private int numberOfLocattions;

    public static final String DATABASE_NAME = "dp4corunadata.db";
    public static final String LOCATION_TABLE = "mylist_data";
    public static final String WAP_TABLE = "wifi_access_point_table";

    /**
     * Data columns for the location features. The count begins at 0.
     */
    public static final String LOCATION_TABLE_COL_ID = "ID";
    public static final String LOCATION_TABLE_COL_LABEL = "label";
    public static final String LOCATION_TABLE_COL_LIGHT = "light";
    public static final String LOCATION_TABLE_COL_SOUND = "sound";
    public static final String LOCATION_TABLE_COL_GMFS = "geo_magnetic_field_strength";
    public static final String LOCATION_TABLE_COL_CELL_TID = "cell_tower_id";
    public static final String LOCATION_TABLE_COL_AREA_CODE = "area_code";
    public static final String LOCATION_TABLE_COL_SIGNAL_STRENGTH = "cell_signal_strength";

    /**
     * Data columns for teh wifi access point features.
     */
    public static final String WIFIAPTABLE_COL_RSSI = "rssi";
    public static final String WIFIAPTABLE_COL_BSSID = "bssid";
    public static final String WIFIAPTABLE_COL_SSID = "ssid";
    public static final String WIFIAPTABLE_COL_PARENTID = "parentid";
    //constants for accessing the cursor values when doing a query
    public static final int WIFIAPTABLE_CURSOR_SSID = 0;
    public static final int WIFIAPTABLE_CURSOR_BSSID = 1;
    public static final int WIFIAPTABLE_CURSOR_RSSI = 2;
    public static final int WIFIAPTABLE_CURSOR_PARENT_INDEX = 3;


    private final String createLocTable = "CREATE TABLE " + LOCATION_TABLE + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                                    + LOCATION_TABLE_COL_LABEL + " TEXT, "
                                    + LOCATION_TABLE_COL_LIGHT + " FLOAT, "
                                    + LOCATION_TABLE_COL_SOUND + " FLOAT, "
                                    + LOCATION_TABLE_COL_GMFS + " FLOAT, "
                                    + LOCATION_TABLE_COL_CELL_TID + " FLOAT, "
                                    + LOCATION_TABLE_COL_AREA_CODE + " FLOAT, "
                                    + LOCATION_TABLE_COL_SIGNAL_STRENGTH + " FLOAT"
                                    +");";


    private final String createWiFIAPTable = "CREATE TABLE " + WAP_TABLE + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                                            + WIFIAPTABLE_COL_SSID + " TEXT, "
                                            + WIFIAPTABLE_COL_BSSID + " TEXT, "
                                            + WIFIAPTABLE_COL_RSSI + " FLOAT, "
                                            + WIFIAPTABLE_COL_PARENTID + " INTEGER, "
                                            + "FOREIGN KEY(" + WIFIAPTABLE_COL_PARENTID + ") REFERENCES " + LOCATION_TABLE + "(ID) "
                                            +");";

    public AppDatabase(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(createLocTable);
        db.execSQL(createWiFIAPTable);
       // db.execSQL(CREATE_SENSORTABLE);
       // db.execSQL(CREATE_APTABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + LOCATION_TABLE);
     //   db.execSQL("DROP TABLE IF EXISTS " + sensorTable);
     //   db.execSQL("DROP TABLE IF EXISTS " + APTable);

        onCreate(db);
    }

    public boolean addData(String item1, String type) {


        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        //If you are inserting location data, add it to locTable


            contentValues.put(LOCATION_TABLE_COL_LIGHT, item1);
            long result = db.insert(LOCATION_TABLE, null, contentValues);


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


    /**
     * Adds a row/sample in the existing database; all of the data for columns is obtained from LocationObject passed in
     * as an argument.
     *
     * @param locationObject LocationObject class which contains data on the location
     * @return boolean true if successful, false if not
     */
    public boolean addData(LocationObject locationObject){
        //checks for the location object argument:
        if( (!(locationObject instanceof LocationObject)) || (locationObject == null) ){
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        //insert data:
        contentValues.put(LOCATION_TABLE_COL_LABEL,locationObject.locationLabel);
        contentValues.put(LOCATION_TABLE_COL_LIGHT,(float)locationObject.getLightLevel());
        contentValues.put(LOCATION_TABLE_COL_SOUND,(float)locationObject.getSoundLevel());
        contentValues.put(LOCATION_TABLE_COL_GMFS,(float)locationObject.getGeoMagneticFieldStrength());
        contentValues.put(LOCATION_TABLE_COL_CELL_TID,(float)locationObject.getCellId());
        contentValues.put(LOCATION_TABLE_COL_AREA_CODE,(float)locationObject.getAreaCode());
        contentValues.put(LOCATION_TABLE_COL_SIGNAL_STRENGTH,(float)locationObject.getCellSignalStrength());

        //insert the row:
        long result = db.insert(LOCATION_TABLE, null, contentValues);

        if(result == -1) return false;
        return true;
    }

    /**
     * Adds an entry to the mylist_data table in the database. this is called after a location has been sampled and is
     * ready to be added to the database.
     * @param lod
     * @return
     */
    public boolean addData(LocationObjectData lod){
        //checks for the location object argument:
        if( (!(lod instanceof LocationObjectData)) || (lod == null) ){
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        //insert data:
        contentValues.put(LOCATION_TABLE_COL_LABEL,lod.getRoomName()); //for now will use just room name for label
        contentValues.put(LOCATION_TABLE_COL_LIGHT,(float)lod.getLightLevel());
        contentValues.put(LOCATION_TABLE_COL_SOUND,(float)lod.getSoundLevel());
        contentValues.put(LOCATION_TABLE_COL_GMFS,(float)lod.getGeoMagenticValue());
        contentValues.put(LOCATION_TABLE_COL_CELL_TID,(float)lod.getCellId());
        contentValues.put(LOCATION_TABLE_COL_AREA_CODE,(float)lod.getAreaCode());
        contentValues.put(LOCATION_TABLE_COL_SIGNAL_STRENGTH,(float)lod.getCellSignalStrength());

        //insert the row:
        long result = db.insert(this.LOCATION_TABLE, null, contentValues);

        //get the index of row just entered into data for the wifi access point list and add the wifi access point list to wifiap table:
        String indexQuery = "SELECT ID FROM " + this.LOCATION_TABLE + ";";
        Cursor parentCrs = db.rawQuery(indexQuery,null);
        parentCrs.moveToLast();
        int parentIndex = parentCrs.getInt(0);
        if(!addWAPData(lod.getwifiApList(),parentIndex)){
            Log.i("FromDataBaseTest","Failed at wifi ap data entry"); //(!!!)
        }

        if(result == -1){
            Log.i("FromDataBaseTest","Failed at location entry");
            return false;
        }
        return true;
    }


    /**
     * Adds the wifi access point list data obtained from the sampling to the database table wifi_access_point_table.
     * Will be called for each entry to the mylistdata_table
     * @param wapList
     * @param parentIndex
     * @return
     */
    private boolean addWAPData(List<WiFiAccessPoint> wapList, int parentIndex){
        if( (wapList == null) ){
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        for(WiFiAccessPoint element : wapList){
            ContentValues contentValues = new ContentValues();
            contentValues.put(WIFIAPTABLE_COL_SSID, element.getSsid());
            contentValues.put(WIFIAPTABLE_COL_BSSID,element.getBssid());
            contentValues.put(WIFIAPTABLE_COL_RSSI,(float)element.getRssi());
            contentValues.put(WIFIAPTABLE_COL_PARENTID,parentIndex);

            long result = db.insert(WAP_TABLE, null, contentValues);

            if(result == -1) return false;
        }

        return true;
    }


    /**
     * Returns cursor object which contains all the data available in the database.
     *
     *  (!!!)Note from Rizwan - Got rid of the parameter tableType (String tableType) for now since i don't know what its for.
     *
     * @return
     */
    public Cursor getListContents(){

        SQLiteDatabase db = this.getWritableDatabase();

        String queryString = "SELECT "
                            + LOCATION_TABLE_COL_ID + ","
                            + LOCATION_TABLE_COL_LABEL + ","
                            + LOCATION_TABLE_COL_LIGHT + ","
                            + LOCATION_TABLE_COL_SOUND + ","
                            + LOCATION_TABLE_COL_GMFS + ","
                            + LOCATION_TABLE_COL_CELL_TID + ","
                            + LOCATION_TABLE_COL_AREA_CODE + ","
                            + LOCATION_TABLE_COL_SIGNAL_STRENGTH
                            + " FROM " + LOCATION_TABLE + ";";

        Log.i("FromSQLMeth",queryString);

        Cursor data = db.rawQuery(queryString, null);

        return data;
    }


    /* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * Below is the section for obtaining data for the machine learning section. The below methods will gather all
     * necessary data and compile them into a MLData object.
     *  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     */

    /**
     * Gathers all required data for the machine learning model and returns them inside of a MLData Object (in this package).
     * The data within the MLData object include: (description : name in MLData class)
     *  - 2d float array of features (will be used to create the NDArray for ML model) : features
     *  - array of encoded labels : encodedLabels
     *  - list of location labels : lables
     *  - mapping of labels to encoded labels : labelMap
     * @return
     */
    public MLData getMLDataFromDatabase(){
        float[][] featureData = this.getFormattedLocationFeatures();
        List<String> labels = this.getAllLocationLabels();

        //create map instance and populate it, while getting labels encoded, using encodeLocationLabels:
        Map<String,float[]> locLabelMap = new HashMap<>();
        float[][] encodedLabels = this.encodeLocationLabels(labels,locLabelMap);

        MLData toBeReturned = new MLData(featureData,encodedLabels,locLabelMap,labels);

        return toBeReturned;
    }

    /**
     * Will obtain all of the location features available in the database of the device and return them as a 2d array of
     * floats. Each row will be a location while each column will be a feature. The labels have to be obtained
     * separately.
     * @return float[][] all feature data available in device database
     */
    public float[][] getFormattedLocationFeatures(){
        /*
            The process of obtaining all of the features will proceed as follows:
                - First obtain the number of location samples available in the database. This is for the creation of the
                2d array to be returned.
                    --> Will be done by querying the largest "ID" value in the database; the largest ID value should
                    correspond to the number of locations in the database assuming no duplicates.

                - Second, send in query to obtain all of values for the features. This step will end with receiving
                a cursor object which will be iterated through to obtain data.

                - Third, iterate through cursor object and fill in the 2d float array.

                Note the float array's dimensions are (number of locations x number of features). The number of features
                is already known. Currently its 6.
         */

        SQLiteDatabase db = this.getWritableDatabase();

        int numOfLocations = this.getNumberOfLocationsInDatabase();

        float[][] featuresArray = new float[numOfLocations][NUMBER_OF_FEATURES];

        //query database for all features:
        String featuresQuery = "SELECT "
                                + LOCATION_TABLE_COL_LIGHT + ", "
                                + LOCATION_TABLE_COL_SOUND + ", "
                                + LOCATION_TABLE_COL_GMFS + ", "
                                + LOCATION_TABLE_COL_CELL_TID + ", "
                                + LOCATION_TABLE_COL_AREA_CODE + ", "
                                + LOCATION_TABLE_COL_SIGNAL_STRENGTH + " "
                                + "FROM " + LOCATION_TABLE + ";";
        Cursor featuresDataCursor = db.rawQuery(featuresQuery,null);

        //iterate through cursor and place data within float array:
        int i = 0;
        while(featuresDataCursor.moveToNext()){
            for(int j=0;j<NUMBER_OF_FEATURES;j++){
                featuresArray[i][j] = featuresDataCursor.getFloat(j);
            }
            i++;
        }

        return featuresArray;
    }


    public ArrayList<String> getAllLocationLabels(){
        /*
            Obtain all of the labels in the database location table
             1- query the database for all location labels
             2- populate list by iterating through the cursor returned by the query
             3- return list
         */
        SQLiteDatabase db = this.getWritableDatabase();

        String labelQueryString = "SELECT "+  LOCATION_TABLE_COL_LABEL + " FROM " + LOCATION_TABLE + ";";
        Cursor labelCursor = db.rawQuery(labelQueryString,null);

        ArrayList<String> toBeReturned = new ArrayList<>();
        while(labelCursor.moveToNext()){
            toBeReturned.add(labelCursor.getString(0));
        }

        return toBeReturned;
    }


    /*
    This method will pass in a location label and then query the database table to check if this location label is equal to one of the labels in the column of location labels.  If it is, it will
    return all an array list of all the location features.

    @returns ArrayList<String> with sensor data

     */
    public Cursor CheckIfLocationLabelExists(String sampleLabel){

        SQLiteDatabase db = this.getWritableDatabase();

        String labelQueryString = "SELECT "+  LOCATION_TABLE_COL_LABEL + " FROM " + LOCATION_TABLE + ";";
        Cursor labelCursor = db.rawQuery(labelQueryString,null);

        ArrayList<String> toBeReturned = new ArrayList<>();
        while(labelCursor.moveToNext()) {
            toBeReturned.add(labelCursor.getString(0));
        }

        for(int a=0;a<toBeReturned.size();a++){

            if(sampleLabel.equals(toBeReturned.get(a))){           //If the label is in the arrayList, then return that tuples location features


                String featuresQuery = "SELECT "
                        + LOCATION_TABLE_COL_LIGHT + ", "
                        + LOCATION_TABLE_COL_SOUND + ", "
                        + LOCATION_TABLE_COL_GMFS + ", "
                        + LOCATION_TABLE_COL_CELL_TID + ", "
                        + LOCATION_TABLE_COL_AREA_CODE + ", "
                        + LOCATION_TABLE_COL_SIGNAL_STRENGTH + " "
                        + "FROM " + LOCATION_TABLE + ";";


                Cursor featuresDataCursor = db.rawQuery(featuresQuery,null);

                return featuresDataCursor;

            }


        }




        return null;


        }


    /**
     * Creates binarized labels from given string labels. Essentially will take the number of total labels and create
     * integer values for each label based on that. Each label will be number, a power of 10, based on its position
     * in the label string list. if the label is at index 2 then its encoded label will be 10^2. The 1st one will be
     * 0 and we will go up to n-1, n being hte number of labels.
     *
     * @param locationLabels list of labels in database
     * @param map will be mapping between the strings and their  integer counterpart; populated in this method
     * @return int[] array of labels.
     */
    private float[][] encodeLocationLabels(List<String> locationLabels, Map<String,float[]> map){
        int numberOfCategories = locationLabels.size();
        float encodedLables[][] = new float[numberOfCategories][numberOfCategories];
        for(int i=0;i<numberOfCategories;i++){
            if(i==0){
                encodedLables[i][i] = 0;
                map.put(locationLabels.get(i),encodedLables[i]);
                continue;
            }
            encodedLables[i][i] = 1;
            map.put(locationLabels.get(i),encodedLables[i]);
        }
        return encodedLables;
    }


    /**
     * Helper method to retrieve the numeber of locations available in the database. Does so by finding the max
     * ID value in the locations table which assuming no duplicates should indicate the total number of lcoations in the
     * database.
     * @return int number of locations/rows
     */
    private int getNumberOfLocationsInDatabase(){
        SQLiteDatabase db = this.getWritableDatabase();

        //obtain  number of locations:
        String numOfLocQueryString = "SELECT MAX(" + LOCATION_TABLE_COL_ID + ") FROM " + LOCATION_TABLE + ";";
        Cursor numOfLocCursor = db.rawQuery(numOfLocQueryString,null);
        numOfLocCursor.moveToNext(); // cursor starts at position -1 so have to move to 0.
        int numOfLocations = numOfLocCursor.getInt(0);

        return numOfLocations;
    }


    /**
     * Retrieves all of the wifi access points in the list and groups them based on location.
     * @return List<List<WiFiAccesPoint>> a 2d list of wifi access point objects; check wifi access point class for
     *          details on their fields.
     */
    public List<List<WiFiAccessPoint>> getWifiAPListByLocation(){
        /*query to obtain the number of locations; number of locations = highest id value in location table (assuming no duplicates)
           This is necessary because each location has its own List of wifi access points so need to know the number of
           locations to make it easier to create list of wifi access points.
         */
        String queryLocationTableForNumberOfLocations = "SELECT MAX(" + AppDatabase.LOCATION_TABLE_COL_ID + ") FROM " + AppDatabase.LOCATION_TABLE + ";";

        /*
            The query should return a cursor with the following indices:
                0 - the ssid ; obtain with cursor.getString(0)
                1 - bssid (mac address) ; obtain with cursor.getFloat(1)
                2 - rssi value; obtain with cursor.getFloat(2)
                3 - parent index (which location the wifi-accespoint belongs to) get with cursor.getInt(3)
                        -- will be checked first to see which location list the access point will go to.
                ** see below for the use of the query and cursor
                *** see above in variable section for the constants that correspond to the above options
         */
        String queryString = "SELECT "
                + AppDatabase.WIFIAPTABLE_COL_SSID + ", "
                + AppDatabase.WIFIAPTABLE_COL_BSSID + ", "
                + AppDatabase.WIFIAPTABLE_COL_RSSI + ", "
                + AppDatabase.WIFIAPTABLE_COL_PARENTID + " "
                + "FROM " + AppDatabase.WAP_TABLE
                + ";";

        SQLiteDatabase db = this.getWritableDatabase();

        //get the number of locations and add that many lists to the list of wifi ap's
        Cursor parentIntCrs = (db.rawQuery(queryLocationTableForNumberOfLocations,null));
        parentIntCrs.moveToNext();
        int numOfLocations = parentIntCrs.getInt(0);
        List<List<WiFiAccessPoint>> listToBePopulated = new ArrayList<>();
        for(int i=0;i<numOfLocations;i++){
            listToBePopulated.add(new ArrayList<>());
        }

        //populate the list of lists (2d array) with wifi ap data obtained from database:
        Cursor crs = db.rawQuery(queryString,null);
        while(crs.moveToNext()){
            int listIndex = crs.getInt(WIFIAPTABLE_CURSOR_PARENT_INDEX);
            List<WiFiAccessPoint> locationSample = listToBePopulated.get(listIndex-1);
            locationSample.add(new WiFiAccessPoint(crs.getString(WIFIAPTABLE_CURSOR_SSID),
                                                   crs.getDouble(WIFIAPTABLE_CURSOR_RSSI),
                                                   crs.getString(WIFIAPTABLE_CURSOR_BSSID)));
        }

        return listToBePopulated;
    }
}