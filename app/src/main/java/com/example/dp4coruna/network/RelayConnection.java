package com.example.dp4coruna.network;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.*;
import java.io.*;
import java.security.PrivateKey;

public class RelayConnection implements Runnable {
    // This thread handles an message that arrives at a device in the background via port 3899. Upon receiving the
    // message, it splits the string on '#' into an array. If the array has length 3, then the message must be forwarded
    // after decryption. Otherwise, it is the recipient, and the decrypted message can be printed to the console and a
    // message of success can be sent back to the previous node.

    private PrivateKey myPrivateKey;
    private Socket relayReceiverSocket;
    private DataInputStream receiverReadBuffer;
    private DataOutputStream receiverWriteBuffer;
    private Context context;

    public RelayConnection(PrivateKey myPK, Socket relayRS, DataInputStream dis, DataOutputStream dos, Context context) {
        this.myPrivateKey = myPK;
        this.relayReceiverSocket = relayRS;
        this.receiverReadBuffer = dis;
        this.receiverWriteBuffer = dos;
        this.context = context;
    }

    @Override
    public void run() {
        try {
            String receivedMessage = receiverReadBuffer.readUTF();
            // First - convert the base64-encoded string into a JSON string, and then into a JSON Object.
            JSONObject receivedJSON = receivedToJSON(receivedMessage);
            // Now, use the RSA private key to decrypt the key/ip fields, and that to decrypt the msg field.
            JSONObject decryptedJSON = decrypt(receivedJSON, myPrivateKey);
            String decryptedMessage = decryptedJSON.getString("msg");
            String nextIP = decryptedJSON.getString("ip");

            if (nextIP.equals("0")) {
                // We're the recipient.
                Intent recipientIntent = new Intent(NetworkReceiveActivity.RECEIVE_MESSAGE_BROADCAST);
                recipientIntent.putExtra("decryptedMessage", decryptedMessage);
                LocalBroadcastManager.getInstance(context).sendBroadcast(recipientIntent);
                // Send "Received" back through the network. (If this were confidential, would encrypt).
                receiverWriteBuffer.writeUTF("Received");

            } else {
                // We need to forward the message. First, broadcast to the NetworkRelayActivity to output.
                Intent relayIntent = new Intent(NetworkRelayActivity.RECEIVE_MESSAGE_BROADCAST);
                relayIntent.putExtra("incomingMessage", receivedMessage);
                relayIntent.putExtra("outgoingMessage", decryptedMessage);
                LocalBroadcastManager.getInstance(context).sendBroadcast(relayIntent);

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
                forwarderWriteBuffer.writeUTF(decryptedMessage);
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
        } catch(IOException | JSONException ioe) {
            Log.d("RelayConnection", "IO Exception occurred with the relay connection socket.");
            ioe.printStackTrace();
        }

        return;

    }

    public static JSONObject receivedToJSON(String receivedData){
        byte[] dataBytes = receivedData.getBytes();
        String decodedData = new String(Base64.decode(dataBytes, Base64.DEFAULT));
        JSONObject dataToJSON = new JSONObject();
        try {
            dataToJSON = new JSONObject(decodedData);
        } catch(JSONException je){
            Log.d("RelayConnection", "JSONException thrown when decoding encrypted JSON.");
            je.printStackTrace();
        }
        return dataToJSON;
    }

    public static JSONObject decrypt(JSONObject data, PrivateKey privateKey){
        try {
            String key = RSA.decrypt(data.getString("key"), privateKey);
            String destIP = RSA.decrypt(data.getString("ip"), privateKey);

            //decrypt extracted message using AES key
            String msg = AES.decrypt(data.getString("msg"), key);

            //encode to base64 if necessary
            if(!destIP.equals("0")){
                byte[] msgByte = msg.getBytes();
                msg = Base64.encodeToString(msgByte, Base64.DEFAULT);
            }

            JSONObject decryptedToJSON = new JSONObject();
            decryptedToJSON.put("msg", msg);
            decryptedToJSON.put("ip", destIP);
            return decryptedToJSON;
        } catch (JSONException je) {
            Log.d("RelayConnection", "JSONException thrown when decrypting encrypted JSON.");
            je.printStackTrace();
        }

        return null;

    }

}
