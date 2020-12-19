package com.example.dp4coruna.mapmanagement;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.example.dp4coruna.R;
import com.example.dp4coruna.dataManagement.AppDatabase;
import com.example.dp4coruna.localLearning.location.LocationObject;
import com.example.dp4coruna.localLearning.location.dataHolders.AreaLabel;
import com.example.dp4coruna.localLearning.location.dataHolders.WiFiAccessPoint;
import com.example.dp4coruna.localLearning.location.learner.CosSimilarity;
import com.example.dp4coruna.localLearning.location.learner.LocationGrabber;
import com.example.dp4coruna.localLearning.location.learner.SensorReader;
import com.example.dp4coruna.network.RelayServer;
import com.example.dp4coruna.network.Transmitter;
import com.example.dp4coruna.utilities.AddressDialog;
import com.example.dp4coruna.utilities.DialogCallBack;
import com.example.dp4coruna.utilities.JSONFunctions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.PublicKey;
import java.util.*;

public class MapTrainActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnCircleClickListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, DialogCallBack {


    /*
        Class Contents:
            * Class Constants
            * Class Variables
            * Lifecycle Methods
            * Content Initiation Methods
            * GoogleMap SDK Methods
            * Internal Logic Functions
            * DialogCallBack Methods
            * GoogleMap SDK Methods (that are unused)
     */

    // * Class constants:
    private final int SUBMIT_MAP_LABEL = 0;
    private final int SUBMIT_LOCATION_FEATURES = 1;
    public static final String RECEIVE_MESSAGE_BROADCAST = "com.example.dp4coruna.MAP_TRAIN_RECEIVE"; // for connection with network;
    private final int SECOND_CONSTANT = 1000; // = 1 second in milliseconds

    // * Class Variables
    private Context activityContext;
    private Timer autoDetectTimer;

    private GoogleMap map;
    private boolean trainingMode = false;
    private boolean autodetect = false;

    //      - Objects from view:
    private TextView text_left;
    private TextView text_lng;

    //latitude and longitude of selected point (for training purposes)
    private double classVar_latitude;
    private double classVar_longitude;

    // Handler for marker placement
    private Handler markerPlacement;
    private Looper  markerPlacement_Looper;

    //handler for timer to view communication:
    private Handler updateViewhandler;

    //map marker container:
    private Map<AreaLabel,Marker> markerContainer;

    private Marker current_marker = null;



    /*
        * Lifecycle Method(s)
     */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_train);

        //set the view elements
        this.text_left = findViewById(R.id.maptrain_textbox_left);
        this.text_lng = findViewById(R.id.maptrain_lng);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_train);
        mapFragment.getMapAsync(this);

        this.activityContext = getApplicationContext();
        this.autoDetectTimer = new Timer("AutoDetectionThread");
        this.updateViewhandler = new TimerToViewHandler(Looper.getMainLooper(),this.text_left);

        startMarkerProcedure();
        setBroadCastReceiver();
    }


    /*
         Content Initiation Methods.

         Responsible for setting up content. Will mostly likely be called by onCreate() or by a method
         called by onCreate()
     */


    /*
        Marker Methods
        - Methods that deal with showing and holding map markers
     */
    private void startMarkerProcedure(){
        setMarkerContainer();
        startMarkerPlacementHanlder();
    }

    private void setMarkerContainer(){
        (this.markerContainer) = new HashMap<>();
    }

    private void startMarkerPlacementHanlder(){
        //setup the new handler
        (this.markerPlacement) = new PlaceMarkerHandler(Looper.getMainLooper());
        (this.markerPlacement).sendMessage((this.markerPlacement.obtainMessage()));
    }


    /**
     * Setup broadcast receiver which will receive network transmitted data from
     * RelayConnection class. The custom broadcast receiver for this class is defined
     * somewhere below, look for MapTrainBroadCastReceiver.
     *
     * This method also starts the relay server, this is necessary to receive data
     * from the network.
     */
    private void setBroadCastReceiver(){
        MapTrainBroadCastReceiver mtbcr = new MapTrainBroadCastReceiver();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(mtbcr,new IntentFilter(MapTrainActivity.RECEIVE_MESSAGE_BROADCAST));

        //this instruction sets up server to listen for data from the network
        new Thread(new RelayServer(null,getApplicationContext())).start();
        Toast.makeText(getApplicationContext(),"Setup Done",Toast.LENGTH_LONG).show();
    }



    /*
        * Google Map SDK Methods
            - Currently being used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;

        map.setOnPolylineClickListener(this);
        map.setOnPolygonClickListener(this);
        map.setOnMapClickListener(this);
        map.setOnCircleClickListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnMarkerDragListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if(!this.trainingMode){
            String lat = ""+latLng.latitude;
            String longi = ""+latLng.longitude;
            (this.text_left).setText(lat);
            (this.text_lng).setText(longi);
            Log.i("Coordinates",lat+","+longi);
            return;
        }

        //if currently in training mode:

        //first set the lat/lng class vars to the point selected
        this.classVar_latitude = latLng.latitude;
        this.classVar_longitude = latLng.longitude;

        //call dialogbox for building name and room name:
        DialogFragment df = new AddressDialog(SUBMIT_MAP_LABEL);
        df.show(getSupportFragmentManager(),"AddressDialogFrag");
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if(marker==null){
            return false;
        }
        if(marker.getTag()==null){
            return false;
        }

        Toast.makeText(this, marker.getTitle(),
                Toast.LENGTH_SHORT).show();

        return true;
    }

    /*
        * Internal Logic Functions
            - Utility Function
            - Button Action functions
            - Helper For Button Actions
            - Ground Overlay method
     */

    /*
        GOTO button functions
     */
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

        map.moveCamera(CameraUpdateFactory.newLatLng(currentlatlng));
        //Zoom in on the user's current location
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlatlng, 21.00f));

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
        Detect Button function
     */
    public void detectLocationButton(View view){
        //detectLocation();
//        if(!this.autodetect){
//            this.autodetect = true;
//            startAutoDetection();
//        }
//        else{
//            this.autodetect = false;
//            (this.autoDetectTimer).cancel();
//        }

        detectLocation();
    }

    public void detectLocation(){
        //if previous marker is green then re-set it to red:
        if((this.current_marker) != null){
            (this.current_marker).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }

        if(this.markerContainer.size() != 0){

            List<WiFiAccessPoint> start = SensorReader.scanWifiAccessPoints(getApplicationContext());
            AreaLabel currentAreaLabel = (new CosSimilarity(getApplicationContext()).checkCosSin_vs_allLocations_v2(start));


            (this.current_marker) = (this.markerContainer).get(currentAreaLabel);
            (this.current_marker).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

            LatLng currentlatlng = new LatLng((this.current_marker).getPosition().latitude,(this.current_marker).getPosition().longitude);

            map.moveCamera(CameraUpdateFactory.newLatLng(currentlatlng));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlatlng, 21.00f));
        }
        else{
            initiateNetReq();
        }
    }

    /**
     * Method to detect user location based on response from network.
     * If the current device does not have the location it will request from
     * another device using the network.
     */
    public void detectLocationUsingNetwork(AreaLabel arealabel){

        LatLng ll = new LatLng(arealabel.latitude,arealabel.longitude);
        Marker netMarker = map.addMarker(new MarkerOptions()
                .position(ll)
                .title(arealabel.title));
        netMarker.setTag(0);

        (this.current_marker) = netMarker;
        (this.current_marker).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        LatLng currentlatlng = new LatLng((this.current_marker).getPosition().latitude,(this.current_marker).getPosition().longitude);

        //add risk zone:
        drawRiskArea(arealabel);

        map.moveCamera(CameraUpdateFactory.newLatLng(currentlatlng));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlatlng, 21.00f));
    }


    /**
     * Uses obtained area labels to draw risk zones.
     *
     * Currently uses only lat/lng with a dummy radius to draw a risk area.
     * The risk will be determined by the AreaLabel's associated num of cases or some other pre-determined
     * value.
     *
     * @param arealabel
     */
    private void drawRiskArea(AreaLabel arealabel){
        LatLng coord = new LatLng(arealabel.latitude,arealabel.longitude);

        @ColorInt int low = Color.argb(50,255, 25, 25);
        @ColorInt int high = Color.argb(100,255, 25, 25);

        int risk = arealabel.riskLevel;
        int riskColor = risk == 0 ? low:high;

        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(coord);
        circleOptions.radius(5);
        circleOptions.strokeWidth(1);
        circleOptions.strokeColor(riskColor);
        circleOptions.fillColor(riskColor);
        circleOptions.clickable(true);
        Circle circle = map.addCircle(circleOptions);
        circle.setTag("User 1");

    }

    private void initiateNetReq(){
        /*
            The below condition should be for demo only. It checks if the device has no markers in
            database and if that is the case then sends a network request. Normally this would be used
            for specific locations and the device will have markers already in device.
         */
        //do network code here:
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendLocRequest();
            }
        },"NetworkReqThread").start();
    }

    /*
        Sample button function
     */
    public void sampleButtonEvent(View view){
        //call dialogbox for building name and room name:
        DialogFragment df = new AddressDialog(SUBMIT_LOCATION_FEATURES);
        df.show(getSupportFragmentManager(),"SamplingLabels");
    }

    /*
        Handler Functions
            - Functions that will be called by the handler to change certain view props
     */

    public void putMarkersOnMap(){
        Cursor markers = (new AppDatabase(activityContext)).queryMapMarkers();
        while(markers.moveToNext()){
            String current_building = markers.getString(0);
            String current_room = markers.getString(1);
            double current_latitude = markers.getDouble(2);
            double current_longitude = markers.getDouble(3);

            String marker_title = current_building + " " + current_room;
            AreaLabel current_al = new AreaLabel(markers.getString(0),markers.getString(1),current_latitude,current_longitude);

            if(!markerContainer.containsKey(current_al)){
                LatLng marker_post = new LatLng(markers.getDouble(2),markers.getDouble(3));
                Marker temp = map.addMarker(new MarkerOptions()
                        .position(marker_post)
                        .title(marker_title));
                temp.setTag(0);
                markerContainer.put(current_al,temp);
            }
        }

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


    public void groundOverlayEvent(View view){
        LatLng coreloc = new LatLng(40.52155103834118,-74.46171607822181);
        map.moveCamera(CameraUpdateFactory.newLatLng(coreloc));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(coreloc, 18.00f));
        groundOverlay();
    }

    private void groundOverlay(){
        LatLng coreloc = new LatLng(40.52160736327198,-74.46183912456036);
        GroundOverlayOptions goo = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.core_1st_floor_small_realigned_2))
                .anchor(0,0)
                .position(coreloc,75f);

        map.addGroundOverlay(goo);
    }


    /*
        * DialogCallBack Methods
            - DialogCallBack interface used to cooperate with dialog box
     */

    /**
     * Triggers when right side button is pressed in the dialog box, for this
     * particular dialog box that will be 'Submit'.
     * @param dialogFragment
     */
    @Override
    public void onRightButtonPress(DialogFragment dialogFragment,int submit_type) {
       //Extract the user-entered building and room name
        AddressDialog dbox = (AddressDialog) dialogFragment;
        String building = ((TextView) dbox.getDialog().findViewById(R.id.dialogtwotextboxes_topBox)).getText().toString();
        String room = ((TextView) dbox.getDialog().findViewById(R.id.dialogtwotextboxes_bottomBox)).getText().toString();


        if(submit_type == SUBMIT_MAP_LABEL){
            mapLabelSubmission(building,room);
        }

        if(submit_type == SUBMIT_LOCATION_FEATURES){
            sampleData(building,room);
        }
    }

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
     * Triggers when left side button (Cancel) is pressed on the dialog box.
     * @param dialogFragment
     */
    @Override
    public void onLeftButtonPress(DialogFragment dialogFragment) {
        Toast.makeText(getApplicationContext(),"Submission Cancelled",Toast.LENGTH_LONG).show();
    }

    /*
            * Google Map SDK Methods
                - Currently NOT being used. Here if required for future functionalities.
         */
    @Override
    public void onCircleClick(Circle circle) {}


    @Override
    public void onMarkerDragStart(Marker marker) {}

    @Override
    public void onMarkerDrag(Marker marker) {}

    @Override
    public void onMarkerDragEnd(Marker marker) {}

    @Override
    public void onPolygonClick(Polygon polygon) {}

    @Override
    public void onPolylineClick(Polyline polyline) {}



    /*
        * Handler Classes
            - Handlers that are used for various tasks.
                - Task 1: Place markers on the map
                - Task 2: light up marker the user is in
     */

    private class PlaceMarkerHandler extends Handler {

        public PlaceMarkerHandler(Looper looperToBeUsed){
            super(looperToBeUsed);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);


            putMarkersOnMap();
        }

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

    private class TimerToViewHandler extends Handler{
        private TextView txtview;

        public TimerToViewHandler(Looper looper,TextView txtView){
            super(looper);
            this.txtview = txtView;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int counter = msg.arg1;
            (this.txtview).setText(""+counter);

            if(counter == 0){
                detectLocation();
            }
        }
    }


    /*
        ------------------------------------------Network Code--------------------------------
     */

    /**
     *  Sends the request for markers over the network.
     *  Currently uses hardcoded ip address, will need to fix this later.
     */
    private void sendLocRequest(){
        String[] ips = {
                "192.168.1.159",
                "192.168.1.199",
                "192.168.1.164"
        };

        List<String> deviceAddresses = new ArrayList<>();
        String deviceAddress;

        deviceAddresses.addAll(Arrays.asList(ips));
        deviceAddress = ips[2];

        List<PublicKey> rsaEncryptKeys = new ArrayList<PublicKey>();
        LocationObject lobNet = new LocationObject(getApplicationContext());
        lobNet.updateLocationData();

        new Thread(new Transmitter(deviceAddresses, deviceAddress,rsaEncryptKeys, lobNet,"hello","transmitter")).start();
    }


    private class MapTrainBroadCastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                String receivedAreaLabel = (JSONFunctions.receivedToJSON(intent.getStringExtra("outgoingMessage"))).getString("msg");
                Log.i("ALJsonString",receivedAreaLabel);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            AreaLabel receivedMarkerData = extractAreaLabel(intent);
            Toast.makeText(getApplicationContext(),receivedMarkerData.toString(),Toast.LENGTH_LONG).show();
            detectLocationUsingNetwork(receivedMarkerData);
        }
    }

    /**
     * Obtains the area label object from the message received from device in network.
     * @param intent
     * @return
     */
    private AreaLabel extractAreaLabel(Intent intent){
        String receivedMessage = intent.getStringExtra("outgoingMessage");
        JSONObject msgJson = JSONFunctions.receivedToJSON(receivedMessage);

        //get the area label:
        String areaLabelString = "";
        try {
            areaLabelString = msgJson.getString("msg");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i("ALJsonString",areaLabelString);
        return AreaLabel.fromJson(areaLabelString);
    }


    /*
        Counter for auto location detection:
     */

    private int counter = 10;

    private void startAutoDetection(){
        autoDetectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                counter--;

                Message msg = updateViewhandler.obtainMessage();
                msg.arg1 = counter;
                updateViewhandler.sendMessage(msg);

                if(counter == 0){
                    counter = 10;
                }
            }
        },0,SECOND_CONSTANT);
    }
}

