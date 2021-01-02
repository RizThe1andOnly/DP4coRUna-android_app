package com.example.dp4coruna.mapmanagement;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import org.json.*;
import androidx.fragment.app.FragmentActivity;

import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.BubbleIconFactory;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;

import static org.bytedeco.leptonica.global.lept.COLOR_BLUE;
import static org.bytedeco.leptonica.global.lept.COLOR_GREEN;
import static org.bytedeco.leptonica.global.lept.COLOR_RED;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnCircleClickListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;

    String jsonDirectionsString; //holds response from HTTP request

    //Holds coordinates of walkways/hallways
    //Dummy Data
    ArrayList<Marker> walkwaypoints = new ArrayList<Marker>();

    //Holds coordinates of high risk locations
    //Dummy data for now
    ArrayList<LatLng> highriskcoordinates = new ArrayList<LatLng>();

    ArrayList<Polyline> lines=new ArrayList<>();

    GraphWorld graph;
    boolean startcheck=false;
    public boolean enteredInputs=false;
    public boolean buttonPressed=false;

    public static int riskPath=0;

    ArrayList<LatLng> riskPoints = new ArrayList<LatLng>();

    public static int riskCount=0;
    public   static ArrayList<RiskZone> listOfZones = new ArrayList<RiskZone>();



    String source="";
    String dest="";

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
        Button button = (Button)findViewById(R.id.DisplayRoutesButton);

        Polyline polyline=null;

        ArrayList<Double> distanceList = new ArrayList<Double>();

        ArrayList<Node> nodesList = new ArrayList<>();

        //ArrayList<String> resultList = new ArrayList<>();

        Boolean firstComputation = true;

        RiskZone r1 = null;
        RiskZone r2 = null;




        //MALL EXAMPLE
        //Fair Oaks Mall Coordinates

        int checkFirstNode=0;

        //Adds text to each of the nodes

        sendDirectionsRequest();



        //SHORTEST PATH ALGORITHM:
      //  File file = new File("C:\\Users\\Akshay\\Desktop\\Nodes.txt");
        //File file = new File("router\\Nodes.txt");

        Scanner scanner = null;
        try {
            DataInputStream textFileStream = new DataInputStream(getAssets().open(String.format("FairOaksExample.txt")));
            scanner = new Scanner(textFileStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        graph = new GraphWorld(scanner);

/*
        FIND RISK ZONES BY COMPUTING DISTANCE FROM EACH STORE TO COVID RISK POINT
  */

        if(firstComputation && enteredInputs) {

            for (int i = 0; i < graph.members.length; i++) {

                if (r1!=null){

                    listOfZones.add(r1);
                }
                riskCount = 0;

                LatLng latLng = new LatLng(graph.members[i].lat, graph.members[i].lon);

                for (int y = 0; y < riskPoints.size(); y++) {

                    Double lat1 = latLng.latitude;
                    Double lat2 = riskPoints.get(y).latitude;

                    Double lon1 = latLng.longitude;
                    Double lon2 = riskPoints.get(y).longitude;

                    lat1 = Math.toRadians(lat1);
                    lat2 = Math.toRadians(lat2);

                    lon1 = Math.toRadians(lon1);
                    lon2 = Math.toRadians(lon2);

                    double dlat = lat1 - lat2;
                    double dlon = lon1 - lon2;

                    double a = Math.pow(Math.sin(dlat / 2), 2);
                    double c = 2 * Math.asin(Math.sqrt(a));

                    // Radius of earth in kilometers. Use 3956
                    // for miles
                    double r = 6371;
                    double lat_d = (c * r);

                    // do the same for lon
                    a = Math.pow(Math.sin(dlon / 2), 2);
                    c = 2 * Math.asin(Math.sqrt(a));
                    double lon_d = (c * r);

                    double result = lat_d + lon_d;
                    System.out.println("MEMBER IS " + graph.members[i].name + " DISTANCE FROM RISKPOINT " + result);


                    //If the distance from the store to the covid point is less than a meter, increment riskCounter
                    if (result <0.01) {

                        if (riskCount == 0) { //If not initialized then initialize the risk zone
                            r1 = new RiskZone(graph.members[i].name, riskCount);

                            riskCount = 1;
                            r1.numCOVIDPoints++;

                        } else {

                            r1.numCOVIDPoints++;
                        }


                    }

                }

            }

            /*
            for(int x=0;x<listOfZones.size();x++){


                System.out.println("RISKZONE NAME IS "+listOfZones.get(x).name + "NUM COVID POINTS "+listOfZones.get(x).numCOVIDPoints);
            }


             */

            firstComputation=false;
        }


            for (int x = 0; x < graph.members.length; x++) {

                System.out.println("Member is " + graph.members[x].name);

                riskCount = 0;


                LatLng latLng = new LatLng(graph.members[x].lat, graph.members[x].lon);


                if (checkFirstNode == 0) {  //Only update the position of the map to first store from textfile

                    //Moves to that user's current location
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                    //Zoom in on the user's current location
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(graph.members[x].lat, graph.members[x].lon), 18.5f));

                    checkFirstNode = 1;

                }


                mMap.addMarker(new MarkerOptions().position(latLng).infoWindowAnchor(25, 25).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title(graph.members[x].name)).showInfoWindow();
                showTextOnMarker(this, mMap, latLng, graph.members[x].name, 150, 12);


                for (int y = 0; y < listOfZones.size(); y++) {

                    if (listOfZones.get(y).name.equals(graph.members[x].name)) {

                        if (listOfZones.get(y).numCOVIDPoints == 1) {
                            mMap.addMarker(new MarkerOptions().position(latLng).infoWindowAnchor(25, 25).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)).title(graph.members[x].name)).showInfoWindow();
                          if(riskPath==0)
                            drawCircularZone(latLng, 7);
                        } else if (listOfZones.get(y).numCOVIDPoints >= 3) {
                            mMap.addMarker(new MarkerOptions().position(latLng).infoWindowAnchor(25, 25).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)).title(graph.members[x].name)).showInfoWindow();
                          if(riskPath==0)
                            drawCircularZone(latLng, 7);

                        }

                    }

                }


                Node node = new Node();
                node.name = graph.members[x].name;

                node.latLng = latLng;

                nodesList.add(node);

                for (Neighbor ptr = graph.members[x].first; ptr != null; ptr = ptr.next) {


                    System.out.println("Neighbor of member " + graph.members[x].name + " is " + graph.members[ptr.fnum].name);

                }
            }



        button.setOnClickListener(new View.OnClickListener(){


            @Override
            public void onClick(View view) {

                buttonPressed=true;
                Log.d("I AM HERE","BUTTON PRESSED");


            }
        });


        for (Polyline line : lines) {


            line.remove();
        }

        lines.clear();


        riskPath=0;

        if(enteredInputs) {



                while(riskPath<2){

                        System.out.println("Risk path value is "+riskPath);

                        ArrayList<String> resultList = new ArrayList<>();
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

                    System.out.println("START END STATES " + graph.sstate + " " + graph.gstate);

                    System.out.println("resultList " + resultList);


                    LatLng l1 = null;
                    LatLng l2 = null;




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

                        System.out.println("Added LINES" + lines.get(x));


                        if(riskPath==0) {
                            polyline.setColor(Color.GREEN);

                            System.out.println("PRINTING GREEN LINE");
                        }
                        else {
                            polyline.setColor(Color.RED);

                            System.out.println("PRINTING RED LINE");
                        }

                    } //resultList end

                    buttonPressed = false;
                    enteredInputs = false;

                    riskPath++;

                } //while loop




            }




        //These just show an example of what is possible
        showCircularRiskZones();
        markHighRiskZones();
        showMallHighRiskZones();
        addHighRiskCoordinates();
        highriskcoordinates.forEach((n) -> drawCircularZone(n, 10));
        drawCircularZone( new LatLng(40.547242016103894, -74.334968291223305), 10);


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
                            List<Route> routes = new ArrayList<Route>();

                            //get values from JSON as ArrayList of Route objects
                            routes = parseJSONintoRoutes(response);

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



        if(marker==null)
            return false;

        if(startcheck==false) {
            source = marker.getTitle();

            graph.srcnode=source;



            Toast.makeText(this,"START NODE: "+marker.getTitle(), (int) 15.0).show();

            startcheck=true;
        }
        else {
            dest=marker.getTitle();

            graph.destnode = dest;

            System.out.println("DEBUG DESTINATION NODE"+graph.destnode);

            Toast.makeText(this,"DESTINATION NODE: "+marker.getTitle(), (int) 15.0).show();

             //Set this to true to show we are ready to compute routes

            enteredInputs=true;

             /*

           HARDCODING COVID POINTS
         */
            LatLng covidPoint = new LatLng(38.862684749697756, -77.35871813361132);

            riskPoints.add(covidPoint);

            riskPoints.add(new LatLng(38.86265141491132, -77.35874259697574));

            riskPoints.add(new LatLng(38.862695067605586, -77.35878642716558));


            riskPoints.add(new LatLng(38.86276267859017, -77.3584054522046));

            riskPoints.add(new LatLng(38.86276633347367, -77.35841416938206));

            riskPoints.add(new LatLng(38.86277938662797, -77.35846647245495));


            for(int x=0;x<riskPoints.size();x++){


                mMap.addMarker(new MarkerOptions().position(riskPoints.get(x)).infoWindowAnchor(25, 25).icon(BitmapDescriptorFactory.defaultMarker(2.0f)));

            }


            onMapReady(mMap);

            startcheck=false;

        }
        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return true;
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

    /**
     * Shows all routes from start to destination. Start and destination
     * should be set before calling this method (pressing the show routes button).
     * @param view
     */
    public void showAllRoutes(View view){
        sendDirectionsRequest();
    }



}
