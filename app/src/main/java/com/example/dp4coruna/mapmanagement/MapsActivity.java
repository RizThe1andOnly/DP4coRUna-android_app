package com.example.dp4coruna.mapmanagement;

import android.content.Intent;
import android.graphics.Color;
import org.json.*;
import androidx.fragment.app.FragmentActivity;

import android.location.Address;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnCircleClickListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;

    String jsonDirectionsString; //holds response from HTTP request
    String jsonCOVIDData;

    //holds all circle objects which mark covid risk locations, used for clearing map
    ArrayList<Circle> covidClusterCircles = new ArrayList<Circle>();

    //Holds coordinates of walkways/hallways
    //Dummy Data
    ArrayList<Marker> walkwaypoints = new ArrayList<Marker>();

    String destinationStreetAddress;
    String destinationCity;
    String destinationState;
    String destinationZipcode;

    LocationObject lo;

    int numClicks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();

        //get destination info from previous activity
        destinationStreetAddress = intent.getExtras().getString("destinationStreetAddress");
        destinationCity = intent.getExtras().getString("destinationCity");
        destinationState = intent.getExtras().getString("destinationState");
        destinationZipcode = intent.getExtras().getString("destinationZipcode");

        Log.d("DEBUG", destinationStreetAddress);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        numClicks=0;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just zoom in at the user's current location
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //gets user's current location
        lo = new LocationObject(this, this);
        lo.setupLocation();
        List<Address> addresses = lo.getListOfAddresses();

        double latitude = lo.getLatitude();
        double longitude = lo.getLongitude();

        LatLng currentlocation = new LatLng(latitude, longitude);

        mMap.addMarker(new MarkerOptions().position(currentlocation).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentlocation));
        //Zoom in on the user's current location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.10f));

        //Set up listeners once map is ready
        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnCircleClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);

        //sendCOVIDDataRequest();
    }


    /**Method to create url for sending HTTP request to access directions
     * origin is obtained using location object
     * destination is obtained from user's input in previous activity (enterDestinationActivity)
     * see: https://developers.google.com/places/web-service/place-id
     * also: https://developers.google.com/maps/documentation/directions/overview#directions-requests
     * @return String
     */
    public String createStringURL(){

        //This is the secure way to retrieve our API key
        //note: it resolves at run time, so don't worry if there is an error here
        String api_key = getString(R.string.maps_api_key);

        String[] destinationArray = destinationStreetAddress.split(" ", 10);

        //iterate through user input location and add to destination request string
        String destination = "destination=";
        for(int i=0; i<destinationArray.length-1; i++){
            destination=destination + destinationArray[i] + "+";
        }
        destination = destination + destinationArray[destinationArray.length-1];


        String[] originArray = lo.getAddress().split(" ", 10);

        //iterate through location object address and add to origin request string
        String origin = "origin=";
        for(int i=0; i<originArray.length-1; i++){
            origin=origin + originArray[i] + "+";
        }
        origin = origin + originArray[originArray.length-1];


        String url = "https://maps.googleapis.com/maps/api/directions/json?\n" +
                origin + "&" + destination + "\n" +
                "&key=" + api_key + "&alternatives=true";

        //the alternatives line allows for alternative routes to be shown

        return url;
    }

    /**Sends HTTP request for directions
     * direction request return value is a JSON
     * which is temporarily saved as a global variable for later access (jsonDirectionsString)
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
                            List<Route> routes = new ArrayList<Route>();

                            //get values from JSON as ArrayList of Route objects
                            routes = parseJSONintoRoutes(response);

                            if(routes==null){
                                return;
                            }

                            //Add these alternate routes to the Map
                            addRoutePolylines(routes);
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
     * parse into an ArrayList of Route objects
     * where each route object contains details for an alternative route
     * @param response
     * @throws JSONException
     */
    public List<Route> parseJSONintoRoutes(String response) throws JSONException {
        if (response == null) {
            return null;
        }

        List<Route> routes = new ArrayList<Route>();
        JSONObject jsonData = new JSONObject(response);
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");

        //iterate through all possible routes
        for (int i = 0; i < jsonRoutes.length(); i++) {
            JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
            Route route = new Route();

            //get route info from JSON
            JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
            JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
            JSONObject jsonLeg = jsonLegs.getJSONObject(0);
            JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
            JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
            JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
            JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");

            //set attributes for Route object
            route.setDistance(jsonDistance.getString("text"));
            route.setDuration(jsonDuration.getString("text"));
            route.setEndAddress(jsonLeg.getString("end_address"));
            route.setStartAddress(jsonLeg.getString("start_address"));
            route.setStartLocation(new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng")));
            route.setEndLocation(new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng")));
            route.setPoints(PolyUtil.decode(overview_polylineJson.getString("points")));

            //for now, setting risk randomly based on iteration
            route.setRandomRisk(i);

            //add alternate Route to arraylist
            routes.add(route);
        }

        return routes;
    }

    /**Takes an arraylist of Routes, and adds each one to the map using clickable polylines
     * For now, risk/color coding is random
     * note: Polyline tags are set here for the user to obtain route information when clicked later
     * @param routes
     */
    public void addRoutePolylines(List<Route> routes){

        if(routes.isEmpty()){
            return;
        }

        //clear any residual markings on the map
        //mMap.clear();

        //set origin and destination markers
        //these are explicitly from the directions request
        LatLng originCoordinates = routes.get(0).getStartLocation();
        LatLng destinationCoordinates = routes.get(0).getEndLocation();
        mMap.addMarker(new MarkerOptions().position(destinationCoordinates).title(routes.get(0).getEndAddress()));
        mMap.addMarker(new MarkerOptions().position(originCoordinates).title(routes.get(0).getStartAddress()));

        //get center point
        LatLng center = LatLngBounds.builder().include(originCoordinates).include(destinationCoordinates).build().getCenter();

        //zoom in/out camera to include origin, destination markers and the center point between them
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(originCoordinates);
        builder.include(destinationCoordinates);
        builder.include(center); //center point
        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

        //iterate through all alternative routes
        for(int i = 0; i<routes.size(); i++){

            Route route = routes.get(i);
            String risk = route.getRisk();


            Log.i("FromMapActivity",risk + " "+ i);

            if(risk.equals("High")) {
                mMap.addPolyline((new PolylineOptions())
                        .color(Color.RED)
                        .width(10)
                        .clickable(true)
                        .addAll(route.getPoints())).setTag("Risk: " + risk + "\nTime: " + route.getDuration() + "\nDistance: " + route.getDistance());
            }
            if(risk.equals("Medium")) {
                mMap.addPolyline((new PolylineOptions())
                        .color(Color.DKGRAY) //for now just to be able to see it better
                        .width(10)
                        .clickable(true)
                        .addAll(route.getPoints())).setTag("Risk: " + risk + "\nTime: " + route.getDuration() + "\nDistance: " + route.getDistance());
            }

            if(risk.equals("Low")) {
                mMap.addPolyline((new PolylineOptions())
                        .color(Color.GREEN)
                        .width(10)
                        .clickable(true)
                        .addAll(route.getPoints())).setTag("Risk: " + risk + "\nTime: " + route.getDuration() + "\nDistance: " + route.getDistance());
            }
        }
    }

    /**This method is called when the user clicks on a Polyline
     * It displays the Route information obtained from the Polyline Tag as a toast
     * @param polyline
     */
    @Override
    public void onPolylineClick(Polyline polyline) {
        Toast.makeText(this, polyline.getTag().toString(),
                Toast.LENGTH_LONG).show();
    }


    @Override
    public void onPolygonClick(Polygon polygon) {
        Toast.makeText(this, "COVID-19 Risk: " + polygon.getTag().toString(),
                Toast.LENGTH_SHORT).show();

    }

    /**Displays the circles tag when clicked
     * This consists of COVID data including location and active cases
     * @param circle
     */
    @Override
    public void onCircleClick(Circle circle) {
        Toast.makeText(this, circle.getTag().toString(),
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
     * Displays markers tag
     * return value indicates success or failure
     *
     * @param marker
     * @return boolean
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
    if(marker==null){
        return false;
    }
    if(marker.getTag()==null){
        return false;
    }
        Toast.makeText(this, marker.getTag().toString(),
                Toast.LENGTH_SHORT).show();
    return true;
    }
    /*
    /**
     * Called when the user clicks a marker
     * Removes marker from map
     * return value indicates success or failure
     *
     * @param marker
     * @return boolean
     */
    /*
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
*/
    /**
     * Shows all routes from start to destination. Start and destination
     * should be set before calling this method (pressing the show routes button).
     * @param view
     */
    public void showAllRoutes(View view){
        sendDirectionsRequest();
    }

    /**Sends HTTP request for COVID Data
     * This data is updated hourly and contains total number of cases per county
     * Data is collected from WHO, CDC and many others
     * Data obtained from: https://coronavirus-resources.esri.com/datasets/628578697fb24d8ea4c32fa0c5ae1843_0/data?geometry=161.214%2C-0.075%2C23.226%2C52.222&orderBy=Last_Update&orderByAsc=false&selectedAttribute=Active&where=(Confirmed%20%3E%200)
     * return value is a JSON
     * which is temporarily saved as a global variable (JSONcovidData) for later access
     */
    public void sendCOVIDDataRequest() {

        //Instantiate a new Request Queue
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = "https://services1.arcgis.com/0MSEUqKaxRlEPj5g/arcgis/rest/services/ncov_cases_US/FeatureServer/0/query?where=1%3D1&outFields=*&outSR=4326&f=json";

        //Request a response from the URL
        //Will return a JSON formatted string on success
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("JSON", response.substring(0, 10000));
                        jsonCOVIDData = response;
                       if (response==null){
                           return;
                       }

                        try {
                            ArrayList<COVIDCluster> covidClusters = new ArrayList<COVIDCluster>();

                            //get values from JSON as ArrayList of COVID Cluster objects
                            covidClusters = parseJSONintoCOVIDClusters(response);

                            if(covidClusters==null){
                                return;
                            }

                            //add to map
                            addCOVIDClusterstoMap(covidClusters);
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

    /**Given a JSON string in format returned by https://coronavirus-resources.esri.com/datasets/
     * parse into an ArrayList of COVIDCluster objects
     * See: https://coronavirus-resources.esri.com/datasets/628578697fb24d8ea4c32fa0c5ae1843_0/geoservice?geometry=-85.480%2C38.827%2C-71.604%2C41.759&selectedAttribute=Active&where=(Confirmed%20%3E%200)
     * @param response
     * @throws JSONException
     */
    public ArrayList<COVIDCluster> parseJSONintoCOVIDClusters(String response) throws JSONException {
        if (response == null) {
            return null;
        }

       ArrayList<COVIDCluster> covidClusters = new ArrayList<COVIDCluster>();
        JSONObject jsonData = new JSONObject(response);
        JSONArray jsonFeatures = jsonData.getJSONArray("features");

        //iterate through feature array
        for (int i = 0; i < jsonFeatures.length(); i++) {
            JSONObject jsonFeature = jsonFeatures.getJSONObject(i);
            COVIDCluster covidCluster = new COVIDCluster();

            //get feature info from JSON
            JSONObject jsonAttribute = jsonFeature.getJSONObject("attributes");

            //set attributes for CovidCluster Object
            //covidCluster.county = (jsonAttribute.getString("Province_State"));
            covidCluster.setState(jsonAttribute.getString("Province_State"));
            covidCluster.setCountry(jsonAttribute.getString("Country_Region"));
            covidCluster.setLocation(jsonAttribute.getString("Combined_Key"));

            //avoids null pointer when latitude/longitude values in database are "null"
            String latitude = jsonAttribute.getString("Lat");
            String longitude = jsonAttribute.getString("Long_");
            if(latitude!="null" && longitude!="null") {
                LatLng coordinates = (new LatLng(jsonAttribute.getDouble("Lat"), jsonAttribute.getDouble("Long_")));
                    covidCluster.setCoordinates(coordinates);
            }
            covidCluster.setConfirmed(jsonAttribute.getInt("Confirmed"));
            covidCluster.setRecovered(jsonAttribute.getInt("Recovered"));
            covidCluster.setDeaths(jsonAttribute.getInt("Deaths"));
            covidCluster.setActive(jsonAttribute.getInt("Active"));
            covidCluster.setRiskLevel();
            Log.d("JSON", covidCluster.getLocation());


            //add alternate covidCluster to arraylist
            covidClusters.add(covidCluster);
        }

        return covidClusters;
    }

    /*
     */
    public void addCOVIDClusterstoMap(List<COVIDCluster> COVIDClusters){

        if(COVIDClusters==null){
            return;
        }

        if(COVIDClusters.isEmpty()){
            return;
        }

        //Iterate through list of covid clusters
        for(int i = 0; i<COVIDClusters.size(); i++){

            COVIDCluster COVIDcluster = COVIDClusters.get(i);

           if(COVIDcluster.getCoordinates()!=null) {


               CircleOptions circleOptions = new CircleOptions();
               circleOptions.center(COVIDcluster.getCoordinates());
               circleOptions.radius(4000);

               //hish risk, red
               if(COVIDcluster.getRisklevel()=="High") {
                   circleOptions.strokeWidth(1);
                   circleOptions.strokeColor(0x46FF0000);
                   circleOptions.fillColor(0x46FF0000);
               }
               //medium risk, yellow
               else if(COVIDcluster.getRisklevel()=="Medium") {
                   circleOptions.strokeWidth(1);
                   circleOptions.strokeColor(0x467F8000);
                   circleOptions.fillColor(0x467F8000);
               }
               else{ //low risk, blue
                   circleOptions.strokeWidth(1);
                   circleOptions.strokeColor(0x460000FF);
                   circleOptions.fillColor(0x460000FF);
               }



               circleOptions.clickable(true);


               //Add circle to the map, with tag
               Circle circle = mMap.addCircle(circleOptions);
               circle.setTag((COVIDcluster.getLocation() +
                       "\n\nCOVID-19:" +
                       "\nActive Cases: " + COVIDcluster.getActive() +
                       "\nDeaths: " + COVIDcluster.getDeaths() +
                       "\nTotal Cases: " + COVIDcluster.getConfirmed()));

               //add circle to arraylist, so we can remove from map later
               covidClusterCircles.add(circle);

           }
        }
    }


    /**Called when "Show Risk Zones" Button is clicked
     * Calls sendCOVIDDataRequest() to obtain COVID data and mark up map
     * Note: This method will take a couple seconds to appear on the map
     * It needs to run through a LOT of data
     * @param view
     */
    public void showRiskZones(View view) {
        numClicks++;
        if(numClicks%2==0){
            if (!(covidClusterCircles.isEmpty())){

                //clears all covid circles from map
                covidClusterCircles.forEach((n) -> n.remove());
            }

        }
        else {
            sendCOVIDDataRequest();
        }
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
