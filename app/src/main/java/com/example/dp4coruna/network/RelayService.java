package com.example.dp4coruna.network;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class RelayService extends Service {
    private RelayServer relayServer;
    private RelayHandler relayHandler;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
