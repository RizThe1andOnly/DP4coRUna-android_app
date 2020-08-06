package com.example.dp4coruna;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class showRoutesActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_routes);
        Bundle bundle = getIntent().getExtras();
    }
}
