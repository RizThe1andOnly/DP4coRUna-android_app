package com.example.dp4coruna.localLearning.location;

import android.app.Activity;
import android.content.Context;
import android.location.Address;

import com.example.dp4coruna.localLearning.location.dataHolders.CellData;
import com.example.dp4coruna.localLearning.location.dataHolders.LocationObjectData;
import com.example.dp4coruna.localLearning.location.dataHolders.WiFiAccessPoint;
import com.example.dp4coruna.localLearning.location.learner.SensorReader;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.List;


/**
 * Represents a particular location with its name and features. Features include address and other sensor data
 * (see SensorReader and LocationObject classes which this class extends). Each of these location objects can be a
 * room or building based on data.
 */
public class LocationObject extends SensorReader implements Serializable{
    /*
                            -----------------FOR THE PURPOSES OF JSON-------------------
        The functionality of transforming a LocationObject object into a JSON string and turning is back is supported in this
        class through the convertLocationToJSON() and (static method) getLocationFromJSON(String locationJSON) methods.

        Note-1:
            - getLocationFromJSON is static method and should be used like this when converting JSON into LocationObject:
                            LocationObject lob = LocationObject.getLocationFromJSON(jsonStringArgument);

        Note-2: getLocationFromJSON will return a location object only with data in it, that object will not be able to use
        updateLocationData. To do that simply create a new LocationObject.
     */

    //chekcer variable to make sure the context and activities have been set:
    private boolean updateable;

    //network request type constants and variable ((!!!) Names may need to be changed here for clarity)
    public static final String SEND_LABEL_REQUEST_FEATURES = "requestingfeatures";
    public static final String SEND_FEATURES_REQUEST_LABEL = "requestinglableprobabilities";
    public String requestType;

    public String locationLabel;

    //protected String knownFeatureName; //from address, ie "Brooklyn Bridge"

    //location attributes from UI
    protected String buildingName;
    protected String roomName;
    protected String roomNumber;


    /**
     * Creates empty location object that will be filled with database data.
     */
    public LocationObject(){
        super();
    }

    /**
     * Creates an instance of Location utilizing parent and grandparent SensorReader and LocationGrabber.
     * Here super() calls on SensorReader constructor.
     * @param inheritedActivity
     * @param inheritedContext
     */
    public LocationObject(Activity inheritedActivity, Context inheritedContext){
        super(inheritedActivity,inheritedContext);
        this.updateable = true;
        //(!!!)this.updateLocationData(); //while creating this object its fields will be set with data available at time
    }

    /**
     * Private constructor solely for the purpose of transforming a LocationObject JSON to a LocationObject object.
     * This location object cannot call updateLocationData.
     * @param locobj
     */
    private LocationObject(LocationObjectData locobj){
        super();

        this.updateable = false; // to keep from calling update on this class by accident (can't do since context and activity not present).

        this.addresses = locobj.getAddresses();
        this.longitude = locobj.getLongitude();
        this.latitude = locobj.getLatitude();
        this.address = locobj.getAddress();
        this.city = locobj.getCity();
        this.state = locobj.getState();
        this.country = locobj.getCountry();
        this.zipcode = locobj.getZipcode();
        this.knownFeatureName = locobj.getKnownFeatureName();
        this.altitude_inMeters = locobj.getAltitude();
        this.wifiApList = locobj.getwifiApList();
        this.lightLevel = locobj.getLightLevel();
        this.geoMagenticValue = locobj.getGeoMagenticValue();
        this.soundLevel = locobj.getSoundLevel();
        this.currentCellData = locobj.getCurrentCellData();
        this.cellId = locobj.getCellId();
        this.areaCode = locobj.getAreaCode();
        this.cellSignalStrength=locobj.getCellSignalStrength();
        this.buildingName = locobj.getBuildingName();
        this.roomName = locobj.getRoomName();
        this.roomNumber = locobj.getRoomNumber();
        this.streetaddress = locobj.getStreetAddress();
    }


    /**
     * Sets the fields of the calling LocationObject instance with the most recent and available data from sensors and
     * google location api.
     *
     * This method calls super.sense() = SensorReader.sense() which itself will call super.sense() = LocationGrabber.setupLocation().
     * These methods will call of the other methods in their respective classes to obtain sensor and location data.
     *
     * This method may be called as many time as necessary, and will update the data each time by calling data gathering
     * methods.
     *
     * Note: if this object was create through getLocationFromJSON then calling this method will do nothing.
     */
    public void updateLocationData(){
        if(!this.updateable) return;
        super.sense();
    }

    /**
     * Returns an original string representation of the class for testing purposes.
     * @return String representation of the locationobject instance
     */
    @Override
    public String toString() {
        return "LocationObject{" +
                "   "+this.address + "\n" +
                "   "+"Latitude: "+ this.latitude + "\n" +
                "   "+"Longitude: " + this.longitude + "\n" +
                "   "+"Altitude: " + this.altitude_inMeters + "\n" +
                " Sensor Data:"+"\n" +
                "   "+"Light: "+ this.lightLevel +"\n" +
                "   "+"Sound: "+ this.soundLevel + "\n" +
                "   "+"GeoMagneticField: "+ this.geoMagenticValue + "\n" +
                "   "+"Cell Data: "+"\n" +
                "   "+" "+this.currentCellData.toString()+"\n" +
                "   "+"Wifi Access-Point List: " + "\n" +
                "   "+" "+ WiFiAccessPoint.getListStringRepresent(this.wifiApList)+"\n" +
                "}";
    }



    /*
        ---------------------------------Getter/Setter section.----------------------------------
     */

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

    /**
     * Setter for the type of request this location object will be making on the network. For this please use the
     * static string constants provided by LocationObject class.
     * @param requestType LocationObject.SEND_LABEL_REQUEST_FEATURES or LocationObject.SEND_FEATURES_REQUEST_LABEL
     */
    public void setRequestType(String requestType){
        this.requestType = requestType;
    }


    /**
     * Sets the location label to the given input.
     * @param label
     */
    public void setLocationLabel(String label){
        this.locationLabel = label;
    }

    /**
     * Returns latitude in degrees
     * @return double
     */
    public double getLatitude(){
        return this.latitude;
    }

    /**
     * Returns longitude in degrees
     * @return double
     */
    public double getLongitude(){
        return this.longitude;
    }

    /**
     * Returns the altitude in meters.
     * @return
     */
    public double getAltitude(){
        return this.altitude_inMeters;
    }

    /**
     * Returns the address found by the google location api
     * @return String : House/Apt # Street, City, State/Province, ZipCode, Country
     */
    public String getAddress(){
        return this.address;
    }

    /**
     * Returns the city of the location
     * @return String
     */
    public String getCity(){
        return this.city;
    }

    /**
     * Returns state/province of the location obtained
     * @return String
     */
    public String getState(){
        return this.state;
    }

    /**
     * Returns the Country of the location obtained
     * @return String
     */
    public String getCountry(){
        return this.country;
    }

    /**
     * Returns the zip code of the location obtained
     * @return
     */
    public String getZipcode(){
        return this.zipcode;
    }

    /**
     *  (!!!)
     * @return
     */
    public String getKnownFeatureName(){
        return this.knownFeatureName;
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

    public String getStreetAddress(){
        return this.streetaddress;
    }
    /**
     * Returns the list of wifi access points found with scan.
     * Each element a WiFiAccessPoint object has content:
     *  -SSID
     *  -BSSID
     *  -RSSI
     * @return
     */
    public List<WiFiAccessPoint> getWifiAccessPointList(){
        return this.wifiApList;
    }

    /**
     * Returns the light level scanned by device's ambient light sensor.
     * @return double
     */
    public double getLightLevel(){
        return this.lightLevel;
    }

    /**
     * Returns the value of the geomagnetic force at the current location. Location in this case is determined by
     * the latitude, longitude, and altitude obtained through this class's grandparent class LocationGrabber.
     * @return double
     */
    public double getGeoMagneticFieldStrength(){
        return this.geoMagenticValue;
    }

    /**
     * Returns the max sound leveled sampled by device's mic. (Needs more work at the moment !!!)
     * @return double
     */
    public double getSoundLevel(){
        return this.soundLevel;
    }

    /**
     * Returns the cell tower information obtained through scans. (!!! May need to adjust)
     * @return CellData
     */
    public CellData getCellData(){
        return this.currentCellData;
    }

    /**
     * Returns the cell tower id that the device is currently connected to.
     * @return double
     */
    public double getCellId(){
        return this.cellId;
    }

    /**
     * Returns the area code of the cell tower that the device is currently connected to.
     * @return double
     */
    public double getAreaCode(){
        return this.areaCode;
    }

    /**
     * Returns a list of addresses obtained from geocoder
     * @return
     */
    public List<Address> getListOfAddresses(){
        return this.addresses;
    }

    /**
     * Returns the cell signal strength between the device and the cell tower it is connected to at the moment.
     * @return double
     */
    public double getCellSignalStrength(){
        return this.cellSignalStrength;
    }

    /**
     * Returns the label of the calling locationobject.
     * @return String: label of location
     */
    public String getLocationLabel(){
        return this.locationLabel;
    }



    /*
        --------------------------------------Below is the JSON section.------------------------------------
        Responsible for creating JSON string from object and returning LocationObject from JSON string.
        Note: getLocationFromJSON is a static method.
     */


    /**
     * Converts this current LocationObject to it JSON string representation and returns the string.
     * @return String the JSON reprsentation of the object.
     */
    public String convertLocationToJSON(){
        Gson gson = new Gson();
        String json = gson.toJson(new LocationObjectData(this));
        return json;
    }

    /**
     * Creates a LocationObject from json passed in as an argument. Uses the LocationObjectData class as an intermediary
     * to create the location object. NOTE: The returned LocationObject cannot call updateLocationData.
     *
     * @param locationJSON json string representing the location object
     * @return LocationObject
     */
    public static LocationObject getLocationFromJSON(String locationJSON){
        LocationObjectData data = new Gson().fromJson(locationJSON, LocationObjectData.class);
        LocationObject holderLocationObject = new LocationObject(data);
        return holderLocationObject;
    }


}

