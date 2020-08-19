package com.example.dp4coruna.network;

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

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class NetworkTransmitActivity extends AppCompatActivity {

    private ProgressBar timer;
    private TextView locMeasurementsField;
    private LocationObject networkLocObj;
    private List<String> deviceAddresses;
    private List<PublicKey> rsaEncryptKeys;

    public static final String RECEIVE_MESSAGE_BROADCAST = "com.example.dp4coruna.network.RECEIVE_MESSAGE";

    private static class TransmitHandler extends Handler {
        private ProgressBar timer;

        public TransmitHandler(ProgressBar timer) {
            super(Looper.getMainLooper());
            this.timer = timer;
        }

        @Override
        public void handleMessage(Message msg) {
            timer.setProgress(msg.what);
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
                uiHandler.sendEmptyMessage(intent.getIntExtra("progress", -1));
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

        // Create handler and receiver to listen to the TransmitterService and update the progress bar as needed.
        timer = (ProgressBar)findViewById(R.id.locationMeasurementsProgress);
        tHandler = new TransmitHandler(timer);
        tbReceiver = new TransmitBroadcastReceiver(tHandler);
        IntentFilter iFilter = new IntentFilter(RECEIVE_MESSAGE_BROADCAST);
        localBroadcastManager = LocalBroadcastManager.getInstance(NetworkTransmitActivity.this);
        localBroadcastManager.registerReceiver(tbReceiver, iFilter);
        LocationObject locObj = new LocationObject(NetworkTransmitActivity.this, this);
        // Get the list of deviceAddresses and public keys (hard-coded for now) from the bundle.
//        Intent intentFromMain = getIntent();
//        Bundle argsFromMain = intentFromMain.getBundleExtra("Bundle");
//        deviceAddresses = (ArrayList<String>) argsFromMain.getSerializable("deviceAddresses");
//        rsaEncryptKeys = (ArrayList<PublicKey>) argsFromMain.getSerializable("rsaEncryptKeys");
        



    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(tbReceiver);
    }
}
