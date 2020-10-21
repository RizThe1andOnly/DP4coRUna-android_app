package com.example.dp4coruna.mapmanagement;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.dp4coruna.R;
import com.example.dp4coruna.localLearning.location.LocationObject;
import com.example.dp4coruna.reportPositiveTestActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnCircleClickListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener{

    private GoogleMap mMap;
    ArrayList<Marker> walkwaypoints = new ArrayList<Marker>();
    ArrayList<LatLng> highriskcoordinates = new ArrayList<LatLng>();

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
     * we just add a marker near Menlo Park Mall
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

        //double latitude = lo.getLatitude();
        //double longitude = lo.getLongitude();

        //Menlo Park Mall Coordinates
        double latitude = 40.5478735;
        double longitude = -74.335214;

        // Add a marker at the current location and move the camera
        LatLng currentlocation = new LatLng(latitude, longitude);
        //mMap.addMarker(new MarkerOptions().position(currentlocation).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentlocation));

        //Zoom in on the user's current location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17.10f));

      //markHighRiskZones();
        //showMallHighRiskZones();
        //addHighRiskCoordinates();
        showCircularRiskZones();
        //highriskcoordinates.forEach((n) -> drawCircularZone(n, 10));
        //drawCircularZone( new LatLng(40.547242016103894, -74.334968291223305), 10);
        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnCircleClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);
    }

    /**
     * marks high risk zones on the map with polylines
     * need latitude/longitude coordinates
     * The longitudes and latitudes currently in the method are for example only
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

    public void showMallHighRiskZones(){
        //    mMap.addMarker(new MarkerOptions().position(new LatLng(40.221794, -74.731460)).title("High Risk COVID-19 Zone"));
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .clickable(true)
                .add(
                        new LatLng(40.547242016103894, -74.334968291223305),
                        new LatLng(40.547291695522524, -74.33552954345943),
                        new LatLng(40.546626243020114, -74.33567404747009),
                        new LatLng(40.54648026040382, -74.3351248651743)));

        Polygon polygon2 = mMap.addPolygon(new PolygonOptions()
                .clickable(true)
                .add(
                        new LatLng(40.54778492098206, -74.33563180267811),
                        new LatLng(40.547638686126504, -74.33565694838762),
                        new LatLng(40.54768352472244, -74.33592885732651),
                        new LatLng(40.547816511745154, -74.33590237051249)));

        polygon.setStrokeColor(0xffF69F9E);
        polygon.setFillColor(0x1EFF0000);
        polygon.setTag("High");

        polygon2.setStrokeColor(0xffF69F9E);
        polygon2.setFillColor(0x1EFF0000);
        polygon2.setTag("High");
    }

    @Override
    public void onPolylineClick(Polyline polyline) {

    }

    @Override
    public void onPolygonClick(Polygon polygon) {
        Toast.makeText(this, "COVID-19 Risk: " + polygon.getTag().toString(),
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onCircleClick(Circle circle) {
        Toast.makeText(this, "COVID 19 Risk: High",
                Toast.LENGTH_SHORT).show();

        }

    @Override
    public void onMapClick(LatLng clickCoordinates) {
        double latitude = clickCoordinates.latitude;
        double longitude = clickCoordinates.longitude;

        //set marker attributes
        MarkerOptions pointmarker = new MarkerOptions();
        pointmarker.position(clickCoordinates);
        pointmarker.icon(BitmapDescriptorFactory.defaultMarker(270));

        //add marker to map
        Marker marker = mMap.addMarker(pointmarker);
        walkwaypoints.add(marker);

        Toast.makeText(this, "Latitude: " + latitude + "\nLongitide:"+  longitude,
                Toast.LENGTH_SHORT).show();

    }

    /**Called when the user clicks a marker
     * return value indicates success or failure
     * @param marker
     * @return boolean
     */
    @Override
    public boolean onMarkerClick(Marker marker) {

        if (marker != null) {
            //remove coordinates from arraylist
            walkwaypoints.remove(marker);

            //clears the marker the user clicked
            marker.remove();

            return true;
        }

        //remove coordinates from arraylist
        //walkwaypoints.remove(marker);

        //clears the marker the user clicked
        //marker.remove();

        //return true;

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }

    //Called at beginning of Marker Drag
    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    //Called repeatedly while marker is being dragged
    @Override
    public void onMarkerDrag(Marker marker){

    }
    //Called when a marker has finished being dragged.
    @Override
    public void onMarkerDragEnd(Marker marker){

    }



    private void drawCircularZone(LatLng point, int radius){

        // Instantiating CircleOptions to draw a circle around the marker
        CircleOptions circleOptions = new CircleOptions();

        // Specifying the center of the circle
        circleOptions.center(point);

        // Radius of the circle
        circleOptions.radius(radius);

        // Border color of the circle
        //circleOptions.strokeColor(Color.BLACK);

        // Fill color of the circle
        //circleOptions.fillColor(0x30ff0000);

        // Border width of the circle
        circleOptions.strokeWidth(1);
        circleOptions.strokeColor(0x1EFF0000);
        circleOptions.fillColor(0x1EFF0000);

        circleOptions.clickable(true);

        // Adding the circle to the GoogleMap
        mMap.addCircle(circleOptions);

    }

    private void addHighRiskCoordinates(){
        highriskcoordinates.add(new LatLng(40.54648026040382, -74.3351248651743));
        highriskcoordinates.add(new LatLng(40.547816511745154, -74.33590237051249));
    }

    private void showCircularRiskZones(){
        drawCircularZone(new LatLng(40.54648026040382, -74.3351248651743), 10);
        drawCircularZone(new LatLng(40.547816511745154, -74.33590237051249), 20);
        drawCircularZone(new LatLng(40.54724940432747, -74.33537866920233), 25);
        drawCircularZone(new LatLng(40.54996694148023, -74.33652564883232), 15);
       // drawCircularZone(new LatLng(40.547816511745154, -74.33590237051249), 10);
       // drawCircularZone( new LatLng(40.547242016103894, -74.334968291223305), 10);
       // drawCircularZone( new LatLng(40.547242016103894, -74.334968291223305), 10);

    }

    public void savePointsClicked(View view){
        Toast.makeText(this, "Saving Points...",
                Toast.LENGTH_SHORT).show();

        //clears all markers from map
        walkwaypoints.forEach((n) -> n.remove());

        //clears entire map, including overlay
       // mMap.clear();
    }


}
