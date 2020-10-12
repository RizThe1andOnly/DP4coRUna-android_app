package com.example.dp4coruna;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dp4coruna.dataManagement.AppDatabase;
import com.example.dp4coruna.localLearning.SubmitLocationLabel;
import com.example.dp4coruna.localLearning.learningService.LocalLearningService;
import com.example.dp4coruna.localLearning.learningService.movementTracker.Acceleration.AccelerationSensor;
import com.example.dp4coruna.localLearning.learningService.movementTracker.Acceleration.CalibrationTask;
import com.example.dp4coruna.localLearning.learningService.movementTracker.MovementSensor;
import com.example.dp4coruna.localLearning.learningService.movementTracker.TrackMovement;
import com.example.dp4coruna.localLearning.location.LocationObject;
import com.example.dp4coruna.localLearning.location.dataHolders.WiFiAccessPoint;
import com.example.dp4coruna.localLearning.location.learner.CosSimilarity;
import com.example.dp4coruna.localLearning.location.learner.SensorReader;
import com.example.dp4coruna.ml.MLModel;
import android.database.Cursor;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;

import java.util.*;


public class TempResultsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private TextView dataView;
    private Spinner options;
    private String[] optionNames= {
            "GetLocation",
            "ShowDatabase",
            "MLTrain",
            "MLOutput",
            "CosSim"
    };
    private String optionToRun;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_results);
        dataView = findViewById(R.id.dataViewBox);
        setSpinnerView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setSpinnerView(){
        this.options = findViewById(R.id.TempResultOptionSpinner);
        List<String> optionList = new ArrayList<>();
        optionList.addAll(Arrays.asList(optionNames));
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                                                                android.R.layout.simple_spinner_item,
                                                                optionList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        (this.options).setAdapter(spinnerAdapter);

        //set on item selected response:
        (this.options).setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int itemIndex, long l) {
        (this.optionToRun) = (String) adapterView.getItemAtPosition(itemIndex);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        (this.optionToRun) = (String) adapterView.getItemAtPosition(0);
    }


    public void onTRButtonPress(View view){
        switch ((this.optionToRun)){
            case "GetLocation" : getLocObj(); break;
            case "ShowDatabase" : printDBContent(); break;
            case "CosSim" : cosineSimilarityTest(); break;
            case "MLTrain" : trainMLModel(); break;
            case "MLOutput" : MLModelOutput(); break;
            default: dataView.append("Method For this Instruction Not Yet Implemented.\n"); break;
        }
    }

    public void getLocObj(){
        Intent locationSubmissionIntent = new Intent(this, SubmitLocationLabel.class);
        startActivity(locationSubmissionIntent);

        dataView.append("Location Obtained and stored!");
    }

    public void printDBContent(){
        String dataStringFull = new AppDatabase(getApplicationContext()).getLocationTableContents();
        dataView.append(dataStringFull);
    }

    public void cosineSimilarityTest(){
        List<WiFiAccessPoint> start = SensorReader.scanWifiAccessPoints(getApplicationContext());
        dataView.append(new CosSimilarity(getApplicationContext()).checkCosSim_vs_allLocations(start));
    }

    private void trainMLModel(){
        new MLModel(getApplicationContext(),MLModel.TRAIN_MODEL_AND_SAVE_IN_DEVICE);
    }

    private void MLModelOutput(){
        MLModel ml = new MLModel(getApplicationContext(),MLModel.LOAD_MODEL_FROM_DEVICE);
        LocationObject lob = new LocationObject(getApplicationContext());
        lob.updateLocationData();
        dataView.append(ml.getOutput(lob));
    }

}
