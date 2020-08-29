package com.example.dp4coruna.localLearning;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.dp4coruna.MainActivity;
import com.example.dp4coruna.R;
import com.example.dp4coruna.dataManagement.AppDatabase;
import com.example.dp4coruna.localLearning.location.dataHolders.LocationObjectData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;

public class SubmitLocationLabel extends AppCompatActivity {



    TextView latlong;
    TextView addresscurrent;

    EditText buildingname;
    EditText roomname;
    EditText roomnumber;


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    LocationObjectData lod;
    private static final String TAG = "ListDataActivity";

    AppDatabase myDatabaseHelper;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submit_location_label);
        Bundle bundle = getIntent().getExtras();

        //Get JSON from previous activity and convert into LocationObjectData to retrieve data fields
        String JSONObjectString = bundle.getString("LocationObjectData");
        lod = LocationObjectData.convertJSONToLocationObjectData(JSONObjectString);

        Log.d("JSON", JSONObjectString);

        myDatabaseHelper = new AppDatabase(this);

        //connects UI components
        latlong = findViewById(R.id.latlong);
        buildingname = findViewById(R.id.buildingname);
        roomname = findViewById(R.id.roomname);
        roomnumber = findViewById(R.id.roomnumber);
        addresscurrent = findViewById(R.id.addresscurrent);

        //Display current location for user
        String show = lod.getAddress();
        addresscurrent.setText(show);
        latlong.setText("Latitude: " + lod.getLatitude() + "\nLongitude: "+ lod.getLongitude());

    }

    /**
     * When the user hits submit, LocationObjectData is generated with GPS Location and user-input location details
     * @param view
     */
    public void submitButtonPressed(View view){


        //on Submit, store user input strings in appropriate fields
        String buildingName = buildingname.getText().toString();
        String roomName = roomname.getText().toString();
        String roomNumber = roomnumber.getText().toString();

        //Add user input building name, room name and room number to LocationObjectData
        lod.setBuildingName(buildingName);
        lod.setRoomName(roomName);
        lod.setRoomNumber(roomNumber);

        //At this point, this LocationObjectData now has all fields for location and sensor data filled
        //including user input fields for room name etc


        //convert location object data fields to location label and back
        //testing purposes
        String test = lod.createLocationLabel();
        Log.d("locationlabel", test);
        LocationObjectData testlod = LocationObjectData.extractLocationLabel(test);
        Log.d("locationlabel", Double.toString(testlod.getLongitude()));



        //ADDING LOCATION DATA TO THE DATABASE:
        myDatabaseHelper.addData(lod);
        Toast.makeText(getApplicationContext(),"Added Data",Toast.LENGTH_LONG);

        myDatabaseHelper.updateExistingEntry(522,"geo_magnetic_field_strength","Cool Room");



        float[][] outputTable = new float[15][15];

        outputTable = myDatabaseHelper.getFormattedLocationFeatures();


        for(int i=0;i<=5;i++){

            Log.d("PRINTING ROW CONTAINING LOCATION FEATURES ",i+" ");

            for(int j=0;j<=3;j++) {

                Log.d("VALUE: ", outputTable[i][j] + " ");


            }


        }





        for(int i=0;i<=5;i++){

            Log.d("PRINTING UPDATED ROW CONTAINING LOCATION FEATURES ",i+" ");

            for(int j=0;j<=9;j++) {

                Log.d("VALUE: ", outputTable[i][j] + " ");



            }


        }





        //convert LocationObjectData to JSON
        String JSONstring = lod.convertLocationObjectDataToJSON();
        Log.d("JSON", JSONstring);

        //better printing of JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(JSONstring);
        String easyReadJSONString = gson.toJson(je);
        Log.d("JSON", easyReadJSONString);

        Bundle bundle = new Bundle();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void AddData(String item1, String type) {
/**
        boolean insertData = myDatabaseHelper.addData(item1, type);

        if(!insertData){

            toastMessage("Error");
        }
        else
        {

            toastMessage("Successfully inserted.");


        }
 */
    }

    public void toastMessage(String message){

        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }


    private void populateListView(){

        Log.d(TAG, "Populate ListView:  Displaying data in the list view");

        //(!!!) For the three lines below i got rid of the previous parameter in accordance with getting rid of parameter
        // tableType in getListContents in DatabaseTest. Rizwan
        Cursor data = myDatabaseHelper.getListContents();

        Cursor data2 = myDatabaseHelper.getListContents();

        Cursor data3 = myDatabaseHelper.getListContents();

        ArrayList<String> gpsList = new ArrayList();
        ArrayList<String> sensorList = new ArrayList<>();
        ArrayList<String> apList = new ArrayList<>();


        if(data.getCount()!=0) {  //If the gps data is not empty, then add to ArrayList

            while (data.moveToNext()) {

                gpsList.add(data.getString(1));


            }

        }

        if(data2.getCount()!=0) {  //If the sensor data is not empty, then add to ArrayList

            while (data2.moveToNext()) {

                sensorList.add(data2.getString(1));


            }

        }

        if(data3.getCount()!=0) {  //If the AP data is not empty, then add to ArrayList

            while (data3.moveToNext()) {

                apList.add(data3.getString(1));


            }

        }


        for (int x=0;x<gpsList.size();x++){

            Log.d(TAG, "PRINTING DATABASE FIELD: "+ gpsList.get(x));


        }


    }


}
