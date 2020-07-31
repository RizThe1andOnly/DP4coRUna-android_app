package com.example.dp4coruna;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.*;
import android.telephony.*;
import android.util.Log;
import androidx.core.content.ContextCompat;
import java.util.List;

/**
 * Class that contains methods to obtain sensor data from device.
 */
public class SensorReader implements SensorEventListener {

    private static final String LOG_CAT_TAG = "SensorReader";

    private SensorManager sm;
    private HandlerThread ht;

    private double lightLevel;
    private double geoMagenticVale;

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


    //sense light and geo-magnetic forces here:

    /**
     * Will get light level when called. Creates a new thread and waits for said thread to
     * obtain sensor readings, since sensors report data in async manner. Will call helper "obtainSensorReadings" to complete task.
     * @param context current context of the application to access the sensor services
     */
    public void getLightLevel(Context context){
        this.lightLevel = 0;
        this.sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        obtainSensorReadings(this.sm,Sensor.TYPE_LIGHT);
    }

    public void getGeoMagneticLevel(Context context){
        this.geoMagenticVale = 0;
        this.sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

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


}
