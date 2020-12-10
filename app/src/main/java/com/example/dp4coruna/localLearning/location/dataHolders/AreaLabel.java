package com.example.dp4coruna.localLearning.location.dataHolders;

import com.google.gson.Gson;

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
}
