package com.example.dp4coruna;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.*;
import android.util.Log;
import androidx.core.content.ContextCompat;

import java.util.Collections;
import java.util.List;

/**
 * Class that contains methods to obtain sensor data from device.
 */
public class SensorReader {

    private static final String LOG_CAT_TAG = "SensorReader";
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
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED){

            if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)){
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                List<CellInfo> cellInfoList = tm.getAllCellInfo();
                getCellInfoFeatures(cellInfoList);
            }
        }

    }

    private static void getCellInfoFeatures(List<CellInfo> cellInfoList){
        for(CellInfo element : cellInfoList){
            String toBeLogged = "";

            if(element instanceof CellInfoGsm){
                CellIdentityGsm gsmi = ((CellInfoGsm) element).getCellIdentity();
                toBeLogged = String.valueOf(gsmi.getLac());
                toBeLogged += " " + String.valueOf(((CellInfoGsm) element).getCellSignalStrength().getLevel());
            }
            else if(element instanceof CellInfoLte){
                CellIdentityLte ltei = ((CellInfoLte) element).getCellIdentity();
                toBeLogged = String.valueOf(ltei.getTac());
                toBeLogged += " " + String.valueOf(((CellInfoLte) element).getCellSignalStrength().getLevel());
            }

            Log.i(LOG_CAT_TAG,toBeLogged);
        }
    }

}
