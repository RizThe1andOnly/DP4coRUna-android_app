package com.example.dp4coruna.network;

import android.util.Base64;
import android.util.Log;

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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NetworkConnector implements Runnable {
    List<String> deviceAddresses;
    List<PublicKey> rsaEncryptKeys;
    PublicKey myPublicKey;

    public NetworkConnector(List<String> dvas, List<PublicKey> rsaEKs, PublicKey pk) {
        this.deviceAddresses = dvas;
        this.rsaEncryptKeys = rsaEKs;
        this.myPublicKey = pk;
    }

    @Override
    public void run() {
        // This isn't a secure way of initializing the network, but we can try it for now to allow for dynamic network setup.
        // First, listen on port 9877 for the server "beacon" broadcast message.
        try {
            DatagramSocket broadcastSocket = new DatagramSocket(9877);
            broadcastSocket.setSoTimeout(10000); // We want it to give the server 10 seconds to acknowledge it.
            byte[] serverIP = new byte[128];
            DatagramPacket packetFromServer = new DatagramPacket(serverIP, serverIP.length);
            String serverAddress = "";
            // Try to receive, and if a timeout exception is thrown after 5 seconds, then we assume that this is the first
            // device to connect to the network.
            while (true) {
                try {
                    broadcastSocket.receive(packetFromServer);
                    serverAddress = new String(packetFromServer.getData(), 0, packetFromServer.getLength());
                    break;
                } catch (SocketTimeoutException ste) {
                    break;
                }
            }


            if (serverAddress.equals("")) {
                deviceAddresses.add(InetAddress.getLocalHost().getHostAddress()); // Add itself.
                rsaEncryptKeys.add(myPublicKey); // Add its own public key as the first key.
                return; // It's the first device on the network.
            }

            // If we got here, open a connection with the server on port 9879 and send it it's IP address.
            Socket initializeSocket = null;
            while (initializeSocket == null) {
                try {
                    initializeSocket = new Socket(InetAddress.getByName(serverAddress), 9879);
                    break;
                } catch(IOException ioe) {}
            }
            DataInputStream newDeviceReadBuffer = new DataInputStream(initializeSocket.getInputStream());
            DataOutputStream newDeviceWriteBuffer = new DataOutputStream(initializeSocket.getOutputStream());

            newDeviceWriteBuffer.writeUTF(InetAddress.getLocalHost().getHostAddress());
            String received = newDeviceReadBuffer.readUTF();
            if (!received.equals("Received"))   return;
            // Now, send the public key over to the server so that it can add it to the master ArrayList and return
            // that as a comma-separated list.
            byte[] encodedPublicKey = myPublicKey.getEncoded();
            String b64PublicKey = Base64.encodeToString(encodedPublicKey, Base64.DEFAULT);
            newDeviceWriteBuffer.writeUTF(b64PublicKey);
            received = newDeviceReadBuffer.readUTF();
            if (!received.equals("Received"))   return;
            // Server should add this to the master ArrayList of all the addresses in the network and return that
            // as a comma-separated list.
            String addressList = newDeviceReadBuffer.readUTF();
            newDeviceWriteBuffer.writeUTF("Received");
            deviceAddresses.clear();
            deviceAddresses.addAll((List<String>) Arrays.asList(addressList.split(",")));

            // Read the comma-separated list of base64 encoded public keys from the server, convert it to an ArrayList
            // of public keys, and confirm that it's been received.
            String publicKeyStrings = newDeviceReadBuffer.readUTF();
            rsaEncryptKeys.clear();
            rsaEncryptKeys.addAll((List<PublicKey>)Arrays.stream(publicKeyStrings.split(","))
                    .map(pk -> {
                        try {
                            byte[] publicKeyBytes = Base64.decode(pk, Base64.DEFAULT);
                            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
                            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                            return keyFactory.generatePublic(keySpec);
                        } catch(NoSuchAlgorithmException | InvalidKeySpecException ex) {
                            System.out.println("Exception thrown in lambda function to generate rsaEncryptKeys");
                            ex.printStackTrace();
                        }
                        return null;
                    })
                    .collect(Collectors.toList()));
            newDeviceWriteBuffer.writeUTF("Received");
            System.out.println("deviceAddresses and rsaEncryptKeys at end of connectToNetwork");
            // Loop through deviceAddresses to see whether or not that is properly populated.
            for (int i = 0; i < deviceAddresses.size(); i++) {
                Log.i("Transmitter", "Device Address: " + deviceAddresses.get(i));
            }
            // Loop through rsaEncryptKeys to see whether or not it is properly populated.
            for (int i = 0; i < rsaEncryptKeys.size(); i++) {
                String keyString = Base64.encodeToString(rsaEncryptKeys.get(i).getEncoded(), Base64.DEFAULT);
                Log.i("Transmitter","RSA Public Key: " + keyString + "\n");
            }


        } catch(IOException ioe) {
            Log.d("Transmitter","IO Exception thrown when connecting to the network.");
            ioe.printStackTrace();
        }

    }

    public List<String> getDeviceAddresses() {
        return this.deviceAddresses;
    }

    public List<PublicKey> getRsaEncryptKeys() {
        return this.rsaEncryptKeys;
    }

}
