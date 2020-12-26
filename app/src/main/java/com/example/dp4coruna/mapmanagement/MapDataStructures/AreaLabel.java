package com.example.dp4coruna.mapmanagement.MapDataStructures;

import android.os.Environment;
import android.util.Log;
import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.example.dp4coruna.phpServer.ServerConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Holds the Building name and the room/area name of an area.
 */
public class AreaLabel {

    //fields for DB schema
    public double latitude;
    public double longitude;
    public String date;
    public String building;
    public String room;
    public String county;
    public int numCovidCases;

    static List<AreaLabel> areaLabelList;

    public String area;
    public String title;
    public int riskLevel;

    public String lat_string;
    public String lng_string;


    //filepath to save the route info for indoor routing:
    public static final String FILEPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/indoorGraph.txt";
    //public static final String FILEPATH = "/assets/dynamicList.txt";
    //private static String dbQueryHandler;
    public Handler dbQueryHandler;
    public static List<String> queryResults;
    public static Handler DBQueryHandler;

    public static String ALL_LOCATION_QUERY = "SELECT * FROM userLocation";

    public AreaLabel(String building, String area){
        this.building = building;
        this.area = area;
        this.title = building + " " + area;
    }

    public AreaLabel(String building, String area, double latitude, double longitude){
        this.building = building;
        this.area = area;
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = building + " " + area;

        setLatLngStrings();
    }

    public AreaLabel(String building, String area, double latitude, double longitude,int riskLevel){
        this.building = building;
        this.area = area;
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = building + " " + area;
        this.riskLevel = riskLevel;

        setLatLngStrings();
    }

    /**
     *Constructor for creating AreaLabel objects from database
     */
    public AreaLabel(double latitude, double longitude, String date, String building,
                     String area, String county, int numCovidCases){
        this.latitude=latitude;
        this.longitude=longitude;
        this.date=date;
        this.building=building;
        this.area=area;
        this.county=county;
        this.numCovidCases=numCovidCases;

        setLatLngStrings();
    }

    /**
     * For database update purposes
     */
    private void setLatLngStrings(){
        this.lat_string = String.valueOf(this.latitude);
        this.lng_string = String.valueOf(this.longitude);
    }


    /*
                                    --------------------Getters and Setters--------------------
     */

    public void setRiskLevel(int riskLevel) {
        this.riskLevel = riskLevel;
    }
    public void setNumCovidCases(int numCovidCases) { this.numCovidCases = numCovidCases; }
    public static List<AreaLabel> getAreaLabels(){ return areaLabelList; }


    /*
                                    -------------------Object Utility Functions--------------------
     */

    /**
     * This method checks to see if two area markers are in the same building. This is done
     * through the building attribute of an AreaLabel object and the in turn the building
     * attribute within the sqlite database. So the building attribute of the two object being
     * compared has to match in order for this method to return true.
     *
     * @param o
     * @return Boolean
     */
    public boolean sameBuilding(Object o){
        if(this == o) return true;
        if(o==null || getClass() != o.getClass()) return false;
        AreaLabel areaLabel = (AreaLabel) o;
        return (this.building).equals(areaLabel.building);
    }

    /**
     * Enables search in a hash table/ map based on area labels for an object.
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AreaLabel areaLabel = (AreaLabel) o;

        boolean toBeReturned = false;

        if(this.area == null || areaLabel.area == null ) {
            toBeReturned = (this.building.equals(areaLabel.building) && this.room.equals(areaLabel.room));
        }

        if(this.room == null || areaLabel.room == null){
            toBeReturned = (this.building.equals(areaLabel.building) && this.area.equals(areaLabel.area));
        }

        return toBeReturned;
    }

    /**
     * This method allows this object to be used as a key for hashtables/maps.
     * This is required for MapActivity.
     * @return
     */
    @Override
    public int hashCode() {
        return Objects.hash(building, area);
    }

    @Override
    public String toString() {
        return "AreaLabel{" +
                "building='" + building + '\'' +
                ", area='" + area + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }



    /*
                                    ---------------------JSON/GSON Functions----------------------
     */

    /**
     * Converts the calling object to its JSON representation and returns the
     * JSON string. This will primarily used for the network portion of the code.
     * @return
     */
    public String convertToJson(){
        Gson gson = new Gson();
        String json = gson.toJson(this);
        return json;
    }

    public static AreaLabel fromJson(String areaLabelJson){
         return (new Gson().fromJson(areaLabelJson,AreaLabel.class));
    }



    /*
                            ------------------------Indoor Routing Functionalities---------------------

           Currently static method to create text file for indoor routing:
     */

    public static void writeAreasToFile(List<AreaLabel> inputs){
        String fileContents = "";
        String numLines = String.valueOf(inputs.size());

        //put number of lines in the file:
        fileContents += numLines + "\n";

        //put each location label and their lat/lng in the file:
        for(AreaLabel al : inputs){

            String name = generateIndoorRoutingName(al);
            String lat = String.valueOf(al.latitude);
            String lng = String.valueOf(al.longitude);

            fileContents += name + "|" + lat + "|" + lng + "\n";
        }

        //put edges in fileContents:
        for(int i=0;i<inputs.size();i++){
            AreaLabel alStart = inputs.get(i);
            String startNodeName = generateIndoorRoutingName(alStart);
            for(int j=(i+1);j< inputs.size();j++){
                AreaLabel alEnd = inputs.get(j);
                String endNodeName = generateIndoorRoutingName(alEnd);

                fileContents += startNodeName + "|" + endNodeName + "\n";
            }
        }

        //get rid of final \n:
        fileContents = fileContents.substring(0,fileContents.length()-1);

        //write to file
        Log.i("FromAreaLabel",fileContents);
        try {
            FileWriter fw = new FileWriter(FILEPATH);
            fw.write(fileContents);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String generateIndoorRoutingName(AreaLabel al){
        return al.building + "-" + al.area;
    }

       /*
                                    ---------------------Database Functions----------------------
     */

    /**Method queries the database with the given sql statement
     * DBQuery Handler takes care of the strings that are returned
     * @param context
     * @param sqlStatement
     */
    public static void getQueryResults(Context context, String sqlStatement){
        queryResults = new ArrayList<>();
        DBQueryHandler = new DBQueryHandler(Looper.getMainLooper());
        ServerConnection sc3 = new ServerConnection(context, DBQueryHandler);
        sc3.queryDatabase(sqlStatement);
    }

    /**
     * Method queries the database with the given sql statement, except now a handler is passed in as
     * an argument. This is based on the getQueryResult() defined above.
     *
     * @param context
     * @param sqlStatement
     * @param markerPlacerHandler
     */
    public static void getQueryResults(Context context, String sqlStatement, Handler markerPlacerHandler){
        queryResults = new ArrayList<>();
        DBQueryHandler = markerPlacerHandler;
        ServerConnection sc3 = new ServerConnection(context, DBQueryHandler);
        sc3.queryDatabase(sqlStatement);
    }


    /**Handles the string values returned from a DB query
     * Parses the strings to create a list of AreaLabel objects with the appropriate fields
     * This list is saved in the global List<AreaLabel> areaLabelList
     * note: fields in DB must contain no spaces and no null values
     */
    private static class DBQueryHandler extends Handler {

        public DBQueryHandler(Looper mainLooper){
            super(mainLooper);
            Log.i("AreaLabelFeat", "in main looper");
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i("AreaLabelFeat", "in handle message");
            if(msg.obj instanceof String){
                String message = (String) msg.obj;
                Log.i("AreaLabelFeat", message);
            }

            if(msg.obj instanceof List){
                Log.i("AreaLabelFeat", "List Instance");
                List<String> output = (List<String>) msg.obj;
                areaLabelList = new ArrayList<>();

                for(String s : output){
                    Log.i("AreaLabelFeat", s);

                    //parse string values into fields
                    AreaLabel al = parseStringintoAreaLabel(s);

                    //add to list of AreaLabels
                    areaLabelList.add(al);

                }
            }
        }
    }

    /**Method takes a row from the userLocation table as a string
     * and returns a new AreaLabel instance from the parsed values
     * note: the values in the userLocation table must not be null, and must not contain spaces
     * @param rowString
     * @return
     */
    public static AreaLabel parseStringintoAreaLabel(String rowString){
        String[] rowArray = rowString.split(" ", 7);
        AreaLabel al = new AreaLabel(Double.parseDouble(rowArray[0]), Double.parseDouble(rowArray[1]),
                rowArray[2], rowArray[3],
                rowArray[4], rowArray[5], Integer.parseInt(rowArray[6]));
        return al;
    }

}
