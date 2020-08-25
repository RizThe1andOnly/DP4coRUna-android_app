package com.example.dp4coruna.localLearning.learningService;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.example.dp4coruna.localLearning.location.LocationObject;

import java.util.concurrent.BlockingQueue;

public class LearningHandler extends Handler {
    private BlockingQueue passedBQ;
    private LocationObject locationObject;

    public LearningHandler(Looper looper){
        super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

    }
}
