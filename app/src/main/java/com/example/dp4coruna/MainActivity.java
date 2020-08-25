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

import com.example.dp4coruna.localLearning.location.LocationObject;
import com.example.dp4coruna.localLearning.SubmitLocationLabel;
import com.example.dp4coruna.mapmanagement.enterDestinationActivity;
import com.example.dp4coruna.network.NetworkReceiveActivity;
import com.example.dp4coruna.network.NetworkRelayActivity;
import com.example.dp4coruna.network.NetworkTransmitActivity;
import com.example.dp4coruna.network.RelayService;
import com.example.dp4coruna.network.TransmitterService;
import com.google.android.gms.location.FusedLocationProviderClient;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    //DEMO TESTING IP ADDRESSES:
    private final String TRANSMITTER_IP_ADDRESS = "192.0.0.2"; //s9 transmitter "g960u"
    private final String RELAY_IP_ADDRESS = "192.168.1.155"; // s7 relay "g930t"
    private final String RECEIVER_IP_ADDRESS = "192.168.1.159"; //s7e receiver "g935t"

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
        // Start RelayService
        Intent relayServiceIntent = new Intent(this, RelayService.class);
        Bundle relayServiceBundle = new Bundle();
        // Hardcoded an RSA Private key here and added it into the bundle.
        relayServiceBundle.putString("privateKey", "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAIErGYgoi8NJZsnXCAL7Q+c3V/4IlCLUIAbEOBm2FcNmeyAC6X/FySVijVhs56ub95St6sjqEGLy9EIy/w2kRoOLVymyzgm+CsL8YN6BkJIy1gjn5Ay4BARTXy4xdDphR7Cmr52rSAp6LlQAgga8oPVq83LYnHNyHo259WXNoQ6ZAgMBAAECgYA7zkDcEinkGbBF9BH5j205OR68uCwelCjf+SogfXZGKsUHZVHHn0Qq0x6uun3pryVK6duzeuxrZCJEJGiDYHRk9U/S1GDg5MU71F8ICzi6GtsdXZcLM1ktPUN7fp99xH46iU/bTxi/CtUe3j7DQDHwjkqL7ru9FwSzho6A7zU0AQJBAO1cPLkES0WbzSpp88ObVyUjs+2aPEIpiuReYMU9yBE+BKQ/SD4Ambe3JDYTmnpzDyamtK5SE2p4S/ti9/UncckCQQCLT9STTYNMlDYPSpIa45H0NTLSBoRxfzgdINrnS1UN/wQor991WA8z0XpGO6fdCAebNEqsny5mQliY05BajJ5RAkEA0/uGd65wEzC8IN8TR2TahV7HeLJAks5LLv1i64TrwwpiVtX1jPo4Tq0PeAQ1+Jn9tAU6ZF0E3heltFOFI7sgkQJAfTmPHbHJWmbHiUtAtgblxZykSAIvv03aBOTpoIsos2IOPPyKYxJ659tejA9HvvlezPZeQXj83lK5DPbvhVVtYQJBAL/rruFE+2dqZ8622YIaw92qYUpaT9jaSeqIK4zyuIJdSX+MVYt8+7DrIL8+2tHI7T4QMokoPdi9oz6dLjfthaw=");
        relayServiceIntent.putExtra("pkBundle", relayServiceBundle);
        startService(relayServiceIntent);
        // Start NetworkReceiveActivity
        Bundle networkReceiveBundle = new Bundle();
        Intent networkReceiveIntent = new Intent(this, NetworkReceiveActivity.class);
        networkReceiveIntent.putExtras(networkReceiveBundle);
        startActivity(networkReceiveIntent);
    }

    public void relay(View view){
        // Start RelayService
        Intent relayServiceIntent = new Intent(this, RelayService.class);
        Bundle relayServiceBundle = new Bundle();
        // Hardcoded an RSA Private key here and added it into the bundle. This will change after the demo.
        relayServiceBundle.putString("privateKey", "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAKkQ3E+et+4z3TjjdnaLH2MIfoMQhdfp3qp9k/GAmiUuMalYK9Q5PPKoiECRN+Z/WpdyDm/W60QOqidbMgPFlf+aLtVfqF8Y7wZDt++TWpJdYyRh2mssAlzfhiItnNRX4Z4KJP9kjstFXNIFaMn1rd/L/1TVQwtz8CjXhTP8kicrAgMBAAECgYA5fRc9J75pjE9EQeyNYL9agI/sZ1jr04W4uZzt+QnxbKTMbcPxlUkJRo+WTQsSIOogJ1OLaixz5vyrB1KZf72CLx44sK/imIx0HZM7wSAZ72A/VGUoPWP+ZVk4L4bPqqq5t0Z1p09qA6BkBOXh410dCz7ARCgLEe3LN8aB30zSuQJBAO3MTtQiWqYgJXvvjJCJTrPZLJJSOulsT4C5WMBKBXQsgLqsCZ3wYnI9d4YMGCa62BwX37b73CC3HbcfynA/D3UCQQC2AbvTR5RNUtzCagBUL3YlAUfzsG7/sQQVngDebg3tepev8WKFBiw6N/PRiJ0plgidDjAIgW+EkDt1qO280SgfAkAMCC5k5WgYx7+dyb0fAxOMXgy3SpnYfbZ4GOi4sgYcnrPUvieuah9REHMfwTTnoMSWh062f3/f1+QVA/LGQyqRAkEAjk3ytjIMIRz9oEBS+3+UZ0CGKmGzl9WmtOQyF7eCykAE47re5dU6tVZUG2suPnqhR3L1WWEieUpwQwGOyAfczwJBAIYseEOXMCKQkIqVz9IPDUoChnLy0uA8n6Ibhs8AJIvfN++YE2EE4voLvJdBIFL6l8Sg61mfwmeKgfIXGxiwwMs=");
        relayServiceIntent.putExtra("pkBundle", relayServiceBundle);
        startService(relayServiceIntent);
        // Start NetworkRelayActivity
        Bundle networkRelayBundle = new Bundle();
        Intent networkRelayIntent = new Intent(this, NetworkRelayActivity.class);
        networkRelayIntent.putExtras(networkRelayBundle);
        startActivity(networkRelayIntent);
    }

    public void transmit(View view){
        // Start the transmitter service.
        Intent serviceIntent = new Intent(this, TransmitterService.class);
        // TODO: Hardcode IP addresses to this.
        ArrayList<String> deviceAddresses = new ArrayList<String>();

        //hardcoded ips:
        deviceAddresses.add(TRANSMITTER_IP_ADDRESS); // s9 transmitter "g960u"
        deviceAddresses.add(RELAY_IP_ADDRESS); // s7 relay "g930t"
        deviceAddresses.add(RECEIVER_IP_ADDRESS); // s7e receiver "g935t"

        // The b64-encoded strings here are hard-coded. After the demo, this will be changed.
        ArrayList<String> rsaEncryptKeys = new ArrayList<String>();
        rsaEncryptKeys.add("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCtCiVENSQ2bEZ3sC8XZfJ+cvGTPhkT/o3MXBqA1dB5TLuqBjZnG26DaLd22Owyv/rZ0ryZlSe9T/6kuiQuk8GrCO9ZqL2JBcCMcfxus2OY41mghtb+rY6tCxbroAj1HcnTllYktD3I7yEE7Dsx8VhUMMuzxhUqlyU8mtgVBYJtQQIDAQAB");
        rsaEncryptKeys.add("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCpENxPnrfuM90443Z2ix9jCH6DEIXX6d6qfZPxgJolLjGpWCvUOTzyqIhAkTfmf1qXcg5v1utEDqonWzIDxZX/mi7VX6hfGO8GQ7fvk1qSXWMkYdprLAJc34YiLZzUV+GeCiT/ZI7LRVzSBWjJ9a3fy/9U1UMLc/Ao14Uz/JInKwIDAQAB");
        rsaEncryptKeys.add("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCBKxmIKIvDSWbJ1wgC+0PnN1f+CJQi1CAGxDgZthXDZnsgAul/xcklYo1YbOerm/eUrerI6hBi8vRCMv8NpEaDi1cpss4JvgrC/GDegZCSMtYI5+QMuAQEU18uMXQ6YUewpq+dq0gKei5UAIIGvKD1avNy2Jxzch6NufVlzaEOmQIDAQAB");

        Bundle params = new Bundle();
        params.putStringArrayList("deviceAddresses", deviceAddresses);
        params.putStringArrayList("rsaEncryptKeys", rsaEncryptKeys);
        params.putString("transmitterAddress", "");
        //serviceIntent.putExtra("Bundle", params);(!!!)
        //startService(serviceIntent);
        // Start the UI activity that will update based on transmissions.
        Bundle bundle = new Bundle();
        Intent intent = new Intent(this, NetworkTransmitActivity.class);
        intent.putExtra("Bundle",params);
        startActivity(intent);
    }

}
