package com.example.smartmodeswitcher;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.app.NotificationManager;
import android.content.Intent;
import android.provider.Settings;
import android.os.Build;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartmodeswitcher.mode.WorkScheduler;
import com.example.smartmodeswitcher.sensors.SensorEventListenerImpl;
import com.example.smartmodeswitcher.sensors.SensorManagerHelper;
import com.example.smartmodeswitcher.ui.ProfileEditorActivity;
import com.example.smartmodeswitcher.ui.ProfileListActivity;

public class MainActivity extends AppCompatActivity {

    private SensorManagerHelper sensorManagerHelper;
    private SensorEventListenerImpl sensorEventListener;
    private TextView tvContext, tvValues, tvSensorStatus;
    private Button btnEditProfiles;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WorkScheduler.scheduleProfileEvaluation(this);

        // Grant DND access
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (!notificationManager.isNotificationPolicyAccessGranted()) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission Needed")
                        .setMessage("Please allow Do Not Disturb access so the app can change sound modes automatically.")
                        .setPositiveButton("Grant Access", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }


        tvContext = findViewById(R.id.tvContext);
        tvValues = findViewById(R.id.tvValues);
        tvSensorStatus = findViewById(R.id.tvSensorStatus);

        tvSensorStatus.setText("");
        sensorEventListener = new SensorEventListenerImpl(this, tvContext, tvValues);
        sensorManagerHelper = new SensorManagerHelper(this, sensorEventListener, tvSensorStatus);

        btnEditProfiles = findViewById(R.id.btnEditProfiles);
        btnEditProfiles.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileEditorActivity.class);
            startActivity(intent);
        });

        Button manageProfilesButton = findViewById(R.id.manageProfilesButton);
        manageProfilesButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileListActivity.class);
            startActivity(intent);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManagerHelper.registerSensors();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManagerHelper.unregisterSensors();
    }
}