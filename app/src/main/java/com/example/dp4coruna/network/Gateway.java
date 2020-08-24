package com.example.dp4coruna.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

public class Gateway implements Runnable {
    public AtomicBoolean clientConnected;

    public Gateway() {
        this.clientConnected = new AtomicBoolean();
    }

    @Override
    public void run() {
        try {
            // This just needs to create a UDP broadcast throughout the network at port 9877 as long as clientConnected is false.
            DatagramSocket broadcastSocket = new DatagramSocket();
            broadcastSocket.setBroadcast(true);
            byte[] serverIP = InetAddress.getLocalHost().getHostAddress().getBytes();
            DatagramPacket broadcastPacket = new DatagramPacket(serverIP, serverIP.length, InetAddress.getByName("255.255.255.255"), 9877);
            // Send once every second as long as clientConnected is false.
            while (!clientConnected.get()) {
                broadcastSocket.send(broadcastPacket);
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException ie) {
                    System.out.println("GatewayThread was interrupted when sleeping.");
                    ie.printStackTrace();
                }
            }
            // Close the broadcast UDP socket and return.
            broadcastSocket.close();
        } catch(IOException ioe) {
            System.out.println("IOException thrown in the Gateway thread.");
            ioe.printStackTrace();
        }

    }
}
