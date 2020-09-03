package com.example.dp4coruna;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dp4coruna.dataManagement.AppDatabase;
import com.example.dp4coruna.localLearning.learningService.LocalLearningService;
import com.example.dp4coruna.localLearning.learningService.movementTracker.Acceleration.AccelerationSensor;
import com.example.dp4coruna.localLearning.learningService.movementTracker.MovementSensor;
import com.example.dp4coruna.localLearning.learningService.movementTracker.TrackMovement;
import com.example.dp4coruna.localLearning.location.LocationObject;
import com.example.dp4coruna.ml.MLModel;
import android.database.Cursor;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;


public class TempResultsActivity extends AppCompatActivity {

    private TextView dataView;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_results);
        dataView = findViewById(R.id.dataViewBox);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


}
