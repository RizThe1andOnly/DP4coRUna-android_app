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
import android.widget.*;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.example.dp4coruna.R;
import com.example.dp4coruna.dataManagement.AppDatabase;
import com.example.dp4coruna.localLearning.location.LocationObject;
import com.example.dp4coruna.mapmanagement.MapDataStructures.AreaLabel;
import com.example.dp4coruna.localLearning.location.dataHolders.WiFiAccessPoint;
import com.example.dp4coruna.localLearning.location.learner.CosSimilarity;
import com.example.dp4coruna.localLearning.location.learner.LocationGrabber;
import com.example.dp4coruna.localLearning.location.learner.SensorReader;
import com.example.dp4coruna.mapmanagement.MapModel.MapTrainActivityModel;
import com.example.dp4coruna.network.RelayServer;
import com.example.dp4coruna.network.Transmitter;
import com.example.dp4coruna.phpServer.ServerConnection;
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
            * Internal Logic Functions; Button action methods
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

    private MapTrainActivityModel mm;

    //  - Spinner Variables: spinner for selecting methods to run:
    //      - Spinner:
    private Spinner optionSpinner;
    //      - Spinner items
    private String[] options = {
            "Detect",
            "Train",
            "Sample",
            "Detect With Network"
    };
    private String optionSelected;

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


    // hard-coded ip addresses:  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! IP ADDRESSES HERE
    private String[] hardcoded_ips = { // please put in addresses of devices based on the label next to them
            "192.168.1.159", // transmitter device
            "192.168.1.199", // relay device
            "192.168.1.164" // receiver device
    };


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

        setBroadCastReceiver();
        setUpSpinner();
    }


    /*
         Content Initiation Methods.

         Responsible for setting up content. Will mostly likely be called by onCreate() or by a method
         called by onCreate()
     */

    //set up drop down menu for lists:
    private void setUpSpinner(){

        (this.optionSpinner) = findViewById(R.id.maptrainactivity_spinner);

        List<String> optionNames = new ArrayList<>();
        optionNames = Arrays.asList(this.options);

        ArrayAdapter<String> optionNamesArrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                optionNames
        );
        optionNamesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        (this.optionSpinner).setAdapter(optionNamesArrayAdapter);
        (this.optionSpinner).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                optionSelected = (String) adapterView.getItemAtPosition(index);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                optionSelected = (String) adapterView.getItemAtPosition(0);
            }
        });
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
        this.mm = new MapTrainActivityModel(getApplicationContext(),googleMap,this.text_left);

        map.setOnPolylineClickListener(this);
        map.setOnPolygonClickListener(this);
        map.setOnMapClickListener(this);
        map.setOnCircleClickListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnMarkerDragListener(this);

        (this.mm).updateMap();
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
        (this.mm).classVar_latitude = latLng.latitude;
        (this.mm).classVar_longitude = latLng.longitude;

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
        Run button:
     */
    public void runOption(View view){
        switch ((this.optionSelected)){
            case "Train"  : setTrainingMode(view); break;
            case "Sample" : sampleButtonEvent(view);break;
            case "Detect" : (this.mm).detectCurrentLocation();break;
            case "Detect With Network" : initiateNetReq(); break;
        }
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


    /**
     * Method to detect user location based on response from network.
     * If the current device does not have the location it will request from
     * another device using the network.
     */
    public void detectLocationUsingNetwork(AreaLabel arealabel){

        LatLng ll = new LatLng(arealabel.latitude,arealabel.longitude);
        String areaLabelTag = arealabel.building + "-" + arealabel.area;
        Marker netMarker = map.addMarker(new MarkerOptions()
                .position(ll)
                .title(arealabel.title));
        netMarker.setTag(areaLabelTag);

        (this.current_marker) = netMarker;
        (this.current_marker).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        LatLng currentlatlng = new LatLng((this.current_marker).getPosition().latitude,(this.current_marker).getPosition().longitude);

        //add risk zone:
        //drawRiskArea(arealabel);

        map.moveCamera(CameraUpdateFactory.newLatLng(currentlatlng));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlatlng, 21.00f));
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


    /**
     * Get multiple samples of location features for a particular label (building,room).
     * Will get 10 samples per call.
     */
    private void sampleData(String building, String room){
        Handler updateCountHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                text_left.setText(msg.arg1);
            }
        };

        Thread sampleLocData = new Thread(()->{
            // lambda runnable class

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
                updateCountHandler.sendMessage(msg);
            }

            Thread.currentThread().interrupt();
        },"SampleLocDataThread");

        sampleLocData.start();
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
            //mapLabelSubmission_toPhpServer(building,room);
            (this.mm).mapLabelSubmission(building,room);
        }

        if(submit_type == SUBMIT_LOCATION_FEATURES){
            (this.mm).sampleData(building,room);
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
     * Add the new map label to the aws serve we have using the ServerConnection class.
     * @param building
     * @param room
     */
    private void mapLabelSubmission_toPhpServer(String building, String room){
        String date = "\"2020-12-21\"";
        String lat = String.valueOf((this.classVar_latitude));
        String lng = String.valueOf((this.classVar_longitude));
        String county = "\"Passaic\"";
        building = "\"" + building + "\"";
        room = "\"" + room + "\"";
        String numCases = "0";

        String columnsString = "latitude,longitude,date_added,building_name,room_name,county,numCases";
        String valuesString = lat + "," + lng + "," + date + "," + building + "," + room + "," + county + "," + numCases;
        List<String> args = new ArrayList<>();
        args.add(columnsString);
        args.add(valuesString);

        (new ServerConnection(getApplicationContext())).queryDatabaseUnprepared(ServerConnection.ADD_NEW_MAP_LABEL,args);
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
        ------------------------------------------Network Code--------------------------------
     */

    /**
     *  Sends the request for markers over the network.
     *  Currently uses hardcoded ip address, will need to fix this later.
     */
    private void sendLocRequest(){
        String[] ips = hardcoded_ips;

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

}

