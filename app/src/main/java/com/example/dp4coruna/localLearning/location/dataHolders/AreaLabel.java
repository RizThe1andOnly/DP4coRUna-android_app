package com.example.dp4coruna.localLearning.location.dataHolders;

import java.util.Objects;

/**
 * Holds the Building name and the room/area name of an area.
 */
public class AreaLabel {
    public String building;
    public String area;
    public double latitude;
    public double longitude;

    public AreaLabel(String building, String area){
        this.building = building;
        this.area = area;
    }

    public AreaLabel(String building, String area, double latitude, double longitude){
        this.building = building;
        this.area = area;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AreaLabel areaLabel = (AreaLabel) o;
        return (building.equals(areaLabel.building) && area.equals(areaLabel.area));
    }

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
}
