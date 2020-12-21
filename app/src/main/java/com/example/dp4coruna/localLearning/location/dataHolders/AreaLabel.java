package com.example.dp4coruna.localLearning.location.dataHolders;

import android.os.Environment;
import android.util.Log;
import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Holds the Building name and the room/area name of an area.
 */
public class AreaLabel {
    public String building;
    public String area;
    public double latitude;
    public double longitude;
    public String title;
    public int riskLevel;

    //filepath to save the route info for indoor routing:
    public static final String FILEPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/indoorGraph.txt";
    //public static final String FILEPATH = "/assets/dynamicList.txt";

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

    /*
                                    --------------------Getters and Setters--------------------
     */

    public void setRiskLevel(int riskLevel) {
        this.riskLevel = riskLevel;
    }


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
        //Log.i("FromAreaLabel",fileContents);
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
}
