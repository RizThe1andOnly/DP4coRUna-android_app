package com.example.dp4coruna.dataManagement;

import android.os.Bundle;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;
import com.example.dp4coruna.dataManagement.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dp4coruna.R;
import com.example.dp4coruna.localLearning.location.dataHolders.LocationObjectData;

public class databaseDemoActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_demo);
        Bundle bundle = getIntent().getExtras();

        ScrollView dbscroll1 = findViewById(R.id.dbScroll1);
        HorizontalScrollView dbscroll2 = findViewById(R.id.dbScroll2);
        ScrollView dbscroll3 = findViewById(R.id.dbScroll3);

        TextView dbText1 = findViewById(R.id.dbText1);
        TextView dbText2 = findViewById(R.id.dbText2);
        TextView dbText3 = findViewById(R.id.dbText3);

        AppDatabase ad = new AppDatabase(getApplicationContext());
        dbText1.setText(ad.getUserTableContents());
        dbText2.setText(ad.getUserLocationTableContents());
        //dbText3.setText(ad.getRiskZoneTableContents());
        dbText3.setText(ad.getJoinLocationAndUserTables());
    }



}
