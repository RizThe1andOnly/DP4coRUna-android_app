package com.example.dp4coruna.network;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.dp4coruna.R;
import com.example.dp4coruna.location.LocationObject;
import com.example.dp4coruna.location.LocationObjectData;

import org.w3c.dom.Text;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkTransmitActivity extends AppCompatActivity {

    private ProgressBar timer;
    private TextView locMeasurementsField;
    private LocationObject networkLocObj;
    private List<String> deviceAddresses;
    private List<PublicKey> rsaEncryptKeys;
    private String deviceAddress;




    public static final String RECEIVE_MESSAGE_BROADCAST = "com.example.dp4coruna.network.RECEIVE_MESSAGE";


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

//        Get the list of deviceAddresses and public keys (hard-coded for now) from the bundle.
//        Intent intentFromMain = getIntent();
//        Bundle argsFromMain = intentFromMain.getBundleExtra("Bundle");
//        deviceAddresses = (ArrayList<String>) argsFromMain.getSerializable("deviceAddresses");
//        rsaEncryptKeys = (ArrayList<PublicKey>) argsFromMain.getSerializable("rsaEncryptKeys");
            deviceAddresses = new ArrayList<String>();
            rsaEncryptKeys = new ArrayList<PublicKey>();
            deviceAddress = "";

            // Instantiate the handler, receiver, and broadcastmanager that'll be used to update the UI upon receiving messages from the newly spawned thread.
            tHandler = new TransmitHandler(timer, locMeasurementsField);
            tbReceiver = new TransmitBroadcastReceiver(tHandler);
            localBroadcastManager = LocalBroadcastManager.getInstance(this);
            localBroadcastManager.registerReceiver(tbReceiver, new IntentFilter(RECEIVE_MESSAGE_BROADCAST));
            new Thread(new Transmitter(deviceAddresses, deviceAddress, rsaEncryptKeys, this, networkLocObj)).start();
        }

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isThreadRunning", true);
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        localBroadcastManager.unregisterReceiver(tbReceiver);
//    }
}
