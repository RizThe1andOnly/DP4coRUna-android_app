package com.example.dp4coruna.localLearning.location.learner;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.*;
import android.media.MediaRecorder;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.*;
import android.telephony.*;
import android.util.Log;
import androidx.core.content.ContextCompat;
import com.example.dp4coruna.localLearning.location.dataHolders.CellData;
import com.example.dp4coruna.localLearning.location.dataHolders.WiFiAccessPoint;

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
public class SensorReader extends LocationGrabber implements SensorEventListener {

    private final String LOG_CAT_TAG = "SensorReader";
    private final long SOUND_SAMPLING_TIME = 3000; //in milliseconds

    //environmental scanning options:
    private final static int SCAN_LIGHT_LEVEL = 0;
    private final static int SCAN_GEOMAG_LEVEL = 1;

    //tools to obtain sensor data:
    private SensorManager sm;
    private HandlerThread ht;
    private MediaRecorder soundRecorder;

    //various sensor data:
    protected List<WiFiAccessPoint> wifiApList;
    protected double lightLevel;
    protected double geoMagenticValue;
    protected double soundLevel;
    protected CellData currentCellData;
    protected double cellId;
    protected double areaCode;
    protected double cellSignalStrength;
    protected List<Float> geoMagVector;


    /**
     * Constructor to be used to gather sensor reader and the one that will be used through super(). This one requires
     * context and activity, which are necessary for gathering sensor information.
     * @param inheritedActivity
     * @param inheritedContext
     */
    protected SensorReader(Activity inheritedActivity, Context inheritedContext){
        super(inheritedContext,inheritedActivity);
        this.wifiApList = new ArrayList<>();
        this.currentCellData = new CellData();
        this.geoMagVector = new ArrayList<>();
    }

    protected SensorReader(Context inheritedContext) {
        super(inheritedContext);
        this.wifiApList = new ArrayList<>();
        this.currentCellData = new CellData();
        this.geoMagVector = new ArrayList<>();
    }

    /**
     * Constructor to be used by LocationObject for the sole purpose of retrieving and holding json data.
     * NOT FOR ACTUAL USE
     */
    protected SensorReader(){

    }


    //interaction with other classes:

    /**
     * Sets all sensor values by calling all the methods that obtain sensor
     * data and sets the values to the proper attributes for the object instance.
     */
    public void sense(){
        //setup the location grabber so that location data is available:
        super.setupLocation();

        scanWifiAccessPoints(this.inheritedContext,this.wifiApList);
        this.runEnvironmentSensor(this.inheritedContext,SCAN_LIGHT_LEVEL);
        this.runEnvironmentSensor(this.inheritedContext,SCAN_GEOMAG_LEVEL);
        this.soundLevel = this.scanSoundLevel();
        this.scanCellInfoAtMoment(this.inheritedContext,null,this.currentCellData);

        //just in case will set each individual element of cell data here as well
        if(this.currentCellData != null){
            this.cellId = (this.currentCellData).cellTowerId;
            this.cellSignalStrength = (this.currentCellData).cellSignalStrength;
            this.areaCode = (this.currentCellData).areaCode;
        }

    }

    @Override
    public String toString() {
        return super.toString() + "\n" +
                "Sensor Data{ \n" +
                "   Light Level(unit): " + this.lightLevel + "\n" +
                "   GeoMagneticField (nanoteslas): " + this.geoMagenticValue + "\n" +
                "   Sound Level (): " + this.soundLevel + "\n"+
                "   " + this.currentCellData.toString() + "\n" +
                "   " + WiFiAccessPoint.getListStringRepresent(this.wifiApList)+ "\n" +
                "}";
    }

    //end of interaction section


    // get wifi access point info here:
    /**
     * Gets list of current wifi access points.
     *
     * This is a static method because scanning wifi access points is a utility that is required by
     * other services aside from LocationObject. The WifiScanningService class will call this method to
     * obtain list of wifi access points at two points in time to compare.
     *
     * @param context current device context required to obtain the wifi-access point data
     * @param listToBePopulated list which will hold the access point data; each element is an access point
     */
    public static void scanWifiAccessPoints(Context context, List<WiFiAccessPoint> listToBePopulated){
        if(context == null) return;
        if(listToBePopulated == null){
            listToBePopulated = new ArrayList<>();
        }


    }

    public static List<WiFiAccessPoint> scanWifiAccessPoints(Context context){
        if (context == null) return null;
        List<WiFiAccessPoint> listToBePopulated = new ArrayList<>();



        return listToBePopulated;
    }

    private static void processWifiScanResults(List<WiFiAccessPoint> listToBePopulated,Context context){
        //create wifimanager object to get list of wifi access point
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> tempList = wifiManager.getScanResults();

        if(tempList == null){
            Log.i("From wifi ap","got null list from wifi manager"); //!!!
        }
        else{
            Log.i("From wifi ap",tempList.toString());
        }

        //populate provide list with wifi access point data (ssid,rssi)
        for(ScanResult element:tempList){
            listToBePopulated.add(new WiFiAccessPoint(element.SSID,element.level,element.BSSID));
        }
    }

    private static void startWifiScan(Context context){
        final Context localContextInstance = context;

        //Create Handler Thread for wifi scanning purposes:
        HandlerThread swsHt = new HandlerThread("WifiScanThread");
        swsHt.start();
        final Looper swsLooper = swsHt.getLooper();


    }

    //end of wifi access point operations

    // get cell tower info here:

    /**
     * Gets the cell tower information at the time this method is called. Parameters are empty objects (not null, empty)
     * which will have their data fields populated by this function.
     * @param context Device Context to be used for accessing Android OS services
     * @param listToBePopulated Empty list which will be filled with CellData objects containing relevant cell details
     * @param primaryCellDataObject CellData Object that will be analyzed (first one in list) (!!! needs to be reserached)
     */
    public void scanCellInfoAtMoment(Context context, List<CellData> listToBePopulated, CellData primaryCellDataObject){
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
        CellData firstSelection = listToBePopulated.get(0);
        primaryCellDataObject.copyDataOf(firstSelection);
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


    //sense light and geomag here (Async Task !!!). Also since this uses device sensors and requires sensor even listener interface
    // the interface implement statement is above and the required methods are below.
    /**
     * Will get light/geo-mag level when called. Creates a new thread and waits for said thread to
     * obtain sensor readings, since sensors report data in async manner. Will call helper "obtainSensorReadings"
     * to complete task.
     *
     * @param context current context of the application to access the sensor services
     */
    public void runEnvironmentSensor(Context context, int scanType){
        this.lightLevel = 0;
        this.sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        switch (scanType){
            case SCAN_LIGHT_LEVEL:
                obtainSensorReadings(this.sm,Sensor.TYPE_LIGHT);
                break;

            case SCAN_GEOMAG_LEVEL:
                obtainSensorReadings(this.sm,Sensor.TYPE_MAGNETIC_FIELD);
                break;
        }
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

        if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            this.reportGeoMagVal(sensorEvent);
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


    //geo-mag sensing will take place here:
    /**
     * Report the geomagnetic field values. Will set a list of values with geo-mag vector as well as
     * overall (euclidian) magnitude.
     * @param sensorEvent
     */
    public void reportGeoMagVal(SensorEvent sensorEvent){
        /*
            Indicies:
                - 0 : x-axis
                - 1 : y-axis
                - 2 : z-axis
                - 3 : magnitude (for new created list)
         */

        float[] geoMagVector = sensorEvent.values;

        float x = geoMagVector[0];
        float y = geoMagVector[1];
        float z = geoMagVector[2];
        float magnitude = (float)Math.sqrt((Math.pow(x,2)+Math.pow(y,2)+Math.pow(z,2)));

        this.geoMagenticValue = magnitude;
        this.geoMagVector.add(x);
        this.geoMagVector.add(y);
        this.geoMagVector.add(z);
        this.geoMagVector.add(magnitude);
    }


    //sound recording and sound level reporting section (Async Task !!!) :

    /**
     * Returns the maximum sound amplitude measured by device mic during sampling.
     * @return
     */
    public double scanSoundLevel(){
        this.setupSoundRecorder();
        double soundLevel = this.sampleSoundLevel();

        this.soundRecorder.release();
        this.soundRecorder = null;

        return soundLevel;
    }

    /**
     * Creates new MediaRecorder object and call all the necessary functions to adjust settings and prepare
     * recorder to record, then sets new object to class variable "soundRecorder" so it is accessible.
     *
     * These instructions are from Android MediaRecorder package that are required to set the audio recorder to the
     * proper state. Android MediaRecorder behaves like state machine. To carry out certain functions like
     * recording sound the recorder has to be in the particular state for the task.
     */
    private void setupSoundRecorder(){
        MediaRecorder recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/recorderStore.3gp");//creates a file to store audio values (required)
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



    //get geo-magnetic fields (Async Task !!! : Async due to requiring Location data):

    /**
     * Obtains the geomagneticfield strength in nanoteslas
     * Uses an instance of LocationGrabber to obtain location details : latitude, longitude, altitude
     * These three factors are required parameters for GeomagneticField object.
     *
     * @return double geomagnetic field strength (nanoteslas)
     */
    public double scanGeoMagneticField(){
        GeomagneticField gmf = new GeomagneticField((float)this.latitude,(float)this.longitude,
                (float)this.altitude_inMeters, System.currentTimeMillis());
        return gmf.getFieldStrength();
    }



    /*
            ----------------------------Setters for sensors (required for data transference)----------------------
                                  see: databasemanagement.AppDatabase.checkIfLocationLabelExists()
     */

    public void setLightLevel(double lightLevel){
        this.lightLevel = lightLevel;
    }

    public void setSoundLevel(double soundlevel){
        this.soundLevel = soundlevel;
    }

    public void setGeoMagenticValue(double geoMag){
        this.geoMagenticValue = geoMag;
    }

    public void setCellTID(double cellTID){
        this.cellId = cellTID;
    }

    public void setAreaCode(double areaCode){
        this.areaCode = areaCode;
    }

    public void setCellSignalStrength(double cellsignalstrength){
        this.cellSignalStrength = cellsignalstrength;
    }

}

