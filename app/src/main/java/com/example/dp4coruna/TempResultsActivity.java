package com.example.dp4coruna;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dp4coruna.dataManagement.AppDatabase;
import com.example.dp4coruna.localLearning.learningService.LocalLearningService;
import com.example.dp4coruna.localLearning.learningService.movementTracker.AccelerationSensor;
import com.example.dp4coruna.localLearning.learningService.movementTracker.MovementSensor;
import com.example.dp4coruna.localLearning.learningService.movementTracker.TrackMovement;
import com.example.dp4coruna.localLearning.location.LocationObject;
import com.example.dp4coruna.localLearning.SubmitLocationLabel;
import com.example.dp4coruna.ml.MLModel;
import android.database.Cursor;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;


public class TempResultsActivity extends AppCompatActivity {

    private TextView dataView;
    private AccelerationSensor acls;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_results);

        //this.checkForPermissions(getApplicationContext());

        dataView = findViewById(R.id.dataViewBox);

        // service code test calls:
        //startLLService();
        //bindToLLService();

        //acls = new AccelerationSensor(dataView,getApplicationContext());
        this.testMovementSensor();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(this.startMTSIntent);
    }

    /*
                        -----------------------------NORMAL TESTING CODE-----------------------------
     */


    private boolean controlAccl = false;
    /**
     * Will create new instances of LocationGrabber and SensorReader and get data they have to offer
     * @param view triggerSampleButton
     */
    public void onTriggerSamplingButtonPress(View view){
        //LocationObject lob = getLocationObjectData();
        //dataView.append("Current Light Level : " + lob.getLightLevel() + "\n");

//        controlAccl = !controlAccl;
//        if(controlAccl){
//            acls.startSensor();
//        }
//        else{
//            acls.stopSensor();
//        }

        this.startMovementTrackerService();
    }

    public void trainButtonEvent(View view){
        MLModel mlm = new MLModel(getApplicationContext());
        mlm.trainAndSaveModel();
        dataView.append("Successfully (maybe?) trained model and saved to device.\n");

//        LocationObject lob = new LocationObject(getApplicationContext());
//        lob.updateLocationData();
//        dataView.append(lob.convertLocationToJSON() + "\n");
    }


    /**
     * Will retrieve data from the database once pressed and show it somewhere.
     * @param view showDataBaseDataButton
     */
    public void onShowDataBaseDataButtonPress(View view){
        String toBePrinted = "";

        //if(crs == null) crs = dbt.getListContents();

//        String[] cnames = crs.getColumnNames();
//
//        for(int i=0;i< cnames.length;i++){
//            toBePrinted += cnames[i] + " : ";
//        }
//
//        toBePrinted += "\n";

        //toBePrinted = this.getMLDataObj();


        MLModel mlm = new MLModel(getApplicationContext(),MLModel.LOAD_MODEL_FROM_DEVICE);
        NDArray input = obtainDummyInputData();
        INDArray output = mlm.mln.output(input,false);

        toBePrinted += mlm.trainingData.getLabelNamesList() + "\n";
        toBePrinted += output.toStringFull();

        dataView.append(toBePrinted);
    }




    private NDArray obtainDummyInputData(){
        AppDatabase dbt = new AppDatabase(getApplicationContext());
        String queryString = "SELECT light,sound,geo_magnetic_field_strength,cell_tower_id,area_code,cell_signal_strength FROM mylist_data WHERE ID = 3";
        Cursor dataRow = dbt.getReadableDatabase().rawQuery(queryString,null);
        float[] sample = new float[6];
        dataRow.moveToNext();
        for(int i=0;i<6;i++){
            sample[i] = dataRow.getFloat(i);
        }

        NDArray inputArr = new NDArray(sample);

        return inputArr;
    }


    /*
        Testing services for obtaining location:
            - vars to be used for service testing
            - methods to test the service
     */

    private boolean controlService = false;
    private boolean controlInfoIntake = false;
    private LocalLearningService llservice;
    private Intent startIntent;
    private Thread ct;

    /**
     * Start the service. with this onCreate and onStartCommand will start. This will
     * get new thread created which will continuously update locationobject data.
     */
    private void startLLService(){
        startIntent = new Intent(this,LocalLearningService.class);
        startService(startIntent);
    }

    /**
     * Binds this activity to the created locallearningservice; meaning we can use the functions
     * within LocalLearningService here to extract the data being obtained by the service.
     */
    private void bindToLLService(){
        Intent intent = new Intent(this,LocalLearningService.class);
        bindService(intent,sconnect, Context.BIND_AUTO_CREATE);
    }


    private ServiceConnection sconnect = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LocalLearningService.LocalLearningServiceBinder binder = (LocalLearningService.LocalLearningServiceBinder) iBinder;
            llservice = binder.getBinderService();
            controlService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            controlService = false;
        }
    };




    /*
                        -----------------------------NORMAL TESTING CODE END-----------------------------
     */






    /*
                    --------------------------------DEMO CODE 8/21/2020------------------------------------
     */


    /**
     * Create a LocationObject instance and obtain current data and then store data into the device database
     * @param view
     */
    public void getAndStoreLocationData(View view){
        LocationObject lob = new LocationObject(TempResultsActivity.this, getApplicationContext());
        Intent locationLabelIntent = new Intent(this, SubmitLocationLabel.class);
        Bundle bndl = new Bundle();
        lob.updateLocationData();
        String jsonRep = lob.convertLocationToJSON();
        locationLabelIntent.putExtras(bndl);
        locationLabelIntent.putExtra("LocationObjectData", jsonRep);
        startActivity(locationLabelIntent);
        Toast.makeText(getApplicationContext(),"Location Added",Toast.LENGTH_SHORT).show();
    }

    /**
     * Trains the machine learning model with the data currently available in device database.
     * @param view
     */
    public void trainModel(View view){
        MLModel mlm = new MLModel(getApplicationContext(), MLModel.TRAIN_MODEL_AND_SAVE_IN_DEVICE);
        Toast.makeText(getApplicationContext(),"Trained Model",Toast.LENGTH_SHORT).show();
    }


    /*
                 --------------------------------DEMO CODE 8/21/2020 (END)------------------------------------
     */


    /*
                                        -------------Movement Tracker test code----------------
     */

    private MovementSensor ms;
    private boolean moveStart = false;

    private void testMovementSensor(){
        ms = new MovementSensor(getApplicationContext(),dataView,MovementSensor.CALLED_FROM_ACTIVITY);
    }

    private void movementTracking(){
        this.moveStart = !(this.moveStart);

        if(this.moveStart){
            this.ms.startMovementSensor();
        }
        else{
            this.ms.stopMovementSensor();
        }
    }


    private Intent startMTSIntent;
    //start service code:
    private void startMovementTrackerService(){
        this.startMTSIntent = new Intent(this, TrackMovement.class);
        startService(this.startMTSIntent);
    }

    /*
                                        -------------Movement Tracker test code (END)----------------
     */

}
