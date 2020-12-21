package com.example.dp4coruna.localLearning.location.dataHolders;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.example.dp4coruna.TempResultsActivity;
import com.example.dp4coruna.phpServer.ServerConnection;
import com.google.gson.Gson;

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

    //private static String dbQueryHandler;
    public Handler dbQueryHandler;
    public static List<String> queryResults;
    public static Handler DBQueryHandler;


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
    }

    public AreaLabel(String building, String area, double latitude, double longitude,int riskLevel){
        this.building = building;
        this.area = area;
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = building + " " + area;
        this.riskLevel = riskLevel;
    }

    /**
     *Constructor for creating AreaLabel objects from database
     */
    public AreaLabel(double latitude, double longitude, String date, String building,
                     String room, String county, int numCovidCases){
        this.latitude=latitude;
        this.longitude=longitude;
        this.date=date;
        this.building=building;
        this.room=room;
        this.county=county;
        this.numCovidCases=numCovidCases;
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
     * Enables search in a hash table/ map based on area labels for an object.
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AreaLabel areaLabel = (AreaLabel) o;
        return (building.equals(areaLabel.building) && area.equals(areaLabel.area));
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
