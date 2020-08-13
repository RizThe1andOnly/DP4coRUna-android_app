package com.example.dp4coruna.location;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.dp4coruna.MainActivity;
import com.example.dp4coruna.R;
import com.example.dp4coruna.location.LocationGrabber;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.List;

public class SubmitLocationLabel extends AppCompatActivity {

    TextView latlong;
    TextView addresscurrent;

    EditText buildingname;
    EditText roomname;
    EditText roomnumber;

    LocationObjectData lod;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submit_location_label);
        Bundle bundle = getIntent().getExtras();

        //Get JSON from previous activity and convert into LocationObjectData to retrieve data fields
        String JSONObjectString = bundle.getString("LocationObjectData");
        lod = LocationObjectData.convertJSONToLocationObjectData(JSONObjectString);

        Log.d("JSON", JSONObjectString);


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
}
