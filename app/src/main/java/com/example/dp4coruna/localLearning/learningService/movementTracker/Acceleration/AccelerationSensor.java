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
import com.example.dp4coruna.dataManagement.AppDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of acceleration sensor for the purpose of detecting motion.
 *
 * Implementation just started, no where near close to finish. Need to find or develop algorithm that takes
 * the accelerometer data and determine motion.
 */
public class AccelerationSensor {

    /*
            Calculate change in motion (from moving to stationary (after being stationary for a while)) by using
            data from the accelerometer. Essentially this class will use the fact that velocity is integral of
            acceleration to track movement.

            velocity is accleration x time. this class will track acceleration data and keep sampling period as
            constant to attain velocity.

            velocity of zero or around zero is stationary (around zero due to offsets present within device
            accelerometer, there will be calibration functionality in this app implemented in
            CalibrationTask class but it can only do so much and the values may not be exactly zero
            when user is stationary) and non-zero velocity means motion. This class will detect when user starts then
            stops motion, once this detection occurs location wifi access point list comparison will be called.

            When motion is first started a scan for wifi access points will be started, that list will be stored as
            a variable in the service, after the user becomes stationary again another wifi access point list will be
            obtained through a scan. Those two lists will be used for the wifi access point list comparison.
     */


    // ------------------------class constants start:

    /*
        ACCELERATION_THRESHOLD = a threshold to determine whether there is actual acceleration or if values are
        result of device offset. This is necessary because android device accelerometer's have inconsistent offsets.
        Because of this a small value will be chosen and if the absolute value of acceleration returned by accelerometer
        is less than that value then it will be set to zero.

        Android acceleration is measured in meters per second squared. The current threshold chosen is 0.01 which
        is equal to 1 centimeter per second squared.
     */
    private final double ACCELERATION_THRESHOLD = 0.01;

    private final double CONVERT_NANO_TO_REGULAR = Math.pow(10,-9); // = 1*(10^-9). Multiply with value in nano to transform to regular.

    //---------------------class constants section end


    //-----------------------class variables section start:

    private TextView outputView;
    private Context context;

    //sensor variables:
    private SensorManager accelerometerSensorManager;
    private Sensor accelerometer;
    private SensorEventListener accelerometerEventListener; // created by the createAccelerationEventListener() method

    //thread/handler related variables
    private HandlerThread ht;
    private Handler motionDetectedHandler;

    //velocity calculation variables:
    private long sensorTimeStamp; // value recorded each time sensor reports value; required for getting velocity from accel
    private List<Float> velocityVector; // 0: x-axis, 1: y-axis, 2: z-axis, 3: magnitude using euclidean formula
    private List<Float> offsetVector; // same axes as above

    //velocity state variables:
    private boolean startedMoving;
    private boolean stoppedMovingAfterStarting;

    //----------------------class variables section end


    /*
            Constructors and database callers:
                - methods to construct object and initiate class variables
                - method to call database for acceleration offsets (for calibration)
     */

    /**
     * Should only be called from an activity with a text view.
     * @param outputWindow
     * @param context
     * @param motionDetectedHandler
     */
    public AccelerationSensor(TextView outputWindow, Context context, Handler motionDetectedHandler){
        this.doAccelerationSensorClassSetup(context,motionDetectedHandler);

        //test output
        this.outputView = outputWindow;
    }

    /**
     * Called from services.
     * @param context
     * @param motionDetectedHandler
     */
    public AccelerationSensor(Context context, Handler motionDetectedHandler){
        this.doAccelerationSensorClassSetup(context,motionDetectedHandler);
    }


    /**
     * Call device database to obtain the acceleration offsets obtained through calibration.
     * Will use method from AppDatabase class.
     * @return
     */
    private List<Float> getAccelerationOffsets(){
        AppDatabase ad = new AppDatabase(this.context);
        List<Float> offsetVector = ad.getOffsetData();
        return offsetVector;
    }

    /**
     * Common method to be called by class constructors to carry out all the functions every constructor is
     * responsible for. Setting all the class variables that are to be set by each and every constructor.
     * This includes setting up the accelerometer sensor: create objects, set variables, and call methods
     * that are required for the task. Also to get the acceleration offset vector and initialize the velocity
     * vector.
     *
     * Will call helper methods to carry out tasks.
     *
     * @param context
     * @param motionDetectedHandler
     */
    private void doAccelerationSensorClassSetup(Context context, Handler motionDetectedHandler){
        this.context = context;
        this.setInitialAccelerationSensorSettings(context,motionDetectedHandler);
        this.setInitialVelocityVector();
    }

    /**
     * Sets the initial velocity vector with zero values so that it may be updated
     * each time accelerometer reports new data.
     *
     * List will have 4 elements with indices and values as follows:
     *  0: x-axis,
     *  1: y-axis,
     *  2: z-axis,
     *  3: magnitude using euclidean formula
     */
    private void setInitialVelocityVector(){
        this.velocityVector = new ArrayList<>();
        (this.velocityVector).add((float)0);
        (this.velocityVector).add((float)0);
        (this.velocityVector).add((float)0);
        (this.velocityVector).add((float)0);
    }

    /**
     * Creates/obtains the objects that will be used for sensing acceleration. The sensor manager is obtained from
     * context and then the accelerometer is obtained from the sensor manager. Here the event upon accelerometer
     * reporting is set.
     * @param context
     * @param motionDetectedHandler
     */
    private void setInitialAccelerationSensorSettings(Context context,Handler motionDetectedHandler){
        this.accelerometerSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)){
            this.accelerometer = this.accelerometerSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        }
        else{
            Toast.makeText(context,"Accelerometer not Available",Toast.LENGTH_LONG).show();
        }
        this.motionDetectedHandler = motionDetectedHandler;
        this.accelerometerEventListener = this.createAccelerationEventListener();

        //accelerometer offset saved in device:
        //get offset vector and assign to class instance
        this.offsetVector = this.getAccelerationOffsets();
    }



    /*
            Sensor data gathering and control methods
     */

    /**
     * Starts process where accelerometer will report data and that data will be processed to detect motion.
     */
    public void startSensor(){
        /*
            Creates and starts new handler thread to register the accelerometer event to.
         */

        this.ht = new HandlerThread("AccelerometerSensorThread");
        this.ht.start();

        Handler accelSensorHandler = new Handler(this.ht.getLooper());
        accelerometerSensorManager.registerListener(this.accelerometerEventListener,accelerometer,SensorManager.SENSOR_DELAY_UI,accelSensorHandler);
    }

    /**
     * Stops accelerometer reporting and processing of that data.
     */
    public void stopSensor(){
        /*
            Stops the handler thread that accelerometer event listener is register to. Un-registers the
            accelerometer event listener from the sensor manger being used by this class.
         */

        this.ht.interrupt();
        accelerometerSensorManager.unregisterListener(accelerometerEventListener);
    }

    private void testingMethod(List<Float> valuesToBeReported){
        if(this.outputView == null) return;

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if((valuesToBeReported.get(0)>=0)&&(valuesToBeReported.get(1)>=0)&&(valuesToBeReported.get(2)>=0)){
            return;
        }

        (this.outputView).append("" + (valuesToBeReported).toString() + "\n\t-----------------\n");// index 3 = magnitude
    }


    /*
        Functions called by the constructor to create different class elements:
     */

    /**
     * Event listener that accelerometer will report to. From here the linear acceleration data will be processed into
     * velocity and motion status will be determined (hopefully).
     * @return
     */
    private SensorEventListener createAccelerationEventListener(){
        SensorEventListener toBeReturned = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                /*
                    Calculate velocity using reported acceleration data:
                 */
                List<Float> accelData = getAccelerometerData(sensorEvent);
                calculateVelocity(accelData,sensorEvent.timestamp);

                //testing method used to reported collected velocity:
                testingMethod(accelData);

                boolean motionDetected = false;

                /*
                    Carry out logic to determine whether motion has been detected using acceleration data here:
                        - if motion has been detected set motionDetected to true. !
                 */


                //after motion detection takes place send message:
                if(motionDetected){
                    Message msg = (motionDetectedHandler).obtainMessage();
                    (motionDetectedHandler).sendMessage(msg);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {}//not being used, but required for interface
        };

        return toBeReturned;
    }


    /*
        Functions to be used with accelerometer thread to read data provided by the sensor
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

        //adjust the acceleration based on offset:
        for(int i=0;i<readings.length;i++){
            float reading_abs = Math.abs(readings[i]);
            float offset_abs = Math.abs((this.offsetVector).get(i));
            readings[i] = (reading_abs<offset_abs) ? 0:readings[i];
        }

        //get the acceleration magnitude:
        float accelMag = getMagnitude(readings);

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
     * @param vector
     * @return
     */
    private float getMagnitude(float[] vector){
        return ((float)Math.sqrt((Math.pow(vector[0],2)+Math.pow(vector[1],2)+Math.pow(vector[2],2))));
    }


    /*
        Process Velocity:
            - Will obtain and store velocity data based on readings provided by the accelerometer.
            - Will compare obtained velocities to determine when motion started/stopped
     */

    /**
     * Calculates velocity from given acceleration data for each axis.
     *
     * Follows the formula : V[axis]_k = V[axis]_(k-1) + A[axis]*(SAMPLING_PERIOD)
     * '_' = subscripts
     * Uses the class variables for calculations.
     * @param currentAcceleration
     * @param currentTime
     */
    private void calculateVelocity(List<Float> currentAcceleration, long currentTime){
        /*
            Both velocity and acceleration list have indices:
                0: x-axis
                1: y-axis
                2: z-axis
         */


        float adjustedAcceleration_x = currentAcceleration.get(0);
        float adjustedAcceleration_y = currentAcceleration.get(1);
        float adjustedAcceleration_z = currentAcceleration.get(2);

        //calculate the time period between reporting periods for velocity calculation:
        long prevTime = this.sensorTimeStamp;
        this.sensorTimeStamp = currentTime; //move up the timestamp to get accurate period between sensor reports
        float timeDiff = (float)((currentTime - prevTime) * CONVERT_NANO_TO_REGULAR);

        //calculate new vector values using adjusted acceleration:
        float velocity_x = (this.velocityVector).get(0) + (adjustedAcceleration_x) * timeDiff;
        float velocity_y = (this.velocityVector).get(1) + (adjustedAcceleration_y) * timeDiff;
        float velocity_z = (this.velocityVector).get(2) + (adjustedAcceleration_z) * timeDiff;

        //update velocity vector with new values:
        (this.velocityVector).set(0,velocity_x);
        (this.velocityVector).set(1,velocity_y);
        (this.velocityVector).set(2,velocity_z);
        (this.velocityVector).set(3,(this.getMagnitude(new float[]{velocity_x,velocity_y,velocity_z})));

        //check to see if user has stopped after moving or started moving after being stopped:
        determineMotionStatus(this.velocityVector);
    }

    /**
     * Determines whether user is moving or stationary based on the velocity provided through argument.
     *
     * NOTE: use the fact that reporting is in meters per second and do threshold were if its around
     * centimeters per second then its no longer significant motion.
     * @param velocity
     */
    private void determineMotionStatus(List<Float> velocity){
        
    }
}
