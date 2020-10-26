package com.example.dp4coruna.mapmanagement;

import org.json.*;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnCircleClickListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;

    //Holds coordinates of walkways/hallways
    ArrayList<Marker> walkwaypoints = new ArrayList<Marker>();

    //Holds coordinates of high risk locations
    //Dummy data for now
    ArrayList<LatLng> highriskcoordinates = new ArrayList<LatLng>();

    String jsonDirectionsString;

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
     * we just zoom in at Rutgers
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //This segment is only necessary for getting the users current location on the map
        //We will eventually need this
        /*
        LocationObject lo = new LocationObject(this, this);
        lo.setupLocation();
        List<Address> addresses = lo.getListOfAddresses();

        double latitude = lo.getLatitude();
        double longitude = lo.getLongitude();
    */

        /*
        //MALL EXAMPLE
        //Menlo Park Mall Coordinates
        double latitude = 40.5478735;
        double longitude = -74.335214;

        // Add a marker at the current location and move the camera
        LatLng currentlocation = new LatLng(latitude, longitude);
        //mMap.addMarker(new MarkerOptions().position(currentlocation).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentlocation));

        //Zoom in on the user's current location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17.10f));

        //These just show an example of what is possible
        //showCircularRiskZones();
        //markHighRiskZones();
        //showMallHighRiskZones();
        //addHighRiskCoordinates();
        //highriskcoordinates.forEach((n) -> drawCircularZone(n, 10));
        //drawCircularZone( new LatLng(40.547242016103894, -74.334968291223305), 10);
         */

        //Rutgers Example
        double latitude = 40.511737;
        double longitude = -74.444011;

        //move the camera
        LatLng rutgers = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(rutgers));
        //Zoom in
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 13.10f));


        //Set up listeners once map is ready
        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnCircleClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);

        sendDirectionsRequest();
    }


    /**Method to create url for sending HTTP request to access directions
     * Right now, origin and destination are hard coded using place IDs
     * see: https://developers.google.com/places/web-service/place-id
     * also: https://developers.google.com/maps/documentation/directions/overview#directions-requests
     * Note: * We will eventually alter this so the user's route request from submitLocationLabel
     *      * can be fed as a parameter to this function
     * @return String
     */
    public String createStringURL(){

        //This is the secure way to retrieve our API key
        //note: it resolves at run time, so don't worry if there is an error here
        String api_key = getString(R.string.maps_api_key);

        String origin = "origin=place_id:ChIJe9PW7UPHw4kRsY6sdgb2l-U";
        String destination = "destination=place_id:ChIJCfpC8GrGw4kRWzKTC3RIe58";

        String url = "https://maps.googleapis.com/maps/api/directions/json?\n" +
                origin + "&" + destination + "\n" +
                "&key=" + api_key + "&alternatives=true";

        //the alternatives line allows for alternative routes to be shown

        return url;
    }

    /**Sends HTTP request for directions
     * direction request return value is a JSON
     * which is temporarily saved as a global variable for later access
     */
    public void sendDirectionsRequest() {

        //Instantiate a new Request Queue
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = createStringURL();

        //Request a response from the URL
        //Will return a JSON formatted string on success
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("JSON", response.substring(0, 10000));
                        jsonDirectionsString = response;
                        try {
                            parseJSONintoPolylines(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("JSON", "Unable to retrieve response");
            }
        });

        //add to the request queue
        queue.add(stringRequest);

    }


    /**Given a JSON string in format returned by Google Directions API
     * parse into different possible routes, and add to map as polylines
     * This function will need to be broken into two later
     * @param jsonString
     * @throws JSONException
     */
    protected void parseJSONintoPolylines(String jsonString) throws JSONException {
        try {

            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArrayRoute = jsonObject.getJSONArray("routes");
            int possibleroutes = jsonArrayRoute.length();

            //iterate through all possible routes
            for (int i = 0; i < possibleroutes; i++) {
                JSONObject jsonObjectRoute = jsonArrayRoute.getJSONObject(i);
                JSONArray jsonArrayLegs = jsonObjectRoute.getJSONArray("legs");

                //for each possible route, iterate through legs
                for (int j = 0; j < jsonArrayLegs.length(); j++) {
                    JSONObject jsonObjectLegs = jsonArrayLegs.getJSONObject(j);
                    JSONArray jsonArraySteps = jsonObjectLegs.getJSONArray("steps");
                    String[] polylineArray = new String[jsonArraySteps.length()];

                    //for each leg, iterate through points
                    for (int k = 0; k < jsonArraySteps.length(); k++) {
                        JSONObject jsonObjectSteps = jsonArraySteps.getJSONObject(k);
                        JSONObject jsonObjectLegPolyline = jsonObjectSteps.getJSONObject("polyline");
                        String polygon = jsonObjectLegPolyline.getString("points");
                        polylineArray[k] = polygon;
                    }
                    int count2 = polylineArray.length;
                    for (int l = 0; l < count2; l++) {

                        //Add route 1 to map
                        if(i==0) {
                            mMap.addPolyline((new PolylineOptions())
                                    .color(Color.GREEN)
                                    .width(10)
                                    .clickable(true)
                                    .addAll(PolyUtil.decode(polylineArray[l])));
                        }
                        //Add route 2 to map
                        if(i==1) {
                            mMap.addPolyline((new PolylineOptions())
                                    .color(Color.RED)
                                    .width(10)
                                    .clickable(true)
                                    .addAll(PolyUtil.decode(polylineArray[l])));
                        }
                    }
                }
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * marks high risk zones on the map with polygons
     * need latitude/longitude coordinates of 3 or more locations
     * The longitudes and latitudes currently in the method are for example only
     */
    public void markHighRiskZones() {
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

    /**
     * Example showing how to outline risk zones given four coordinates
     * Marks up the map
     */
    public void showMallHighRiskZones() {
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

    /**
     * Called when the user clicks the map
     * Adds a marker to the location the user clicked
     * This can later be altered to save walkway coordinates
     *
     * @param clickCoordinates
     */
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

        Toast.makeText(this, "Latitude: " + latitude + "\nLongitide:" + longitude,
                Toast.LENGTH_SHORT).show();

    }

    /**
     * Called when the user clicks a marker
     * Removes marker from map
     * return value indicates success or failure
     *
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
    public void onMarkerDrag(Marker marker) {

    }

    //Called when a marker has finished being dragged.
    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    /**
     * Method which will mark up the map with risk zones
     * given a single coordinate, and a radius
     *
     * @param point
     * @param radius
     */

    private void drawCircularZone(LatLng point, int radius) {

        CircleOptions circleOptions = new CircleOptions();

        //Set point and radius
        circleOptions.center(point);
        circleOptions.radius(radius);

        //format options
        circleOptions.strokeWidth(1);
        circleOptions.strokeColor(0x1EFF0000);
        circleOptions.fillColor(0x1EFF0000);

        circleOptions.clickable(true);

        // Adding the circle to the GoogleMap
        mMap.addCircle(circleOptions);

    }

    private void addHighRiskCoordinates() {
        highriskcoordinates.add(new LatLng(40.54648026040382, -74.3351248651743));
        highriskcoordinates.add(new LatLng(40.547816511745154, -74.33590237051249));
    }


    /**
     * Marks up the map with circular risk zones
     * using dummy data
     */
    private void showCircularRiskZones() {
        drawCircularZone(new LatLng(40.54648026040382, -74.3351248651743), 10);
        drawCircularZone(new LatLng(40.547816511745154, -74.33590237051249), 20);
        drawCircularZone(new LatLng(40.54724940432747, -74.33537866920233), 25);
        drawCircularZone(new LatLng(40.54996694148023, -74.33652564883232), 15);
    }

    /**
     * Save Points for now just clears the map of markers
     * This can be altered later to save walkway/hallway coordinates
     *
     * @param view
     */
    public void savePointsClicked(View view) {
        Toast.makeText(this, "Saving Points...",
                Toast.LENGTH_SHORT).show();

        //clears all markers from map
        walkwaypoints.forEach((n) -> n.remove());

        //clears entire map, including overlay
        // mMap.clear();
    }



}
