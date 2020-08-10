package com.example.dp4coruna.mapmanagement;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.example.dp4coruna.R;
import com.example.dp4coruna.location.LocationGrabber;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //LocationObject lo = LocationObject.getLocationObjectWithLocationData(this,this);
        //lo.setup();
        LocationGrabber lo = new LocationGrabber(this, this);
        lo.setupLocation();

        //double latitude = lo.getLatitude();
        //double longitude = lo.getLongitude();

        // Add a marker in Sydney and move the camera
        //LatLng currentlocation = new LatLng(latitude, longitude);
        LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker at Current Location"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        //mMap.addMarker(new MarkerOptions().position(currentlocation).title("Marker at Current Location"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(currentlocation));
    }
}
