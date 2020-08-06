package com.example.dp4coruna;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class showRoutesActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_routes);
        Bundle bundle = getIntent().getExtras();

        //get bundle from previous activity
        String originStreetAddress = bundle.getString("originStreetAddress");
        String originCity = bundle.getString("originCity");
        String originState = bundle.getString("originState");
        String originZipcode = bundle.getString("originZipcode");

        String destinationStreetAddress = bundle.getString("destinationStreetAddress");
        String destinationCity = bundle.getString("destinationCity");
        String destinationState = bundle.getString("destinationState");
        String destinationZipcode = bundle.getString("destinationZipcode");
}



}
