package com.example.dp4coruna.localLearning.learningService;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.example.dp4coruna.localLearning.learningService.movementTracker.MovementSensor;
import com.example.dp4coruna.localLearning.learningService.movementTracker.TrackMovement;
import com.example.dp4coruna.localLearning.location.LocationObject;
import com.example.dp4coruna.localLearning.location.dataHolders.WiFiAccessPoint;
import com.example.dp4coruna.localLearning.location.learner.SensorReader;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 *
 * Local learning handled by LocationObject and the contents of
 * com.example.dp4coruna.location.learner
 */
public class LocalLearningService extends Service {

    private final static long SLEEP_DURATION_MILLIS = 1000; // time = 1 second

    private final IBinder llsBinder = new LocalLearningServiceBinder();

    private HandlerThread ht;
    private Looper htLooper;

    // two handlers that will be used with this service:
    private StartMotionSensingHandler motionInitiator;
    private HandleMotionDetection motionDetectedHandler;

    //two wifi ap lists that will be compared:
    private List<WiFiAccessPoint> wifiAccessPointList_atStart;
    private List<WiFiAccessPoint> wifiAccessPointList_atEnd;

    public class LocalLearningServiceBinder extends Binder {
        public LocalLearningService getBinderService(){
            return LocalLearningService.this;
        }
    }

    public LocalLearningService(){

    }

    /**
     * Will create new thread and pass in handler to obtain location data
     */
    @Override
    public void onCreate() {
        super.onCreate();

        this.ht = new HandlerThread("HandleMotionThread");
        (this.ht).start();
        this.htLooper = (this.ht).getLooper();

        this.motionDetectedHandler = new HandleMotionDetection(this.htLooper,this.wifiAccessPointList_atStart,this.wifiAccessPointList_atEnd);
        this.motionInitiator = new StartMotionSensingHandler(this.htLooper,this.motionDetectedHandler);

        //initialize wifi ap lists:
        this.wifiAccessPointList_atStart = new ArrayList<>();
        this.wifiAccessPointList_atEnd = new ArrayList<>();
        this.obtainWifiAccessPointsList((this.wifiAccessPointList_atStart)); // populate list at start
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Message msg = (this.motionInitiator).obtainMessage();
        (this.motionInitiator).sendMessage(msg);

        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return llsBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(getApplicationContext(),"Service Stopped (hopefully)",Toast.LENGTH_LONG).show();
    }


    private class StartMotionSensingHandler extends Handler {
        private Handler motionDetectionHandler;

        public StartMotionSensingHandler(Looper looper, Handler motionDetectionHandler){
            super(looper);
            this.motionDetectionHandler = motionDetectionHandler;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            MovementSensor ms = new MovementSensor(getApplicationContext(),MovementSensor.CALLED_FROM_SERVICE,this.motionDetectionHandler);
            ms.startMovementSensor();
        }
    }

    private class HandleMotionDetection extends Handler{
        private List<WiFiAccessPoint> wifiAccessPointList_atStart;
        private List<WiFiAccessPoint> wifiAccessPointList_atEnd;

        public HandleMotionDetection(Looper looper, List<WiFiAccessPoint> wap_start, List<WiFiAccessPoint> wap_end){
            super(looper);
            this.wifiAccessPointList_atStart = wap_start;
            this.wifiAccessPointList_atEnd = wap_end;
        }

        /**
         * Carries out logic when transition from motion to non-motion is detected. After detection message will be
         * sent to this handler.
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            obtainWifiAccessPointsList(wifiAccessPointList_atEnd); // populate wifi access point list at end.

            /*
             *  - Carry out wifi access point list comparison logic here, read CollabLoc paper for detail on that process.
             *  - After that has been done make calls to other services/functions as necessary.
             */

            //after other things have been called reset two wifi ap lists:
            // don't know if clear function very well, should investigate further.
            this.wifiAccessPointList_atStart.clear();
            this.wifiAccessPointList_atEnd.clear();

        }


    }


    /*
        Utility Functions Section:
            Functions used for calculations or obtaining data using other classes.
            This includes:
                - Obtaining List of Wifi-accesspoints that device has scanned from the area using the SensorReader class
                - Calculating the cosine similarity between two wifi accesspoints lists

            These methods will be used by the above methods/handlers.
     */

    /**
     * Use SensorReader class's scanWifiAccessPoints() method to obtain a list of wifi-accesspoints in the device's
     * current location.
     * @param wifiAccessPointList
     */
    private void obtainWifiAccessPointsList(List<WiFiAccessPoint> wifiAccessPointList){
        SensorReader.scanWifiAccessPoints(getApplicationContext(),wifiAccessPointList);

        if(wifiAccessPointList.size() > 0){
            Toast.makeText(getApplicationContext(),"Wifi AP retrieval successful",Toast.LENGTH_SHORT); //(!!!) For testing only, get rid of later.
        }
        else{
            Toast.makeText(getApplicationContext(),"Wifi AP retrieval failed",Toast.LENGTH_SHORT); //(!!!) For testing only, get rid of later.
        }
    }

    /**
     * Calculate cosine similarity between two lists of wifi access points as per the equation given in the
     * CollabLoc paper.
     * @param start List at point A or starting point
     * @param end List at point B or end (current location) point
     * @return double the cosine similarity
     */
    private double cosineSimilarity(List<WiFiAccessPoint> start, List<WiFiAccessPoint> end){
        //for normalization (if req) see the helper method cosineSimilarity_RssiProcessing:
        List<Double> start_processedRssiList = cosineSimilarity_RssiProcessing(start);
        List<Double> end_processedRssiList = cosineSimilarity_RssiProcessing(end);

        // rss = root sum squared; denominator section of the cosine similarity equation
        double rss_start = cosineSimilarity_RootSumSquaredR(start_processedRssiList);
        double rss_end = cosineSimilarity_RootSumSquaredR(end_processedRssiList);

        //numerator section of the cosine similarity equation
        double outerSum = 0;
        double innerSum = 0;
        for(int i=0;i<start.size();i++){
            WiFiAccessPoint a_AP = start.get(i);
            double a_Rssi = start_processedRssiList.get(i);
            for(int j=0;j<end.size();j++){
                WiFiAccessPoint b_AP = end.get(j);
                double b_Rssi = end_processedRssiList.get(j);
                int delta = cosineSimilarity_CompareMacAddress(a_AP,b_AP);
                double abdelta = 0;
                if(delta == 1) abdelta = a_Rssi * b_Rssi;
                innerSum += abdelta;
            }
            outerSum += innerSum;
            innerSum = 0;
        }

        double similarity = outerSum / (rss_start * rss_end);

        return similarity;
    }

    /**
     * Helper method to be used by cosineSimilarity() to get length of wifi access point list
     * length. Length being the square root of the sum of each Rssi value squared of the given list.
     *
     * Has the 'W' at the end to indicate it takes WifiAccessPoint List
     *
     * @param arr
     * @return
     */
    private double cosineSimilarity_RootSumSquaredW(List<WiFiAccessPoint> arr){
        double squaredSum = 0;
        for(WiFiAccessPoint w : arr){
            squaredSum = squaredSum + Math.pow(w.getRssi(),2);
        }
        return Math.sqrt(squaredSum);
    }

    /**
     * Helper method to be used by cosineSimilarity() to get length of wifi access point list
     * length. Length being the square root of the sum of each Rssi value squared of the given list.
     *
     * Has the 'R' at the end to indicate it takes list of doubles or rssi values as the argument.
     *
     * @param arr
     * @return
     */
    private double cosineSimilarity_RootSumSquaredR(List<Double> arr){
        double squaredSum = 0;
        for(double r : arr){
            squaredSum = squaredSum + Math.pow(r,2);
        }
        return Math.sqrt(squaredSum);
    }

    /**
     * Helper method used by cosineSimilarity() to determine if two wifi access points have the same MAC address which
     * would mean they are the same wifi access point.
     * @param a Access point a
     * @param b Access point b
     * @return 1 if Mac address matches, 0 otherwise.
     */
    private int cosineSimilarity_CompareMacAddress(WiFiAccessPoint a, WiFiAccessPoint b){
        if((a.getBssid()).equals(b.getBssid())) return 1;
        return 0;
    }

    /**
     * Helper method called by cosineSimilarity to process Rssi values of a list. Currently maybe
     * normalize rssi values.
     * @param arr
     */
    private List<Double> cosineSimilarity_RssiProcessing(List<WiFiAccessPoint> arr){
        /*
            Normalization would happen by extracting all of the rssi values into
            separate list then getting normalized values.
         */
        List<Double> rssiVals = new ArrayList<>();
        for(WiFiAccessPoint w : arr){
            rssiVals.add(w.getRssi());
        }

        //further process of the rssiVals list here if necessary:

        return rssiVals;
    }

}
