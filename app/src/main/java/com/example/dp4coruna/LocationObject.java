package com.example.dp4coruna;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

public class LocationObject {
    private static final int NUMBER_OF_FEATURES = 7;

    private String locationLabel;
    private List<ScanResult> APs;
    private List<Double> features;

    public LocationObject(Context currentContext){
        this.locationLabel = "";
        this.features = new ArrayList<>();
        this.getWifiAccessPointsList(currentContext);
    }


    private void getWifiAccessPointsList(Context currentContext){
        WifiManager currentWifiManager = (WifiManager) currentContext.getSystemService(Context.WIFI_SERVICE);
        this.APs = currentWifiManager.getScanResults();
    }




}
