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

    private void obtainWifiAccessPointsList(List<WiFiAccessPoint> wifiAccessPointList){
        SensorReader.scanWifiAccessPoints(getApplicationContext(),wifiAccessPointList);

        if(wifiAccessPointList.size() > 0){
            Toast.makeText(getApplicationContext(),"Wifi AP retrieval successful",Toast.LENGTH_SHORT); //(!!!) For testing only, get rid of later.
        }
        else{
            Toast.makeText(getApplicationContext(),"Wifi AP retrieval failed",Toast.LENGTH_SHORT); //(!!!) For testing only, get rid of later.
        }
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

}
