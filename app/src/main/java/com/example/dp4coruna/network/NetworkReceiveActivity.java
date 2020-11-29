package com.example.dp4coruna.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.dp4coruna.R;
import com.example.dp4coruna.localLearning.SubmitLocationLabel;
import com.example.dp4coruna.localLearning.location.LocationObject;
import com.example.dp4coruna.ml.MLModel;


public class NetworkReceiveActivity extends AppCompatActivity {

    public static final String RECEIVE_MESSAGE_BROADCAST = "com.example.dp4coruna.network.NETWORK_RECEIVE_ACTIVITY";

    private TextView outputText;

    private class ReceiverHandler extends Handler {
        private TextView receivedLocationView;

        public ReceiverHandler(TextView receivedLocation) {
            super(Looper.getMainLooper());
            this.receivedLocationView = receivedLocation;
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle broadcastBundle = msg.getData();
            String receivedLocation = broadcastBundle.getString("decryptedMessage");
            receivedLocationView.setText(receivedLocation);
        }

    }

    private class ReceiverBroadcastReceiver extends BroadcastReceiver {
        private ReceiverHandler uiHandler;

        public ReceiverBroadcastReceiver(ReceiverHandler receiverHandler) {
            super();
            this.uiHandler = receiverHandler;
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(RECEIVE_MESSAGE_BROADCAST)) {
                Message msg = uiHandler.obtainMessage();
                Bundle broadcastBundle = new Bundle();
                broadcastBundle.putString("decryptedMessage", intent.getStringExtra("decryptedMessage"));
                msg.setData(broadcastBundle);
                uiHandler.sendMessage(msg);
            }
        }

    }

    private ReceiverHandler receiverHandler;
    private ReceiverBroadcastReceiver receiverBroadcastReceiver;
    private LocalBroadcastManager localBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_receive);//(!!!) if this class's name is changed please change corresponding res's name in res->layout and here
        outputText = findViewById(R.id.outputTextView_networkreceive);
        receiverHandler = new ReceiverHandler(outputText);
        receiverBroadcastReceiver = new ReceiverBroadcastReceiver(receiverHandler);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        localBroadcastManager.registerReceiver(receiverBroadcastReceiver, new IntentFilter(NetworkReceiveActivity.RECEIVE_MESSAGE_BROADCAST));

        new Thread(new RelayServer(null,getApplicationContext())).start();
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
