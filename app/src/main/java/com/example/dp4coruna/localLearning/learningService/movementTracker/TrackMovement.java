package com.example.dp4coruna.localLearning.learningService.movementTracker;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import androidx.annotation.Nullable;

public class TrackMovement extends Service{

    //vars:
    private HandlerThread ht;
    private Looper htLooper;
    private HandleMotionDetection hmd;
    private IBinder tmBinder = new TrackMovementBinder();


    /*
        Binding Tools: creation of sub-class to allow for binding.
     */
    public class TrackMovementBinder extends Binder{
        public TrackMovement getServiceBinder(){
            return TrackMovement.this;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();

        this.ht = new HandlerThread("HandleMovementTracker");
        (this.ht).start();
        this.htLooper = (this.ht).getLooper();
        this.hmd = new HandleMotionDetection((this.htLooper));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Message msg = (this.hmd).obtainMessage();
        (this.hmd).sendMessage(msg);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return this.tmBinder;
    }

    /*
     * Construct handler for getting and using the motion detection data
     */

    private class HandleMotionDetection extends Handler{

        public HandleMotionDetection(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            //create and launch the motion sensor class:
//            MovementSensor ms = new MovementSensor(getApplicationContext(),MovementSensor.CALLED_FROM_SERVICE);
//            ms.startMovementSensor();
        }
    }

}
