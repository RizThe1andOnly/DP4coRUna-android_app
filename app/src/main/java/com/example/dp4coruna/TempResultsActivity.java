package com.example.dp4coruna;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dp4coruna.dataManagement.AppDatabase;
import com.example.dp4coruna.dataManagement.remoteDatabase.DbConnection;
import com.example.dp4coruna.localLearning.SubmitLocationLabel;
import com.example.dp4coruna.localLearning.learningService.LocalLearningService;
import com.example.dp4coruna.localLearning.learningService.movementTracker.Acceleration.AccelerationSensor;
import com.example.dp4coruna.localLearning.learningService.movementTracker.Acceleration.CalibrationTask;
import com.example.dp4coruna.localLearning.learningService.movementTracker.MovementSensor;
import com.example.dp4coruna.localLearning.learningService.movementTracker.TrackMovement;
import com.example.dp4coruna.localLearning.location.LocationObject;
import com.example.dp4coruna.localLearning.location.dataHolders.CosSimLabel;
import com.example.dp4coruna.localLearning.location.dataHolders.WiFiAccessPoint;
import com.example.dp4coruna.localLearning.location.learner.CosSimilarity;
import com.example.dp4coruna.localLearning.location.learner.SensorReader;
import com.example.dp4coruna.ml.MLModel;
import android.database.Cursor;
import org.bytedeco.opencv.presets.opencv_core;
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
            "CosSim",
            "Demo"
    };
    private String optionToRun;

    // demo vars:
    private TextView timerView;
    private Handler demoHandler;
    private Looper demoHandlerLooper;
    private Thread demoThread;
    private HandlerThread demoHt;
    private final String MAX_TIMER_VAL = "10";
    private final long COUNTDOWN_TIMER_VAL = 10000;
    private CountDownTimer demoCDT;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_results);
        dataView = findViewById(R.id.dataViewBox);
        setSpinnerView();
        timerView =  findViewById(R.id.TempResultsTextView_Timer);
        timerView.setText(MAX_TIMER_VAL);
        this.demoHt = new HandlerThread("DemoThread");
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
            case "Demo" : demoFunc(); break;
            default: dataView.append("Method For this Instruction Not Yet Implemented.\n"); break;
        }
    }


    /*
        Testing functions that will be triggered upon button press and corresponding selection in the dropdown menu
        for TempResults page.
     */
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
        //dataView.append(WiFiAccessPoint.getListStringRepresent(start));
        //dataView.append(new CosSimilarity(getApplicationContext()).checkCosSim_vs_allLocations(start));

        //CosSimLabel csl = (new CosSimilarity(getApplicationContext()).checkCosSin_vs_allLocations_v2(start));
        //dataView.append(csl.arealabel.building + "\t" +csl.arealabel.area + "\t" + csl.cosSimVal);
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

    // demo related button actions:
    private boolean startDemoVar = true;
    private void demoFunc(){
//        if(startDemoVar){
//            startDemo();
//            startDemoVar = false;
//        }
//        else{
//            stopDemo();
//        }

        DbConnection dbc = new DbConnection(null,getApplicationContext());
        dbc.createTableExample();
    }


    /*
            --------------------------Demo 2 Specific Code-------------------------------
     */

    private void startDemo(){
        (this.demoHt).start();
        (this.demoHandlerLooper) = (this.demoHt).getLooper();
        (this.demoHandler) = new OutputHandler((this.demoHandlerLooper),getApplicationContext());
        (this.demoHandler).sendMessage((this.demoHandler).obtainMessage());
    }

    private void stopDemo(){
        (this.demoHandlerLooper).quitSafely();
        (this.demoCDT).cancel();
    }


    private void runTimer(Context context){
        (this.demoCDT) = new CountDownTimer(COUNTDOWN_TIMER_VAL,1000){
            @Override
            public void onTick(long timeRemaining) {
                timerView.setText(""+timeRemaining/1000);
            }

            @Override
            public void onFinish() {
                timerView.setText(MAX_TIMER_VAL);
                List<WiFiAccessPoint> wapList = SensorReader.scanWifiAccessPoints(context);
                String cosSimResults = new CosSimilarity(context).checkCosSim_vs_allLocations(wapList);
                dataView.append(WiFiAccessPoint.getListStringRepresent(wapList)+"\n");
                dataView.append(cosSimResults + "\n");
                demoHandler.sendMessage(demoHandler.obtainMessage());
            }
        }.start();
    }

    /**
     * Handler to be used by the services to populate the TempResults textview with provided output.
     * The output will be sent using the "obj" object of the "msg" object that is part of Handler.
     * The output will be processed and printed here.
     */
    private class OutputHandler extends Handler {
        private Context context;

        public OutputHandler(Looper looperIn,Context context){
            super(looperIn);
            //this.dataview = dataview;
            this.context = context;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Handler printHandler = new Handler(Looper.getMainLooper()){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    runTimer(context);
                }
            };
            printHandler.sendMessage(printHandler.obtainMessage());
        }
    }


}
