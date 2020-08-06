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
import android.provider.MediaStore;
import android.telephony.*;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

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
    private List<WiFiAccessPoint> wifiApList;
    private double lightLevel;
    private double geoMagenticValue;
    private double soundLevel;
    private CellData currentCellData;
    private double cellId;
    private double areaCode;
    private double cellSignalStrength;


    public SensorReader(Activity inheritedActivity, Context inheritedContext){
        this.inheritedActivity = inheritedActivity;
        this.inheritedContext = inheritedContext;
    }


    //interaction with other classes:

    /**
     * Sets all sensor values by calling all the methods that obtain sensor
     * data and sets the values to the proper attributes for the object instance.
     */
    public void sense(){
        getWifiAccessPoints(this.inheritedContext,this.wifiApList);
        this.getLightLevel(this.inheritedContext);
        this.geoMagenticValue = this.getGeoMagneticField();
        this.soundLevel = this.sampleSoundLevel();
        this.currentCellData = this.getCellInfoAtMoment(this.inheritedContext,null);

        //just in case will set each individual element of cell data here as well
        if(this.currentCellData != null){
            this.cellId = (this.currentCellData).cellTowerId;
            this.cellSignalStrength = (this.currentCellData).cellSignalStrength;
            this.areaCode = (this.currentCellData).areaCode;
        }
    }



    //end of interaction section


    // get wifi access point info here:
    /**
     * Gets list of current wifi access points
     * @param context current device context required to obtain the wifi-access point data
     * @param listToBePopulated list which will hold the access point data; each element is an access point
     */
    public void getWifiAccessPoints(Context context, List<WiFiAccessPoint> listToBePopulated){
        if(context == null) return;

        if(listToBePopulated == null){
            listToBePopulated = new ArrayList<>();
        }

        //create wifimanager object to get list of wifi access point
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> tempList = wifiManager.getScanResults();

        //populate provide list with wifi access point data (ssid,rssi)
        for(ScanResult element:tempList){
            listToBePopulated.add(new WiFiAccessPoint(element.SSID,element.level));
        }
    }

    //end of wifi access point operations

    // get cell tower info here:
    /**
     * Gets the cell tower information at the time this method is called.
     * @param context current application context to access services related to obtaining cell tower info
     */
    public CellData getCellInfoAtMoment(Context context, List<CellData> listToBePopulated){
        if(listToBePopulated == null){
            listToBePopulated = new ArrayList<>();
        }

        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED){

            if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)){
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                List<CellInfo> cellInfoList = tm.getAllCellInfo();
                getCellInfoFeatures(cellInfoList,listToBePopulated);
            }
        }

        // !!! for now, need to research this more:
        if(listToBePopulated.size() == 0) return null;
        return listToBePopulated.get(0);
    }

    /**
     * Helper method to go through CellInfo list and get cell tower id, local area code/tracking area code,
     * and cell signal strength. telephonymanager.getAllCellInfo gives a list of cell infos however they all seem
     * to be the same (as of now) so only one will be used from the list.
     * @param cellInfoList list of CellInfo objects that contain data regarding cell tower
     */
    private void getCellInfoFeatures(List<CellInfo> cellInfoList, List<CellData> listToBePopulated){
        for(CellInfo element : cellInfoList){
            double signalStrength;
            double lac, tac;
            double cellId;

            if(element instanceof CellInfoGsm){
                signalStrength = ((CellInfoGsm) element).getCellSignalStrength().getLevel();// !!! adjust unit
                CellIdentityGsm gsmi = ((CellInfoGsm) element).getCellIdentity();
                lac = gsmi.getLac();
                cellId = gsmi.getCid();
                listToBePopulated.add(new CellData(signalStrength,cellId,lac,"Gsm"));
            }
            else if(element instanceof CellInfoLte){
                signalStrength = ((CellInfoLte) element).getCellSignalStrength().getLevel();// !!! adjust unit
                CellIdentityLte ltei = ((CellInfoLte) element).getCellIdentity();
                tac = ltei.getTac();
                cellId = ltei.getCi();
                listToBePopulated.add(new CellData(signalStrength,cellId,tac,"Lte"));
            }

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

    //end of light sensing


    //sound recording and sound level reporting section:

    /**
     * Returns the maximum sound amplitude measured by device mic during sampling.
     * @return
     */
    public double getSoundLevel(){
        this.setupSoundRecorder();
        double soundLevel = this.sampleSoundLevel();

        this.soundRecorder.release();
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
     *
     * Utilizes Blockingqueue to wait for sound to be recorded and sound level to be reported before allowing main thread
     * to move on.
     */
    private double sampleSoundLevel(){
        final MediaRecorder localRecorderReference = this.soundRecorder;
        final BlockingQueue<Double> blkQ = new ArrayBlockingQueue<>(1);

        //start the recorder and do dummy call
        localRecorderReference.start();
        localRecorderReference.getMaxAmplitude(); // dummy call to function because apparently first call always returns zero; actual call has to be second

        //stops the recorder after time period from SOUND_SAMPLING_TIME. Uses Timer class to delay and then stop, this
        //creates a new thread so need to use final vars -> async response
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                localRecorderReference.stop();
                //Log.i("From timer",String.valueOf(localRecorderReference.getMaxAmplitude()));
                try {
                    blkQ.put((double)localRecorderReference.getMaxAmplitude());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },this.SOUND_SAMPLING_TIME);

        //block: wait for sound level to be available / wait for async response from timer
        double soundLevelReceived = 0;
        try {
            soundLevelReceived = blkQ.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return soundLevelReceived;
    }

    //end of sound sensing


    //get geo-magnetic fields:

    /**
     * Obtains the geomagneticfield strength in nanoteslas
     * Uses an instance of LocationGrabber to obtain location details : latitude, longitude, altitude
     * These three factors are required parameters for GeomagneticField object.
     *
     * @return double geomagneti field strength (nanoteslas)
     */
    public double getGeoMagneticField(){
        LocationGrabber lg = new LocationGrabber(this.inheritedContext,this.inheritedActivity);
        lg.setupLocation();
        GeomagneticField gmf = new GeomagneticField((float)lg.getLatitude(),(float)lg.getLongitude(),
                (float)lg.getAltitude_inMeters(), System.currentTimeMillis());
        return gmf.getFieldStrength();
    }


}

