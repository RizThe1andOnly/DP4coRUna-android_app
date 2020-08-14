package com.example.dp4coruna.location;

import android.app.Activity;
import android.content.Context;
import android.location.Address;

import com.google.gson.Gson;

import java.util.List;


/**
 * Represents a particular location with its name and features. Features include address and other sensor data
 * (see SensorReader and LocationObject classes which this class extends). Each of these location objects can be a
 * room or building based on data.
 *
 * (!!!) android_app_demo note: I got rid of the "this.label = ..." statement in the constructor, put it back when
 *                              required. Rizwan
 */
public class LocationObject extends SensorReader {
    public String locationLabel;

    //protected String knownFeatureName; //from address, ie "Brooklyn Bridge"

    //location attributes from UI
    protected String buildingName;
    protected String roomName;
    protected String roomNumber;


    /**
     * Creates an instance of Location utilizing parent and grandparent SensorReader and LocationGrabber.
     * Here super() calls on SensorReader constructor.
     * @param inheritedActivity
     * @param inheritedContext
     */
    public LocationObject(Activity inheritedActivity, Context inheritedContext){
        super(inheritedActivity,inheritedContext);
        //(!!!)this.updateLocationData(); //while creating this object its fields will be set with data available at time
    }

    /**
     * Sets the fields of the calling LocationObject instance with the most recent and available data from sensors and
     * google location api.
     */
    public void updateLocationData(){
        super.sense();
    }

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


    // (!!!) alternative to all of the getters above; NOT FINISHED YET:
    public static final String REQ_ADDRESS = "address";
    public static final String REQ_LATITUDE = "latitude";
    public static final String REQ_LONGITUDE = "longitude";
    public static final String REQ_ALTITUDE = "altitude";
    public static final String REQ_CITY = "city";
    public static final String REQ_STATE = "state";
    public static final String REQ_COUNTRY = "country";
    public static final String REQ_ZIPCODE = "zipcode";
    public static final String REQ_WIFI_ACCESS_POINT_LIST = "wifiapl";
    public static final String REQ_LIGHT = "lightlevel";
    public static final String REQ_GEOMAG = "geomag";
    public static final String REQ_SOUND = "soundlevel";
    public static final String REQ_CELLDATA = "celldataobject";
    public static final String REQ_CELL_TOWER_ID = "celltowerid";
    public static final String REQ_CELL_SIGNAL_STRENGTH = "cellsignalstrength";
    public static final String REQ_AREA_CODE = "areacode";

    public Object getLocationField(String request){
        return 0;
    }

    public String convertLocationToJSON(){
        Gson gson = new Gson();
        String json = gson.toJson(new LocationObjectData(this));
        return json;
    }

}

