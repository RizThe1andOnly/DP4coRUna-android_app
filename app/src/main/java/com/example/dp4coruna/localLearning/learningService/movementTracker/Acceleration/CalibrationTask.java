package com.example.dp4coruna.localLearning.learningService.movementTracker.Acceleration;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.example.dp4coruna.dataManagement.AppDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class will calibrate the accelerometer data. Each device's accelerometer contains innate offset.
 * This class will measure stationary accelerometer data for a while then obtain a mean offset for
 * each axis. Upon obtaining the offset, this class will save it to in device database.
 */
public class CalibrationTask {
    /*
        Approach to task:

            1st - Have accelerometer provide data for fixed period of time and then take the final
            value presented and use that as offset.
                -- Will achieve this by having separate thread that gathers the accelerometer data and
                updates an array in the main section. This separate thread will also be given a timer task,
                which when done will interrupt the thread.
                    --- In order to carry this out we will require a Runnable and TimerTask implementation.
     */

    //class constants:
    private final long CALIBRATION_SAMPLING_PERIOD = 10000; // time in milliseconds. Currently 10 seconds.

    //class variables:
    private Context context;

    //sensor related variables:
    private SensorManager sm;
    private Sensor accelSensor;
    private SensorEventListener sel;

    private HandlerThread ht;
    private Looper calibrationLooper;

    //approach 1 variables:
    private List<Float> offsetList;


    /**
     * Class constructor that requires the context. Context is required
     * to access sensor data. Will use the context to set up sensor manager and
     * sensor. After creating the instance call calibrate() to start obtaining offset data.
     * @param context
     */
    public CalibrationTask(Context context){
        this.context = context;

        /*
            add three zero values to the offsetlist to initialize three elems.
            This is done so sensor can update these values later.
         */
        this.offsetList = new ArrayList<>();
        (this.offsetList).add((float)0);
        (this.offsetList).add((float)0);
        (this.offsetList).add((float)0);

        //set up sensors:
        this.sm = (SensorManager) (this.context).getSystemService(Context.SENSOR_SERVICE);
        this.accelSensor = (this.sm).getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    /**
     * Obtains calibration data for accelerometer and stores it in device database. This method call other,
     * internal, class methods in order to achieve this.
     */
    public void calibrate(){
        // offset data gathering section methods called here:
        this.doCalibration();

        // add offset data section:
        this.storeObtainedOffsetData();

        // data should now be obtained and saved to device SQLite database

        Log.i("FromCalibrationTask",(this.offsetList).toString());
    }

    /**
     * Obtains calibration data for accelerometer and stores it in device database. This method calls other,
     * internal, class methods in order to achieve this. Difference between this function and
     * calibrate() is that this will be called by app when it starts, and if data already exists then
     * this will not update it.
     */
    public void autoCalibrate(){
        if(this.getCalibrationValues() != null){
            return;
        }

        this.doCalibration();
        this.storeObtainedOffsetData();

    }

    /**
     * Returns the offsets obtained through calibrate() in a List<Float> for each axis. Will call the
     * getOffsetData() method from AppDatabase to get saved data. The axis indices are as
     * follows:
     *  0 : x-axis
     *  1 : y-axis
     *  2 : z-axis
     * @return List of floats containing offset data
     */
    public List<Float> getCalibrationValues(){
        AppDatabase ad = new AppDatabase(this.context);
        return (ad.getOffsetData());
    }


    /*
        Below methods are for starting and managing sensor readings during calibration. Composed of starting
        separate thread to gather sensor data, updating values based on sensor data, and then shutting down
        thread that obtains sensor data and stopping the sensor from reporting any more data.
        These methods will be linked through doCalibration() and will be called from there which itself will be
        called by calibrate().

                    --------------------Offset Data Gathering Section Start--------------------
     */

    /**
     * Function will be run by other thread. Will start and run
     * accelerometer sensing. Will also register the sensor to listener
     * which will be implemented by this class.
     */
    private void doCalibration(){
        //set up handler thread and handler to deal with the incoming sensor data
        this.ht = new HandlerThread("AccelerationCalibrationThread");
        (this.ht).start();
        this.calibrationLooper = (this.ht).getLooper();

        //below handler given a definition to start a timer task which will terminate it
        Handler calibrationHandler = new Handler((this.calibrationLooper)) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                terminationTask();
            }
        };

        /*
            Set up sensor to deliver information. The sensor
            event listener is registered and defined here.

            Also set up sensor event listener variable here so that it can be registered to listener
            then unregister when its time to stop sensor reporting.

            registerListener will have event listener defined and will
            be given the handler thread to run on.
         */
        this.sel = new SensorEventListener() {
            /**
             * Called each time sensor reports data. Will call updateOffsetValuesTask to update the list with new
             * values.
             * @param sensorEvent
             */
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                updateOffsetValuesTask(sensorEvent);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {} //method not used, only here because interface requires it

        };
        (this.sm).registerListener((this.sel),(this.accelSensor),SensorManager.SENSOR_DELAY_UI,calibrationHandler);

        //send message to handler to start terminationTimerTask
        Message msg = calibrationHandler.obtainMessage();
        calibrationHandler.sendMessage(msg);

        /*
            Wait for sensor thread to terminate here. Waiting will take place because need data
            before other functions can be executed. Since sensor reporting is asynchronous this will
            make it so we definitely have data before we continue with other functions. The thread will
            be terminated after values have been reported, after sampling time defined by CALIBRATION_SAMPLING_PERIOD,
            through the timer/timer task.
         */
        try {
            (this.ht).join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * Called by the onSensorUpdated callback everytime sensor reports data. Will update existing list of offsetvalues
     * with the ones obtained.
     * @param sensorEvent
     */
    private void updateOffsetValuesTask(SensorEvent sensorEvent){
        /*
            As found on Android Developer Documentation the indices for values are as follows:
                0 : x-axis
                1 : y-axis
                2 : z-axis
         */
        float[] obtainedOffsetValues = sensorEvent.values;

        //update values here:
        for(int i=0;i<obtainedOffsetValues.length;i++){
            float obtained_abs = Math.abs(obtainedOffsetValues[i]);
            float current_abs = Math.abs((this.offsetList).get(i));

            if(obtained_abs>current_abs){
                (this.offsetList).set(i,obtainedOffsetValues[i]);
            }
        }
    }


    /**
     * Starts timer and defines timertask which will end the
     * looper on which acceleration sensor reports. This method
     * will also unregister the sensor so that continuous reporting
     * does not take place.
     */
    private void terminationTask(){
        Timer terminationTimer = new Timer("TerminationTimer");
        terminationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sm.unregisterListener(sel); //unregister sensor listener and stop sensor from reporting
                calibrationLooper.quitSafely(); // stop the thread created to obtain sensor data
            }
        }, CALIBRATION_SAMPLING_PERIOD);
    }

                        //  -----------Offset data gathering section end-----------------------



    /*
            Section for storing obtained data into device database

                        -----Storing data section start-----------
     */

    private void storeObtainedOffsetData(){
        AppDatabase ad = new AppDatabase(this.context);
        ad.addOffsetData((this.offsetList));
    }
    //              ------------data storing section end------------------


}
