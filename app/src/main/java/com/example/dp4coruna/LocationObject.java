package com.example.dp4coruna;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

public class LocationObject {
    private static final int NUMBER_OF_FEATURES = 7;

    //location attributes from GPS
    protected String streetAddress;
    protected String city;
    protected String state;
    protected String country;
    protected String zipcode;
    protected double latitude;
    protected double longitude;

    protected String knownFeatureName; //from address, ie "Brooklyn Bridge"

    //location attributes from UI
    protected String buildingName;
    protected String roomName;
    protected String roomNumber;

    private List<ScanResult> APs;
    private List<Double> features;

    //different features obtained from sensor and location:
    private double lightLevel;
    private double soundLevel;
    private double geoMagLevel;
    private double cellTowerId;
    private double localAreaCode;
    private double cellSignalStrength;

    public LocationObject(Context currentContext){
        //this.locationLabel = "";
        this.features = new ArrayList<>();
        this.getWifiAccessPointsList(currentContext);
    }


    private void getWifiAccessPointsList(Context currentContext){
        WifiManager currentWifiManager = (WifiManager) currentContext.getSystemService(Context.WIFI_SERVICE);
        this.APs = currentWifiManager.getScanResults();
    }


    /**
     * Returns a new location object with all location related data fields filled by LocationGrabber
     * Note: User input location data (room name, etc) must be added to object separately
     * @param inheritedContext
     * @param inheritedActivity
     * @return LocationObject
     */
    public static LocationObject getLocationObjectWithLocationData(Context inheritedContext, Activity inheritedActivity){
        LocationObject locobj = new LocationObject(inheritedContext);

        LocationGrabber locgrab = new LocationGrabber(inheritedContext, inheritedActivity);
        //locgrab.updateLocation();
        locgrab.setupLocation();

        locobj.streetAddress = locgrab.getAddress();
        locobj.city= locgrab.getCity();
        locobj.state = locgrab.getState();
        locobj.country = locgrab.getCountry();
        locobj.zipcode = locgrab.getZipcode();
        locobj.knownFeatureName = locgrab.getKnownFeatureName();

        locobj.latitude = locgrab.getLatitude();
        locobj.longitude = locgrab.getLongitude();

        return locobj;
    }

    /**
     * Setter for building name, must be set from UI
     * @param buildingName
     */
    public void setBuildingName(String buildingName){
        this.buildingName=buildingName;
    }

    /**
     * Setter for room name, must be set from UI
     * @param roomName
     */
    public void setRoomName(String roomName){
        this.roomName=roomName;
    }

    /**
     * Setter for room name, must be set from UI
     * @param roomNumber
     */
    public void setRoomNumber(String roomNumber){
        this.roomNumber=roomNumber;
    }


}
