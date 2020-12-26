package com.example.dp4coruna.mapmanagement.MapModel;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;
import com.example.dp4coruna.R;
import com.example.dp4coruna.dataManagement.AppDatabase;
import com.example.dp4coruna.localLearning.location.LocationObject;
import com.example.dp4coruna.phpServer.ServerConnection;
import com.google.android.gms.maps.GoogleMap;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Does everything the MapActivityModel does with some additional features for training location features.
 * This model creates new location markers as well as samples location features (primarily Wifi access points) associated
 * with location markers.
 */
public class MapTrainActivityModel extends MapActivityModel{

    protected TextView text_left;

    public MapTrainActivityModel(Context activityContext, GoogleMap mMap, TextView text_left) {
        super(activityContext, mMap);
        this.text_left = text_left;
    }


    public void mapLabelSubmission(String building, String room){

        double lat = this.classVar_latitude;
        double lng = this.classVar_longitude;

        if((this.classAd).addMapLabelData(building,room,lat,lng)){
            Toast.makeText(this.activityContext,"Submit Successful",Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this.activityContext,"Submit Fail",Toast.LENGTH_LONG).show();
        }
        updateMap();
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

        (new ServerConnection(this.activityContext)).queryDatabaseUnprepared(ServerConnection.ADD_NEW_MAP_LABEL,args);
    }


    /**
     * Get multiple samples of location features for a particular label (building,room).
     * Will get 10 samples per call.
     */
    public void sampleData(String building, String room){

        Handler updateCountHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                text_left.setText(String.valueOf(msg.arg1));
            }
        };

        Thread sampleLocData = new Thread(()->{
            // lambda runnable class

            LocationObject lo = new LocationObject(this.activityContext);
            AppDatabase ad = this.classAd;

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

}
