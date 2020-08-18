package com.example.dp4coruna.network;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dp4coruna.R;
import com.example.dp4coruna.location.LocationObject;

public class NetworkTransmit extends AppCompatActivity {

    private LocationObject lob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_transmit);//(!!!) if this class's name is changed please change corresponding res's name in res->layout and here
        lob = new LocationObject(NetworkTransmit.this,getApplicationContext());
    }


    public void getData(View view){
        lob.updateLocationData();

    }

    public void transmitData(View view){

    }
}
