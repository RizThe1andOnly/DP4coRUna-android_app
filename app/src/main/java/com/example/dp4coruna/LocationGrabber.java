package com.example.dp4coruna;

import android.Manifest;
import android.app.Activity;
import android.content.Context;

import android.content.Intent;

import android.content.pm.PackageManager;
import android.hardware.Sensor;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Parcelable;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.widget.Toast.LENGTH_LONG;

public class LocationGrabber {

    private FusedLocationProviderClient fusedLocationClient;

    /*
    double longitude;
    double latitude;

    List<Address> addresses;

    String address;
    String city;
    String state;
    String country;
    String postalCode;
    String knownName;
    */

    /**
     * Requests location information from phone using Google API
     * Sets global variables: latitude, longitude and address
     *
     */
    protected double[] getLocation(final Context context){
        Log.d("here", "top of get location");

        //final double[] array = new double[2];
        //array[0] = 0;
        //array[1] = 1;
        //new double[] coordinates = {latitude}{longitude};
        // request current location update
        // This segment is necessary if the user has not previously
        // logged their Location using any google services
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationCallback mLocationCallback = new LocationCallback() {

            //Asynchronous - called when device location is available
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.d("here", "no location found");
                    return; //no location found, exit
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                    }
                }
            }
        };

        Log.d("here", "before updates");

        //updates location to what we have just stored in mLocationRequest
        LocationServices.getFusedLocationProviderClient(context).requestLocationUpdates(mLocationRequest, mLocationCallback, null);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.d("here", "onsuccess");
                        if (location != null) {

                            //set lat/long global variables
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            double[] array = new double[2];
                            array[0] = latitude;
                            array[1] = longitude;
                            Log.d(Double.toString(latitude), Double.toString(longitude));

                            try {
                                //set address global variables
                                setAddress(context, array);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else{

                        }
                    }
                });;

        double[] array2 = new double[2];
        array2[0]=0;
        array2[1]=0;
    return array2;
    }

    /**
     *
     * Sets address global variables based on longitude and latitude
     * @throws IOException
     */
    public List<Address> setAddress(Context context, double[] coordinates) throws IOException {
        Geocoder geocoder;
        geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;

        if (geocoder.isPresent()) {
            addresses = geocoder.getFromLocation(coordinates[0], coordinates[1], 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
           return addresses;
        }

        else{
            addresses = null;
            //address = "";
            //city = "";
            //state = "";
            //country = "";
            //postalCode="";
            //knownName="";
        }

        return addresses;
    }

}
