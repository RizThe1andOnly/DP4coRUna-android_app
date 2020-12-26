package com.example.dp4coruna.mapmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dp4coruna.R;
import com.example.dp4coruna.localLearning.location.dataHolders.LocationObjectData;


public class enterDestinationActivity extends AppCompatActivity {

    EditText originstreetaddress;
    EditText origincity;
    EditText originstate;
    EditText originzipcode;

    EditText deststreetaddress;
    EditText destcity;
    EditText deststate;
    EditText destzipcode;

    LocationObjectData lod;

    private Intent intent;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_destination);
        Bundle bundle = getIntent().getExtras();

        //Get JSON from previous activity and convert into LocationObjectData to retrieve data fields
        String JSONObjectString = bundle.getString("LocationObjectData");
        lod = LocationObjectData.convertJSONToLocationObjectData(JSONObjectString);

        //connect UI Components
        originstreetaddress = findViewById(R.id.originstreetaddress);
        origincity = findViewById(R.id.origincity);
        originstate = findViewById(R.id.originstate);
        originzipcode = findViewById(R.id.originzipcode);

        deststreetaddress = findViewById(R.id.deststreetaddress);
        destcity = findViewById(R.id.destcity);
        deststate = findViewById(R.id.deststate);
        destzipcode = findViewById(R.id.destzipcode);

        //fill origin fields with location data
        originstreetaddress.setText(lod.getStreetAddress());
        origincity.setText(lod.getCity());
        originstate.setText(lod.getState());
        originzipcode.setText(lod.getZipcode());

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
        intent = new Intent(this, MapsActivity.class);
        intent.putExtras(bundle);
        intent.putExtra("originStreetAddress", originStreetAddress);
        intent.putExtra("originCity", originCity);
        intent.putExtra("originState", originState);
        intent.putExtra("originZipcode", originZipcode);
        intent.putExtra("destinationStreetAddress", destinationStreetAddress);
        intent.putExtra("destinationCity", destinationCity);
        intent.putExtra("destinationState", destinationState);
        intent.putExtra("destinationZipcode", destinationZipcode);

        //setResult(RESULT_OK,intent);
    }

}
