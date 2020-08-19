package com.example.dp4coruna.network;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dp4coruna.R;
import com.example.dp4coruna.location.LocationObject;
import com.example.dp4coruna.location.SubmitLocationLabel;
import com.example.dp4coruna.ml.MLModel;


public class NetworkReceiveActivity extends AppCompatActivity {

    private TextView outputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_receive);//(!!!) if this class's name is changed please change corresponding res's name in res->layout and here
        outputText = findViewById(R.id.outputTextView_networkreceive);
    }


    /**
     * Triggered on press of GetData. It will create location object and store it in database.
     * @param view
     */
    public void sampleData(View view){
        LocationObject lob = new LocationObject(NetworkReceiveActivity.this,getApplicationContext());
        Intent locationLabelIntent = new Intent(this, SubmitLocationLabel.class);
        Bundle bndl = new Bundle();
        lob.updateLocationData();
        String jsonRep = lob.convertLocationToJSON();
        locationLabelIntent.putExtras(bndl);
        locationLabelIntent.putExtra("LocationObjectData", jsonRep);
        startActivity(locationLabelIntent);
    }


    /**
     * Train the machine learning model using the currently gathered data:
     * @param view
     */
    public void trainMLModel(View view){
        MLModel mlm = new MLModel(getApplicationContext(), MLModel.TRAIN_MODEL_AND_SAVE_IN_DEVICE);
        outputText.append("Model Trained \n");
    }

    /**
     * Get the array of probabilities from the machine learning model
     * @param view
     */
    public void getPredictionProbabilities(View view){
        MLModel mlm = new MLModel(getApplicationContext(),MLModel.LOAD_MODEL_FROM_DEVICE);
        //for now:(!!!) prints the parameters of the model, in the future will decode json from network and print
        //prediction probabilities
        String strng = mlm.mln.params().toStringFull();
        outputText.append(strng);
    }


}
