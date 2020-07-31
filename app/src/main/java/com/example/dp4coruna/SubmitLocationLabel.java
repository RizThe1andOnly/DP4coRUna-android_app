package com.example.dp4coruna;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class SubmitLocationLabel extends AppCompatActivity {

    EditText streetAddress;
    EditText city;
    EditText state;
    EditText zipcode;
    EditText country;

    EditText buildingname;
    EditText roomname;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submit_location_label);
        Bundle bundle = getIntent().getExtras();
        List<Address>  addresses = bundle.getParcelableArrayList("addresses");

        //connects UI textfields
        streetAddress = findViewById(R.id.streetAddress);
        city= findViewById(R.id.city);
        state = findViewById(R.id.state);
        zipcode = findViewById(R.id.zipcode);
        country = findViewById(R.id.country);
        buildingname = findViewById(R.id.buildingname);
        roomname = findViewById(R.id.roomname);

        //On create, populate text fields with location data
        //avoids null pointers where location fields do not have information
        if (addresses!=null) {
            if(addresses.get(0).getSubThoroughfare()!=null && addresses.get(0).getThoroughfare()!=null) {
                streetAddress.setText(addresses.get(0).getSubThoroughfare() + " " + addresses.get(0).getThoroughfare());
            }
            if(addresses.get(0).getLocality()!=null) {
                city.setText(addresses.get(0).getLocality());
            }
            if(addresses.get(0).getAdminArea()!=null) {
                state.setText(addresses.get(0).getAdminArea());
            }
            if(addresses.get(0).getCountryName()!=null) {
                country.setText(addresses.get(0).getCountryName());
            }
            if(addresses.get(0).getPostalCode()!=null) {
                zipcode.setText(addresses.get(0).getPostalCode());
            }


        }
    }

    public void submitButtonPressed(View view){

        //on Submit, store strings in appropriate fields
        String StreetAddress = streetAddress.getText().toString();
        String City = city.getText().toString();
        String State = state.getText().toString();
        String Country = country.getText().toString();
        String Zipcode = zipcode.getText().toString();
        String BuildingName = buildingname.getText().toString();
        String RoomName = roomname.getText().toString();

        //data will need to be stored somewhere from here

        Bundle bundle = new Bundle();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
