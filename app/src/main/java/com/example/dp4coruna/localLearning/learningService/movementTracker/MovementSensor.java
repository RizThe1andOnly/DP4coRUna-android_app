package com.example.dp4coruna.localLearning.learningService.movementTracker;

import android.content.Context;
import android.hardware.*;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Uses android's built in significant motion sensor functionalities to detect movement, this is not
 * necessarily what we want for our app. For now this will be used, but in the future solution using
 * linear accelerometer data for our purposes.
 *
 * For now, this class will be used with TrackMovement Service.
 */
public class MovementSensor extends TriggerEventListener {

    private TextView outputView;
    private Context context;

    //sensor vars:
    private SensorManager sensorManager;
    private Sensor motionTracker;

    //motion detection handler variable:
    private Handler motionDetectedHandler;

    //vars to indicate from what (activity/service) this class is called
    public static final int CALLED_FROM_ACTIVITY = 0;
    public static final int CALLED_FROM_SERVICE = 1;
    private int call_source;

    /**
     * Used to test output using the text view in whichever activity calls this.
     * @param context
     * @param outputView
     */
    public MovementSensor(Context context, TextView outputView, int source){
        this.context = context;
        this.outputView = outputView;
        this.call_source = source;

        setUpSensors(context);
    }

    /**
     * Use to test output while working with a service. output will be displayed through Toasts.
     * @param context
     */
    public MovementSensor(Context context, int source, Handler motionDetectedHandler){
        this.context = context;
        setUpSensors(context);
        this.call_source = source;
        this.motionDetectedHandler = motionDetectedHandler;
    }

    /**
     * Initializes the sensor manager and sensor. Used because different constructors exist
     * that do different things but all will still use the below instructions.
     * @param context
     */
    private void setUpSensors(Context context){
        /*
            set up sensor manager, declare types of sensors, register sensor with listener to await callbacks upon
            sensor reporting.
         */
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.motionTracker = (this.sensorManager).getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);

        if(this.motionTracker == null){
            Log.i("FromMovementSensor","Motion Tracker non-existent");
        }
    }


    /**
     * Carries out logic after android sends signal that significant motion has been detected.
     * @param triggerEvent
     */
    @Override
    public void onTrigger(TriggerEvent triggerEvent) {
        /*
            put logic for after motion detection here (for now will print a message saying motion detected):
         */

        if(this.call_source == CALLED_FROM_ACTIVITY){
            outputView.append("Motion Detected");
        }
        else{ // called from a service:
            Toast.makeText(this.context, "Motion Detected", Toast.LENGTH_SHORT).show();
            Message msg = (this.motionDetectedHandler).obtainMessage();
            (this.motionDetectedHandler).sendMessage(msg);
        }

        this.sensorManager.requestTriggerSensor(MovementSensor.this,this.motionTracker);
    }


    /**
     * Used, for now, for testing purposes. Starts sensor trigger listener from which ever activity calls it.
     */
    public void startMovementSensor(){
        this.sensorManager.requestTriggerSensor(MovementSensor.this,this.motionTracker);
    }

    public void stopMovementSensor(){

    }

}
