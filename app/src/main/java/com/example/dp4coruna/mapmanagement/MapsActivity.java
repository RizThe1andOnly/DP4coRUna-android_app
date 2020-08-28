package com.example.dp4coruna.mapmanagement;

import androidx.fragment.app.FragmentActivity;

import android.location.Address;
import android.os.Bundle;
import android.widget.Toast;

import com.example.dp4coruna.R;
import com.example.dp4coruna.localLearning.location.LocationObject;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener{

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

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

        LocationObject lo = new LocationObject(this, this);
        lo.setupLocation();
        List<Address> addresses = lo.getListOfAddresses();

        double latitude = lo.getLatitude();
        double longitude = lo.getLongitude();

        // Add a marker at the current location and move the camera
        LatLng currentlocation = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(currentlocation).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentlocation));

        //Zoom in on the user's current location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 9.0f));

      //  markHighRiskZones();
        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);
    }

    /**
     * marks high risk zones on the map with polylines
     * need latitude/longitude coordinates
     */
    public void markHighRiskZones(){
    //    mMap.addMarker(new MarkerOptions().position(new LatLng(40.221794, -74.731460)).title("High Risk COVID-19 Zone"));
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .clickable(true)
                .add(
                        new LatLng(40.221794, -74.731460),
                        new LatLng(40.477562, -74.381940),
                        new LatLng(40.367631, -74.266029),
                        new LatLng(40.382412, -74.349752),
                        new LatLng(40.2277747, -74.467500)));

        polygon.setStrokeColor(0xffF69F9E);
        polygon.setTag("High");


    }

    @Override
    public void onPolylineClick(Polyline polyline) {

    }

    @Override
    public void onPolygonClick(Polygon polygon) {
//        Toast.makeText(this, "COVID-19 Risk: " + polygon.getTag().toString(),
  //              Toast.LENGTH_SHORT).show();

    }
}
