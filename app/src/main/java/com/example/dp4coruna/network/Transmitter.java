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
    private Context context;
    private AtomicBoolean isDestroyed;
    private AtomicBoolean isTimerFinished;
    private LocationObject locObj;


    private CountDownTimer countDownTimer = new CountDownTimer(10000, 50) {
        @Override
        public void onTick(long msLeft) {
            int progress = (int)(10000 - msLeft) / (100);
            Intent progressUpdateIntent = new Intent(NetworkTransmitActivity.RECEIVE_MESSAGE_BROADCAST);
            progressUpdateIntent.putExtra("progress", progress);
            progressUpdateIntent.putExtra("location", "");
            LocalBroadcastManager.getInstance(context).sendBroadcast(progressUpdateIntent);
        }

        @Override
        public void onFinish() {
            // Update the LocationObject's measurements and display to the user. For now, that's commented out since my device can't run the sensor.
            // networkLocObj.updateLocationData();
            // Send the updated location object to the activity to display.
            Intent locationUpdateIntent = new Intent(NetworkTransmitActivity.RECEIVE_MESSAGE_BROADCAST);
            locationUpdateIntent.putExtra("progress", -1);
            locationUpdateIntent.putExtra("location", locObj.convertLocationToJSON());
            LocalBroadcastManager.getInstance(context).sendBroadcast(locationUpdateIntent);
            isTimerFinished.set(true);
            Log.d("Transmitter", "CountdownTimer is done.");

        }
    };

    public Transmitter(List<String> dvas, String dva, List<PublicKey> rsaEKs, Context context, LocationObject locationObject) {
        this.deviceAddresses = dvas;
        this.transmitterAddress = dva;
        this.rsaEncryptKeys = rsaEKs;
        this.context = context;
        this.isDestroyed = new AtomicBoolean(false);
        this.isTimerFinished = new AtomicBoolean(true);
        this.locObj = locationObject;
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
