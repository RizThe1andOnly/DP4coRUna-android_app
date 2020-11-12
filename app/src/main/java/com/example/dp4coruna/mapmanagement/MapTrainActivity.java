package com.example.dp4coruna.mapmanagement;

import android.content.Context;
import android.database.Cursor;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import com.example.dp4coruna.R;
import com.example.dp4coruna.dataManagement.AppDatabase;
import com.example.dp4coruna.localLearning.location.LocationObject;
import com.example.dp4coruna.localLearning.location.dataHolders.AreaLabel;
import com.example.dp4coruna.localLearning.location.dataHolders.CosSimLabel;
import com.example.dp4coruna.localLearning.location.dataHolders.WiFiAccessPoint;
import com.example.dp4coruna.localLearning.location.learner.CosSimilarity;
import com.example.dp4coruna.localLearning.location.learner.LocationGrabber;
import com.example.dp4coruna.localLearning.location.learner.SensorReader;
import com.example.dp4coruna.utilities.AddressDialog;
import com.example.dp4coruna.utilities.DialogCallBack;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MapTrainActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnCircleClickListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, DialogCallBack {


    /*
        Class Contents:
            * Class Constants
            * Class Variables
            * Lifecycle Methods
            * GoogleMap SDK Methods
            * Internal Logic Functions
            * DialogCallBack Methods
            * GoogleMap SDK Methods (that are unused)
     */

    // * Class constants:
    private final int SUBMIT_MAP_LABEL = 0;
    private final int SUBMIT_LOCATION_FEATURES = 1;


    // * Class Variables
    private Context activityContext;

    private GoogleMap map;
    private boolean trainingMode = false;

    //      - Objects from view:
    private TextView text_lat;
    private TextView text_lng;

    //latitude and longitude of selected point (for training purposes)
    private double classVar_latitude;
    private double classVar_longitude;

    // Handler for marker placement
    private Handler markerPlacement;
    private Looper  markerPlacement_Looper;

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
        this.text_lat = findViewById(R.id.maptrain_lat);
        this.text_lng = findViewById(R.id.maptrain_lng);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_train);
        mapFragment.getMapAsync(this);

        this.activityContext = getApplicationContext();

        startMarkerProcedure();
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
        if(!this.trainingMode) return;

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
    public void detectLocation(View view){
        //if previous marker is green then re-set it to red:
        if((this.current_marker) != null){
            (this.current_marker).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }

        List<WiFiAccessPoint> start = SensorReader.scanWifiAccessPoints(getApplicationContext());
        CosSimLabel csl = (new CosSimilarity(getApplicationContext()).checkCosSin_vs_allLocations_v2(start));
        AreaLabel currentAreaLabel = csl.arealabel;

        (this.current_marker) = (this.markerContainer).get(currentAreaLabel);
        (this.current_marker).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        LatLng currentlatlng = new LatLng((this.current_marker).getPosition().latitude,(this.current_marker).getPosition().longitude);

        map.moveCamera(CameraUpdateFactory.newLatLng(currentlatlng));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlatlng, 21.00f));
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
        TextView showCount = findViewById(R.id.maptrain_lat);

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
                    updateCountHandler.sendMessage(msg);
                }

                Thread.currentThread().interrupt();
            }
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
}

