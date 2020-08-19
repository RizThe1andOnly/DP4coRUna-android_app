package com.example.dp4coruna.network;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.example.dp4coruna.location.LocationObject;

import java.security.PublicKey;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Transmitter implements Runnable {
    private List<String> deviceAddresses;
    private List<PublicKey> rsaEncryptKeys;
    private String transmitterAddress;
    private Context serviceContext;
    private AtomicBoolean isDestroyed;
    private AtomicBoolean isTimerFinished;
    private LocationObject locObj;


    private CountDownTimer countDownTimer = new CountDownTimer(10000, 50) {
        @Override
        public void onTick(long msLeft) {
            int progress = (int)(10000 - msLeft) / (100);
            Intent progressUpdateIntent = new Intent(NetworkTransmitActivity.RECEIVE_MESSAGE_BROADCAST);
            progressUpdateIntent.putExtra("progress", progress);
            LocalBroadcastManager.getInstance(serviceContext).sendBroadcast(progressUpdateIntent);
        }

        @Override
        public void onFinish() {
            // Update the LocationObject's measurements and display to the user. For now, that's commented out since my device can't run the sensor.
            // networkLocObj.updateLocationData();
//            String locationString = networkLocObj.toString();
//            String locationJSON = networkLocObj.convertLocationToJSON();
//            locMeasurementsField.setText(locationString);
//            Toast.makeText(NetworkTransmitActivity.this, "Location data securely sent to receiver to retrieve label.", Toast.LENGTH_SHORT).show();
            isTimerFinished.set(true);
            Log.d("Transmitter", "CountdownTimer is done.");


        }
    };

    public Transmitter(List<String> dvas, String dva, List<PublicKey> rsaEKs, Context serviceContext) {
        this.deviceAddresses = dvas;
        this.transmitterAddress = dva;
        this.rsaEncryptKeys = rsaEKs;
        this.serviceContext = serviceContext;
        this.isDestroyed = new AtomicBoolean(false);
        this.isTimerFinished = new AtomicBoolean(true);
    }

    @Override
    public void run() {
        // First, initialize the countdowntimer to run for 10 seconds and then the rest of the code in onFinish does the actual encryption and
        // transmission.
        while(!isDestroyed.get()) {
            if (isTimerFinished.get()) {
                Log.d("Transmitter", "Starting countdowntimer");
                isTimerFinished.set(false);
                countDownTimer.start();
            }
        }

    }


    public void destroy() {
        isDestroyed.set(true);
    }
}
