package com.example.dp4coruna.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.dp4coruna.R;

import org.w3c.dom.Text;

public class NetworkRelayActivity extends AppCompatActivity {
    public static final String RECEIVE_MESSAGE_BROADCAST = "com.example.dp4coruna.NETWORK_RELAY_RECEIVE_MESSAGE";


    private static class RelayHandler extends Handler {
        private TextView incomingMsgView;
        private TextView outgoingMsgView;

        public RelayHandler(TextView incomingMsgView, TextView outgoingMsgView) {
            super(Looper.getMainLooper());
            this.incomingMsgView = incomingMsgView;
            this.outgoingMsgView = outgoingMsgView;
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle broadcastBundle = msg.getData();
            String incomingMsg = broadcastBundle.getString("incomingMessage");
            String outgoingMsg = broadcastBundle.getString("outgoingMessage");
            incomingMsgView.setText(incomingMsg);
            outgoingMsgView.setText(outgoingMsg);
        }

    }

    private class RelayBroadcastReceiver extends BroadcastReceiver {
        private RelayHandler uiHandler;

        public RelayBroadcastReceiver(RelayHandler relayHandler) {
            super();
            this.uiHandler = relayHandler;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(RECEIVE_MESSAGE_BROADCAST)) {
                Message msg = uiHandler.obtainMessage();
                Bundle broadcastBundle = new Bundle();
                broadcastBundle.putString("incomingMessage", intent.getStringExtra("incomingMessage"));
                broadcastBundle.putString("outgoingMessage", intent.getStringExtra("outgoingMessage"));
                msg.setData(broadcastBundle);
                uiHandler.sendMessage(msg);
            }
        }
    }

    private RelayHandler relayHandler;
    private RelayBroadcastReceiver relayBroadcastReceiver;
    private LocalBroadcastManager localBroadcastManager;
    private TextView incomingMessageView;
    private TextView outgoingMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_relay);//(!!!) if this class's name is changed please change corresponding res's name in res->layout and here
        incomingMessageView = findViewById(R.id.incomingMessage);
        outgoingMessageView = findViewById(R.id.outgoingMessage);
        relayHandler = new RelayHandler(incomingMessageView, outgoingMessageView);
        relayBroadcastReceiver = new RelayBroadcastReceiver(relayHandler);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(relayBroadcastReceiver, new IntentFilter(NetworkRelayActivity.RECEIVE_MESSAGE_BROADCAST));

        new Thread(new RelayServer(null,getApplicationContext()),"RelayThread").start();

    }
}
