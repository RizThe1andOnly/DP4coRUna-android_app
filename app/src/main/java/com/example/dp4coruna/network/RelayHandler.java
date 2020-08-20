package com.example.dp4coruna.network;

import android.provider.ContactsContract;

import java.net.*;
import java.io.*;
import java.security.PrivateKey;

public class RelayHandler implements Runnable {
    // This thread handles an message that arrives at a device in the background via port 3899. Upon receiving the
    // message, it splits the string on '#' into an array. If the array has length 3, then the message must be forwarded
    // after decryption. Otherwise, it is the recipient, and the decrypted message can be printed to the console and a
    // message of success can be sent back to the previous node.

    final PrivateKey myPrivateKey;
    final Socket relayReceiverSocket;
    final DataInputStream receiverReadBuffer;
    final DataOutputStream receiverWriteBuffer;

    public RelayHandler(PrivateKey myPK, Socket relayRS, DataInputStream dis, DataOutputStream dos) {
        this.myPrivateKey = myPK;
        this.relayReceiverSocket = relayRS;
        this.receiverReadBuffer = dis;
        this.receiverWriteBuffer = dos;
    }

    @Override
    public void run() {
        try {
            String receivedMessage = receiverReadBuffer.readUTF();
            // Split the input string on the pound character.
            String[] messageParts = receivedMessage.split("#");
            String messageIP = messageParts[0];
            String aesPW = messageParts[1];
            String nextIP = "";

            System.out.println("Just received message - " + receivedMessage);
            aesPW = RSA.decrypt(aesPW, myPrivateKey); // Decrypt the second part of the message using the RSA private key to get the AES password.
            messageIP = AES.decrypt(messageIP, aesPW); // Decrypt the first part of the message using the now-decrypted AES key.

            String[] messageIPParts = messageIP.split("#");
            String message = messageIPParts[0];
            if (messageIPParts.length == 3) {
                // There is an IP address and it is a relay node.
                nextIP = messageIPParts[2];
                message = messageIPParts[0] + "#" + messageIPParts[1];
            }

            System.out.println("After removing one layer of decryption - " + message);
            if (nextIP.equals("")) {
                // We're the recipient.
                System.out.println("Received message: " + message);
                // Send "Received" back through the network. (If this were confidential, would encrypt).
                receiverWriteBuffer.writeUTF("Received");

            } else {
                // We need to forward the message.
                Socket relayForwarderSocket = null;
                while (relayForwarderSocket == null) {
                    try {
                        relayForwarderSocket = new Socket(InetAddress.getByName(nextIP), 3899);
                        break;
                    } catch(IOException ioe) {}
                }
                DataInputStream forwarderReadBuffer = new DataInputStream(relayForwarderSocket.getInputStream());
                DataOutputStream forwarderWriteBuffer = new DataOutputStream(relayForwarderSocket.getOutputStream());
                // Send the message as-is through the socket.
                forwarderWriteBuffer.writeUTF(message);
                System.out.println("Forwarded message");
                // Wait for confirmation that it eventually got to the destination.
                String received = forwarderReadBuffer.readUTF();
                // Send it back to relayReceiverSocket.
                receiverWriteBuffer.writeUTF(received);
                // Close forwarder socket down.
                forwarderReadBuffer.close();
                forwarderWriteBuffer.close();
                relayForwarderSocket.close();

            }
            // Close receiver down.
            receiverReadBuffer.close();
            receiverWriteBuffer.close();
            relayReceiverSocket.close();
        } catch(IOException ioe) {
            System.out.println("IO Exception occurred with the Public Key server socket.");
            ioe.printStackTrace();
        }

        return;

    }

}
