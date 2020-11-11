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

    //Holds coordinates of walkways/hallways
    //Dummy Data
    ArrayList<Marker> walkwaypoints = new ArrayList<Marker>();

    //Holds coordinates of high risk locations
    //Dummy data for now
    ArrayList<LatLng> highriskcoordinates = new ArrayList<LatLng>();

    String destinationStreetAddress;
    String destinationCity;
    String destinationState;
    String destinationZipcode;

    LocationObject lo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();

        //get info from previous activity
        String originStreetAddress = intent.getExtras().getString("originStreetAddress");
        String originCity = intent.getExtras().getString("originCity");
        String originState = intent.getExtras().getString("originState");
        String originZipcode = intent.getExtras().getString("originZipcode");

        destinationStreetAddress = intent.getExtras().getString("destinationStreetAddress");
        destinationCity = intent.getExtras().getString("destinationCity");
        destinationState = intent.getExtras().getString("destinationState");
        destinationZipcode = intent.getExtras().getString("destinationZipcode");

        Log.d("DEBUG", destinationStreetAddress);

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

        //gets current location
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
/*
        //Rutgers Example
        double latitude = 40.511737;
        double longitude = -74.444011;

        //move the camera
        LatLng rutgers = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(rutgers));
        //Zoom in
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 13.10f));
*/

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
        //these are explicity from the directions request
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
    public void onPolygonClick(Polygon polygon) {
        Toast.makeText(this, "COVID-19 Risk: " + polygon.getTag().toString(),
                Toast.LENGTH_SHORT).show();

    }

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
                        jsonDirectionsString = response;
                       if (response==null){
                           return;
                       }

                        try {
                            List<COVIDCluster> covidClusters = new ArrayList<COVIDCluster>();

                            //get values from JSON as ArrayList of Route objects
                            covidClusters = parseJSONintoCOVIDClusters(response);

                            if(jsonDirectionsString==null){
                                return;
                            }

                            //Add these alternate routes to the Map
                            //addRoutePolylines(routes);
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
    public List<COVIDCluster> parseJSONintoCOVIDClusters(String response) throws JSONException {
        if (response == null) {
            return null;
        }

        List<COVIDCluster> covidClusters = new ArrayList<COVIDCluster>();
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

               // Add circle to the map, with Tag
               mMap.addCircle(circleOptions).setTag(COVIDcluster.getLocation() +
                       "\n\nCOVID-19:" +
                       "\nActive Cases: " + COVIDcluster.getActive() +
                       "\nDeaths: " + COVIDcluster.getDeaths() +
                       "\nTotal Cases: " + COVIDcluster.getConfirmed());
           }
        }
    }


    /**Called when "Show Risk Zones" Button is clicked
     * Calls sendCOVIDDataRequest() to obtain COVID data and mark up map
     * @param view
     */
    public void generatePointsClicked(View view) {
        sendCOVIDDataRequest();
        //        Toast.makeText(this, "Saving Points...",
        //              Toast.LENGTH_SHORT).show();

        /*
        //LatLng points to outline area where we will place points
        double latitudeUpperLeft = 40.53714868695067;
        double longitudeUpperLeft = -74.4669172167778;
        LatLng upperLeft = new LatLng(latitudeUpperLeft, longitudeUpperLeft);

        double latitudeUpperRight = 40.5360007406821426;
        double longitudeUpperRight = -74.42061759531498;
        LatLng upperRight = new LatLng(latitudeUpperRight, longitudeUpperRight);

        double latitudeLowerRight = 40.47359397014704;
        double longitudeLowerRight = -74.42278549075127;
        LatLng lowerRight = new LatLng(latitudeLowerRight, longitudeLowerRight);

        double latitudeLowerLeft = 40.475973509889535;
        double longitudeLowerLeft = -74.46631204336882;
        LatLng lowerLeft = new LatLng(latitudeLowerLeft, longitudeLowerLeft);

        //create nonclickable, transparent polygon on map outlining area of interest
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                //.strokeColor((0xaa222345))
                .strokeColor((0xffF69F9E))
                .clickable(false)
                .add(upperLeft, upperRight, lowerRight, lowerLeft));

        //Long_point >  Long_LowerLeft + Lat_point * ( Long_UpperLeft - Long_LowerLeft)/ ( Lat_UpperLeft - Lat_LowerLeft)


        double randomlatitude = ThreadLocalRandom.current().nextDouble(40.473, 40.54);
        double randomlongitude = ThreadLocalRandom.current().nextDouble(-74.4669, -74.420);

        LatLng randomlatlong = new LatLng(randomlatitude, randomlongitude);
        //mMap.addMarker(new MarkerOptions().position(randomlatlong));

        CircleOptions circleOptions = new CircleOptions();

        //Set point and radius
        circleOptions.center(randomlatlong);
        circleOptions.radius(70);

        //format options
        circleOptions.strokeWidth(1);
        circleOptions.strokeColor(0x46FF0000);
        circleOptions.fillColor(0x46FF0000);

        circleOptions.clickable(true);

        // Adding the circle to the GoogleMap
        mMap.addCircle(circleOptions);



        //clears entire map, including overlay
        // mMap.clear();

         */
    }

    public void generateRandomLeftHeavy(){

        //generate
        for(int i=0; i<10; i++){
            double randomlatitude = ThreadLocalRandom.current().nextDouble(40.47057, 40.53168);
            double randomlongitude = ThreadLocalRandom.current().nextDouble(-74.46058, -74.4239);

            LatLng randomlatlong = new LatLng(randomlatitude, randomlongitude);
            //mMap.addMarker(new MarkerOptions().position(randomlatlong));

            CircleOptions circleOptions = new CircleOptions();

            //Set point and radius
            circleOptions.center(randomlatlong);
            circleOptions.radius(70);

            //format options
            circleOptions.strokeWidth(1);
            circleOptions.strokeColor(0x46FF0000);
            circleOptions.fillColor(0x46FF0000);

            circleOptions.clickable(true);

            // Adding the circle to the GoogleMap
            mMap.addCircle(circleOptions);
        }
    }

    public void generateRandomMidHeavy(){

    }

    public void generateRandomRightHeavy(){

    }




}
