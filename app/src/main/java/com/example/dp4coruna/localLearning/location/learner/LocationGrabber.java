package com.example.dp4coruna.localLearning.location.learner;


import android.Manifest;
import android.app.Activity;
import android.content.Context;


import android.content.pm.PackageManager;
import android.os.*;

import android.location.Geocoder;
import android.location.Location;

import android.util.Log;


import android.location.Address;

import android.widget.Toast;
import androidx.core.app.ActivityCompat;
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

    protected Context inheritedContext;
    protected Activity inheritedActivity;
    protected FusedLocationProviderClient fusedLocationClient;

    //control variables for thread
    protected Location classLocationVar;
    protected LocationResult classLocationResultVar;

    protected List<Address> addresses;


    protected double longitude;
    protected double latitude;
    protected String address; //full address, includes city, state zip and country
    protected String streetaddress; //ex 17 Wayfare St
    protected String city;
    protected String state;
    protected String country;
    protected String zipcode;
    protected String knownFeatureName;
    protected double altitude_inMeters;


    /**
     * Constructor to be used when gathering data because takes activity and context to use for google api's.
     * Currently access level is at protected so that only sub-classes can use this constructor.
     * @param inheritedContext
     * @param inheritedActivity
     */
    protected LocationGrabber(Context inheritedContext, Activity inheritedActivity) {
        this.inheritedContext = inheritedContext;
        this.inheritedActivity = inheritedActivity;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(inheritedActivity);
    }

    /**
     * Constructor to be used for holding data and for json data transfer purposes. NOT FOR ACTUAL USE.
     */
    protected LocationGrabber() {

    }

    /** !!!
     * Method to be called after object has been created; it will set all of the location variables.
     * Creates a new thread using the LocationGrabberRunnable class (below) to stall main thread while location data is
     * collected.
     *
     *
     *  !!! For now only getLastLocation implemented; will do location update later
     */
    public synchronized void setupLocation() {
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
    protected synchronized void updateLocation() {
        final LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        // !!!
        final LocationCallback mLocationCallback = new LocationCallback() {

            //Asynchronous - called when device location is available
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.i("From callBackHandler", "Got here but thread not quitting " + Thread.currentThread().getName());
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

        if (ActivityCompat.checkSelfPermission(this.inheritedContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this.inheritedContext,"Permission for this operation not granted",Toast.LENGTH_LONG).show();
            return;
        }
        this.fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

    }


    /** !!! Redid fusedlocation stuff to block thread while waiting for data. Also changed
     *      getFusedLocationProviderClient(Context) to getFusedLocationProviderClient(Activity).
     * Requests location information from phone using Google API
     * Sets global variables: latitude, longitude and address
     */
    private synchronized void getLocation(){
        final LocationGrabber lgForOnSuccessListener = LocationGrabber.this;
        try {
            if (ActivityCompat.checkSelfPermission(this.inheritedContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this.inheritedContext,"Permission for this operation not granted",Toast.LENGTH_LONG).show();
                return;
            }
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
    private void setAddress(Location location) throws IOException {
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
            this.streetaddress = this.addresses.get(0).getSubThoroughfare() + " " + this.addresses.get(0).getThoroughfare();
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



    /*
                        -----------Setters for location fields (req for data transferring from database------------
                                     see: databasemanagement.AppDatabase.checkIfLocationLabelExists()

          ** Currently there is nothing that needs to be set for this class **
     */


}



