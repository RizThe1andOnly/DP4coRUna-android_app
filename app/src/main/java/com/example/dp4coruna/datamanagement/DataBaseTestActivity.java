package com.example.dp4coruna.datamanagement;

import android.database.Cursor;
import android.os.Bundle;


//import com.example.dp4coruna.LocationObject;
//import com.example.dp4coruna.SensorReader;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.example.dp4coruna.R;

import java.util.ArrayList;

public class DataBaseTestActivity extends AppCompatActivity {

    private static final String TAG = "ListDataActivity";
    DatabaseTest myDatabaseHelper;
    private EditText editText;
    private Button btnAdd, btnViewData;
//    LocationObject locationObject;
  //  SensorReader sensorReader;

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myDatabaseHelper = new DatabaseTest(this);


        /**
         * The below instructions reference resources that don't exist in this project. Mainly
         * the layout file associated with this activity isn't in res->layout, so one has to be
         * created or transfered.
         */
//        editText = (EditText) findViewById(R.id.editText);
//        btnAdd = (Button) findViewById(R.id.button_id);
//        btnViewData = (Button) findViewById(R.id.button2_id);
//
//        listView = (ListView) findViewById(R.id.listView_id);



        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String newEntry = editText.getText().toString();

                if(editText.length()!=0){

                    AddData(newEntry);



                }

               /* locationObject.setBuildingName("Library");
                locationObject.setRoomName("Room 1");
                locationObject.setRoomNumber("1512");



                sensorReader.getGeoMagneticField();
                sensorReader.getSoundLevel();

*/




            }
        });


        btnViewData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                populateListView();



            }
        });



        }

    public void AddData(String newEntry) {



        boolean insertData = myDatabaseHelper.addData(newEntry,null); //(!!! asked for a type didn't know what to put so put null to suppress error, sorry if i screwed it up. Rizwan)

        if(!insertData){

            toastMessage("Error");
        }
        else
        {

            toastMessage("Successfully inserted.");


        }
    }


    public void toastMessage(String message){

        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }

    private void populateListView(){

        Log.d(TAG, "Populate ListView:  Displaying data in the list view");

        Cursor data = myDatabaseHelper.getListContents(); //(!!!) put null here because error was showing up, probably my fault. Rizwan

        ArrayList<String> listData = new ArrayList();

        if(data.getCount()!=0) {  //If the database is not empty, then add to ArrayList

            while (data.moveToNext()) {

                listData.add(data.getString(1));

                ListAdapter listAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, listData);

                listView.setAdapter(listAdapter);




            }

        }

    }



}








