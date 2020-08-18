package com.example.dp4coruna.network;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_transmit);//(!!!) if this class's name is changed please change corresponding res's name in res->layout and here
        // Get the progress bar and TextView for the location object output.
        timer = findViewById(R.id.location_measurements_progress);
        locMeasurementsField = findViewById(R.id.loc_measurement_text);
        // Initialize the location object.
        networkLocObj = new LocationObject(NetworkTransmitActivity.this, getApplicationContext());
        // Get the list of deviceAddresses and public keys (hard-coded for now) from the bundle.
//        Intent intentFromMain = getIntent();
//        Bundle argsFromMain = intentFromMain.getBundleExtra("Bundle");
//        deviceAddresses = (ArrayList<String>) argsFromMain.getSerializable("deviceAddresses");
//        rsaEncryptKeys = (ArrayList<PublicKey>) argsFromMain.getSerializable("rsaEncryptKeys");
        
        // Initialize the countdown timer for what to do every 10 seconds.
        CountDownTimer countDownTimer = new CountDownTimer(10000, 50) {
            @Override
            public void onTick(long msLeft) {
                timer.setProgress((int)(10000 - msLeft) / (100));
            }

            @Override
            public void onFinish() {
                // Update the LocationObject's measurements and display to the user.
                networkLocObj.updateLocationData();
                LocationObjectData networkLocObjData = new LocationObjectData(networkLocObj);
                String locationString = networkLocObj.toString();
                String locationJSON = networkLocObjData.convertLocationObjectDataToJSON();
                locMeasurementsField.setText(locationString);
                Toast.makeText(NetworkTransmitActivity.this, "Location data securely sent to receiver to retrieve label.", Toast.LENGTH_SHORT).show();

                // Create an instance of the TransmitterThread runnable and run it on a thread.
//                Thread transmitThread = new Thread(new TransmitterRunnable(deviceAddresses, rsaEncryptKeys, locationJSON));
//                transmitThread.start();
//                try {
//                    transmitThread.join();
//                    Toast.makeText(NetworkTransmitActivity.this, "Location data securely sent to receiver to retrieve label.", Toast.LENGTH_SHORT).show();
//                } catch(InterruptedException ie) {
//                    Log.d("NetworkTransmitActivity", "TransmitterThread interrupted when waiting for it.");
//                }


            }
        };

        while (true) {
            countDownTimer.start();
        }

    }
}
