package com.example.dp4coruna.mapmanagement;

import android.util.Log;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Random;

/**This class holds the Route Info received from Google Directions API
 * After being parsed from the JSON
 */

public class Route {
    private String distance; //given in miles
    private String duration; //given in minutes
    private String startAddress;
    private String endAddress;
    private LatLng startLocation;
    private LatLng endLocation;
    private List<LatLng> points; //all latitudinal and longitudinal points which make up the route
    private String risk; //"High", "Medium" or "Low"

    public void setDistance(String distance){
        this.distance = distance;
    }

    public void setDuration(String duration){
        this.duration = duration;
    }

    public void setStartAddress(String startAddress){
        this.startAddress = startAddress;
    }

    public void setEndAddress(String endAddress){
        this.endAddress = endAddress;
    }

    public void setStartLocation(LatLng startLocation){
        this.startLocation = startLocation;
    }

    public void setEndLocation(LatLng endLocation){
        this.endLocation = endLocation;
    }

    public void setPoints(List<LatLng> points){
        this.points = points;
    }

    /**For now, this sets the risk of the route based on the parameter given
     * 0 for low, 1 for high, all else for medium
     * will need to write a new method to take into account "coronavirus risk areas" nearby
     * @param number
     */
    public void setRandomRisk(int number){
        //Random randomGenerator = new Random();
        //int rand = new Random().nextInt(3);


        if (number==0){
            this.risk="Low";
        }
        else if (number==1){
            this.risk="High";
        }
        else{
            this.risk="Medium";
        }

    }

    public String getDistance(){
        return this.distance;
    }

    public String getDuration(){
        return this.duration;
    }

    public String getStartAddress(){
        return this.startAddress;
    }

    public String getEndAddress(){
        return this.endAddress;
    }

    public LatLng getStartLocation(){
        return this.startLocation;
    }

    public LatLng getEndLocation(){
        return this.endLocation;
    }

    public List<LatLng> getPoints(){
        return this.points;
    }

    public String getRisk(){
        return this.risk;
    }


}
