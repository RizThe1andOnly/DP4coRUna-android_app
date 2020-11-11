package com.example.dp4coruna.mapmanagement;

import android.content.Context;
import android.database.Cursor;
import android.os.*;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import com.example.dp4coruna.R;
import com.example.dp4coruna.dataManagement.AppDatabase;
import com.example.dp4coruna.localLearning.location.learner.LocationGrabber;
import com.example.dp4coruna.utilities.AddressDialog;
import com.example.dp4coruna.utilities.DialogCallBack;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;

public class MapTrainActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnCircleClickListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, DialogCallBack {


    /*
        Class Contents:
            * Class Variables
            * Lifecycle Methods
            * GoogleMap SDK Methods
            * Internal Logic Functions
            * DialogCallBack Methods
            * GoogleMap SDK Methods (that are unused)
     */


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

        startMarkerPlacementHanlder();
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
        DialogFragment df = new AddressDialog();
        df.show(getSupportFragmentManager(),"AddressDialogFrag");
    }

    /*
        * Internal Logic Functions
            - Utility Function
            - Button Action functions
            - Helper For Button Actions
     */


    private void startMarkerPlacementHanlder(){
//        //set the new thread for the handler
//        HandlerThread markerPlacement_ht = new HandlerThread("MarkerPlacement");
//        markerPlacement_ht.start();
//        this.markerPlacement_Looper = markerPlacement_ht.getLooper();

        //setup the new handler
        (this.markerPlacement) = new PlaceMarkerHandler(Looper.getMainLooper());
        //(this.markerPlacement).sendMessage((this.markerPlacement.obtainMessage()));
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

        map.moveCamera(CameraUpdateFactory.newLatLng(currentlatlng));
        //Zoom in on the user's current location
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlatlng, 21.00f));

    }

    public void setTrainingMode(View view){
        this.trainingMode = true;
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
    public void onRightButtonPress(DialogFragment dialogFragment) {
       //Extract the user-entered building and room name
        AddressDialog dbox = (AddressDialog) dialogFragment;
        String building = ((TextView) dbox.getDialog().findViewById(R.id.dialogtwotextboxes_topBox)).getText().toString();
        String room = ((TextView) dbox.getDialog().findViewById(R.id.dialogtwotextboxes_bottomBox)).getText().toString();

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
    public boolean onMarkerClick(Marker marker) { return false; }

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

            Cursor markers = (new AppDatabase(activityContext)).queryMapMarkers();
            while(markers.moveToNext()){
                String marker_title = markers.getString(0) + " " + markers.getString(1);
                LatLng marker_post = new LatLng(markers.getDouble(2),markers.getDouble(3));
                Marker temp = map.addMarker(new MarkerOptions()
                                            .position(marker_post)
                                            .title(marker_title));
                temp.setTag(0);
            }
        }
    }
}

