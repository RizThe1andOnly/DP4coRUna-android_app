package com.example.dp4coruna.network;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Base64;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.example.dp4coruna.localLearning.location.LocationObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class Transmitter implements Runnable {
    private List<String> deviceAddresses;
    private List<PublicKey> rsaEncryptKeys;
    private String transmitterAddress;
    private Context context;
    private AtomicBoolean isDestroyed;
    private AtomicBoolean isTimerFinished;
    private LocationObject locObj;


    private CountDownTimer countDownTimer = new CountDownTimer(10000, 50) {
        @Override
        public void onTick(long msLeft) {
            int progress = (int)(10000 - msLeft) / (100);
            Intent progressUpdateIntent = new Intent(NetworkTransmitActivity.RECEIVE_MESSAGE_BROADCAST);
            progressUpdateIntent.putExtra("progress", progress);
            progressUpdateIntent.putExtra("location", "");
            LocalBroadcastManager.getInstance(context).sendBroadcast(progressUpdateIntent);
        }

        @Override
        public void onFinish() {
            // Update the LocationObject's measurements and display to the user. For now, that's commented out since my device can't run the sensor.
            // locObj.updateLocationData();
            // Get the string to be sent to the receiver device.
            String locationMessage = locObj.convertLocationToJSON();
            // Encrypt with 2 layers (as per Onion Routing protocol).
            List<String> path = new ArrayList<String>();
            List<PublicKey> pathPublicKeys = new ArrayList<PublicKey>();
            // Path has device addresses in reverse, not including the transmitter/relayer. First element in list is 0 since
            // receiver doesn't forward it.
            path.add("0");
            for (int i = deviceAddresses.size() - 1; i > 1; i--) {
                path.add(deviceAddresses.get(i));
            }
            // PathPublicKeys has all RSA public keys in reverse except for transmitter's.
            for (int i = rsaEncryptKeys.size() - 1; i > 0; i--) {
                pathPublicKeys.add(rsaEncryptKeys.get(i));
            }
            // This method will encrypt the message with as many layers as specified in the path, using the corresponding keys, and will encode
            // the final result in base64.
            String encryptedMessage = transmitterEncrypt(deviceAddresses.size() - 1, locationMessage, path, pathPublicKeys);
            try {
                // Open a socket with the relayer device and send the encrypted message.
                Socket relaySocket = null;
                while (relaySocket == null) {
                    try {
                        relaySocket = new Socket(InetAddress.getByName(deviceAddresses.get(1)), 3899);
                    } catch (IOException ioe) {}
                }
                DataInputStream readBuffer = new DataInputStream(relaySocket.getInputStream());
                DataOutputStream writeBuffer = new DataOutputStream(relaySocket.getOutputStream());
                // Send the message and wait for response.
                writeBuffer.writeUTF(encryptedMessage);
                String received = readBuffer.readUTF();
                if (!received.equals("Received")) {
                    Log.d("Transmitter", "Didn't receive proper confirmation receipt from the relay.");
                    return;
                }
                readBuffer.close();
                writeBuffer.close();

            } catch (IOException ioe) {
                Log.d("Transmitter", "IO Exception occurred with the relay connection socket.");
                ioe.printStackTrace();
            }

            // Send the updated location object to the activity to display.
            Intent locationUpdateIntent = new Intent(NetworkTransmitActivity.RECEIVE_MESSAGE_BROADCAST);
            locationUpdateIntent.putExtra("progress", -1);
            locationUpdateIntent.putExtra("location", locObj.convertLocationToJSON());
            LocalBroadcastManager.getInstance(context).sendBroadcast(locationUpdateIntent);
            isTimerFinished.set(true);
            Log.d("Transmitter", "CountdownTimer is done.");

        }
    };

    public Transmitter(List<String> dvas, String dva, List<PublicKey> rsaEKs, Context context, LocationObject locationObject) {
        this.deviceAddresses = dvas;
        this.transmitterAddress = dva;
        this.rsaEncryptKeys = rsaEKs;
        this.context = context;
        this.isDestroyed = new AtomicBoolean(false);
        this.isTimerFinished = new AtomicBoolean(true);
        this.locObj = locationObject;
    }

    @Override
    public void run() {
        Log.i("FromTransmitter","Got to begining of run");
        // First, initialize the countdowntimer to run for 10 seconds and then the rest of the code in onFinish does the actual encryption and
        // transmission.
//        while(!isDestroyed.get()) {
//            if (isTimerFinished.get()) {
//                Log.d("Transmitter", "Starting countdowntimer");
//                isTimerFinished.set(false);
//                countDownTimer.start();
//            }
//        }



        /*
                                -----------------RUN onFinish() code here once----------------------
         */

        // Update the LocationObject's measurements and display to the user. For now, that's commented out since my device can't run the sensor.
         locObj.updateLocationData();
        // Get the string to be sent to the receiver device.
        String locationMessage = locObj.convertLocationToJSON();
        // Encrypt with 2 layers (as per Onion Routing protocol).
        List<String> path = new ArrayList<String>();
        List<PublicKey> pathPublicKeys = new ArrayList<PublicKey>();
        // Path has device addresses in reverse, not including the transmitter/relayer. First element in list is 0 since
        // receiver doesn't forward it.
        path.add("0");
        for (int i = deviceAddresses.size() - 1; i > 1; i--) {
            path.add(deviceAddresses.get(i));
        }
        // PathPublicKeys has all RSA public keys in reverse except for transmitter's.
        for (int i = rsaEncryptKeys.size() - 1; i > 0; i--) {
            pathPublicKeys.add(rsaEncryptKeys.get(i));
        }
        // This method will encrypt the message with as many layers as specified in the path, using the corresponding keys, and will encode
        // the final result in base64.
        String encryptedMessage = transmitterEncrypt(deviceAddresses.size() - 1, locationMessage, path, pathPublicKeys);
        try {
            // Open a socket with the relayer device and send the encrypted message.
            Socket relaySocket = null;
            Log.i("FromTransmitterOutsideWhile","Got here"); //(!!!)
            while (relaySocket == null) {
                try {
                    relaySocket = new Socket(InetAddress.getByName(deviceAddresses.get(1)), 3899);
                    Log.i("FromTransmitterInsideTry","GOt here after realysocket"); //(!!!)
                } catch (IOException ioe) {
                    Log.i("FromTransmitterInsideCatch","ioexception in relay socket");
                    ioe.printStackTrace();
                }
            }
            DataInputStream readBuffer = new DataInputStream(relaySocket.getInputStream());
            DataOutputStream writeBuffer = new DataOutputStream(relaySocket.getOutputStream());
            // Send the message and wait for response.
            writeBuffer.writeUTF(encryptedMessage);
            Log.i("FromTransmitterWait","Waiting For Response"); //(!!!)
            String received = readBuffer.readUTF();
            Log.i("FromTransmitterWait","Received Response"); //(!!!)

            if (!received.equals("Received")) {
                Log.d("Transmitter", "Didn't receive proper confirmation receipt from the relay.");
                return;
            }
            readBuffer.close();
            writeBuffer.close();

        } catch (IOException ioe) {
            Log.d("Transmitter", "IO Exception occurred with the relay connection socket.");
            ioe.printStackTrace();
        }

        // Send the updated location object to the activity to display.
        Intent locationUpdateIntent = new Intent(NetworkTransmitActivity.RECEIVE_MESSAGE_BROADCAST);
        locationUpdateIntent.putExtra("progress", -1);
        locationUpdateIntent.putExtra("location", locObj.convertLocationToJSON());
        LocalBroadcastManager.getInstance(context).sendBroadcast(locationUpdateIntent);
        //isTimerFinished.set(true); //(!!! not necessary for once)
        Log.d("Transmitter", "CountdownTimer is done.");


    }

    public void destroy() {
        isDestroyed.set(true);
    }

    public String randomizeKey() {
        int leftLimit = 48; //nothing before '0'
        int rightlimit = 122; //nothing after 'z'
        int length = 32; //32 chars long
        Random random = new Random();

        String result = random.ints(leftLimit, rightlimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint,
                        StringBuilder::append)
                .toString();
        return result;

    }

    public String transmitterEncrypt(int numDevices, String msg, List<String> ipList, List<PublicKey> publicKeys) {
        for (int i = 0; i < numDevices; i++) {
            String aes_key = randomizeKey();
            String message = AES.encrypt(msg, aes_key); //msg will only be the location data on the first iteration

            String ip = RSA.encrypt(ipList.get(i), publicKeys.get(i));
            String key = RSA.encrypt(aes_key, publicKeys.get(i));

            JSONObject msgJSON = new JSONObject();
            try {
                msgJSON.put("msg", message);
                msgJSON.put("key", key);
                msgJSON.put("ip", ip);
            } catch(JSONException je) {
                Log.d("Transmitter", "JSONException thrown when creating encrypted JSON.");
                je.printStackTrace();
            }

            //more secure (less explicit) keys need to be used eventually for implementation (a/b/c etc.)

            msg = msgJSON.toString();
        }

        //encode when done encrypting
        byte[] msgByte = msg.getBytes();
        msg = Base64.encodeToString(msgByte, Base64.DEFAULT);

        return msg;
    }
}
