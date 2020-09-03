package com.example.dp4coruna.localLearning.learningService.movementTracker.Acceleration;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of acceleration sensor for the purpose of detecting motion.
 *
 * Implementation just started, no where near close to finish. Need to find or develop algorithm that takes
 * the accelerometer data and determine motion.
 */
public class AccelerationSensor implements SensorEventListener {
    private TextView outputView;
    private SensorManager accelerometerSensorManager;
    private Sensor accelerometer;
    private HandlerThread ht;
    private Handler motionDetectedHandler;

    /**
     * Should only be called from an activity with a text view.
     * @param outputWindow
     * @param context
     * @param motionDetectedHandler
     */
    public AccelerationSensor(TextView outputWindow, Context context, Handler motionDetectedHandler){

        // construct the sensor manager and obtain the accelerometer from it:
        accelerometerSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)){
            accelerometer = accelerometerSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        }
        else{
            Toast.makeText(context,"Accelerometer not Available",Toast.LENGTH_LONG).show();
        }
        this.motionDetectedHandler = motionDetectedHandler;
        this.outputView = outputWindow;
    }

    /**
     * Called from services.
     * @param context
     * @param motionDetectedHandler
     */
    public AccelerationSensor(Context context, Handler motionDetectedHandler){
        // construct the sensor manager and obtain the accelerometer from it:
        accelerometerSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)){
            accelerometer = accelerometerSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        }
        else{
            Toast.makeText(context,"Accelerometer not Available",Toast.LENGTH_LONG).show();
        }
        this.motionDetectedHandler = motionDetectedHandler;
    }



    public void startSensor(){
        this.ht = new HandlerThread("AccelerometerSensorThread");
        this.ht.start();

        Handler accelSensorHandler = new Handler(this.ht.getLooper());
        accelerometerSensorManager.registerListener(AccelerationSensor.this,accelerometer,SensorManager.SENSOR_DELAY_UI,accelSensorHandler);
    }

    public void stopSensor(){
        this.ht.interrupt();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        List<Float> accelData = getAccelerometerData(sensorEvent);
        boolean motionDetected = false;

        /*
            Carry out logic to determine whether motion has been detected using acceleration data here:
                - if motion has been detected set motionDetected to true. !
         */


        //after motion detection takes place send message:
        if(motionDetected){
            Message msg = (this.motionDetectedHandler).obtainMessage();
            (this.motionDetectedHandler).sendMessage(msg);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /*
        Functions to be used with accelerometer thread to read and process accelerometer data
     */


    /**
     * Extract the accelerometer vector and pass data into other functions for further processing.
     * @param sensorEvent Accelerometer readings
     */
    private List<Float> getAccelerometerData(SensorEvent sensorEvent){
        /*
            Readings with indices as follows:
                - 0 : x-axis
                - 1 : y-axis
                - 2 : z-axis
         */
        float[] readings = sensorEvent.values;

        //get the acceleration magnitude:
        float accelMag = getAccelerationMagnitude(readings);

        /*
            Create a list<float> of all values obtained from sensorevent as well as the calculated magnitude
            with indicies:
                - 0 : x-axis
                - 1 : y-axis
                - 2 : z-axis
                - 3 : magnitude
         */
        List<Float> accelerometerdata = new ArrayList<>();
        for(int i=0;i<readings.length;i++){
            accelerometerdata.add(readings[i]);
        }
        accelerometerdata.add(accelMag);

        return accelerometerdata;
    }

    /**
     * Gets the magnitude for acceleration from the 3-d accel data provided.
     * @param vals
     * @return
     */
    private float getAccelerationMagnitude(float[] vals){
        return ((float)Math.sqrt((Math.pow(vals[0],2)+Math.pow(vals[1],2)+Math.pow(vals[2],2))));
    }
}
