package com.example.smartmodeswitcher;
import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.app.NotificationManager;
import android.content.Intent;
import android.provider.Settings;
import android.os.Build;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkAndScheduleProfileEvaluation();

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

        // Add FAB click listener
        findViewById(R.id.fabAddProfile).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileEditorActivity.class);
            startActivity(intent);
        });

    }

    private void checkAndScheduleProfileEvaluation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // For Android 10 and above, check for background location permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                            BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
                    );
                    return; // Wait for permission result before scheduling
                }
            }
            // Permissions all granted, schedule work
            WorkScheduler.scheduleProfileEvaluation(this);

        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkAndScheduleProfileEvaluation(); // Check for background permission next
            } else {
                showPermissionDeniedDialog("Location access is required to activate profiles based on location.");
            }
        } else if (requestCode == BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                WorkScheduler.scheduleProfileEvaluation(this);
            } else {
                showPermissionDeniedDialog("Background location access is required for continuous profile evaluation.");
            }
        }
    }

    private void showPermissionDeniedDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage(message)
                .setPositiveButton("Retry", (dialog, which) -> checkAndScheduleProfileEvaluation())
                .setNegativeButton("Cancel", null)
                .show();
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