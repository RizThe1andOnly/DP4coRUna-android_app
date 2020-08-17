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

import java.util.List;


public class TempResultsActivity extends AppCompatActivity {

    private TextView dataView;

    private LocationObject lo;

    private DatabaseTest dbt;

    private Cursor crs;

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
        dbt.addData(lo);
    }


    /**
     * Will retrieve data from the database once pressed and show it somewhere.
     * @param view showDataBaseDataButton
     */
    public void onShowDataBaseDataButtonPress(View view){
        String toBePrinted = "";

        if(crs == null) crs = dbt.getListContents();

//        String[] cnames = crs.getColumnNames();
//
//        for(int i=0;i< cnames.length;i++){
//            toBePrinted += cnames[i] + " : ";
//        }
//
//        toBePrinted += "\n";

        toBePrinted = this.getMLDataObj();

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
        int[] ldata = mld.encodedLabels;

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
            toBeReturned += ldata[i] + "\n";
        }

        return toBeReturned;
    }
}
