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
import com.example.dp4coruna.ml.MLData;
import com.example.dp4coruna.localLearning.location.LocationObject;
import com.example.dp4coruna.localLearning.SubmitLocationLabel;
import com.example.dp4coruna.localLearning.location.dataHolders.WiFiAccessPoint;
import com.example.dp4coruna.ml.MLModel;
import android.database.Cursor;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;

import java.util.List;


public class TempResultsActivity extends AppCompatActivity {

    private TextView dataView;
    private LocalLearningService lls;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_results);

        //this.checkForPermissions(getApplicationContext());

        dataView = findViewById(R.id.dataViewBox);

        // service code test calls:
        startLLService();
        bindToLLService();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(startIntent);
    }

    /*
                        -----------------------------NORMAL TESTING CODE-----------------------------
     */

    /**
     * Will create new instances of LocationGrabber and SensorReader and get data they have to offer
     * @param view triggerSampleButton
     */

    public void onTriggerSamplingButtonPress(View view){
        LocationObject lob = getLocationObjectData();
        dataView.append("Current Light Level : " + lob.getLightLevel() + "\n");
    }

    public void trainButtonEvent(View view){
        MLModel mlm = new MLModel(getApplicationContext());
        mlm.trainAndSaveModel();
        dataView.append("Successfully (maybe?) trained model and saved to device.\n");
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

        toBePrinted += output.toStringFull();

        dataView.append(toBePrinted);
    }




    private NDArray obtainDummyInputData(){
        AppDatabase dbt = new AppDatabase(getApplicationContext());
        String queryString = "SELECT light,sound,geo_magnetic_field_strength,cell_tower_id,area_code,cell_signal_strength FROM mylist_data WHERE ID = 1";
        Cursor dataRow = dbt.getReadableDatabase().rawQuery(queryString,null);
        float[] sample = new float[6];
        dataRow.moveToNext();
        for(int i=0;i<6;i++){
            sample[i] = dataRow.getFloat(i);
        }

        NDArray inputArr = new NDArray(new float[] {0,0,0,0,0,0});

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

    /**
     * Will bind to the locallearningservice and obtain most recent version of locationobject available.
     * @return MOst updated locationobject
     */
    private LocationObject getLocationObjectData(){
        //bindToLLService();
        if (controlService){
            Log.i("fromtempotherthread","point of failure below");
            LocationObject soughtLocation = llservice.getLocationObject();
            Toast.makeText(getApplicationContext(),"Location (Maybe) Found",Toast.LENGTH_SHORT).show();
            return soughtLocation;
        }
        else{
            Toast.makeText(getApplicationContext(),"Not connected Yet",Toast.LENGTH_SHORT).show();
            return null;
        }
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


    private void runContinuousThread(){
        ct = new Thread(new Runnable() {
            @Override
            public void run() {
                TextView dataView = findViewById(R.id.dataViewBox);
                while(controlInfoIntake){
                    LocationObject lob = getLocationObjectData();
                    Log.i("fromtempotherthread","going through loop");
                }
            }
        }, "continuousthread");
        ct.start();
    }


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

}
