package com.example.dp4coruna;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.*;
import android.media.MediaRecorder;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.*;
import android.telephony.*;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class that contains methods to obtain sensor data from device.
 */
public class SensorReader implements SensorEventListener {

    private final String LOG_CAT_TAG = "SensorReader";
    private final long SOUND_SAMPLING_TIME = 3000; //in milliseconds

    private Activity inheritedActivity;
    private Context inheritedContext;

    //tools to obtain sensor data:
    private SensorManager sm;
    private HandlerThread ht;
    private MediaRecorder soundRecorder;

    //various sensor data:
    private double lightLevel;
    private double geoMagenticVale;
    private double soundLevel;
    private double cellTowerId;
    private double localAreaCode;
    private double cellSignalStrength;


    public SensorReader(){}

    public SensorReader(Activity inheritedActivity, Context inheritedContext){
        this.inheritedActivity = inheritedActivity;
        this.inheritedContext = inheritedContext;
    }

    /**
     * Gets list of current wifi access points
     * @param context current device context required to obtain the wifi-access point data
     * @param listToBePopulated list which will hold the access point data; each element is an access point
     */
    public void getWifiAccessPoints(Context context, List<ScanResult> listToBePopulated){
        if(context == null) return;

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> tempList = wifiManager.getScanResults();

        for(ScanResult element:tempList){
            listToBePopulated.add(element);
        }
    }

    /**
     * Gets the cell tower information at the time this method is called.
     * @param context current application context to access services related to obtaining cell tower info
     */
    public void getCellInfoAtMoment(Context context){
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED){

            if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)){
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                List<CellInfo> cellInfoList = tm.getAllCellInfo();
                getCellInfoFeatures(cellInfoList);
            }
        }

    }

    /**
     * Helper method to go through CellInfo list and get cell tower id, local area code/tracking area code,
     * and cell signal strength. telephonymanager.getAllCellInfo gives a list of cell infos however they all seem
     * to be the same (as of now) so only one will be used from the list.
     * @param cellInfoList list of CellInfo objects that contain data regarding cell tower
     */
    private void getCellInfoFeatures(List<CellInfo> cellInfoList){
        for(CellInfo element : cellInfoList){
            //String toBeLogged = "";

            if(element instanceof CellInfoGsm){
                CellIdentityGsm gsmi = ((CellInfoGsm) element).getCellIdentity();
                //toBeLogged = String.valueOf(gsmi.getLac());
                //toBeLogged += " " + String.valueOf(((CellInfoGsm) element).getCellSignalStrength().getLevel());

                // cellId = gsmi.getCid();//subject to change
                // lac = gsmi.getLac();
                // signalStrength = ((CellInfoGsm) element).getCellSignalStrength().getLevel(); //getLevel subject to change
            }
            else if(element instanceof CellInfoLte){
                CellIdentityLte ltei = ((CellInfoLte) element).getCellIdentity();
                //toBeLogged = String.valueOf(ltei.getTac());
                //toBeLogged += " " + String.valueOf(((CellInfoLte) element).getCellSignalStrength().getLevel());

                // cellId = ltei.getCi();//subject to change
                // lac = ltei.getTac();
                // signalStrength = ((CellInfoGsm) element).getCellSignalStrength().getLevel(); //getLevel subject to change
            }

            //Log.i(LOG_CAT_TAG,toBeLogged);
        }
    }


    //sense light here:

    /**
     * Will get light level when called. Creates a new thread and waits for said thread to
     * obtain sensor readings, since sensors report data in async manner. Will call helper "obtainSensorReadings"
     * to complete task.
     * @param context current context of the application to access the sensor services
     */
    public void getLightLevel(Context context){
        this.lightLevel = 0;
        this.sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        obtainSensorReadings(this.sm,Sensor.TYPE_LIGHT);
    }

    /**
     * Accepts the sensor manager and sensor from which to obtain readings, then creates a new handler thread to handle
     * the sensor data request. Waits for said thread to complete tasks.
     * @param sm Sensor Manager object
     * @param sensorType integer constant from the SensorManager class used with sensormanager.getDefaultSensor()
     */
    private void obtainSensorReadings(SensorManager sm, int sensorType){
        Sensor specificSensor = sm.getDefaultSensor(sensorType);

        this.ht = new HandlerThread("SensorReaderThread"); //new thread
        (this.ht).start();
        Handler sensorHandler = new Handler((this.ht).getLooper());
        sm.registerListener(SensorReader.this,specificSensor,SensorManager.SENSOR_DELAY_UI,sensorHandler);
        try{ //wait for new thread to finish
            (this.ht).join();
        } catch (InterruptedException e){
            //don't know what to do here just yet ???!!!
        }
    }

    /**
     * Callback that will receive the light level from sensor and report it
     * @param sensorEvent capture of light level data
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT){
            this.reportLightSensorVal(sensorEvent);
        }

        // free resources after readings obtained:
        (this.sm).unregisterListener(SensorReader.this);
        (this.ht).quitSafely();
    }

    /**
     * Has no purpose currently, here only because it is required by the SensorEventListener interface
     * @param sensor
     * @param i
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    /**
     * Reports the light level when the sensor is called. Will be called by the onSensorChanged callback when light
     * level is to be reported. As per android documentation light level is in values[0] of sensorevent
     * @param sensorEvent
     */
    private void reportLightSensorVal(SensorEvent sensorEvent){
        double lightSensorValue = (double)sensorEvent.values[0];
        this.lightLevel = lightSensorValue;
    }


    /**
     *  Reports the geo-mag level when the sensor is called. Will be called by the onSensorChanged callback when geo-mag
     *  level is to be reported.
     * @param sensorEvent
     */
    private void reportGeoMagneticLevel(SensorEvent sensorEvent){

    }

    //sound recording and sound level reporting section:

    /**
     * Returns the maximum sound amplitude measured by device mic during sampling.
     * @return
     */
    public double getSoundLevel(){
        this.setupSoundRecorder();
        double soundLevel = this.sampleSound();

        this.soundRecorder = null;

        return soundLevel;
    }

    /**
     * Creates new MediaRecorder object and call all the necessary functions to adjust settings and prepare
     * recorder to record, then sets new object to class variable "soundRecorder" so it is accessible.
     */
    private void setupSoundRecorder(){
        MediaRecorder recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        //recorder.setOutputFile("/dev/null");
        recorder.setOutputFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/recorderStore.3gp");
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.soundRecorder = recorder;
    }


    /**
     * Will record sound levels for pre-determined period of time. Recorded sound's max amplitude will be stored as
     * part of the MediaRecorder object which then will be accessed and returned to calling method. Pre-determined
     * period of time will be the SOUND_SAMPLING_TIME class var which.
     */
    private synchronized double sampleSound(){
        //sound sampling will start at 1 second; soundrecorder will start then a timer will stop it after 1 second
        SoundSamplingRunnable samplingTask = new SoundSamplingRunnable(this.soundRecorder,this.SOUND_SAMPLING_TIME,this.inheritedContext);
        Thread samplingThread = new Thread(samplingTask);
        samplingThread.start();
        try {
            samplingThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.i(LOG_CAT_TAG,"message after sound sampling");

        return this.soundRecorder.getMaxAmplitude();
    }

    //get geo-magnetic fields:
    public double getGeoMagneticField(){
        LocationGrabber lg = new LocationGrabber(this.inheritedContext,this.inheritedActivity);
        lg.setupLocation();
        GeomagneticField gmf = new GeomagneticField((float)lg.getLatitude(),(float)lg.getLongitude(),
                (float)lg.getAltitude_inMeters(), System.currentTimeMillis());
        return gmf.getFieldStrength();
    }


}

/**
 * Additional class in file and subclass of Runnable. Created to handle task of sampling sound by device.
 */
class SoundSamplingRunnable implements Runnable{
    private MediaRecorder recorder;
    private long samplingDuration;
    private Context inheritedContext;
    private String LOG_FROM_RUNNABLE = "From Runnable";

    public SoundSamplingRunnable(MediaRecorder recorder,long samplingDuration,Context inheritedContext){
        this.recorder = recorder;
        this.samplingDuration = samplingDuration;
        this.inheritedContext = inheritedContext;
    }

    @Override
    public void run() {
        final MediaRecorder localRecorderVar = this.recorder;
        final Context contextForTimer = this.inheritedContext;
        localRecorderVar.start();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                localRecorderVar.stop();
                Log.i(LOG_FROM_RUNNABLE,"SOund level : " + String.valueOf(localRecorderVar.getMaxAmplitude()));
                localRecorderVar.reset();
                localRecorderVar.release();
            }
        }, this.samplingDuration);


        Log.i(LOG_FROM_RUNNABLE,String.valueOf(localRecorderVar.getMaxAmplitude()));
    }
}
