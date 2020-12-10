package com.example.dp4coruna.network;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.security.PrivateKey;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class RelayServer implements Runnable {
    // This thread behaves as the background thread that listens on port 3899 for incoming connection requests from
    // other relay nodes or transmitter/directory nodes. (We don't know which by design.) Upon receiving a connection,
    // it will spawn a RelayHandlerThread to handle decryption and potential forwarding and/or output of the message.

    private PrivateKey myPrivateKey;
    private Context serviceContext;
    private AtomicBoolean isDestroyed;
    private ServerSocket relayServerSocket;
    private List<Thread> relayConnectionThreads;

    public RelayServer(PrivateKey myPK, Context sContext) {
        this.myPrivateKey = myPK;
        this.serviceContext = sContext;
        this.isDestroyed = new AtomicBoolean(false);
        this.relayConnectionThreads = new ArrayList<Thread>();
    }

    @Override
    public void run() {
        try {
            relayServerSocket = new ServerSocket(3899);
            Socket relayClientSocket = null;
            while (!isDestroyed.get()) {
                Log.d("RelayServer", "Ready for new connections.");
                relayClientSocket = relayServerSocket.accept();
                DataInputStream readBuffer = new DataInputStream(relayClientSocket.getInputStream());
                DataOutputStream writeBuffer = new DataOutputStream(relayClientSocket.getOutputStream());
                // Spawn a new thread to handle the connection request.
                Thread relayConnectionThread = new Thread(new RelayConnection(myPrivateKey, relayClientSocket, readBuffer, writeBuffer, serviceContext));
                relayConnectionThreads.add(relayConnectionThread);
                relayConnectionThread.start();

            }
        } catch(IOException ioe) {
            Log.d("RelayServer", "Time to shutdown relay server socket.");
            ioe.printStackTrace();
        }
        return;
    }

    public void destroy() {
        try {
            for (Thread relayConnectionThread : relayConnectionThreads) {
                relayConnectionThread.join();
            }
            relayServerSocket.close();
            isDestroyed.set(true);
        } catch(IOException | InterruptedException ie) {
            Log.d("RelayServer", "Couldn't shutdown relay server socket.");
            ie.printStackTrace();
        }
    }

}
