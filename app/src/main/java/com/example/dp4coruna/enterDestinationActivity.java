package com.example.dp4coruna;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.util.List;


public class enterDestinationActivity extends AppCompatActivity {

    EditText originstreetaddress;
    EditText origincity;
    EditText originstate;
    EditText originzipcode;

    EditText deststreetaddress;
    EditText destcity;
    EditText deststate;
    EditText destzipcode;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_destination);
        Bundle bundle = getIntent().getExtras();

        originstreetaddress = findViewById(R.id.originstreetaddress);
        origincity = findViewById(R.id.origincity);
        originstate = findViewById(R.id.originstate);
        originzipcode = findViewById(R.id.originzipcode);

        deststreetaddress = findViewById(R.id.deststreetaddress);
        destcity = findViewById(R.id.destcity);
        deststate = findViewById(R.id.deststate);
        destzipcode = findViewById(R.id.destzipcode);

        LocationGrabber lg = new LocationGrabber(this, this);
        lg.setupLocation();
        List<Address> addresses = lg.addresses;

        if (addresses != null) {
            if (addresses.get(0).getSubThoroughfare() != null && addresses.get(0).getThoroughfare() != null) {
                originstreetaddress.setText(addresses.get(0).getSubThoroughfare() + " " + addresses.get(0).getThoroughfare());
                //streetaddress.setText(addresses.get(0).getSubThoroughfare() + " " + addresses.get(0).getThoroughfare());
                // show = (addresses.get(0).getSubThoroughfare() + " " + addresses.get(0).getThoroughfare()+ '\n');
            }
            if (addresses.get(0).getLocality() != null) {
                origincity.setText(addresses.get(0).getLocality());
                //show = show + addresses.get(0).getLocality() + ", ";
            }
            if (addresses.get(0).getAdminArea() != null) {
                originstate.setText(addresses.get(0).getAdminArea());
                // show = show + addresses.get(0).getAdminArea() + " ";
            }
            if (addresses.get(0).getPostalCode() != null) {
                originzipcode.setText(addresses.get(0).getPostalCode());
                //show = show + addresses.get(0).getPostalCode();
            }


        }
    }

    public void showRoutes(View view) {
        //save user input fields into bundle to send to showRoutesActivity
        String originStreetAddress = originstreetaddress.getText().toString();
        String originCity = origincity.getText().toString();
        String originState = originstate.getText().toString();
        String originZipcode = originzipcode.getText().toString();

        String destinationStreetAddress = deststreetaddress.getText().toString();
        String destinationCity = destcity.getText().toString();
        String destinationState = deststate.getText().toString();
        String destinationZipcode = destzipcode.getText().toString();

        Bundle bundle = new Bundle();
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtras(bundle);
        intent.putExtra(originStreetAddress, originStreetAddress);
        intent.putExtra(originCity, originCity);
        intent.putExtra(originState, originState);
        intent.putExtra(originZipcode, originZipcode);
        intent.putExtra(destinationStreetAddress, destinationStreetAddress);
        intent.putExtra(destinationCity, destinationCity);
        intent.putExtra(destinationState, destinationState);
        intent.putExtra(destinationZipcode, destinationZipcode);




        startActivity(intent);
    }

}
