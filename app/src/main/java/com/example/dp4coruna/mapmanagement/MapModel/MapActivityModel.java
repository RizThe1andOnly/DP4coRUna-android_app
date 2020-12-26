package com.example.dp4coruna.mapmanagement.MapModel;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.ColorInt;
import com.example.dp4coruna.dataManagement.AppDatabase;
import com.example.dp4coruna.localLearning.location.dataHolders.WiFiAccessPoint;
import com.example.dp4coruna.localLearning.location.learner.CosSimilarity;
import com.example.dp4coruna.localLearning.location.learner.SensorReader;
import com.example.dp4coruna.mapmanagement.MapDataStructures.AreaLabel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;

import java.util.*;

/**
 * Places all the necessary elements to the map.
 *
 * Currently the markers available in the device database will be shown. Markers in the aws server will not
 * be queried since the server will be shut down. The code to connect to the current type of setup will be kept in the
 * phpServer package.
 */
public class MapActivityModel {

    public Map<AreaLabel, Marker> markerContainer;
    protected GoogleMap mMap;
    protected Context activityContext;
    private Handler markerPlacement;
    public Marker current_marker;
    private List<AreaLabel> currentBuildingAreas;
    protected AppDatabase classAd;

    public double classVar_latitude;
    public double classVar_longitude;

    //constants for type of handling that the handler has to do:
    public static final int MARKER_FROM_DEVICE_DATABASE = 0;
    public static final int MARKER_FROM_PHP_SERVER = 1;

    public MapActivityModel(Context activityContext, GoogleMap mMap){
        this.activityContext = activityContext;
        this.mMap = mMap;
        this.markerPlacement = new PlaceMarkerHandler(Looper.getMainLooper());
        this.classAd = new AppDatabase(this.activityContext);
        setMarkerContainer();
    }


    /**
     *  Updates the map with new server data. This is to account for changes in server by other users.
     */
    public void updateMap(){
        setMarkerContainer();
        startMarkerProcedure();
    }

    /**
     * Uses the location learning methods (CosSim) to get to the current marked location
     */
    public void detectCurrentLocation(){
        //if previous marker is green then re-set it to red:
        if((this.current_marker) != null){
            (this.current_marker).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }

        List<WiFiAccessPoint> start = SensorReader.scanWifiAccessPoints(this.activityContext);
        AreaLabel currentAreaLabel = (new CosSimilarity(this.activityContext).checkCosSin_vs_allLocations_v2(start));

        // get all the area labels associated with the current building:
        this.currentBuildingAreas = (this.classAd).getFullAreaLabels(currentAreaLabel);

        (this.current_marker) = (this.markerContainer).get(currentAreaLabel);
        (this.current_marker).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        LatLng currentlatlng = new LatLng((this.current_marker).getPosition().latitude,(this.current_marker).getPosition().longitude);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentlatlng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlatlng, 21.00f));
    }

    public void zoomToMall(){
        //get a point in the mall:
        Set<AreaLabel> keys = (this.markerContainer).keySet();
        AreaLabel mallAl = null;
        for(AreaLabel al : keys){
            if(al.building.equals("WillowbrookMall")){
                mallAl = al;
                break;
            }
        }

        LatLng ll = new LatLng(mallAl.latitude,mallAl.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ll));
        //Zoom in on the user's current location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mallAl.latitude, mallAl.longitude), 17.10f));
    }

    private void setMarkerContainer(){
        (this.markerContainer) = new HashMap<>();
    }


    private void startMarkerProcedure(){
        //send message to place markers available in the device database (will use key dd for this in var names):
        Message dd_msg = (this.markerPlacement.obtainMessage());
        dd_msg.arg1 = MARKER_FROM_DEVICE_DATABASE;
        (this.markerPlacement).sendMessage((this.markerPlacement.obtainMessage()));

        //below functionality is commented out for the moment due to aws server being down.
        //send message to place markers that are retrieved from the aws server:
        // note: the arg1 value for message will be set to MARKER_FROM_PHP_SERVER in the AreaLabel function called
        //AreaLabel.getQueryResults(this.activityContext,AreaLabel.ALL_LOCATION_QUERY,this.markerPlacement);

    }


    private class PlaceMarkerHandler extends Handler {

        public PlaceMarkerHandler(Looper looperToBeUsed){
            super(looperToBeUsed);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(msg.arg1 == MARKER_FROM_DEVICE_DATABASE){
                putMarkersOnMap();
            }

            if(msg.arg1 == MARKER_FROM_PHP_SERVER){
                //Since the aws server is down the below instruction will be commented out.
                putMarkersOnMap_fromServer(getAreaLabelListFromMessage((List<String>)msg.obj));
            }

        }

    }

    private List<AreaLabel> getAreaLabelListFromMessage(List<String> stringRes){
        List<AreaLabel> alList = new ArrayList<>();
        for(String s : stringRes){
            alList.add(AreaLabel.parseStringintoAreaLabel(s));
        }
        return alList;
    }


    public void putMarkersOnMap(){
        Cursor markers = (new AppDatabase(activityContext)).queryMapMarkers();
        while(markers.moveToNext()){
            String current_building = markers.getString(0);
            String current_room = markers.getString(1);
            double current_latitude = markers.getDouble(2);
            double current_longitude = markers.getDouble(3);

            String marker_title = current_building + "-" + current_room;
            AreaLabel current_al = new AreaLabel(markers.getString(0),markers.getString(1),current_latitude,current_longitude);

            if(!markerContainer.containsKey(current_al)){
                LatLng marker_post = new LatLng(markers.getDouble(2),markers.getDouble(3));
                Marker temp = mMap.addMarker(new MarkerOptions()
                        .position(marker_post)
                        .title(marker_title));
                temp.setTag(marker_title);
                markerContainer.put(current_al,temp);
            }
        }
    }

    public void putMarkersOnMap_fromServer(List<AreaLabel> serverLabels){

        //for demo purposes hardset a map marker to be zoomed into:
        Marker zoomTo = null;

        for(AreaLabel current_al : serverLabels){
            if(!markerContainer.containsKey(current_al)){
                LatLng marker_post = new LatLng(current_al.latitude,current_al.longitude);
                String marker_title = current_al.building + "-" + current_al.area;
                Marker temp = mMap.addMarker(new MarkerOptions()
                        .position(marker_post)
                        .title(marker_title));
                temp.setTag(marker_title);
                markerContainer.put(current_al,temp);

                drawRiskArea(current_al);

                if(current_al.building.equals("Home")){
                    zoomTo = temp;
                }

                // should go inside if statement above: !markerContainer.containsKey(current_al)
            }
        }

        //for demo purposes zoom into specific location hardcoded here:
        mMap.moveCamera(CameraUpdateFactory.newLatLng(zoomTo.getPosition()));
        //Zoom in on the user's current location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(zoomTo.getPosition(), 21.00f));
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

        @ColorInt int none = Color.argb(0,255, 25, 25);
        @ColorInt int low = Color.argb(50,255, 25, 25);
        @ColorInt int high = Color.argb(100,255, 25, 25);

        int risk = arealabel.numCovidCases;
        int riskColor = none;
        String riskTag = "";

        switch (risk){
            case 0:
                riskColor = none;
                riskTag = "None";
                break;
            case 1:
                riskColor = low;
                riskTag = "low";
                break;
            case 2:
                riskColor = high;
                riskTag = "high";
                break;
        }

        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(coord);
        circleOptions.radius(5);
        circleOptions.strokeWidth(1);
        circleOptions.strokeColor(riskColor);
        circleOptions.fillColor(riskColor);
        circleOptions.clickable(true);
        Circle circle = mMap.addCircle(circleOptions);
        circle.setTag(riskTag);

    }

}
