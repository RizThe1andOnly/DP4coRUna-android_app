package com.example.dp4coruna.network;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.example.dp4coruna.localLearning.location.LocationObject;

import java.io.IOException;
import java.net.InetAddress;
import java.security.PublicKey;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class TransmitterService extends Service {

    private List<String> deviceAddresses;
    private List<PublicKey> rsaEncryptKeys;
    private String transmitterAddress;
    private Transmitter transmitterRunnable;
    private Thread transmitterThread;

    @Override
    public void onCreate() {
        super.onCreate();
        deviceAddresses = new ArrayList<String>();
        rsaEncryptKeys = new ArrayList<PublicKey>();
        transmitterAddress = "";

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        Log.i("TransmitterService", "Transmitter Service started successfully.");

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
