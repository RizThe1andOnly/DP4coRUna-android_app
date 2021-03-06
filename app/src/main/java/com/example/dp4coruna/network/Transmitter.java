package com.example.dp4coruna.network;


import android.util.Base64;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.example.dp4coruna.localLearning.location.LocationObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Collections;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Transmitter implements Runnable {
    private List<String> deviceAddresses;
    private List<PublicKey> rsaEncryptKeys;
    private String transmitterAddress;
    private String receiverAddress;
    private AtomicBoolean isDestroyed;
    private LocationObject locObj;
    private String messageToBeSent;
    private Object payload;
    private String relativePost;

    public Transmitter(List<String> dvas, String dva, List<PublicKey> rsaEKs, Object payload,String messageToBeSent, String relativePost) {
        this.deviceAddresses = dvas;
        this.transmitterAddress = dva;
        this.rsaEncryptKeys = rsaEKs;
        this.isDestroyed = new AtomicBoolean(false);
        this.locObj = (LocationObject) payload;
        //this.payload = payload;
        this.receiverAddress = transmitterAddress;
        this.messageToBeSent = messageToBeSent;
        this.relativePost = relativePost;
    }

    public Transmitter(List<String> dvas, String dva, String receiverAddress,List<PublicKey> rsaEKs, Object payload, String messageToBeSent, String relativePost){
        this.deviceAddresses = dvas;
        this.transmitterAddress = dva;
        this.rsaEncryptKeys = rsaEKs;
        this.isDestroyed = new AtomicBoolean(false);
        this.locObj = (LocationObject) payload;
        this.receiverAddress = receiverAddress;
        this.messageToBeSent = messageToBeSent;
        this.relativePost = relativePost;
    }

    @Override
    public void run() {
        Log.i("FromTransmitter","Got to begining of run");
        // Get the string to be sent to the receiver device.
        String location = locObj.convertLocationToJSON();
        String locationMessage = messageToBeSent;
        // Encrypt with 2 layers (as per Onion Routing protocol).
        List<String> path = new ArrayList<String>();
        List<PublicKey> pathPublicKeys = new ArrayList<PublicKey>();
        String firstRelay = "";
        // Path has device addresses in reverse, not including the transmitter/relayer. First element in list is 0 since
        // receiver doesn't forward it.
        synchronized (deviceAddresses) {
            synchronized(rsaEncryptKeys) {
                Log.i("Transmitter",deviceAddresses.get(0));
                path = generatePath(deviceAddresses);

                if (path.size() == 0)   return;
                // First, get the corresponding public keys for each device in the path.

                for (int i = 0; i < path.size(); i++) {
                    //pathPublicKeys.add(rsaEncryptKeys.get(deviceAddresses.indexOf(path.get(i))));
                }
                // Then, remove the last element in the path. That will be the first relay and is unnecessary for encryption.
                // Add a "0" at the start of the path. Now, each element in the path list represents the NEXT element in the path, which
                // is how we need it for encrypting.
                firstRelay = getFirstRelay(path);
                path.add(0, "0");
            }

        }

        // This method will encrypt the message with as many layers as specified in the path, using the corresponding keys, and will encode
        // the final result in base64.
        String encryptedMessage = transmitterEncrypt(path.size(),location, locationMessage, path, pathPublicKeys,transmitterAddress);
        try {
            // Open a socket with the relayer device and send the encrypted message.
            Socket relaySocket = null;
            Log.i("FromTransmitterOutsideWhile","Got here"); //(!!!)
            while (relaySocket == null) {
                try {
                    relaySocket = new Socket(InetAddress.getByName(firstRelay), 3899);
                    Log.i("FromTransmitterInsideTry","GOt here after realysocket"); //(!!!)
                } catch (IOException ioe) {
                    Log.i("FromTransmitterInsideCatch","ioexception in relay socket");
                    ioe.printStackTrace();
                }
            }
            DataInputStream readBuffer = new DataInputStream(relaySocket.getInputStream());
            DataOutputStream writeBuffer = new DataOutputStream(relaySocket.getOutputStream());
            // Send the message and wait for response.
            Log.i("TestUnEncrypt",encryptedMessage);
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

    }

    private String getFirstRelay(List<String> path){
        for (int i=0;i<path.size();i++){
            String elem = path.get(i);
            if(!elem.equals(receiverAddress)){
                return path.remove(i);
            }
        }
        return "";
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

    public String transmitterEncrypt(int numDevices, String lob,String msg, List<String> ipList, List<PublicKey> publicKeys, String src) {
        for (int i = 0; i < numDevices; i++) {
            //String aes_key = randomizeKey();
            //String message = AES.encrypt(msg, aes_key); //msg will only be the location data on the first iteration

            //String ip = RSA.encrypt(ipList.get(i), publicKeys.get(i));
            //String key = RSA.encrypt(aes_key, publicKeys.get(i));

            JSONObject msgJSON = new JSONObject();
            try {
                //msgJSON.put("msg", message);
                msgJSON.put("loc",lob);
                msgJSON.put("msg",msg);
                //msgJSON.put("key", key);
                msgJSON.put("key","");
                //msgJSON.put("ip", ip);
                msgJSON.put("ip",ipList.get(i));
                msgJSON.put("src",src);
                msgJSON.put("relativePost",relativePost);
            } catch(JSONException je) {
                Log.d("Transmitter", "JSONException thrown when creating encrypted JSON.");
                je.printStackTrace();
            }

            //more secure (less explicit) keys need to be used eventually for implementation (a/b/c etc.)

            msg = msgJSON.toString();
        }

        //encode when done encrypting
        byte[] msgByte = msg.getBytes();
        //msg = Base64.encodeToString(msgByte, Base64.DEFAULT);

        return msg;
    }

    public List<String> generatePath(List<String> deviceAddresses) {
        if (deviceAddresses.size() < 3) {
            // If we have less than 3 devices on the network, we cannot transmit via Onion routing.
            return new ArrayList<String>();
        }
        List<String> deviceOptions = new ArrayList<String>(deviceAddresses);
        deviceOptions.remove(transmitterAddress);
        if (deviceOptions.size() < 5) {
            // Just shuffle the list and use that as the path.
            Collections.shuffle(deviceOptions);
            return deviceOptions;
        } else {
            // Shuffle the list and pick the first 4 elements in the shuffled list.
            Collections.shuffle(deviceOptions);
            List<String> path = new ArrayList<String>();
            for (int i = 0; i < 4; i++) {
                path.add(deviceOptions.get(i));
            }
            return path;
        }
    }
}
