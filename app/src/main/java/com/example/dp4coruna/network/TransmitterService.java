package com.example.dp4coruna.network;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import java.security.PublicKey;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class TransmitterService extends Service {
    public static final String RECEIVE_BROADCAST = "com.example.network.RECEIVE_MESSAGE";

    private List<String> deviceAddresses;
    private List<PublicKey> rsaEncryptKeys;
    private String transmitterAddress;
    private Context context;
    private Transmitter transmitterRunnable;
    private Thread transmitterThread;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        Bundle params = intent.getBundleExtra("Bundle");
        deviceAddresses = params.getStringArrayList("deviceAddresses");
        rsaEncryptKeys = (ArrayList<PublicKey>) params.getSerializable("rsaEncryptKeys");
        transmitterAddress = params.getString("transmitterAddress");
        transmitterRunnable = new Transmitter(deviceAddresses, transmitterAddress, rsaEncryptKeys, this, null);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
