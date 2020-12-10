package com.example.dp4coruna.utilities;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONFunctions {

    public static JSONObject receivedToJSON(String receivedData){
        byte[] dataBytes = receivedData.getBytes();
        //String decodedData = new String(Base64.decode(dataBytes, Base64.DEFAULT));
        String decodedData = receivedData;
        JSONObject dataToJSON = new JSONObject();
        try {
            //Log.i("TestUnEncrypt",decodedData);
            dataToJSON = new JSONObject(decodedData);
        } catch(JSONException je){
            //Log.d("RelayConnection", "JSONException thrown when decoding encrypted JSON.");
            je.printStackTrace();
        }
        return dataToJSON;
    }

}
