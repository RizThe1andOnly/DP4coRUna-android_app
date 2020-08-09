package com.example.dp4coruna;


import android.app.Activity;
import android.content.Context;


import android.os.*;

import android.location.Geocoder;
import android.location.Location;

import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;


import android.location.Address;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Tasks;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * Uses Google Location and Tasks APIs to obtain "last known location" of the device.
 * Data features collected in this class and accessible through instance of this class include:
 *
 *  - Physical Data:
 *     - Latitude
 *     - Longitude
 *     - Altitude
 *
 *  - Mailing Data:
 *     - Address
 *     - City
 *     - State
 *     - Country
 *     - ZipCode
 */
public class LocationGrabber {

    public Context inheritedContext;
    public Activity inheritedActivity;
    private FusedLocationProviderClient fusedLocationClient;

    //control variables for thread
    private Location classLocationVar;
    private LocationResult classLocationResultVar;

    public List<Address> addresses;


    private double longitude;
    private double latitude;
    private String address;
    private String city;
    private String state;
    private String country;
    private String zipcode;
    private String knownFeatureName;
    private double altitude_inMeters;


    public LocationGrabber(Context inheritedContext, Activity inheritedActivity){
        this.inheritedContext = inheritedContext;
        this.inheritedActivity = inheritedActivity;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(inheritedActivity);
    }

    /**
     * Getter Methods
     * @return String
     */
    public String getAddress(){
        return this.address;
    }

    public String getCity(){
        return this.city;
    }

    public String getState(){
        return this.state;
    }

    public String getCountry(){
        return this.country;
    }

    public String getZipcode(){
        return this.zipcode;
    }

    public String getKnownFeatureName(){
        return this.knownFeatureName;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public double getAltitude_inMeters() {
        return this.altitude_inMeters;
    }

    /** !!!
     * Method to be called after object has been created; it will set all of the location variables.
     * Creates a new thread using the LocationGrabberRunnable class (below) to stall main thread while location data is
     * collected.
     *
     *
     *  !!! For now only getLastLocation implemented; will do location update later
     */
    public synchronized void setupLocation(){
        final LocationGrabber lgForThreads = LocationGrabber.this;

        //get last location
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                lgForThreads.getLocation();
            }
        }).start();

        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /** !!! Below function has been copied from getLocation and Thread stuff has been added
     *      Also change from getFusedLocationProviderClient(Context) to getFusedLocationProviderClient(Activity)
     * request current location update 
     * This segment is necessary if the user has not previously
     * logged their Location using any google services
     *
     * Will use HandlerThread class to make async task synchronous; will have main thread block until data is available
     */
    protected synchronized void updateLocation(){
        final LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        // !!!
        final LocationCallback mLocationCallback = new LocationCallback() {

            //Asynchronous - called when device location is available
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.i("From callBackHandler","Got here but thread not quitting " + Thread.currentThread().getName());
                if (locationResult == null) {
                    Log.d("here", "no location found");
                    return; //no location found, exit
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                    }
                }
                notify(); //t!!!
            }
        };

        this.fusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback,Looper.myLooper());

    }


    /** !!! Redid fusedlocation stuff to block thread while waiting for data. Also changed
     *      getFusedLocationProviderClient(Context) to getFusedLocationProviderClient(Activity).
     * Requests location information from phone using Google API
     * Sets global variables: latitude, longitude and address
     */
    private synchronized void getLocation(){
        final LocationGrabber lgForOnSuccessListener = LocationGrabber.this;
        try {
            this.classLocationVar = Tasks.await(this.fusedLocationClient.getLastLocation(),500, TimeUnit.MILLISECONDS);
            this.setAddress(this.classLocationVar);
        } catch (ExecutionException e) {
            Log.i("From getLocation","execution exception");
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.i("From getLocation","interrupted exception");
            e.printStackTrace();
        } catch (IOException e){
            Log.i("From getLocation","io exception");
            e.printStackTrace();
        } catch (TimeoutException e) {
            Log.i("From getLocation","timeout exception");
            e.printStackTrace();
        }
        notify();
    }



    /**
     *
     * Sets address global variables based on longitude and latitude
     * @throws IOException
     */ //!!! changed so that function now takes entire location var rather than longitude/latitude, also changed function
        //to void
    public void setAddress(Location location) throws IOException {
        Log.i("here","at set address");
        Geocoder geocoder;
        geocoder = new Geocoder(this.inheritedContext, Locale.getDefault());



        if ( (geocoder.isPresent()) && (location != null)) { //!!! changed condition to account for location object

            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
            this.altitude_inMeters = location.getAltitude();

            this.addresses = geocoder.getFromLocation(this.latitude, this.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            this.address = this.addresses.get(0).getAddressLine(0);
            this.city = this.addresses.get(0).getLocality();
            this.state = this.addresses.get(0).getAdminArea();
            this.country = this.addresses.get(0).getCountryName();
            this.zipcode = this.addresses.get(0).getPostalCode();
            this.knownFeatureName = this.addresses.get(0).getFeatureName();
        }
        else if( (!geocoder.isPresent()) || (location == null) ){
            this.latitude = 0;
            this.longitude = 0;
            this.altitude_inMeters = 0;
            this.addresses = null;

            /**
             * These assignments should be verified to see whether or not these should be the values set if
             * getLastLocation() doesn't work. (!!!)
             */
        }
    }


    @Override
    public String toString() {
        return "LocationGrabber{\n" +
                "     Address: "+ this.address + "\n" +
                "     City: " + this.city +"\n" +
                "     Country: " + this.country + "\n" +
                "     ZipCode: " + this.zipcode + "\n" +
                "     Latitude: " + String.valueOf(this.latitude) + "\n" +
                "     Longitude: " + String.valueOf(this.longitude) + "\n" +
                "     Altitude(meter): " + String.valueOf(this.altitude_inMeters) + "\n" +
                "}";
    }
}



