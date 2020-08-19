package com.example.dp4coruna;

import android.Manifest;
import android.content.Context;

import android.content.Intent;

import android.content.pm.PackageManager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.View;

import com.example.dp4coruna.location.LocationObject;
import com.example.dp4coruna.location.SubmitLocationLabel;
import com.example.dp4coruna.mapmanagement.enterDestinationActivity;
import com.example.dp4coruna.network.NetworkReceiveActivity;
import com.example.dp4coruna.network.NetworkRelayActivity;
import com.example.dp4coruna.network.NetworkTransmitActivity;
import com.example.dp4coruna.network.TransmitterService;
import com.google.android.gms.location.FusedLocationProviderClient;


public class MainActivity extends AppCompatActivity {

    private static final int ACCESS_FINE_LOCATION_REQUEST_CODE = 101;
    private static final int RECORD_AUDIO_REQUEST_CODE = 102;
    private static final int WRITE_TO_EXTERNAL_STORAGE_CODE = 103;
    private static final int ACCESS_WIFISTATE_REQUEST_CODE = 104;
    private static final int READ_EXTERNAL_STORAGE_CODE = 105;

    private FusedLocationProviderClient fusedLocationClient;

    public LocationObject lo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkForPermissions(getApplicationContext());

        // initializing this here allows location data to be retrieved on each subsequent activity
        lo = new LocationObject(MainActivity.this,getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, TransmitterService.class));
    }


    /**
     * Checks for permission at the start of the app.
     * Currently permission being chekced is:
     *      - Access Location Fine
     *      - Record Audio -> for sound sampling
     *      - Write to external storage -> for sound sampling
     *      - Access Wifi state
     */
    private void checkForPermissions(Context context){
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                                ACCESS_FINE_LOCATION_REQUEST_CODE);
        }

        if(ContextCompat.checkSelfPermission(context,Manifest.permission.RECORD_AUDIO)==
                PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{Manifest.permission.RECORD_AUDIO},
                                                RECORD_AUDIO_REQUEST_CODE);
        }

        if(ContextCompat.checkSelfPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                WRITE_TO_EXTERNAL_STORAGE_CODE);
        }

        if(ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_WIFI_STATE)==
                PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{Manifest.permission.ACCESS_WIFI_STATE},
                                                ACCESS_WIFISTATE_REQUEST_CODE);
        }

        if(ContextCompat.checkSelfPermission(context,Manifest.permission.READ_EXTERNAL_STORAGE)==
                PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_CODE);
        }

    }



    public void enterLocationData(View view) {
        Bundle bundle = new Bundle();
        Intent intent = new Intent(this, SubmitLocationLabel.class);

        //fill Location Object with all datafields
        lo.updateLocationData();

        //convert location object to JSON to pass through bundle to next activity
        String JSONLOD = lo.convertLocationToJSON();
        intent.putExtras(bundle);
        intent.putExtra("LocationObjectData", JSONLOD);
        startActivity(intent);
    }

    public void chooseSafeRoute(View view) {
        Bundle bundle = new Bundle();
        Intent intent = new Intent(this, enterDestinationActivity.class);

        //fill Location Object with all datafields
        lo.updateLocationData();

        //convert location object to JSON to pass through bundle to next activity
        String JSONLOD = lo.convertLocationToJSON();
        intent.putExtras(bundle);
        intent.putExtra("LocationObjectData", JSONLOD);
        startActivity(intent);
    }

    public void goToTempResPage(View view){
        Bundle bundle = new Bundle();
        Intent intent = new Intent(this,TempResultsActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void reportPositiveTest(View view){
        Bundle bundle = new Bundle();
        Intent intent = new Intent(this,reportPositiveTestActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void receive(View view){
        Bundle bundle = new Bundle();
        Intent intent = new Intent(this, NetworkReceiveActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void relay(View view){
        Bundle bundle = new Bundle();
        Intent intent = new Intent(this, NetworkRelayActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void transmit(View view){
        // First, start the TransmitterService in the background.
        startService(new Intent(this, TransmitterService.class));
        // Then, start the UI activity that will update based on transmissions.
        Bundle bundle = new Bundle();
        Intent intent = new Intent(this, NetworkTransmitActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

}
