package com.example.dp4coruna.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.dp4coruna.R;
import com.example.dp4coruna.localLearning.location.LocationObject;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

public class NetworkTransmitActivity extends AppCompatActivity {

    private ProgressBar timer;
    private TextView locMeasurementsField;
    private LocationObject networkLocObj;
    private List<String> deviceAddresses;
    private List<PublicKey> rsaEncryptKeys;
    private String deviceAddress;




    public static final String RECEIVE_MESSAGE_BROADCAST = "com.example.dp4coruna.network.NETWORK_TRANSMIT_RECEIVE_MESSAGE";


    private static class TransmitHandler extends Handler {
        private ProgressBar timer;
        private TextView locMeasurementsField;

        public TransmitHandler(ProgressBar timer, TextView locMeasurementsField) {
            super(Looper.getMainLooper());
            this.timer = timer;
            this.locMeasurementsField = locMeasurementsField;
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle broadcastBundle = msg.getData();
            int progress = broadcastBundle.getInt("progress");
            String location = broadcastBundle.getString("location");
            if (progress != -1) {
                timer.setProgress(progress);
            }
            if (!location.equals("")) {
                locMeasurementsField.setText(location);
            }
        }
    }

    private class TransmitBroadcastReceiver extends BroadcastReceiver {
        private TransmitHandler uiHandler;

        public TransmitBroadcastReceiver(TransmitHandler tHandler) {
            super();
            this.uiHandler = tHandler;
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(RECEIVE_MESSAGE_BROADCAST)) {
                Message msg = uiHandler.obtainMessage();
                Bundle broadcastBundle = new Bundle();
                broadcastBundle.putString("location", intent.getStringExtra("location"));
                broadcastBundle.putInt("progress", intent.getIntExtra("progress", -1));
                msg.setData(broadcastBundle);
                uiHandler.sendMessage(msg);
            }
        }
    }

    private TransmitBroadcastReceiver tbReceiver;
    private TransmitHandler tHandler;
    private LocalBroadcastManager localBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_transmit);//(!!!) if this class's name is changed please change corresponding res's name in res->layout and here
        // Only do the following if the activity is being launched for the first time.
        if (savedInstanceState == null) {
            // Get the progress bar, text view, and instantiate a new location object.
            timer = (ProgressBar)findViewById(R.id.locationMeasurementsProgress);
            locMeasurementsField = (TextView)findViewById(R.id.loc_measurement_text);
            networkLocObj = new LocationObject(NetworkTransmitActivity.this, getApplicationContext());
            //TODO call networkLocObj.updateLocationData() (!!!!!!!!!!!!!!!!)

//        Get the list of deviceAddresses and public keys (hard-coded for now) from the bundle.
            Intent intentFromMain = getIntent();
            Bundle argsFromMain = intentFromMain.getBundleExtra("Bundle");
            deviceAddresses = argsFromMain.getStringArrayList("deviceAddresses");
            deviceAddress = argsFromMain.getString("transmitterAddress");
            List<String> b64PublicKeys = argsFromMain.getStringArrayList("rsaEncryptKeys");
            rsaEncryptKeys = new ArrayList<PublicKey>();
            // Convert them to PublicKeys
            recoverPublicKeys(b64PublicKeys);
//            deviceAddresses = new ArrayList<String>();
//            rsaEncryptKeys = new ArrayList<PublicKey>();
//            deviceAddress = "";

            // Instantiate the handler, receiver, and broadcastmanager that'll be used to update the UI upon receiving messages from the newly spawned thread.
            tHandler = new TransmitHandler(timer, locMeasurementsField);
            tbReceiver = new TransmitBroadcastReceiver(tHandler);
            localBroadcastManager = LocalBroadcastManager.getInstance(this);

            localBroadcastManager.registerReceiver(tbReceiver, new IntentFilter(NetworkTransmitActivity.RECEIVE_MESSAGE_BROADCAST));


            //possible fix for too many threads: give below thread a name and check for that name before spawning new
            //thread

            new Thread(new Transmitter(deviceAddresses, deviceAddress, rsaEncryptKeys, this, networkLocObj)).start();
        }

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isThreadRunning", true);
    }

    public void recoverPublicKeys(List<String> b64PublicKeys) {
        for (int i = 0; i < b64PublicKeys.size(); i++) {
            try {
                byte[] publicKeyBytes = Base64.decode(b64PublicKeys.get(i), Base64.DEFAULT);
                X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                rsaEncryptKeys.add(kf.generatePublic(spec));
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                Log.d("NetworkTransmitActivity", "Exception thrown when trying to recover private key.");
                e.printStackTrace();
            }
        }
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        localBroadcastManager.unregisterReceiver(tbReceiver);
//    }
}
