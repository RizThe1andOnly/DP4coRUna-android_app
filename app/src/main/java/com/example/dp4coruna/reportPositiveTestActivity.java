package com.example.dp4coruna;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dp4coruna.dataManagement.AppDatabase;
import com.example.dp4coruna.mapmanagement.MapDataStructures.AreaLabel;
import com.example.dp4coruna.phpServer.ServerConnection;

import java.util.ArrayList;
import java.util.List;


public class reportPositiveTestActivity extends AppCompatActivity {

 Button covidpositivebutton;
 Button cancelbutton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_positive_test);
        Bundle bundle = getIntent().getExtras();

        covidpositivebutton = findViewById(R.id.covidpostitivebutton);
        cancelbutton = findViewById(R.id.cancelbutton);

    }


    public void cancelButtonPressed(View view){
        Bundle bundle = new Bundle();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * This is for proof of concept only. The following only updates one entry in the database. This has the essentials
     * for updating all the places a user has visited with their positive report. All that would have to be done is loop
     * though the areasVisited list and carry out the operation for each area label.
     *
     * Regarding areasVisited: a new device (SQLite) table should be set up in device to store palces a user has visited;
     * the logic for which would have to be implemented. areasVisited should query from that table instead of the table
     * with all AreaLabels.
     *
     * Regarding network connection: the current connection to the server is functional for one query at a time. Batch
     * queries or several queries in quick succession have not been tested and should be before implementing a function
     * that updates many AWS RDS table entries at once.
     *
     * @param view
     */
    public void covidPositiveButtonPressed(View view){
        //from here will need to query database for location label matches with the infected
        //then, notify these people

       //get list of distinct locations in device database:
        List<AreaLabel> areasVisited = (new AppDatabase(getApplicationContext())).getFullAreaLabels();

        AreaLabel updateLabel = areasVisited.get(0);
        Log.i("FromReport",updateLabel.toString());

        String unprepsql = "UPDATE userLocation SET numCases = numCases + 1 WHERE building_name = \"?\" AND room_name = \"?\"";
        List<String> args = new ArrayList<>();
        args.add(updateLabel.building);
        args.add(updateLabel.area);

        (new ServerConnection(getApplicationContext(),new ServerResponseHandler(Looper.getMainLooper()))).queryDatabaseUnprepared(unprepsql,args);
    }


    private class ServerResponseHandler extends Handler {

        public ServerResponseHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            List<String> responseList = (List<String>) msg.obj;
            String response = "0";
            if(responseList.size() > 0 ) response = responseList.get(0);

            Log.i("FromReport",response);
        }
    }


}
