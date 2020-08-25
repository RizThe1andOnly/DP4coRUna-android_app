package com.example.dp4coruna.network;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

import com.example.dp4coruna.localLearning.location.LocationObject;

import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.ArrayList;


public class TransmitterService extends Service {

    private List<String> deviceAddresses;
    private List<PublicKey> rsaEncryptKeys;
    private String transmitterAddress;
    private Transmitter transmitterRunnable;
    private NetworkUpdater networkUpdaterRunnable;
    private Thread transmitterThread;
    private Thread updateNetworkThread;

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

        try {
            transmitterAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (IOException ioe) {
            Log.d("TransmitterService", "IO Exception occurred when getting the local host address.");
            ioe.printStackTrace();
        }

        // First, generate the public/private key pair for this device.
        KeyPair myRSAKeys = RSA.generateKeyPair(1024);
        PublicKey myPublicKey = myRSAKeys.getPublic();
        PrivateKey myPrivateKey = myRSAKeys.getPrivate();
        // Then, connect to the network by starting a thread to run NetworkConnector. This will populate deviceAddresses and rsaEncryptKeys.
        NetworkConnector networkConnector = new NetworkConnector(deviceAddresses, rsaEncryptKeys, myPublicKey);
        Thread networkConnectorThread = new Thread(networkConnector);
        networkConnectorThread.start();
        try {
            networkConnectorThread.join();
            deviceAddresses = networkConnector.getDeviceAddresses();
            rsaEncryptKeys = networkConnector.getRsaEncryptKeys();
        } catch (InterruptedException ie) {
            Log.d("TransmitterService", "NetworkConnectorThread interrupted.");
            ie.printStackTrace();
        }
        // Now, start a thread that will a) act as a server to admit the next device and b) listen for updates thereafter.
        networkUpdaterRunnable = new NetworkUpdater(deviceAddresses, rsaEncryptKeys);
        updateNetworkThread = new Thread(networkUpdaterRunnable);
        updateNetworkThread.start();
        // ---------- Rizwan, you can put your location feature update code inside of a separate thread and call that here. -------------
        // ---------- That code should update the location object at regular intervals and then create a thread that runs   -------------
        // ---------- the Transmitter runnable to attempt and transmit that object IFF there are enough devices open on the -------------
        // ---------- network. For now, I just spawn the Transmitter thread here directly to send one instance of data over. ------------
        LocationObject locationObject = new LocationObject(this);
        locationObject.updateLocationData();
        transmitterRunnable = new Transmitter(deviceAddresses, transmitterAddress, rsaEncryptKeys, locationObject);
        transmitterThread = new Thread(transmitterRunnable);
        transmitterThread.start();
        // Finally, start the RelayService.
        Intent relayIntent = new Intent(this, RelayService.class);
        Bundle pkBundle = new Bundle();
        String myPrivateKeyString = Base64.encodeToString(myPrivateKey.getEncoded(), Base64.DEFAULT);
        pkBundle.putString("privateKey", myPrivateKeyString);
        relayIntent.putExtra("pkBundle", pkBundle);
        startService(relayIntent);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
