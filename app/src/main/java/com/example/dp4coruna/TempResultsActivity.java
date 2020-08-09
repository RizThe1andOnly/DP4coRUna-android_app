package com.example.dp4coruna;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class TempResultsActivity extends AppCompatActivity {

    private static final int ACCESS_FINE_LOCATION_REQUEST_CODE = 201;
    private static final int RECORD_AUDIO_REQUEST_CODE = 202;
    private static final int WRITE_TO_EXTERNAL_STORAGE_CODE = 203;

    private SensorReader sr;
    private LocationGrabber lg;

    private TextView dataView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_results);

        //this.checkForPermissions(getApplicationContext());

        dataView = findViewById(R.id.dataViewBox);
        sr = new SensorReader(TempResultsActivity.this,getApplicationContext());
        lg = new LocationGrabber(getApplicationContext(),TempResultsActivity.this);
    }


    /**
     * Will create new instances of LocationGrabber and SensorReader and get data they have to offer
     * @param view triggerSampleButton
     */
    public void onTriggerSamplingButtonPress(View view){

        sr.sense();
        lg.setupLocation();

        String presentString = "Data Sampled: \n" +
                                "   " + sr.toString() + "\n" +
                                "   " + lg.toString() + "\n" +
                                "--- end of section---\n";

        dataView.append(presentString);
    }
}
