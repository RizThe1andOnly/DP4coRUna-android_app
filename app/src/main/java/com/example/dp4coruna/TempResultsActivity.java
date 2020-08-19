package com.example.dp4coruna;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dp4coruna.datamanagement.DatabaseTest;
import com.example.dp4coruna.datamanagement.MLData;
import com.example.dp4coruna.location.LocationObject;
import com.example.dp4coruna.location.WiFiAccessPoint;
import com.example.dp4coruna.ml.MLModel;
import android.database.Cursor;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;

import java.util.List;


public class TempResultsActivity extends AppCompatActivity {

    private TextView dataView;

    private LocationObject lo;

    private DatabaseTest dbt;

    private Cursor crs;

    private MLModel mlm;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_results);

        //this.checkForPermissions(getApplicationContext());

        dataView = findViewById(R.id.dataViewBox);
        lo = new LocationObject(TempResultsActivity.this,getApplicationContext());

        dbt = new DatabaseTest(getApplicationContext());
    }


    /**
     * Will create new instances of LocationGrabber and SensorReader and get data they have to offer
     * @param view triggerSampleButton
     */
    public void onTriggerSamplingButtonPress(View view){
        lo.updateLocationData();
        String toBePrinted = lo.convertLocationToJSON();
        dataView.append(toBePrinted);
    }

    public void trainButtonEvent(View view){
        mlm = new MLModel(getApplicationContext());
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

        mlm = new MLModel(getApplicationContext(),MLModel.LOAD_MODEL_FROM_DEVICE);
        NDArray input = obtainDummyInputData();
        INDArray output = mlm.mln.output(input,false);

        toBePrinted += output.toStringFull();

        dataView.append(toBePrinted);
    }

    private void cursorStringMeth1(String toBePrinted){
        int k = 0;
        while(crs.moveToNext()){
            if(k == 0){
                toBePrinted += crs.getString(0);
                k++;
                continue;
            }
            toBePrinted += crs.getFloat(0);
            if(k<7){
                toBePrinted += " , ";
            }
            k++;
        }
        Log.i("FromTemp",toBePrinted + "  K = " + String.valueOf(k));
    }

    private String cursorStringMeth2(String toBePrinted){
        while(crs.moveToNext()){
            String line = "";
            int parentIndex = 0;
            for(int i=0;i<crs.getColumnCount();i++){
                if(i == 0){
                    parentIndex = crs.getInt(0);
                    continue;
                }

                if(i==1){
                    line += "(*" +crs.getColumnName(i) + ":";
                    line += crs.getString(i) + "*) , ";
                    continue;
                }

                line += "(" +crs.getColumnName(i) + ":";
                line += crs.getFloat(i) + ")";

                if(i<(crs.getColumnCount()-1)){
                    line += " ,\n";
                }
            }
            //line += getWifiApList(parentIndex);
            toBePrinted += line;
        }

        Log.i("FromTemp",toBePrinted);
        return toBePrinted;
    }



    private String getWifiApList2(){
        List<List<WiFiAccessPoint>> lwap = dbt.getWifiAPListByLocation();

        String toBeReturned = "Wifi ap list by location: {\n";
        for(int i=0;i<lwap.size();i++){
            List<WiFiAccessPoint> row = lwap.get(i);
            toBeReturned += i + " {\n";
            for(int j=0;j<row.size();j++){
                WiFiAccessPoint ap = row.get(j);
                toBeReturned += "(" + ap.getSsid() + " = " + ap.getBssid() + ") : " + ap.getRssi() + "\n";
            }
            toBeReturned += "}\n";
        }

        return toBeReturned;
    }

    private String getMLDataObj(){
        String toBeReturned = "";

        MLData mld = dbt.getMLDataFromDatabase();

        float[][] fdata = mld.features;
        float[][] ldata = mld.encodedLabels;

        toBeReturned += "Features: {\n";
        for(int i=0;i<fdata.length;i++){
            float[] row = fdata[i];
            toBeReturned += "* ";
            for(int j=0;j<row.length;j++){
                toBeReturned += row[j] + " , ";
            }
            toBeReturned += "*\n";
        }
        toBeReturned += "\n\nencodedlabels: {\n";

        for(int i=0;i< ldata.length;i++){
            toBeReturned += ldata[i][i] + "\n";
        }

        return toBeReturned;
    }

    private NDArray obtainDummyInputData(){
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
}
