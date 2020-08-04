package com.example.dp4coruna;

import android.Manifest;
import android.app.Activity;
import android.content.Context;

import android.content.Intent;

import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.os.*;
import android.os.Process;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Geocoder;
import android.location.Location;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.location.Address;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static android.widget.Toast.LENGTH_LONG;

/**
 * Since currently we set the location features and store them in a sequential manner (get features -> store features)
 * we need rest of program stalled while collecting info. The location callbacks do not stall the rest of the program,
 * so when the features are requested the rest of the program executes after request and since data from this request
 * is required for the rest of the program there will be some problems. By creating a new thread and keeping the main
 * thread stalled these problems are resolved (maybe?).
 */


public class LocationGrabber {

    public Context inheritedContext;
    public Activity inheritedActivity;
    private FusedLocationProviderClient fusedLocationClient;

    //control variables for thread
    private Location classLocationVar;
    private LocationResult classLocationResultVar;

    public List<Address> addresses;


    double longitude;
    double latitude;
    public String address;
    public String city;
    public String state;
    public String country;
    public String postalCode;
    public String knownName;


    public LocationGrabber(Context inheritedContext, Activity inheritedActivity){
        this.inheritedContext = inheritedContext;
        this.inheritedActivity = inheritedActivity;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(inheritedActivity);
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

//    /**
//     * Requests location information from phone using Google API
//     * Sets global variables: latitude, longitude and address
//     *
//     *
//     */ // !!! changed function to void
//    protected void getLocation(final Looper callBackLooper){
//
//        final Context context = this.inheritedContext;
//
//
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.inheritedActivity);
//
//        fusedLocationClient.getLastLocation()
//                .addOnSuccessListener(new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(Location location) {
//                        Log.d("here", "onsuccess");
//                        if (location != null) {
//
//                            //!!! passing entire location var into setAddress
//                            //set lat/long global variables
//                            //double latitude = location.getLatitude();
//                            //double longitude = location.getLongitude();
//
//
//                            double[] array = new double[2];
//                            array[0] = latitude;
//                            array[1] = longitude;
//                            Log.d(Double.toString(latitude), Double.toString(longitude));
//
//                            try {
//                                //set address global variables
//                                setAddress(context, location,null);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        else{
//
//                        }
//                    }
//                });
//
//
//        Log.d("here", "before (after) updates");
//
////         !!! Got rid of below due to changing function to void
////        double[] array2 = new double[2];
////        array2[0]=0;
////        array2[1]=0;
////        return array2;
//    }


    /** !!! Below function has been copied from getLocation and Thread stuff has been added
     *      Also change from getFusedLocationProviderClient(Context) to getFusedLocationProviderClient(Activity)
     * request current location update 
     * This segment is necessary if the user has not previously
     * logged their Location using any google services
     *
     * Will use HandlerThread class to make async task synchronous; will have main thread block until data is available
     */
    private synchronized void updateLocation(){
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

        // !!! setting the global latitude and longitude vals:
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();

        if ( (geocoder.isPresent()) && (location != null)) { //!!! changed condition to account for location object
            // !!! changed the array elem arguments to global latitude and longitude
            this.addresses = geocoder.getFromLocation(this.latitude, this.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            // !!! changed local vars to the global variables, got rid of the type declaration
            this.address = this.addresses.get(0).getAddressLine(0);
            this.city = this.addresses.get(0).getLocality();
            this.state = this.addresses.get(0).getAdminArea();
            this.country = this.addresses.get(0).getCountryName();
            this.postalCode = this.addresses.get(0).getPostalCode();
            this.knownName = this.addresses.get(0).getFeatureName();
            // !!! added section for testing purposes:
            String toastString = address;

            Toast toast= Toast.makeText(this.inheritedContext,
                    toastString, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        }

        else if( (!geocoder.isPresent()) || (location == null) ){ //!!! changed condition to account for location obj
            addresses = null;
            //address = "";
            //city = "";
            //state = "";
            //country = "";
            //postalCode="";
            //knownName="";
        }
    }

}



