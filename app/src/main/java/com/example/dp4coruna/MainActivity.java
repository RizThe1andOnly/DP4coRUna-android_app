package com.example.dp4coruna;

import android.Manifest;
import android.content.Context;
<<<<<<< Updated upstream
=======
import android.content.Intent;
>>>>>>> Stashed changes
import android.content.pm.PackageManager;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
<<<<<<< Updated upstream
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
=======
import android.os.Parcelable;
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
>>>>>>> Stashed changes

public class MainActivity extends AppCompatActivity {

    private static final int ACCESS_FINE_LOCATION_REQUEST_CODE = 101;
<<<<<<< Updated upstream
=======
    private FusedLocationProviderClient fusedLocationClient;

    double longitude;
    double latitude;

    List<Address> addresses;

    String address;
    String city;
    String state;
    String country;
    String postalCode;
    String knownName;
>>>>>>> Stashed changes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkForPermissions(getApplicationContext());
<<<<<<< Updated upstream
=======
        getLocation();

>>>>>>> Stashed changes
    }


    /**
     * Checks for permission at the start of the app.
     * Currently permission being chekced is:
     *      - Access Location Coarse
     *
     */
    private void checkForPermissions(Context context){
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                                ACCESS_FINE_LOCATION_REQUEST_CODE);
        }
<<<<<<< Updated upstream
=======

    }

    /**
     * Requests location information from phone using Google API
     * Sets global variables: latitude, longitude and address
     *
     */
   private void getLocation(){

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
                   return; //no location found, exit
               }
               for (Location location : locationResult.getLocations()) {
                   if (location != null) {
                   }
               }
           }
       };

       //updates location to what we have just stored in mLocationRequest
       LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, mLocationCallback, null);


       fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
       fusedLocationClient.getLastLocation()
               .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                   @Override
                   public void onSuccess(Location location) {

                       if (location != null) {

                           //set lat/long global variables
                           latitude = location.getLatitude();
                           longitude = location.getLongitude();

                           try {
                               //set address global variables
                               setAddress();
                           } catch (IOException e) {
                               e.printStackTrace();
                           }
                       }
                       else{
                           Toast toast= Toast.makeText(getApplicationContext(),
                                   "location null", Toast.LENGTH_LONG);
                           toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                           toast.show();
                       }
                   }
               });;
   }

    /**
     *
     * Sets address global variables based on longitude and latitude
     * @throws IOException
     */
    public void setAddress() throws IOException {
        Geocoder geocoder;
        geocoder = new Geocoder(this, Locale.getDefault());

        if (geocoder.isPresent()) {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            address = addresses.get(0).getAddressLine(0);
            city = addresses.get(0).getLocality();
            state = addresses.get(0).getAdminArea();
            country = addresses.get(0).getCountryName();
            postalCode = addresses.get(0).getPostalCode();
            knownName = addresses.get(0).getFeatureName();
        }

        else{
            addresses = null;
            address = "";
            city = "";
            state = "";
            country = "";
            postalCode="";
            knownName="";
        }

>>>>>>> Stashed changes
    }


    public void enterLocationData(View view) {
        Bundle bundle = new Bundle();
        Intent intent = new Intent(this, SubmitLocationLabel.class);
        bundle.putParcelableArrayList("addresses", (ArrayList<? extends Parcelable>) addresses);
        intent.putExtras(bundle);
        startActivity(intent);
    }

}
