package com.example.dp4coruna.location;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.dp4coruna.MainActivity;
import com.example.dp4coruna.R;
import com.example.dp4coruna.datamanagement.DatabaseTest;
import com.example.dp4coruna.location.LocationGrabber;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

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

    DatabaseTest myDatabaseHelper;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submit_location_label);
        Bundle bundle = getIntent().getExtras();

        //Get JSON from previous activity and convert into LocationObjectData to retrieve data fields
        String JSONObjectString = bundle.getString("LocationObjectData");
        lod = LocationObjectData.convertJSONToLocationObjectData(JSONObjectString);

        Log.d("JSON", JSONObjectString);

        myDatabaseHelper = new DatabaseTest(this);

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

        //This is only needed if we allow user to alter their current address
        /*
        String StreetAddress = streetAddress.getText().toString();
        String City = city.getText().toString();
        String State = state.getText().toString();
        String Country = country.getText().toString();
        String Zipcode = zipcode.getText().toString();
         */

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
        //will need to call methods from DatabaseHelper to parse and insert into DB from here



        //ADDING LOCATION DATA TO THE DATABASE:
        myDatabaseHelper.addData(lod);



/**
        AddData(lod.getBuildingName(), "GPS");
        AddData(lod.getRoomName(), "GPS");
        AddData(lod.getRoomNumber(),"GPS");
        AddData(lod.getLongitude()+ " ","GPS");
        AddData(lod.getLatitude()+" ","GPS");
        AddData(lod.getAddress(),"GPS");
        AddData(lod.getStreetAddress(),"GPS");
        AddData(lod.getCity(),"GPS");
        AddData(lod.getState(),"GPS");

        //  ADDING SENSOR DATA TO THE DATABASE:





        AddData(lod.getLightLevel()+" ","SENSORDATA");
        AddData(lod.getSoundLevel()+ " ","SENSORDATA");

        AddData(lod.getGeoMagenticValue()+" ","SENSORDATA");

        //ADDING AP TO THE DATABASE

        AddData(lod.getwifiApList()+" ","AP");
    */  //(!!!)


        Log.d("TEST", "I AM HERE AFTER ADDING DATA TO DATABASE");

        //populateListView();



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
