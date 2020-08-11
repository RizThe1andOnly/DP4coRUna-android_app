package com.example.dp4coruna.location;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.dp4coruna.MainActivity;
import com.example.dp4coruna.R;
import com.example.dp4coruna.location.LocationGrabber;

import java.util.List;

public class SubmitLocationLabel extends AppCompatActivity {

    TextView latlong;
    TextView addresscurrent;

    EditText buildingname;
    EditText roomname;
    EditText roomnumber;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submit_location_label);
        Bundle bundle = getIntent().getExtras();
        //List<Address>  addresses = bundle.getParcelableArrayList("addresses");

        //create LocationObject and parse data to obtain street address etc for UI
        LocationObject lo = new LocationObject(this, this);
        lo.setupLocation();
        List<Address> addresses = lo.getListOfAddresses();

        //connects UI components
        latlong = findViewById(R.id.latlong);
        buildingname = findViewById(R.id.buildingname);
        roomname = findViewById(R.id.roomname);
        roomnumber = findViewById(R.id.roomnumber);
        addresscurrent = findViewById(R.id.addresscurrent);

        String show = "";
        //On create, populate text fields with location data
        //avoids null pointers where location fields do not have information
        if (addresses!=null) {
            if(addresses.get(0).getSubThoroughfare()!=null && addresses.get(0).getThoroughfare()!=null) {
                //streetAddress.setText(addresses.get(0).getSubThoroughfare() + " " + addresses.get(0).getThoroughfare());
                //streetaddress.setText(addresses.get(0).getSubThoroughfare() + " " + addresses.get(0).getThoroughfare());
                show = (addresses.get(0).getSubThoroughfare() + " " + addresses.get(0).getThoroughfare()+ '\n');
            }
            if(addresses.get(0).getLocality()!=null) {
                //city.setText(addresses.get(0).getLocality());
                show = show + addresses.get(0).getLocality() + ", ";
            }
            if(addresses.get(0).getAdminArea()!=null) {
                //state.setText(addresses.get(0).getAdminArea());
                show = show + addresses.get(0).getAdminArea() + " ";
            }
            if(addresses.get(0).getPostalCode()!=null) {
                //zipcode.setText(addresses.get(0).getPostalCode());
                show = show + addresses.get(0).getPostalCode();
            }
            if(addresses.get(0).getCountryName()!=null) {
                //country.setText(addresses.get(0).getCountryName());
                show = show + '\n' + addresses.get(0).getCountryName();
            }
            addresscurrent.setText(show);
            latlong.setText("Latitude: " + lo.getLatitude() + "\nLongitude: " + lo.getLongitude());


        }
    }

    /**
     * When the user hits submit, LocationObject is generated with GPS Location and user-input location details
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

        //on Submit, store strings in appropriate fields
        String buildingName = buildingname.getText().toString();
        String roomName = roomname.getText().toString();
        String roomNumber = roomnumber.getText().toString();


        //Store GPS and UI data in LocationObject
        LocationObject lo = new LocationObject(this, this);
        lo.setupLocation();

        lo.setBuildingName(buildingName);
        lo.setRoomName(roomName);
        lo.setRoomNumber(roomNumber);

        //will need to add sensor information to this location object
        //then call methods in DatabaseHelper to parse and insert into DB

        Bundle bundle = new Bundle();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
