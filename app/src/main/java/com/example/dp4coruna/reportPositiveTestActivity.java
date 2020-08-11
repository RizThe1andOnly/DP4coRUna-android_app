package com.example.dp4coruna;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;


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

    public void covidPositiveButtonPressed(View view){
        //from here will need to query database for location label matches with the infected
        //then, notify these people

        Bundle bundle = new Bundle();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }


}
