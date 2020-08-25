package com.example.dp4coruna.localLearning.learningService;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.example.dp4coruna.localLearning.location.LocationObject;

import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Will perform local learning in the background of the app. Will be
 * a started service as well as a bound service. Once started, will use
 * internal handler to continuously obtain local data and populate a
 * a blockingqueue. The binder then will return contents of queue when
 * another service/activity calls for it.
 *
 * Local learning handled by LocationObject and the contents of
 * com.example.dp4coruna.location.learner
 */
public class LocalLearningService extends Service {

    private final static long SLEEP_DURATION_MILLIS = 1000; // time = 1 second

    public final BlockingQueue<LocationObject> locationQueueContainer = new ArrayBlockingQueue<>(1);
    private Thread locationUpdaterThread;
    private final IBinder llsBinder = new LocalLearningServiceBinder();


    /**
     * Has a while loop that constantly updates a location data and put it in a blocking queue. if the blockingqueue
     * already has stuff in it then it is emptied and then re-filled.
     *
     * The thread is put to sleep for 1 second in each iteration so main thread has chance to access queue.
     */
    private class LocalLearningRunnable implements Runnable{
        @Override
        public void run() {
            try {
                while (true) {
                    if (locationQueueContainer.isEmpty()) {
                        locationQueueContainer.put(new LocationObject(getApplicationContext()));
                    } else {
                        LocationObject temp = locationQueueContainer.take();
                        temp.updateLocationData();
                        locationQueueContainer.put(temp);
                    }
                    Log.i("FromLearningThread","Going to sleep at: " + (new Date()).getTime()); //(!!!)
                    Thread.sleep(SLEEP_DURATION_MILLIS);
                    Log.i("FromLearningThread","Woke up at: " + (new Date()).getTime()); //(!!!)
                }
            } catch (InterruptedException e) {
                Log.i("FromLocalLearningRunnable","Interrupt Exception");
                e.printStackTrace();
            }
        }
    }

    private class LocalLearningRunnablev2 implements Runnable{
        @Override
        public void run() {
            while(true){
                LocationObject lob = new LocationObject(getApplicationContext());
                lob.updateLocationData();
                RetrieveLocationObject rlo = new RetrieveLocationObject(Looper.getMainLooper());
                Message msg = rlo.obtainMessage();
                msg.obj = lob;
                rlo.sendMessage(msg);
                try {
                    Thread.sleep(SLEEP_DURATION_MILLIS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class RetrieveLocationObject extends Handler{
        public RetrieveLocationObject(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            try{
                if (!locationQueueContainer.isEmpty()) {
                    LocationObject temp = locationQueueContainer.take();
                }
                locationQueueContainer.put((LocationObject)msg.obj);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }


    public class LocalLearningServiceBinder extends Binder {
        public LocalLearningService getBinderService(){
            return LocalLearningService.this;
        }
    }

    public LocalLearningService(){

    }

    /**
     * Will create new thread and pass in handler to obtain location data
     */
    @Override
    public void onCreate() {
        super.onCreate();

        locationUpdaterThread = new Thread(new LocalLearningRunnable(),"LocationUpdaterThread");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationUpdaterThread.start();
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return llsBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationUpdaterThread.interrupt();
        Toast.makeText(getApplicationContext(),"Service Stopped (hopefully)",Toast.LENGTH_LONG).show();
    }

    public LocationObject getLocationObject(){
        LocationObject toBeReturned = null;
        try {
            Log.i("FromLLS","Waiting for blocking queue"); //(!!!)
            toBeReturned = locationQueueContainer.take();
            Log.i("FromLLS","Able to access blocking queue"); //(!!!)
        } catch (InterruptedException e) {
            Log.i("fromotempotherthread","something wrong here");
            e.printStackTrace();
        }
        return toBeReturned;
    }
}
