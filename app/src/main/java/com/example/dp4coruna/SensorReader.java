package com.example.dp4coruna;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.Collections;
import java.util.List;

/**
 * Class that contains methods to obtain sensor data from device.
 */
public class SensorReader {

    /**
     * Gets list of current wifi access points
     * @param context current device context required to obtain the wifi-access point data
     * @param listToBePopulated list which will hold the access point data; each element is an access point
     */
    public static void getWifiAccessPoints(Context context, List<ScanResult> listToBePopulated){
        if(context == null) return;

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> tempList = wifiManager.getScanResults();

        for(ScanResult element:tempList){
            listToBePopulated.add(element);
        }
    }

    public static void getCellInfoAtMoment(Context context){
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)){

        }

    }

}
