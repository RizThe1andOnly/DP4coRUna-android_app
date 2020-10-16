package com.example.dp4coruna.localLearning.learningService;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.example.dp4coruna.localLearning.learningService.movementTracker.MovementSensor;
import com.example.dp4coruna.localLearning.learningService.movementTracker.TrackMovement;
import com.example.dp4coruna.localLearning.location.LocationObject;
import com.example.dp4coruna.localLearning.location.dataHolders.WiFiAccessPoint;
import com.example.dp4coruna.localLearning.location.learner.CosSimilarity;
import com.example.dp4coruna.localLearning.location.learner.SensorReader;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 *
 * Local learning handled by LocationObject and the contents of
 * com.example.dp4coruna.location.learner
 *
 * Main service that will be running for the app in a device. Will either have some means of motion detection
 * or signaling. The motion detection/signaling will initiate wifi access point scan. The results from the scan
 * will be compared and based on comparisons other local learning procedures will be initiated.
 */
public class LocalLearningService extends Service {

    /*
     *  This is a service class that is explained in: https://developer.android.com/guide/components/services.
     *  This class also uses Threads, HandlerThreads, and Handlers for its purposes (see Android Developer Guide).
     *
     *  All of the motion detecting and the sensor related stuff are abstracted away from this class and can be found
     *  in the movementTracker folder. Note: its all messed up and will take a while to actually figure out, currently just a mess of experiments, sorry. -Rizwan
     *
     *  Class sets up two handlers on same newly created thread. One handler starts the motion tracker and keeps that
     * running while the other handler waits for the motion handler to report motion. Once motion is reported the waiting
     * handler does other procedures based on local learning. See the "Handler Definition Section" below for the
     * handlers and their code.
     *
     * Also see "Utility Functions" section to see some of local learning process, currently the comparison of the
     * two wifi access points lists.
     */

    private Context context;

    private final static long SLEEP_DURATION_MILLIS = 1000; // time = 1 second
    private final long LOCAL_LEARNING_SAMPLING_PERIOD = 10000; // time = 10 seconds

    private final IBinder llsBinder = new LocalLearningServiceBinder();

    private HandlerThread ht;
    private Looper htLooper;

    // two handlers that will be used with this service:
    private StartMotionSensingHandler handler_DetectingMotion;
    private HandleMotionDetection handler_AfterMotionDetected;


    public class LocalLearningServiceBinder extends Binder {
        public LocalLearningService getBinderService(){
            return LocalLearningService.this;
        }
    }

    /**
     * Constructor will be used with the Demo class TempResults for now.
     * @param demoHandler Handler that will update the output text view with results from the service.
     */
    public LocalLearningService(Handler demoHandler){}

    /**
     * Will create new thread and pass in handler to obtain location data.
     *
     * - Will start each of the handler for executing location learning tasks
     *  - HandleMotionDetection
     *  - StartMotionSensingHandler
     *
     * - initialize wifi access point lists to be used for comparisons.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        this.context = getApplicationContext();

        this.ht = new HandlerThread("HandleMotionThread");
        (this.ht).start();
        this.htLooper = (this.ht).getLooper();


        //the handlers being initialized here are defined below in this file
        this.handler_DetectingMotion = new StartMotionSensingHandler(this.htLooper,this.context);
        this.handler_AfterMotionDetected = new HandleMotionDetection(this.htLooper,this.context);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        Message msg = (this.handler_DetectingMotion).obtainMessage();
//        (this.handler_DetectingMotion).sendMessage(msg);

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


    /*
        Handler definitions Section.
        Currently all handlers defined here will be bound to same extra thread. Contains the
        following handlers:
            - StartMotionSensingHandler: bound to newly created thread to run the motion detecting sensors or
            other tasks that will determine when wifi-sensing should take place.
            - HandleMotionDetection: after signal is received will carry out wifi sensing and
            comparison.
     */

    /**
     * Starts the sensors or other tasks that will determine when to take in wifi access point
     * data.
     */
    private class StartMotionSensingHandler extends Handler {
        /*
            - Receives a message when the service is started.
            - Upon reception of the message code in the handleMessage() will take effect.

         */


        private Context context;

        public StartMotionSensingHandler(Looper looper, Context context){
            super(looper);
            this.context = context;
        }

        /**
         * For now will have a recurring timer to trigger LocalLearning tasks.
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Message msg = handler_AfterMotionDetected.obtainMessage();
                    handler_AfterMotionDetected.sendMessage(msg);
                }
            },1000,LOCAL_LEARNING_SAMPLING_PERIOD);

        }
    }

    /**
     * Will be passed into sensor class or handler and from there will receive message as signal for when
     * to start local learning procedures. This handler will have a starting list of wifi access points and will
     * obtain another list when signaled. Then cosineSimilarity() will be called to compare the two lists. Based
     * on the results different actions will be taken. If cosineSimilarity determines they are similar enough nothing
     * happens otherwise will start other local learning procedures.
     */
    private class HandleMotionDetection extends Handler{

        private Context context;

        public HandleMotionDetection(Looper looper, Context context){
            super(looper);
            this.context = context;
        }

        /**
         * Carries out logic when transition from motion to non-motion is detected. After detection message will be
         * sent to this handler.
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            /*
             *  - Carry out wifi access point list comparison logic here (Cosine Similarity),
             *    read CollabLoc paper for detail on that process.
             *  - After that has been done make calls to other services/functions as necessary.
             */



        }


    }


    /*
        Utility Functions Section:
            Functions used for calculations or obtaining data using other classes.
            This includes:
                - Obtaining List of Wifi-accesspoints that device has scanned from the area using the SensorReader class

            These methods will be used by the above methods/handlers.
     */
    /**
     * Use SensorReader class's scanWifiAccessPoints() method to obtain a list of wifi-accesspoints in the device's
     * current location.
     * @param wifiAccessPointList
     */
    private void obtainWifiAccessPointsList(List<WiFiAccessPoint> wifiAccessPointList){
        Log.i("FromLLS",Thread.currentThread().getName());

        SensorReader.scanWifiAccessPoints(getApplicationContext(),wifiAccessPointList);

        if(wifiAccessPointList.size() > 0){
            Log.i("FromLLS","Wifi Retrieval successful: \n" + wifiAccessPointList.toString());
        }
        else{
            Log.i("FromLLS","Wifi retrieval not successful");
        }
    }

}
