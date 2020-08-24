package com.example.dp4coruna.network;

import android.util.Base64;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateNetwork implements Runnable {
    List<String> deviceAddresses;
    List<PublicKey> rsaEncryptKeys;
    Transmitter transmitterRunnable;


    public UpdateNetwork(List<String> dvas, List<PublicKey> rsaEKs, Transmitter transmitter) {
        this.deviceAddresses = dvas;
        this.rsaEncryptKeys = rsaEKs;
        this.transmitterRunnable = transmitter;
    }

    @Override
    public void run() {
        // When this thread is first spawned, it spawns another thread that acts as a gateway for the network by
        // broadcasting its IP address periodically (once every second) from port 9877 until the clientConnected flag is set to true
        // by the updateNetworkThread. Meanwhile, updateNetworkThread creates a TCP socket to listen from port 9879 until
        // a client connects. At that point, the clientConnected flag is set to true on the other thread and it is joined on.
        // This thread then takes the IP address of the client and adds it to deviceAddresses. It then loops through all of the
        // deviceAddresses except for itself, creates separate sockets to each one of them, and sends the updated
        // list to each of them. Then, without closing the sockets, it reads the new device's public key and adds it to the list,
        // converts that into a comma-separated string, and sends it to all the other devices in the network. Finally, everything is
        // closed.

        try {
            Gateway gatewayRunnable = new Gateway();
            Thread gatewayThread = new Thread(gatewayRunnable);
            gatewayThread.start();
            // TCP socket to listen for client connections at port 9879.
            ServerSocket newDeviceListeningSocket = new ServerSocket(9879);
            Socket newDeviceSocket = newDeviceListeningSocket.accept();
            // We're only servicing one new device at a time, so we won't spawn a new thread.
            // Now that a client has connected, we stop broadcasting.
            gatewayRunnable.clientConnected.set(true);
            DataInputStream newDeviceReadBuffer = new DataInputStream(newDeviceSocket.getInputStream());
            DataOutputStream newDeviceWriteBuffer = new DataOutputStream(newDeviceSocket.getOutputStream());
            // Read the IP address of the new device and send acknowledgment.
            String newDeviceIP = newDeviceReadBuffer.readUTF();
            newDeviceWriteBuffer.writeUTF("Received");
            // Read the base64-encoded string representation of the new device's public key and send acknowledgment.
            String publicKeyString = newDeviceReadBuffer.readUTF();
            newDeviceWriteBuffer.writeUTF("Received");
            // Then, convert the base64-encoded string into a PublicKey object.
            byte[] publicKeyBytes = Base64.decode(publicKeyString, Base64.DEFAULT);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            // Get the lock to add the new device to deviceAddresses (guarantees that the Transmitter thread is not reading deviceAddresses to
            // create a path.) Do the same with rsaEncryptKeys (guarantees that the Transmitter thread is not reading from rsaEncryptKeys
            // while encrypting).
            synchronized(deviceAddresses) {
                deviceAddresses.add(newDeviceIP);
            }
            synchronized(rsaEncryptKeys) {
                rsaEncryptKeys.add(publicKey);
            }
            // Convert deviceAddresses into a comma-separated string that can be sent to the new device.
            String deviceAddressesString = deviceAddresses.stream()
                    .collect(Collectors.joining(","));
            newDeviceWriteBuffer.writeUTF(deviceAddressesString);
            String received = newDeviceReadBuffer.readUTF();
            if (!received.equals("Received")) {
                System.out.println("The new device did not indicate that it received the list of addresses.");
                return;
            }
            // Convert rsaEncryptKeys into a comma-separated string of base64-encoded keys that can be sent to the new device.
            String rsaEncryptKeysString = rsaEncryptKeys.stream()
                    .map(pk -> {
                        byte[] encodedPublicKey = pk.getEncoded();
                        return Base64.encodeToString(encodedPublicKey, Base64.DEFAULT);
                    })
                    .collect(Collectors.joining(","));
            newDeviceWriteBuffer.writeUTF(rsaEncryptKeysString);
            if (!received.equals("Received")) {
                System.out.println("The new device did not indicate that it received the list of public keys.");
                return;
            }
            newDeviceReadBuffer.close();
            newDeviceWriteBuffer.close();
            newDeviceSocket.close();
            newDeviceListeningSocket.close();

            // Lastly, create sockets with each of the devices in deviceAddresses excluding itself and newDeviceIP and send
            // each one of them the string representations of the modified lists.
            for (int i = 0; i < deviceAddresses.size(); i++) {
                String deviceIP = deviceAddresses.get(i);
                if (!deviceIP.equals(InetAddress.getLocalHost().getHostAddress()) && !deviceIP.equals(newDeviceIP)) {
                    Socket updateSocket = null;
                    while (updateSocket == null) {
                        try {
                            updateSocket = new Socket(InetAddress.getByName(deviceIP), 9881);
                            break;
                        } catch(IOException ioe) {}
                    }
                    DataInputStream updateReadBuffer = new DataInputStream(updateSocket.getInputStream());
                    DataOutputStream updateWriteBuffer = new DataOutputStream(updateSocket.getOutputStream());
                    updateWriteBuffer.writeUTF(deviceAddressesString);
                    received = updateReadBuffer.readUTF();
                    if (!received.equals("Received")) {
                        System.out.println("The existing device did not indicate that it received the updated list of addresses.");
                        return;
                    }
                    updateWriteBuffer.writeUTF(rsaEncryptKeysString);
                    received = updateReadBuffer.readUTF();
                    if (!received.equals("Received")) {
                        System.out.println("The existing device did not indicate that it received the updated list of public keys.");
                        return;
                    }
                    updateReadBuffer.close();
                    updateWriteBuffer.close();
                    updateSocket.close();
                }
            }

            // Now, having completed its role as the gateway node, create a socket to listen for updates.
            ServerSocket updateSocket = new ServerSocket(9881);
            while(true) {
                Socket updaterSocket = updateSocket.accept();
                DataInputStream updaterReadBuffer = new DataInputStream(updaterSocket.getInputStream());
                DataOutputStream updaterWriteBuffer = new DataOutputStream(updaterSocket.getOutputStream());
                deviceAddressesString = updaterReadBuffer.readUTF();
                updaterWriteBuffer.writeUTF("Received");
                // Do not update if the Transmitter thread is currently reading from it to create a new path.
                synchronized(deviceAddresses) {
                    deviceAddresses.clear();
                    deviceAddresses.addAll((List<String>) Arrays.asList(deviceAddressesString.split(",")));
                }

                String publicKeyStrings = updaterReadBuffer.readUTF();
                // Do not update if the Transmitter thread is currently reading from it to encrypt the AES keys.
                synchronized(rsaEncryptKeys) {
                    rsaEncryptKeys.clear();
                    rsaEncryptKeys.addAll((List<PublicKey>)Arrays.stream(publicKeyStrings.split(","))
                            .map(pk -> {
                                try {
                                    byte[] pkBytes = Base64.decode(pk, Base64.DEFAULT);
                                    X509EncodedKeySpec ks = new X509EncodedKeySpec(pkBytes);
                                    KeyFactory kf = KeyFactory.getInstance("RSA");
                                    return kf.generatePublic(ks);
                                } catch(NoSuchAlgorithmException | InvalidKeySpecException ex) {
                                    System.out.println("Exception thrown in lambda function for generating rsaEncryptKeys");
                                    ex.printStackTrace();
                                }
                                return null;
                            })
                            .collect(Collectors.toList()));
                }

                updaterWriteBuffer.writeUTF("Received");
                updaterReadBuffer.close();
                updaterWriteBuffer.close();
                updaterSocket.close();

            }

        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException ioe) {
            System.out.println("Exception thrown when trying to receive input from the user regarding transmission.");
            ioe.printStackTrace();
        }


    }

}
