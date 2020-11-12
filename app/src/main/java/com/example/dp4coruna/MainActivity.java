package com.example.dp4coruna;

import android.Manifest;
import android.content.Context;

import android.content.Intent;

import android.content.pm.PackageManager;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;

import com.example.dp4coruna.dataManagement.AppDatabase;
import com.example.dp4coruna.localLearning.learningService.LocalLearningService;
import com.example.dp4coruna.localLearning.location.LocationObject;
import com.example.dp4coruna.mapmanagement.MapTrainActivity;
import com.example.dp4coruna.mapmanagement.MapsActivity;
import com.example.dp4coruna.mapmanagement.enterDestinationActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.example.dp4coruna.localLearning.SubmitLocationLabel;


public class MainActivity extends AppCompatActivity {

    //DEMO TESTING IP ADDRESSES Hardcoded ones:
    private final String TRANSMITTER_IP_ADDRESS = "";
    private final String RELAY_IP_ADDRESS = "";
    private final String RECEIVER_IP_ADDRESS = "";

    private static final int ACCESS_FINE_LOCATION_REQUEST_CODE = 101;
    private static final int RECORD_AUDIO_REQUEST_CODE = 102;
    private static final int WRITE_TO_EXTERNAL_STORAGE_CODE = 103;
    private static final int ACCESS_WIFISTATE_REQUEST_CODE = 104;
    private static final int READ_EXTERNAL_STORAGE_CODE = 105;
    private static final int CHANGE_WIFI_STATE_CODE = 106;
    private static final int REQUEXT_CODE_FOR_ALL = 107;
    private static final String[] PERMISSION_LIST = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CHANGE_WIFI_STATE
    };

    private FusedLocationProviderClient fusedLocationClient;

    public LocationObject lo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkForPermissions(getApplicationContext());

        // initializing this here allows location data to be retrieved on each subsequent activity
        //lo = new LocationObject(MainActivity.this,getApplicationContext());

        //start local learning service:
        //startLocalLearningService();

        (new AppDatabase(getApplicationContext())).getReadableDatabase();
    }

    @Override
    public void onBackPressed() {
        // Do nothing if the back button is pressed.
    }


    private void startLocalLearningService(){
        final Intent intnt = new Intent(this, LocalLearningService.class);
        final Context selfContext = getApplicationContext();

        new Thread(new Runnable() {
            @Override
            public void run() {
                selfContext.startService(intnt);
            }
        },"ServiceThread").start();
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

        for(String perm: PERMISSION_LIST){
            if(ContextCompat.checkSelfPermission(context,perm) == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(MainActivity.this,
                                                    PERMISSION_LIST,
                                                    REQUEXT_CODE_FOR_ALL);
            }
        }

//        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
//                PackageManager.PERMISSION_DENIED) {
//            ActivityCompat.requestPermissions(MainActivity.this,
//                                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                                                ACCESS_FINE_LOCATION_REQUEST_CODE);
//        }
//
//        if(ContextCompat.checkSelfPermission(context,Manifest.permission.RECORD_AUDIO)==
//                PackageManager.PERMISSION_DENIED){
//            ActivityCompat.requestPermissions(MainActivity.this,
//                                                new String[]{Manifest.permission.RECORD_AUDIO},
//                                                RECORD_AUDIO_REQUEST_CODE);
//        }
//
//        if(ContextCompat.checkSelfPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE)==
//                PackageManager.PERMISSION_DENIED){
//            ActivityCompat.requestPermissions(MainActivity.this,
//                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                                                WRITE_TO_EXTERNAL_STORAGE_CODE);
//        }
//
//        if(ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_WIFI_STATE)==
//                PackageManager.PERMISSION_DENIED){
//            ActivityCompat.requestPermissions(MainActivity.this,
//                                                new String[]{Manifest.permission.ACCESS_WIFI_STATE},
//                                                ACCESS_WIFISTATE_REQUEST_CODE);
//        }
//
//        if(ContextCompat.checkSelfPermission(context,Manifest.permission.READ_EXTERNAL_STORAGE)==
//                PackageManager.PERMISSION_DENIED){
//            ActivityCompat.requestPermissions(MainActivity.this,
//                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                    READ_EXTERNAL_STORAGE_CODE);
//        }
//
//        if(ContextCompat.checkSelfPermission(context,Manifest.permission.CHANGE_WIFI_STATE)==
//                PackageManager.PERMISSION_DENIED){
//            ActivityCompat.requestPermissions(MainActivity.this,
//                    new String[]{Manifest.permission.CHANGE_WIFI_STATE},
//                    CHANGE_WIFI_STATE_CODE);
//        }

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

        lo = new LocationObject(MainActivity.this,getApplicationContext());

        //fill Location Object with all datafields
        lo.setupLocation();

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

    public void startMapTrainActivity(View view){
        Intent intent = new Intent(this, MapTrainActivity.class);
        startActivity(intent);
    }

}
