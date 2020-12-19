package com.example.dp4coruna.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.dp4coruna.R;
import com.example.dp4coruna.dataManagement.AppDatabase;
import com.example.dp4coruna.localLearning.SubmitLocationLabel;
import com.example.dp4coruna.localLearning.location.LocationObject;
import com.example.dp4coruna.localLearning.location.dataHolders.AreaLabel;
import com.example.dp4coruna.localLearning.location.dataHolders.CosSimLabel;
import com.example.dp4coruna.localLearning.location.learner.CosSimilarity;
import com.example.dp4coruna.ml.MLModel;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class NetworkReceiveActivity extends AppCompatActivity {

    public static final String RECEIVE_MESSAGE_BROADCAST = "com.example.dp4coruna.network.NETWORK_RECEIVE_ACTIVITY";

    private TextView outputText;

    //random risk factor:
    int riskFactor = 0;

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

            //get location object from the bundle of data
            String jsonString = broadcastBundle.getString("outgoingMessage");
            JSONObject jo = receivedToJSON(jsonString);
            LocationObject lob;
            try {
                lob = LocationObject.getLocationFromJSON(jo.getString("loc"));
            } catch (JSONException e) {
                lob = null;
                e.printStackTrace();
            }

            AreaLabel csl = (new CosSimilarity(getApplicationContext())).checkCosSin_vs_allLocations_v2(lob.getWifiAccessPointList());
            csl.setRiskLevel(riskFactor);
            riskFactor++;

            receivedLocationView.setText(csl.toString());
            String messageToBeSent = csl.convertToJson();
            Log.i("ALJsonString",messageToBeSent);
            sendReply(broadcastBundle.getString("src"),messageToBeSent);
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

                //get the data from the network transmitted data:
                String jsonString = intent.getStringExtra("outgoingMessage");
                String decryptedMessage = intent.getStringExtra("decryptedMessage");
                String src = intent.getStringExtra("src");

                //put data in a bundle to send to handler
                Bundle b = new Bundle();
                b.putString("outgoingMessage",jsonString);
                b.putString("decryptedMessage",decryptedMessage);
                b.putString("src",src);

                Message msg = uiHandler.obtainMessage();
                msg.setData(b);
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


    private void sendReply(String src,String messageOverNetwork){
        String[] ips = {
                "192.168.1.159",
                "192.168.1.199",
                "192.168.1.164"
        };

        List<String> deviceAddresses = new ArrayList<>();
        String deviceAddress;

        deviceAddresses.addAll(Arrays.asList(ips));
        deviceAddress = ips[0];

        List<PublicKey> rsaEncryptKeys = new ArrayList<PublicKey>();
        LocationObject networkLocObj = new LocationObject(getApplicationContext());

        new Thread(new Transmitter(deviceAddresses, deviceAddress, src,rsaEncryptKeys, networkLocObj,messageOverNetwork,"receiver")).start();
    }


    public static JSONObject receivedToJSON(String receivedData){
        byte[] dataBytes = receivedData.getBytes();
        //String decodedData = new String(Base64.decode(dataBytes, Base64.DEFAULT));
        String decodedData = receivedData;
        JSONObject dataToJSON = new JSONObject();
        try {
            Log.i("TestUnEncrypt",decodedData);
            dataToJSON = new JSONObject(decodedData);
        } catch(JSONException je){
            Log.d("Receive", "JSONException thrown when decoding encrypted JSON.");
            je.printStackTrace();
        }
        return dataToJSON;
    }

    /**
     * Gets a list of area labels to get full details of
     * @return
     */
    private List<AreaLabel> getAreaList(){
        Context activityContext = getApplicationContext();
        Cursor markers = (new AppDatabase(activityContext)).queryMapMarkers();
        List<AreaLabel> alList = new ArrayList<>();

        while(markers.moveToNext()){
            String current_building = markers.getString(0);
            String current_room = markers.getString(1);
            double current_latitude = markers.getDouble(2);
            double current_longitude = markers.getDouble(3);

            String marker_title = current_building + " " + current_room;
            AreaLabel current_al = new AreaLabel(markers.getString(0),markers.getString(1),current_latitude,current_longitude);
            alList.add(current_al);
        }

        return alList;
    }

}
