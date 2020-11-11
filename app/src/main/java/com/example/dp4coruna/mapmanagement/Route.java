package com.example.dp4coruna.mapmanagement;

import android.util.Log;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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

    public void setRisk(int number){
        //if no covid data, set random risk
        if(MapsActivity.covidClusterCircles.isEmpty() || MapsActivity.hashmap.isEmpty()){
            //int randomNum = ThreadLocalRandom.current().nextInt(0, 3);
            setRandomRisk(number);
            return;
        }
        else{

            for(int i=0; i<MapsActivity.covidClusterCircles.size(); i++) {

                Circle circle = MapsActivity.covidClusterCircles.get(i);
                LatLng center = circle.getCenter();
                double radius = circle.getRadius();
                COVIDCluster covidCluster = MapsActivity.hashmap.get(circle);
                String clusterRiskLevel = covidCluster.getRisklevel();
                radius = 10;

                for(int j=0; j<points.size(); j++) {

                    LatLng point = points.get(j);

                    double ky = 40000 / 360;
                    double kx = Math.cos(Math.PI * center.latitude / 180.0) * ky;
                    double dx = Math.abs(center.longitude - point.longitude) * kx;
                    double dy = Math.abs(center.latitude - point.latitude) * ky;
                    boolean isPointinCircle = Math.sqrt(dx * dx + dy * dy) <= radius;

                    if(isPointinCircle){
                    if (clusterRiskLevel.equals("High")){
                        this.risk = "High";
                        return;
                    }
                    else if (clusterRiskLevel.equals("Medium")){
                        this.risk = "Medium";
                    }
                    else if (clusterRiskLevel.equals("Low") && this.risk!=null){
                        if(this.risk.equals("Medium")){
                            this.risk = "Medium";
                        }
                        else{
                            this.risk = "Low";
                        }
                    }
                    else{
                        this.risk = "Low";
                    }
                    }
                }
                }
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
