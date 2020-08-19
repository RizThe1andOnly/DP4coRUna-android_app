package com.example.dp4coruna.network;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.dp4coruna.MainActivity;
import com.example.dp4coruna.location.LocationObject;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;


public class TransmitterService extends Service {
    private Thread tsThread;
    private Transmitter transmitRunnable;
    private List<String> deviceAddresses;
    private List<PublicKey> rsaEncryptKeys;
    private String transmitterAddress;

    @Override
    public void onCreate() {
        // Temporarily create with empty.
        deviceAddresses = new ArrayList<String>();
        rsaEncryptKeys = new ArrayList<PublicKey>();
        transmitterAddress = "";

        transmitRunnable = new Transmitter(deviceAddresses, transmitterAddress, rsaEncryptKeys, this);
        tsThread = new Thread(transmitRunnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        // Start the thread.
        tsThread.start();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        transmitRunnable.destroy();
        try {
            tsThread.join();
        } catch(InterruptedException ie) {
            Log.d("TransmitterService", "Transmitter Thread interrupted when joining.");
            ie.printStackTrace();
        }

        super.onDestroy();

    }
}
