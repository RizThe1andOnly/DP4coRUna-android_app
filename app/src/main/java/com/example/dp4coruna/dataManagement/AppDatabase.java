package com.example.dp4coruna.dataManagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.dp4coruna.localLearning.location.LocationObject;
import com.example.dp4coruna.localLearning.location.dataHolders.LocationObjectData;
import com.example.dp4coruna.localLearning.location.dataHolders.WiFiAccessPoint;
import com.example.dp4coruna.ml.MLData;

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
    private final String ADMIN_USER_ID = "admin00";

    public static final String DATABASE_NAME = "dp4corunadata.db";
    public static final String LOCATION_TABLE = "mylist_data";
    public static final String WAP_TABLE = "wifi_access_point_table";
    private static final String ACCEL_OFFSET_TABLE = "accelerometer_offset_table";
    private static final String MAP_LABEL_TABLE = "map_label_table";

    /*
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
    public static final String LOCATION_TABLE_COL_ADDRESS = "location_address";
    public static final String LOCATION_TABLE_COL_ROOM_NAME = "room_name";
    public static final String LOCATION_TABLE_COL_ROOM_NUMBER = "room_number";
    public static final String LOCATION_TABLE_COL_BUILDING_NAME = "building_name";


    /*
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


    /*
        Data columns for the offset table
     */
    public static final String ACCEL_OFFSET_TABLE_COL_X_OFFSET = "xOffset";
    public static final String ACCEL_OFFSET_TABLE_COL_Y_OFFSET = "yOffset";
    public static final String ACCEL_OFFSET_TABLE_COL_Z_OFFSET = "zOffset";

    /*
        Data columns for the map_label_table
     */
    public static final String MAP_LABEL_TABLE_COL_USERID = "userid";
    public static final String MAP_LABEL_TABLE_COL_BUILDING = "building";
    public static final String MAP_LABEL_TABLE_COL_ROOM = "room";
    public static final String MAP_LABEL_TABLE_COL_LATITUDE = "latitude";
    public static final String MAP_LABEL_TABLE_COL_LONGITUDE = "longitude";


    /*
        Table creation SQL statements:
     */

    private final String createLocTable = "CREATE TABLE " + LOCATION_TABLE + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, "
            + LOCATION_TABLE_COL_LABEL + " TEXT, "
            + LOCATION_TABLE_COL_LIGHT + " FLOAT, "
            + LOCATION_TABLE_COL_SOUND + " FLOAT, "
            + LOCATION_TABLE_COL_GMFS + " FLOAT, "
            + LOCATION_TABLE_COL_CELL_TID + " FLOAT, "
            + LOCATION_TABLE_COL_AREA_CODE + " FLOAT, "
            + LOCATION_TABLE_COL_SIGNAL_STRENGTH + " FLOAT, "
            + LOCATION_TABLE_COL_ADDRESS + " TEXT, "
            + LOCATION_TABLE_COL_BUILDING_NAME + " TEXT, "
            + LOCATION_TABLE_COL_ROOM_NAME + " TEXT, "
            + LOCATION_TABLE_COL_ROOM_NUMBER + " INTEGER"
            +");";



    private final String createWiFIAPTable = "CREATE TABLE " + WAP_TABLE + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                                            + WIFIAPTABLE_COL_SSID + " TEXT, "
                                            + WIFIAPTABLE_COL_BSSID + " TEXT, "
                                            + WIFIAPTABLE_COL_RSSI + " FLOAT, "
                                            + WIFIAPTABLE_COL_PARENTID + " INTEGER, "
                                            + "FOREIGN KEY(" + WIFIAPTABLE_COL_PARENTID + ") REFERENCES " + LOCATION_TABLE + "(ID) "
                                            +");";


    private final String createAccelOffsetTable = "CREATE TABLE " + ACCEL_OFFSET_TABLE + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                                                    + ACCEL_OFFSET_TABLE_COL_X_OFFSET + " FLOAT, "
                                                    + ACCEL_OFFSET_TABLE_COL_Y_OFFSET + " FLOAT, "
                                                    + ACCEL_OFFSET_TABLE_COL_Z_OFFSET + " FLOAT "
                                                    +");";


    private final String createMapLabelTable = "CREATE TABLE " + MAP_LABEL_TABLE + "("
                                                + MAP_LABEL_TABLE_COL_USERID + " VARCHAR(255), "
                                                + MAP_LABEL_TABLE_COL_BUILDING + " VARCHAR(255), "
                                                + MAP_LABEL_TABLE_COL_ROOM + " VARCHAR(255), "
                                                + MAP_LABEL_TABLE_COL_LATITUDE + " DOUBLE, "
                                                + MAP_LABEL_TABLE_COL_LONGITUDE + " DOUBLE, "
                                                + "PRIMARY KEY("
                                                    + MAP_LABEL_TABLE_COL_USERID + ", "
                                                    + MAP_LABEL_TABLE_COL_BUILDING + ", "
                                                    + MAP_LABEL_TABLE_COL_ROOM + ")"
                                                +");";


    //constant String for if matching label is not found when looking through device database:
    private static final String LABEL_NOT_FOUND = "Label Not Found";


    public AppDatabase(Context context) {
        super(context, DATABASE_NAME, null, 3);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createLocTable);
        db.execSQL(createWiFIAPTable);
        db.execSQL(createAccelOffsetTable);
        db.execSQL(createMapLabelTable);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + LOCATION_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + WAP_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ACCEL_OFFSET_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + MAP_LABEL_TABLE);
        onCreate(db);
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
        contentValues.put(LOCATION_TABLE_COL_ADDRESS,locationObject.getAddress());
        contentValues.put(LOCATION_TABLE_COL_BUILDING_NAME,locationObject.getBuildingName());
        contentValues.put(LOCATION_TABLE_COL_ROOM_NAME,locationObject.getRoomName());
        contentValues.put(LOCATION_TABLE_COL_ROOM_NUMBER,locationObject.getRoomNumber());

        //insert the row:
        long result = db.insert(LOCATION_TABLE, null, contentValues);

        int parentIndex = getLatestWAPDataIndex();

        if(!addWAPData(locationObject.getWifiAccessPointList(),parentIndex)){
            Log.i("FromDataBaseTest","Failed at wifi ap data entry"); //(!!!)
        }

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
        contentValues.put(LOCATION_TABLE_COL_ADDRESS,lod.getAddress());
        contentValues.put(LOCATION_TABLE_COL_BUILDING_NAME,lod.getBuildingName());
        contentValues.put(LOCATION_TABLE_COL_ROOM_NAME,lod.getRoomName());
        contentValues.put(LOCATION_TABLE_COL_ROOM_NUMBER,lod.getRoomNumber());



        //insert the row:
        long result = db.insert(this.LOCATION_TABLE, null, contentValues);

        int parentIndex = getLatestWAPDataIndex();

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
     * Gets the greatest ID value in the device database, this will correspond to the entry just entered into the
     * database.
     *
     * Note: if database's id values are out of order it might be necessary to get all id values in a list and then
     * get the max value.
     *
     * @return index of the current entry
     */
    private int getLatestWAPDataIndex(){
        //get the index of row just entered into data for the wifi access point list and add the wifi access point list to wifiap table:
        SQLiteDatabase db = this.getWritableDatabase();
        String indexQuery = "SELECT ID FROM " + this.LOCATION_TABLE + ";";
        Cursor parentCrs = db.rawQuery(indexQuery,null);
        parentCrs.moveToLast();
        int parentIndex = parentCrs.getInt(0);
        return parentIndex;
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
     * Returns formatted string representation of the current contents of the Location Table of the device databse
     * @return
     */
    public String getLocationTableContents(){
        Cursor contentCursor = getListContents();

        //format data in cursor into easily viewd form:
        String toBeReturned = "";
        while(contentCursor.moveToNext()){
            String entry = "";
            entry += contentCursor.getString(0) + " : " + contentCursor.getString(1) + "\n";
            entry += "\t" + "Light: " + contentCursor.getFloat(4) + "\n";
            entry += "\t" + "Sound: " + contentCursor.getFloat(5) + "\n";
            entry += "\t" + "GeoMag: " + contentCursor.getFloat(6) + "\n";
            entry += "\t" + "CTI: " + contentCursor.getInt(7) + "\n";
            entry += "\t" + "AreaCode: " + contentCursor.getInt(8) + "\n";
            entry += "\t" + "CellSignalStrength: " + contentCursor.getFloat(9);

            toBeReturned += entry + "\n";
        }

        return toBeReturned;
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
                            + LOCATION_TABLE_COL_ROOM_NAME + ","
                            + LOCATION_TABLE_COL_ROOM_NUMBER + ","
                            + LOCATION_TABLE_COL_BUILDING_NAME + ","
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
     *
     * IF ANY CHANGES ARE MADE TO BELOW SECTION THEN CORRESPONDING CHANGES MUST BE MADE IN MLMODEL OR ESLE
     * EVERYTHING WILL CRASH.
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
        List<String> labels = this.getAllLocationLabels();
        //create map instance and populate it, while getting labels encoded, using encodeLocationLabels:
        Map<String,float[]> encodedLabelsMap = new HashMap<>();
        this.encodeLocationLabels(labels,encodedLabelsMap);

        List<List<Float>> groundTruth_encodedLabels = new ArrayList<>();
        float[][] featureData = this.getFormattedLocationFeatures(encodedLabelsMap,groundTruth_encodedLabels);

        //convert groundTruth_encodedLabels to float[][], due to deeplearning4j requirements
        int numRows = groundTruth_encodedLabels.size();
        int numColumns = groundTruth_encodedLabels.get(0).size();//every row has same number of columns
        float[][] groundTruth_encoded = new float[numRows][numColumns];
        //Log.i("FromAppDatabase",groundTruth_encoded.length+"  "+groundTruth_encoded[0].length);
        for(int i=0;i<numRows;i++){
            for(int j=0;j<numColumns;j++){
                groundTruth_encoded[i][j] = groundTruth_encodedLabels.get(i).get(j);
            }
        }

        MLData toBeReturned = new MLData(featureData,groundTruth_encoded,encodedLabelsMap,labels);

        return toBeReturned;
    }

    /**
     * Will obtain all of the location features available in the database of the device and return them as a 2d array of
     * floats. Each row will be a location while each column will be a feature. The labels have to be obtained
     * separately.
     * @return float[][] all feature data available in device database
     */
    public float[][] getFormattedLocationFeatures(Map<String,float[]> encodedLablesMap, List<List<Float>> groundTruth){
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
                + LOCATION_TABLE_COL_LABEL + ", "
                + LOCATION_TABLE_COL_LIGHT + ", "
                + LOCATION_TABLE_COL_SOUND + ", "
                + LOCATION_TABLE_COL_GMFS + ", "
                + LOCATION_TABLE_COL_CELL_TID + ", "
                + LOCATION_TABLE_COL_AREA_CODE + ", "
                + LOCATION_TABLE_COL_SIGNAL_STRENGTH
                + " FROM " + LOCATION_TABLE + ";";

        Cursor featuresDataCursor = db.rawQuery(featuresQuery,null);

        //iterate through cursor and place data within float array:
        int i = 0;
        while(featuresDataCursor.moveToNext()){
           // one "while" iteration for each location so the ground truth requires an additional list.
            // since each list in ground truth represents a single location (one-hot-encoding).
            groundTruth.add(new ArrayList<>());

            for(int j=0;j<NUMBER_OF_FEATURES+1;j++){ //plus one to account for the label

                //Log.d("PRINTING GET FORMATTEDFEATURES: ",featuresDataCursor.getString(j));

                //put proper encoded label into groundTruth for set of features; the label has to be encoded:
                if (j==0){// we are at the label
                    List currentLocationEncodedLabel_gt = groundTruth.get(i);
                    float[] currentLocationEncodedLabel_fromMap = encodedLablesMap.get(featuresDataCursor.getString(j));
                    for(float elem : currentLocationEncodedLabel_fromMap){
                        currentLocationEncodedLabel_gt.add(elem);
                    }
                    continue;
                }

                featuresArray[i][j-1] = featuresDataCursor.getFloat(j);
            }
            i++;
        }

        return featuresArray;
    }


    /**
     * Obtains all the labels available in the database of the device through SQLite calls.
     * @return
     */
    public ArrayList<String> getAllLocationLabels(){
        /*
            Obtain all of the labels in the database location table
             1- query the database for all location labels
             2- populate list by iterating through the cursor returned by the query
             3- return list
         */
        SQLiteDatabase db = this.getWritableDatabase();

        String labelQueryString = "SELECT DISTINCT "+  LOCATION_TABLE_COL_LABEL + " FROM " + LOCATION_TABLE + ";";
        Cursor labelCursor = db.rawQuery(labelQueryString,null);

        ArrayList<String> toBeReturned = new ArrayList<>();
        while(labelCursor.moveToNext()){
            toBeReturned.add(labelCursor.getString(0));
        }

        return toBeReturned;
    }


    /**
     * This method will pass in a location label and then query the database table to check if this location label is equal to one of the labels in the column of location labels.  If it is, it will
     * return all an array list of all the location features.
     *
     * @returns String JSON rep of location object created from the features obtained, if no matches then "Not Found"
     */
    public String checkIfLocationLabelExists(String sampleLabel){

        SQLiteDatabase db = this.getWritableDatabase();

        // query string looks features of sample which has a label that matches the input: sampleLabel.
        String labelQueryString = "SELECT "
                                + LOCATION_TABLE_COL_LIGHT + ", "
                                + LOCATION_TABLE_COL_SOUND + ", "
                                + LOCATION_TABLE_COL_GMFS + ", "
                                + LOCATION_TABLE_COL_CELL_TID + ", "
                                + LOCATION_TABLE_COL_AREA_CODE + ", "
                                + LOCATION_TABLE_COL_SIGNAL_STRENGTH
                                + " FROM " + LOCATION_TABLE + " WHERE " + LOCATION_TABLE_COL_LABEL + " = ?";

        //points to the row(s) with matching label if it exists
        Cursor labelCursor = db.rawQuery(labelQueryString,new String[] {sampleLabel});

        String locationObjectJson = LABEL_NOT_FOUND;
        if(labelCursor.getCount() > 0 ){
            labelCursor.moveToNext();
            LocationObject emptyLocationObject = new LocationObject();
            emptyLocationObject.setLightLevel(labelCursor.getFloat(0));
            emptyLocationObject.setSoundLevel(labelCursor.getFloat(1));
            emptyLocationObject.setGeoMagenticValue(labelCursor.getFloat(2));
            emptyLocationObject.setCellTID(labelCursor.getFloat(3));
            emptyLocationObject.setAreaCode(labelCursor.getFloat(4));
            emptyLocationObject.setCellSignalStrength(labelCursor.getFloat(5));
            locationObjectJson = emptyLocationObject.convertLocationToJSON();
        }

        return locationObjectJson;
    }


    /* This method passes in a data entry, a location feature (which will be one of the columns), and a label and
       it will iterate through the database to find the correct entry at that specific column and row to
       update the old entry with the new entry

       @void method
    */

    public void updateExistingEntry(float newEntry, String columnType, String sampleLabel){

        SQLiteDatabase db = this.getWritableDatabase();

        String sql = "UPDATE "+LOCATION_TABLE +" SET " + columnType+ " = '"+newEntry+"' WHERE LABEL = '"+sampleLabel + "'";

        db.execSQL(sql);
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
        String numOfLocQueryString = "SELECT COUNT(*) FROM " + LOCATION_TABLE + ";";
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


    /*
        map_label_table section. This section will deal with adding and querying of data related to the map
        position of a particular location. The lat/lng points are obtained from google maps and user input.
     */

    public boolean addMapLabelData(String buildingName,String roomName, double latitude, double longitude){
        return addMapLabelHelper(ADMIN_USER_ID,buildingName,roomName,latitude,longitude);
    }

    public boolean addMapLabelData(String userId, String buildingName, String roomName, double latitude, double longitude){
        return addMapLabelHelper(userId,buildingName,roomName,latitude,longitude);
    }

    private boolean addMapLabelHelper(String userId, String buildingName, String roomName, double latitude, double longitude){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(MAP_LABEL_TABLE_COL_USERID,userId);
        cv.put(MAP_LABEL_TABLE_COL_BUILDING,buildingName);
        cv.put(MAP_LABEL_TABLE_COL_ROOM,roomName);
        cv.put(MAP_LABEL_TABLE_COL_LATITUDE,latitude);
        cv.put(MAP_LABEL_TABLE_COL_LONGITUDE,longitude);

        long result = db.insert(MAP_LABEL_TABLE,null,cv);

        if(result == -1){
            Log.i("FromAppDatabase","Map Label Data Entry Failed");
            return false;
        }

        return true;
    }

    public Cursor queryMapMarkers(){
        SQLiteDatabase db = this.getReadableDatabase();
        String queryString = "SELECT "
                            + MAP_LABEL_TABLE_COL_BUILDING + ", "
                            + MAP_LABEL_TABLE_COL_ROOM + ", "
                            + MAP_LABEL_TABLE_COL_LATITUDE + ", "
                            + MAP_LABEL_TABLE_COL_LONGITUDE +" "
                            + "FROM " + MAP_LABEL_TABLE + ";";
        return db.rawQuery(queryString,null);
    }


    /* ----------------------------------------
            Section for adding and retrieving accelerometer data from the database. Used with CalibrationTask to
            save Accelerometer offset to device database. This offset will be retrieved to be used with accelerometer
            data.
       ----------------------------------------
     */

    /**
     * To be called by CalibrationTast to add new found accelerometer offset data to the database in its own table.
     * This method will check if data already exists and if it does then update it, if it does not then add it.
     * @param offsetValsPerAxis List containing the offset data obtained. The indices should be as follows: 0: x-axis,
     *                          1: y-axis, 2- z-axis
     */
    public void addOffsetData(List<Float> offsetValsPerAxis){
        SQLiteDatabase db = this.getWritableDatabase();
        List<Float> testForDataExistence = getOffsetData();

        //check if data already exists by using the getOffsetData method below
        List<Float> currentData = getOffsetData();
        if(currentData == null){ // meaning data doesn't already exist and needs to be added
            ContentValues cv = new ContentValues();
            cv.put(ACCEL_OFFSET_TABLE_COL_X_OFFSET,offsetValsPerAxis.get(0));
            cv.put(ACCEL_OFFSET_TABLE_COL_Y_OFFSET,offsetValsPerAxis.get(1));
            cv.put(ACCEL_OFFSET_TABLE_COL_Z_OFFSET,offsetValsPerAxis.get(2));

            db.insert(ACCEL_OFFSET_TABLE,null,cv);
            return;
        }

        //will be here if data does already exist and now needs to be updated:
        ContentValues updateCv = new ContentValues();
        updateCv.put(ACCEL_OFFSET_TABLE_COL_X_OFFSET,offsetValsPerAxis.get(0));
        updateCv.put(ACCEL_OFFSET_TABLE_COL_Y_OFFSET,offsetValsPerAxis.get(1));
        updateCv.put(ACCEL_OFFSET_TABLE_COL_Z_OFFSET,offsetValsPerAxis.get(2));
        db.update(ACCEL_OFFSET_TABLE,updateCv,"ID = 1",null);

    }

    public List<Float> getOffsetData(){
        SQLiteDatabase db = this.getReadableDatabase();

        String queryString = "SELECT "
                            + ACCEL_OFFSET_TABLE_COL_X_OFFSET + ", "
                            + ACCEL_OFFSET_TABLE_COL_Y_OFFSET + ", "
                            + ACCEL_OFFSET_TABLE_COL_Z_OFFSET + " "
                            + "FROM " + ACCEL_OFFSET_TABLE + ";";

        Cursor offsetCursor = db.rawQuery(queryString,null);

        if(offsetCursor.getCount() == 0) return null;

        offsetCursor.moveToNext();
        Log.i("FromAppDatabase",offsetCursor.getColumnCount() + "");

        //create offset list and populate with data retrieved from the database:
        List<Float> offsetData = new ArrayList<>();
        offsetData.add(offsetCursor.getFloat(0));
        offsetData.add(offsetCursor.getFloat(1));
        offsetData.add(offsetCursor.getFloat(2));

        return offsetData;
    }

}