package com.example.dp4coruna.mapmanagement;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.*;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.dp4coruna.dataManagement.AppDatabase;
import com.example.dp4coruna.dataManagement.databaseDemoActivity;
import com.example.dp4coruna.mapmanagement.MapDataStructures.AreaLabel;
import com.example.dp4coruna.localLearning.location.learner.LocationGrabber;
import com.example.dp4coruna.mapmanagement.MapDataStructures.Route;
import com.example.dp4coruna.mapmanagement.indoorRouting.*;
import com.example.dp4coruna.mapmanagement.indoorRouting.DataStructures.Neighbor;
import com.example.dp4coruna.mapmanagement.MapModel.COVIDCluster;
import com.example.dp4coruna.mapmanagement.MapModel.MapActivityModel;
import com.example.dp4coruna.utilities.AddressDialog;
import com.example.dp4coruna.utilities.DialogCallBack;
import org.json.*;
import androidx.fragment.app.FragmentActivity;

import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dp4coruna.R;
import com.example.dp4coruna.localLearning.location.LocationObject;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.io.*;
import java.util.*;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnCircleClickListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, AdapterView.OnItemSelectedListener,
        DialogCallBack {

    private GoogleMap mMap;

    String jsonDirectionsString; //holds response from HTTP request
    String jsonCOVIDData;

    //holds all circle objects which mark covid risk locations, used for clearing map
    public static ArrayList<Circle> covidClusterCircles = new ArrayList<Circle>();
    public static HashMap<Circle, COVIDCluster> hashmap = new HashMap<>();

    //Holds coordinates of walkways/hallways
    //Dummy Data
    public static ArrayList<Circle> dangerSpots = new ArrayList<>();

    public static final int ROUTE_SRC_DEST_RESULTS = 0;
    private boolean fromCurrentLocation = true;

    ArrayList<Marker> walkwaypoints = new ArrayList<Marker>();

    String destinationStreetAddress;
    String destinationCity;
    String destinationState;
    String destinationZipcode;

    LocationObject lo;

    int numClicks;

    private List<AreaLabel> currentBuildingAreas;
    private AppDatabase classAd;

    private MapActivityModel mm;

    /*
        From Riz:
        Variables for certain location detection functionalities and view objects
     */


    private boolean lockmap = true;
    private Spinner optionSpinner;

    /*  DROP DOWN OPTIONS
       Options For the spinner. These have code associated with them but won't be shown for demo purposes.
       When necessary copy the below into optionNames.

           "Detect",
           "Show Risk Zones",
           "GPS",
           "Train",
           "Sample",
           "UnlockMap",
           "LockMap",
           "GetRoute",
           "Add DB Dummy Data",
           "Show User Locations",
           "Show DB Contents",
           "Report Positive Test: User 1",
           "Report Positive Test: User 2",
           "Create Area Labels",
           "Print Area Labels"
    */
    private String[] optionNames = {
            "Detect",
            "Zoom To Current",
            "Update Map",
            "Zoom To Mall",
            "Set Indoor Points",
            "Display Indoor Routes",
            "Display Routes(Random)",
            "GetRoute(Trained)",
            "Show Risk Zones"
    };
    private String optionToRun;
    // * Class constants:
    private final int SUBMIT_MAP_LABEL = 0;
    private final int SUBMIT_LOCATION_FEATURES = 1;
    private final int SUBMIT_DESTINATION = 2;
    // * Class Variables
    private Context activityContext;
    private boolean trainingMode = false;
    //latitude and longitude of selected point (for training purposes)
    private double classVar_latitude;
    private double classVar_longitude;
    // Handler for marker placement
    private Handler markerPlacement;
    //map marker container:
    private Map<AreaLabel,Marker> markerContainer;
    private Marker current_marker = null;

    //indoor routing variables: (set src and destination)
    private String indoorRouting_srcnode = null;
    private String indoorRouting_destnode = null;
    private int indoorRouting_setState = 0;
    private boolean enableIndoorRoutingProcedures = false;
    private AreaLabel indoorStartingAL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

//        Intent intent = getIntent();
//
//        //get destination info from previous activity
//        destinationStreetAddress = intent.getExtras().getString("destinationStreetAddress");
//        destinationCity = intent.getExtras().getString("destinationCity");
//        destinationState = intent.getExtras().getString("destinationState");
//        destinationZipcode = intent.getExtras().getString("destinationZipcode");

        //Log.d("DEBUG", destinationStreetAddress);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        this.activityContext = getApplicationContext();
        setSpinnerView();

        this.classAd = new AppDatabase(getApplicationContext());

        numClicks=0;
    }

    /**
     * Event triggered when user presses the run button.
     * What happens depends on the option selected in the spinner/dropdown item.
     *
     * When new functionality added, update the optionNames array with the new functionality name,
     * then update the select case below with the same name and the actual method associated with that
     * function.
     *  - Can transfer over button event methods (and all the methods associated with it) and call the
     *  button method from here instead of creating a new button on the screen. Just pass the view
     *  var into the method called from the switch block.
     * @param view
     */
    public void runEvent(View view){
        switch ((this.optionToRun)){
            case "Display Routes(Random)" : showAllRoutes(view); break;
            case "Show Risk Zones" : showRiskZones(view); break;
            case "GPS" : moveToCurrent_train(view); break;
            case "Detect" : detectLocation(view); break;
            case "Train" : setTrainingMode(view); break;
            case "Sample" : sampleButtonEvent(view); break;
            case "UnlockMap" : this.lockmap = false; break;
            case "LockMap" : this.lockmap = true; break;
            case "GetRoute(Trained)" : getDirections(); break;
            case "Add DB Dummy Data" : addDBDummyData(); break;
            case "Show DB Contents" : showDBContents(view); break;
            case "Show User Locations" : showUserLocations(); break;
            case "Report Positive Test: User 1" : demoUser1(); break;
            case "Report Positive Test: User 2" : demoUser2(); break;
            case "Display Indoor Routes" : indoorRouting();break;
            case "Set Indoor Points" : enableIndoorRoutingProcedures = true;break;
            case "Create Area Labels" : createAreaLabels(this); break;
            case "Print Area Labels" : printAreaLabels(); break;
            case "Zoom To Mall" : zoomToMall();break;
            case "Update Map" : updateMap();break;
            case "Zoom To Current" : zoomToCurrentGPSLocation();break;
            default: Toast.makeText(getApplicationContext(),"No Function for " + (this.optionToRun),Toast.LENGTH_LONG).show();break;
        }
    }

    /**
     * Zooms in to the gps generated coordinates. This is to make things a bit easier to mark locations.
     */
    private void zoomToCurrentGPSLocation(){
        LocationGrabber lg = new LocationGrabber(getApplicationContext());
        lg.setupLocation();
        LatLng currentloc = new LatLng(lg.getLatitude(),lg.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentloc, 18.10f));
    }


    /**
     *  Updates the map with new server data. This is to account for changes in server by other users.
     */
    private void updateMap(){
        (this.mm).updateMap();
    }


    /**
     * Zoom to the mall datapoints
     */
    private void zoomToMall(){
        this.mm.zoomToMall();
    }

    /**Method to demo:
     * querying the MySQL database,
     * parsing the returned strings and using them to
     * create a List of AreaLabel objects with the appropriate fields
     * @param context
     */
    public void createAreaLabels(Context context){
       AreaLabel.getQueryResults(context, "SELECT * FROM userLocation");
    }


    /**Method to demo the Area Label Objects set from createAreaLabel
     * This just runs through the list and prints the fields in the Log
     */
    public void printAreaLabels(){
    Log.d("AreaLabelFeat", "in printAreaLabels");

        List<AreaLabel> arealabellist = new ArrayList<>();
        arealabellist =  AreaLabel.getAreaLabels();

        for(AreaLabel a : arealabellist) {
            Log.i("AreaLabelFeat", Double.toString(a.latitude) + " " + Double.toString(a.longitude) + " "
            + a.date + " " + a.building + " " + a.area+ " " + a.county + " " + a.numCovidCases + "\n");
        }
    }

    /**For Demo:
     * Shows the database with some dummy data
     * Click "Add Dummy Data" first or tables will be empty
     * @param view
     */
    public void showDBContents(View view){
        Bundle bundle = new Bundle();
        Intent intent = new Intent(this, databaseDemoActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**For Demo:
     * Places markers on the locations of users
     * found in database query
     */
    public void showUserLocations(){
        AppDatabase ad = new AppDatabase(getApplicationContext());
        List<LatLng> coords = ad.getUserLocations();

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.54733449622285, -74.33499678969381), 17.10f));

        for(int i=0; i<coords.size(); i++){
            mMap.addMarker(new MarkerOptions().position(coords.get(i)));
        }
        Log.i("RiskTable", Integer.toString(coords.size()));
    }


    /**For Demo:
     * Changes value in DB for User #1 from "positive" to "negative"
     * Change is reflected in "Show DB Contents"
     * Marks these locations on map with danger circles
     */
    public void demoUser1(){
        //clear map
        mMap.clear();
        dangerSpots.forEach((n) -> n.remove());

        //update user 1 in database to "positive"
        AppDatabase ad = new AppDatabase(getApplicationContext());
        ad.updateUserCovidRisk("negative", "positive", 1);

        //query DB for all positive locations
        List<LatLng> coords = ad.getHighRiskLocations();

        //Mark up map with danger circles
        for(int i=0; i<coords.size(); i++){
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(coords.get(i));
            circleOptions.radius(5);
            circleOptions.strokeWidth(1);
            circleOptions.strokeColor(0x46FF0000);
            circleOptions.fillColor(0x46FF0000);
            circleOptions.clickable(true);
            Circle circle = mMap.addCircle(circleOptions);
            circle.setTag("User 1");
            dangerSpots.add(circle);
        }

    }

    /**For Demo:
     * Changes value in DB for User #2 from "positive" to "negative"
     * Change is reflected in "Show DB Contents"
     * Marks these locations on map with danger circles
     */
    public void demoUser2(){
        mMap.clear();
        dangerSpots.forEach((n) -> n.remove());

        AppDatabase ad = new AppDatabase(getApplicationContext());
        ad.updateUserCovidRisk("negative", "positive", 2);
        List<LatLng> coords = ad.getHighRiskLocations();

        for(int i=0; i<coords.size(); i++){
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(coords.get(i));
            circleOptions.radius(5);
            circleOptions.strokeWidth(1);
            circleOptions.strokeColor(0x46FF0000);
            circleOptions.fillColor(0x46FF0000);
            circleOptions.clickable(true);
            Circle circle = mMap.addCircle(circleOptions);
            circle.setTag("User 2");
            dangerSpots.add(circle);
        }
    }


    /**Adds some dummy data to all three tables in DB for demo
     * By default, dummy users are set to be "negative" for covid
     */
    public void addDBDummyData(){
        dangerSpots.forEach((n) -> n.remove());

        AppDatabase ad = new AppDatabase(getApplicationContext());
        ad.addUserTuple(0, "negative");
        ad.addUserTuple(1, "negative");
        ad.addUserTuple(2, "negative");
        ad.getUserTableContents();

        ad.addUserLocationTuple(40.549556289512, -74.33660745620728, "2020-12-03", "Menlo Mall", null, null, 0);
        ad.addUserLocationTuple(40.54930049652604, -74.33626614511014, "2020-12-03", "Menlo Mall", null, null, 0);
        ad.addUserLocationTuple(40.548868169933755, -74.33561973273753, "2020-12-03", "Menlo Mall", null, null, 0);
        ad.addUserLocationTuple(40.548923452865196, -74.33546785265207, "2020-12-03", "Menlo Mall", null, null, 0);

        ad.addUserLocationTuple(40.54629122135982, -74.33646831661463, "2020-12-02", "Menlo Mall", null, null, 1);
        ad.addUserLocationTuple(40.54657248680576, -74.33590304106474, "2020-12-02", "Menlo Mall", null, null, 1);
        ad.addUserLocationTuple(40.546928398000375, -74.3357602134347, "2020-12-02", "Menlo Mall", null, null, 1);
        ad.addUserLocationTuple(40.54745117894556, -74.33596305549143, "2020-12-02", "Menlo Mall", null, null, 1);
        ad.addUserLocationTuple(40.547480222211746, -74.33626983314751, "2020-12-02", "Menlo Mall", null, null, 1);

        ad.addUserLocationTuple(40.54733449622285, -74.33499678969381, "2020-11-30", "Menlo Mall", null, null, 2);
        ad.addUserLocationTuple(40.54772759899485, -74.3354085087776, "2020-11-30", "Menlo Mall", null, null, 2);
        ad.addUserLocationTuple(40.54827100946152, -74.335333977717163, "2020-11-30", "Menlo Mall", null, null, 2);
        ad.addUserLocationTuple(40.54889619354526, -74.33552015572786, "2020-11-30", "Menlo Mall", null, null, 2);

        ad.getUserLocationTableContents();
        ad.getUserTableContents();
        ad.getJoinLocationAndUserTables();

        //updates demo users to negative if info already added to DB
        ad.updateUserCovidRisk("positive", "negative", 0);
        ad.updateUserCovidRisk("positive", "negative", 1);
        ad.updateUserCovidRisk("positive", "negative", 2);
    }

    /**
     * Get directions using the current user location as the start and a selected
     * location from the list of marked locations as destination.
     */
    public void getDirections(){
        LatLng startpoint = (this.mm.current_marker).getPosition();

        //get destination from user entry:
        DialogFragment df = new AddressDialog(SUBMIT_DESTINATION);
        df.show(getSupportFragmentManager(),"DestinationDialogFrag");
    }

    private void getDirection_postSubmission(String building, String room){
        AreaLabel al = new AreaLabel(building,room);
        Marker destinationMarker = (this.mm.markerContainer).get(al);

        if(destinationMarker == null){
            Toast.makeText(getApplicationContext(),"This Destination Does NOT Exist In Trained Data!",Toast.LENGTH_LONG).show();
        }

        destinationMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationMarker.getPosition(), 10.10f));

        sendDirectionsRequest((this.mm.current_marker).getPosition(),destinationMarker.getPosition());
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
        this.mm = new MapActivityModel(this.activityContext,this.mMap);

        //gets user's current location
        lo = new LocationObject(this, this);
        //lo.setupLocation();           //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!   changes
        List<Address> addresses = lo.getListOfAddresses();

        //lo.updateLocationData();      //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!   changes
        double latitude = lo.getLatitude();
        double longitude = lo.getLongitude();

        LatLng currentlocation = new LatLng(latitude, longitude);

        //mMap.addMarker(new MarkerOptions().position(currentlocation).title("Current Location")); //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!   changes
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentlocation));
        //Zoom in on the user's current location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 2.10f));

        //Set up listeners once map is ready
        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnCircleClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);

        //sendCOVIDDataRequest();

        //Riz section:
        //startMarkerProcedure();
        (this.mm).updateMap();
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

    public String createStringURLfromLatLong(LatLng origin, LatLng destination){

        //This is the secure way to retrieve our API key
        //note: it resolves at run time, so don't worry if there is an error here
        String api_key = getString(R.string.maps_api_key);

        String userOrigin = "origin=" + origin.latitude + "," + origin.longitude;
        String userDestination = "destination=" + destination.latitude + "," + destination.longitude;

        String url = "https://maps.googleapis.com/maps/api/directions/json?\n" +
                userOrigin + "&" + userDestination + "\n" +
                "&key=" + api_key + "&alternatives=true";

        return url;
    }

    /**Sends HTTP request for directions
     * direction request return value is a JSON
     * which is temporarily saved as a global variable for later access (jsonDirectionsString)
     */
    public void sendDirectionsRequest(LatLng origin, LatLng destination) {

        //Instantiate a new Request Queue
        RequestQueue queue = Volley.newRequestQueue(this);

        //this method is used if info is retrieved from previous activity
        //String url = createStringURL();

        String url = "";

        //this method is used if using LatLng coordinates
        if(origin !=null && destination!=null) {
            url = createStringURLfromLatLong(origin, destination);
        }
        else {
            //testing, hardcoded LatLng if no input
            url = createStringURLfromLatLong(new LatLng(40.388078, -74.590124), new LatLng(40.934328, -74.718241));
        }

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
            //route.setRandomRisk(i);
            route.setRisk(i);


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
                        .color(0xFF7F8000) //Dark Yellow
                        .width(10)
                        .clickable(true)
                        .addAll(route.getPoints())).setTag("Risk: " + risk + "\nTime: " + route.getDuration() + "\nDistance: " + route.getDistance());
            }

            if(risk.equals("Low")) {
                mMap.addPolyline((new PolylineOptions())
                        .color(0xFF0000FF) //Dark Blue
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
        if(!(this.lockmap)){
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

        if(this.trainingMode){
            //if currently in training mode:

            //first set the lat/lng class vars to the point selected
            this.classVar_latitude = clickCoordinates.latitude;
            this.classVar_longitude = clickCoordinates.longitude;

            //call dialogbox for building name and room name:
            DialogFragment df = new AddressDialog(SUBMIT_MAP_LABEL);
            df.show(getSupportFragmentManager(),"AddressDialogFrag");
        }

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


        if((this.enableIndoorRoutingProcedures)) setIndoorRoutingPoints(marker);

        return true;
    }

    private void setIndoorRoutingPoints(Marker marker){
        switch ((this.indoorRouting_setState)){
            case 0:
                (this.indoorRouting_srcnode) = (String) marker.getTag();
                (this.indoorRouting_setState)++;
                break;
            case 1:
                (this.indoorRouting_destnode) = (String) marker.getTag();
                (this.indoorRouting_setState) = 0;
                (this.enableIndoorRoutingProcedures) = false;
                break;
        }
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
        LocationGrabber lg = new LocationGrabber(getApplicationContext());
        lg.setupLocation();
        LatLng currentlatlng = new LatLng(lg.getLatitude(),lg.getLongitude());

        //need destination from somewhere here
        sendDirectionsRequest(currentlatlng, null);
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
                circleOptions.radius(10000);

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
                hashmap.put(circle, COVIDcluster);

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



    /*
        Riz's Section
        This section has to do with locating where the user is.
            Keywords: GPS,Detect,Train,Sample
        This section also sets up the dropdown(up) menu that holds all of the
        options.
            Keywords: spinner,options,dropdown
     */

    private void setSpinnerView(){
        (this.optionSpinner) = findViewById(R.id.mapactivity_spinner);
        List<String> optionStringList = new ArrayList<>();
        optionStringList.addAll(Arrays.asList(this.optionNames));

        ArrayAdapter<String> aradptr = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                optionStringList);
        aradptr.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        (this.optionSpinner).setAdapter(aradptr);
        (this.optionSpinner).setOnItemSelectedListener(this);
    }

    /*
        Two methods below correspond to the spinner. These are the actions taken when something is
        selected or if nothing is selected.
            - Adapterview.OnItemSelectedListener interface
     */

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int itemIndex, long l) {
        (this.optionToRun) = (String) adapterView.getItemAtPosition(itemIndex);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        (this.optionToRun) = (String) adapterView.getItemAtPosition(0);
    }



    /**
     * Puts new map label into the database. This data will later be used with obtained data and
     * cosine similarity to detect user location.
     * @param building
     * @param room
     */
    private void mapLabelSubmission(String building, String room){
        AppDatabase ad = new AppDatabase(getApplicationContext());
        if(ad.addMapLabelData(building,room,this.classVar_latitude,this.classVar_longitude)){
            Toast.makeText(getApplicationContext(),"Submit Successful",Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"Submit Fail",Toast.LENGTH_LONG).show();
        }
        (this.markerPlacement).sendMessage((this.markerPlacement.obtainMessage()));
    }

    /**
     * Get multiple samples of location features for a particular label (building,room).
     * Will get 10 samples per call.
     */
    private void sampleData(String building, String room){
        TextView showCount = findViewById(R.id.maptrain_textbox_left);

        Handler updateCountHandler = new UpdateCountHandler(Looper.getMainLooper(),showCount);

        Thread sampleLocData = new Thread(new Runnable() {
            @Override
            public void run() {
                LocationObject lo = new LocationObject(getApplicationContext());
                AppDatabase ad = new AppDatabase(getApplicationContext());

                for(int i=0;i<10;i++){
                    lo.updateLocationData();
                    lo.setBuildingName(building);
                    lo.setRoomName(room);
                    ad.addData(lo);

                    int displayCount = i + 1;
                    Message msg = updateCountHandler.obtainMessage();
                    msg.arg1 = displayCount;
                    //updateCountHandler.sendMessage(msg);
                }

                Thread.currentThread().interrupt();
            }
        },"SampleLocDataThread");

        sampleLocData.start();
    }

    /**
     * Used with Button:GOTO
     *
     * Moves the camera to user current location in term of latitude and longitude obtained from google.
     * @param view
     */
    public void moveToCurrent_train(View view){

        //get usable latitude and longitude values to move camera initially
        LocationGrabber lg = new LocationGrabber(getApplicationContext());
        lg.setupLocation();
        LatLng currentlatlng = new LatLng(lg.getLatitude(),lg.getLongitude());

        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentlatlng));
        //Zoom in on the user's current location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlatlng, 21.00f));

    }

    /*
        TRAIN button function(s)
     */
    public void setTrainingMode(View view){
        if(!this.trainingMode){
            this.trainingMode = true;
        }
        else{
            this.trainingMode = false;
        }
        Toast.makeText(getApplicationContext(),"Train Set To: " + this.trainingMode,Toast.LENGTH_LONG).show();
    }

    /*
        Sample button function
     */
    public void sampleButtonEvent(View view){
        //call dialogbox for building name and room name:
        DialogFragment df = new AddressDialog(SUBMIT_LOCATION_FEATURES);
        df.show(getSupportFragmentManager(),"SamplingLabels");
    }

    /**
     * Detect Button function
     * @param view
     */
    public void detectLocation(View view){
        this.mm.detectCurrentLocation();
    }

    /*
        For dialog activities:
     */

    @Override
    public void onRightButtonPress(DialogFragment dialogFragment, int submit_type) {
        AddressDialog dbox = (AddressDialog) dialogFragment;
        String building = ((TextView) dbox.getDialog().findViewById(R.id.dialogtwotextboxes_topBox)).getText().toString();
        String room = ((TextView) dbox.getDialog().findViewById(R.id.dialogtwotextboxes_bottomBox)).getText().toString();


        if(submit_type == SUBMIT_MAP_LABEL){
            mapLabelSubmission(building,room);
        }

        if(submit_type == SUBMIT_LOCATION_FEATURES){
            sampleData(building,room);
        }

        if(submit_type == SUBMIT_DESTINATION){
            getDirection_postSubmission(building,room);
        }
    }

    @Override
    public void onLeftButtonPress(DialogFragment dialogFragment) {
        Toast.makeText(getApplicationContext(),"Submission Cancelled",Toast.LENGTH_LONG).show();
    }



    /**
     * Handler that will receive count from a different thread and update the
     * appropriate textview accordingly.
     */
    private class UpdateCountHandler extends Handler {
        private TextView tv;

        public UpdateCountHandler(Looper looperToBeUsed, TextView tv){
            super(looperToBeUsed);
            this.tv = tv;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int count = msg.arg1;
            (this.tv).setText("\t\t"+count);
        }
    }


    /*
                ---------------------------------Akshay Integration Code--------------------------------
     */

    private GraphWorld graph;
    private boolean enteredInputs = true;
    ArrayList<Polyline> lines=new ArrayList<>();

    private List<AreaLabel> getAreaLabels_forIndoorRouting(){
        Set<AreaLabel> alList = (this.mm.markerContainer).keySet();
        List<AreaLabel> toBeReturned = new ArrayList<>();
        for(AreaLabel current_al : alList){
            toBeReturned.add(current_al);
        }
        return toBeReturned;
    }

    private void indoorRouting(){
        Button button = (Button)findViewById(R.id.DisplayRoutesButton);
        Polyline polyline=null;
        ArrayList<Double> distanceList = new ArrayList<Double>();
        ArrayList<Node> nodesList = new ArrayList<>();
        ArrayList<String> resultList = new ArrayList<>();

        //MALL EXAMPLE
        //Fair Oaks Mall Coordinates

        int checkFirstNode=0;

        //Adds text to each of the nodes

        LocationGrabber lg = new LocationGrabber(getApplicationContext());
        lg.setupLocation();
        LatLng currentlatlng = new LatLng(lg.getLatitude(),lg.getLongitude());

        //need destination from somewhere here
        //sendDirectionsRequest(currentlatlng, null);


        Scanner scanner = null;
        String testFilePath = AreaLabel.FILEPATH;
        this.currentBuildingAreas = getAreaLabels_forIndoorRouting();
        AreaLabel.writeAreasToFile((this.currentBuildingAreas));
        try {
            //DataInputStream textFileStream = new DataInputStream(getAssets().open(String.format()));
            DataInputStream textFileStream = new DataInputStream(new FileInputStream(new File(testFilePath)));
            scanner = new Scanner(textFileStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i("ReportSelections",this.indoorRouting_srcnode + "->" + this.indoorRouting_destnode);
        graph = new GraphWorld(scanner,this.indoorRouting_srcnode,this.indoorRouting_destnode);



        for (int x = 0; x < graph.members.length; x++) {

            System.out.println("Member is " + graph.members[x].name);



            LatLng latLng = new LatLng(graph.members[x].lat,graph.members[x].lon);

            if(checkFirstNode==0){  //Only update the position of the map to first store from textfile

                //Moves to that user's current location
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                //Zoom in on the user's current location
                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(graph.members[x].lat,graph.members[x].lon),  18.5f));

                checkFirstNode=1;

            }


            if(graph.members[x].name.equals("XXIForever") || (graph.members[x].name.equals("Apple")) ) //High-risk nodes example
            {
                mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(graph.members[x].name).snippet(graph.members[x].name)).showInfoWindow();

                showTextOnMarker(this,mMap,latLng,graph.members[x].name,150,12);
                drawCircularZone(latLng,9);
            }
            else {
                mMap.addMarker(new MarkerOptions().position(latLng).infoWindowAnchor(25, 25).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title(graph.members[x].name)).showInfoWindow();
                showTextOnMarker(this,mMap,latLng,graph.members[x].name,150,12);
            }





            Node node = new Node();
            node.name = graph.members[x].name;

            node.latLng = latLng;

            nodesList.add(node);

            for (Neighbor ptr = graph.members[x].first; ptr != null; ptr = ptr.next) {


                System.out.println("Neighbor of member " + graph.members[x].name + " is " + graph.members[ptr.fnum].name);

            }
        }

        if(enteredInputs) {



            ComputeRoute route = new ComputeRoute(graph);
            LinkedList<GraphWorldState> full_path = new LinkedList<GraphWorldState>();
            LinkedList<GraphWorldState> path = route.compute_path();

            if (path == null) {
                System.out.println("No path");
                return;
            }
            System.out.println("RETURN FROM COMPUTE PATH = " + path);
            full_path.addAll(path);
            full_path.removeLast();

            GraphWorldState last_state = path.peekLast();
            if (!last_state.equals(route.world.gstate)) {
                System.out.println("Something is not right");
                // route.world.update_start(last_state);
            } else {
                System.out.println("Completed successfully");
            }


            for (int x = 0; x < nodesList.size(); x++) {

                System.out.println("Name " + nodesList.get(x).name + " Lat Long " + nodesList.get(x).latLng);
            }

            ListIterator<GraphWorldState> iter = full_path.listIterator(0);
            while (iter.hasNext()) {
                GraphWorldState next_state = iter.next();
                System.out.println(next_state);

                resultList.add(next_state.nodename);

                Log.d("PRINTING SHORTEST ROUTE", next_state.nodename + "");


            }

            //   System.out.println(route.world.gstate.);
            Log.d("DESTINATION", graph.destnode);
            resultList.add(graph.destnode);

            System.out.println("START END STATES "+graph.sstate+" "+graph.gstate);

            System.out.println("resultList " + resultList);


            LatLng l1 = null;
            LatLng l2 = null;


            for(Polyline line:lines){


                line.remove();
            }

            lines.clear();



            for (int x = 0; x < resultList.size() - 1; x++) {


                for (int y = 0; y < nodesList.size(); y++) {


                    if (nodesList.get(y).name.equals(resultList.get(x)))  //if the shortest path node is equal to the nodeList nodes name then add polylines for that nodes latlng objects
                    {
                        l1 = nodesList.get(y).latLng;

                    }


                }

                for (int y = 0; y < nodesList.size(); y++) {


                    if (nodesList.get(y).name.equals(resultList.get(x + 1)))  //if the shortest path node is equal to the nodeList nodes name then add polylines for that nodes latlng objects
                    {
                        l2 = nodesList.get(y).latLng;

                    }
                }


                polyline = mMap.addPolyline(new PolylineOptions().clickable(true).add(
                        l1, l2

                ));

                lines.add(polyline);

                System.out.println("Added LINES"+lines.get(x)) ;

                polyline.setColor(Color.RED);


            } //resultList end

            enteredInputs=false;

        }
    }

    /*Adds text to a marker at a Latlng location
     */
    public Marker showTextOnMarker(final Context context, final GoogleMap map,
                                   final LatLng location, final String text, final int padding,
                                   final int fontSize) {
        Marker marker = null;

        if (context == null || map == null || location == null || text == null
                || fontSize <= 0) {
            return marker;
        }

        final TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(fontSize);

        final Paint paintText = textView.getPaint();

        final Rect boundsText = new Rect();
        paintText.getTextBounds(text, 0, textView.length(), boundsText);
        paintText.setTextAlign(Paint.Align.CENTER);

        final Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        final Bitmap bmpText = Bitmap.createBitmap(boundsText.width() + 2
                * padding, boundsText.height() + 2 * padding, conf);

        final Canvas canvasText = new Canvas(bmpText);
        paintText.setColor(Color.BLACK);

        canvasText.drawText(text, canvasText.getWidth() / 2,
                canvasText.getHeight() - padding - boundsText.bottom, paintText);

        final MarkerOptions markerOptions = new MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.fromBitmap(bmpText))
                .anchor(0.5f, 1);

        marker = map.addMarker(markerOptions);

        return marker;
    }

}