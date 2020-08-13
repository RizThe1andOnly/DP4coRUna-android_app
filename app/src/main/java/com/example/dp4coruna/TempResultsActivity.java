package com.example.dp4coruna;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dp4coruna.location.LocationObject;
import com.example.dp4coruna.ml.MLModel;


public class TempResultsActivity extends AppCompatActivity {

    private TextView dataView;

    private LocationObject lo;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_results);

        //this.checkForPermissions(getApplicationContext());

//        dataView = findViewById(R.id.dataViewBox);
//        lo = new LocationObject(TempResultsActivity.this,getApplicationContext());
    }


    /**
     * Will create new instances of LocationGrabber and SensorReader and get data they have to offer
     * @param view triggerSampleButton
     */
    public void onTriggerSamplingButtonPress(View view){
//        lo.updateLocationData();
//        dataView.append(lo.toString() + "\n");

        final MLModel mm = new MLModel();
        mm.obtainDataSet(TempResultsActivity.this);

        Thread thr = new Thread(new Runnable() {
            @Override
            public void run() {
                mm.createMlModel();
                Log.i("ReportParams",mm.mln.params().toStringFull());
            }
        },"mltestthread");
        thr.start();

    }
}
