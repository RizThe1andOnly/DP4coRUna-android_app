package com.example.dp4coruna;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dp4coruna.location.LocationObject;


public class TempResultsActivity extends AppCompatActivity {

    private TextView dataView;

    private LocationObject lo;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_results);

        //this.checkForPermissions(getApplicationContext());

        dataView = findViewById(R.id.dataViewBox);
        lo = new LocationObject(TempResultsActivity.this,getApplicationContext());
    }


    /**
     * Will create new instances of LocationGrabber and SensorReader and get data they have to offer
     * @param view triggerSampleButton
     */
    public void onTriggerSamplingButtonPress(View view){
        lo.updateLocationData();
        dataView.append(lo.toString() + "\n");
    }
}
