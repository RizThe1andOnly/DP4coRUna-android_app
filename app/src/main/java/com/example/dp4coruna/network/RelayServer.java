package com.example.dp4coruna.network;

import java.security.PrivateKey;
import java.net.*;
import java.io.*;

public class RelayServer implements Runnable {
    // This thread behaves as the background thread that listens on port 3899 for incoming connection requests from
    // other relay nodes or transmitter/directory nodes. (We don't know which by design.) Upon receiving a connection,
    // it will spawn a RelayHandlerThread to handle decryption and potential forwarding and/or output of the message.

    final PrivateKey myPrivateKey;

    public RelayServer(PrivateKey myPK) {
        this.myPrivateKey = myPK;
    }

    @Override
    public void run() {
        try {
            ServerSocket relayServerSocket = new ServerSocket(3899);
            Socket relayClientSocket = null;
            while (true) {
                relayClientSocket = relayServerSocket.accept();
                DataInputStream readBuffer = new DataInputStream(relayClientSocket.getInputStream());
                DataOutputStream writeBuffer = new DataOutputStream(relayClientSocket.getOutputStream());
                // Spawn a new thread to handle the connection request.
                Thread relayHandlerThread = new Thread(new RelayHandler(myPrivateKey, relayClientSocket, readBuffer, writeBuffer));
                relayHandlerThread.start();

            }
        } catch(IOException ioe) {
            System.out.println("IO Exception occurred with the Public Key server socket.");
            ioe.printStackTrace();
        }
        return;
    }

}
