package com.example.dp4coruna.network;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

public class RelayService extends Service {
    private RelayServer relayServer;
    private Thread relayServerThread;
    private PrivateKey myPrivateKey;



    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        Log.d("RelayService", "Relay Service has begun.");
        // Get the Private Key from the intent.
        Bundle bundleFromActivity = intent.getBundleExtra("pkBundle");
        String b64PrivateKey = bundleFromActivity.getString("privateKey");
        recoverPrivateKey(b64PrivateKey);
        relayServer = new RelayServer(myPrivateKey, this);
        relayServerThread = new Thread(relayServer);
        relayServerThread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        relayServer.destroy();
        try {
            relayServerThread.join();
        } catch (InterruptedException ie) {
            Log.d("RelayService", "RelayServerThread interrupted when trying to join.");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void recoverPrivateKey(String b64PrivateKey) {
        try {
            byte[] b64PrivateKeyBytes = Base64.decode(b64PrivateKey, Base64.DEFAULT);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(b64PrivateKeyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            myPrivateKey = kf.generatePrivate(keySpec);
            Arrays.fill(b64PrivateKeyBytes, (byte) 0);
        } catch(NoSuchAlgorithmException | InvalidKeySpecException e) {
            Log.d("RelayService", "Exception thrown when trying to recover private key.");
            e.printStackTrace();
        }
    }
}
