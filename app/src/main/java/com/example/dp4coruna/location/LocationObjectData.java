package com.example.dp4coruna.location;

import android.location.Address;
import com.google.gson.Gson;
import java.util.List;

/**
 * Class purely for storing and retrieval of Location/Sensor Data
 * This is also the object that will be converted to and from JSON
 */

public class LocationObjectData {

    //location data
    private List<Address> addresses;
    private double longitude;
    private double latitude;
    private String address; //full address including city, state zip
    private String streetaddress; //ex. 32 Cherry Way
    private String city;
    private String state;
    private String country;
    private String zipcode;
    private String knownFeatureName;
    private double altitude_inMeters;

    private String buildingName;
    private String roomName;
    private String roomNumber;

    //sensor data:
    private List<WiFiAccessPoint> wifiApList;
    private double lightLevel;
    private double geoMagenticValue;
    private double soundLevel;
    private CellData currentCellData;
    private double cellId;
    private double areaCode;
    private double cellSignalStrength;

    public LocationObjectData(LocationObject locobj){
        this.addresses = locobj.getListOfAddresses();
        this.longitude = locobj.getLongitude();
        this.latitude = locobj.getLatitude();
        this.address = locobj.getAddress();
        this.city = locobj.getCity();
        this.state = locobj.getState();
        this.country = locobj.getCountry();
        this.zipcode = locobj.getZipcode();
        this.knownFeatureName = locobj.getKnownFeatureName();
        this.altitude_inMeters = locobj.getAltitude();
        this.wifiApList = locobj.getWifiAccessPointList();
        this.lightLevel = locobj.getLightLevel();
        this.geoMagenticValue = locobj.getGeoMagneticFieldStrength();
        this.soundLevel = locobj.getSoundLevel();
        this.currentCellData = locobj.getCellData();
        this.cellId = locobj.getCellId();
        this.areaCode = locobj.getAreaCode();
        this.cellSignalStrength=locobj.getCellSignalStrength();
        this.buildingName = locobj.getBuildingName();
        this.roomName = locobj.getRoomName();
        this.roomNumber = locobj.getRoomNumber();
        this.streetaddress = locobj.getStreetAddress();
    }

    public List<Address> getAddresses() {
        return this.addresses;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public String getAddress() {
        return this.address;
    }

    public String getStreetAddress() {
        return this.streetaddress;
    }

    public String getCity() {
        return this.city;
    }

    public String getState() {
        return this.state;
    }

    public String getCountry() {
        return this.country;
    }

    public String getZipcode() {
        return this.zipcode;
    }

    public String getKnownFeatureName(){
        return this.knownFeatureName;
    }

    public List<WiFiAccessPoint> getwifiApList() {
        return this.wifiApList;
    }

    public double getAltitude() {
        return this.altitude_inMeters;
    }

    public double getLightLevel() {
        return this.lightLevel;
    }

    public double getGeoMagenticValue() {
        return this.geoMagenticValue;
    }

    public double getSoundLevel() {
        return this.soundLevel;
    }

    public CellData getCurrentCellData() {
        return this.currentCellData;
    }

    public double getCellId() {
        return this.cellId;
    }

    public double getAreaCode() {
        return this.areaCode;
    }

    public double getCellSignalStrength() {
        return this.cellSignalStrength;
    }

    public String getBuildingName(){
        return this.buildingName;
    }

    public String getRoomName(){
        return this.roomName;
    }

    public String getRoomNumber(){
        return this.roomNumber;
    }

    public void setBuildingName(String buildingName){
        this.buildingName = buildingName;
    }

    public void setRoomName(String roomName){
        this.roomName = roomName;
    }

    public void setRoomNumber(String roomNumber){
        this.roomNumber = roomNumber;
    }


    public static LocationObjectData convertJSONToLocationObjectData(String JSONString){
        LocationObjectData data = new Gson().fromJson(JSONString, LocationObjectData.class);
        return data;
    }

    public String convertLocationObjectDataToJSON(){
        Gson gson = new Gson();
        String json = gson.toJson(this);
        return json;
    }
}
