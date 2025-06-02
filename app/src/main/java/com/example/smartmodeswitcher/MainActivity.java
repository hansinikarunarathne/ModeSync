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
import android.util.Log;
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
    private static final String TAG = "MainActivity";
    private SensorManagerHelper sensorManagerHelper;
    private SensorEventListenerImpl sensorEventListener;
    private TextView tvContext, tvValues, tvSensorStatus;
    private Button btnEditProfiles;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 101;
    private boolean isInitialized = false;
    private boolean isResumed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Initializing activity");
        setContentView(R.layout.activity_main);

        initializeViews();
        setupClickListeners();
        
        // Check permissions in sequence
        checkInitialPermissions();
        isInitialized = true;
    }

    private void initializeViews() {
        Log.d(TAG, "initializeViews: Setting up views");
        tvContext = findViewById(R.id.tvContext);
        tvValues = findViewById(R.id.tvValues);
        tvSensorStatus = findViewById(R.id.tvSensorStatus);
        btnEditProfiles = findViewById(R.id.btnEditProfiles);

        tvSensorStatus.setText("");
        sensorEventListener = new SensorEventListenerImpl(this, tvContext, tvValues);
        sensorManagerHelper = new SensorManagerHelper(this, sensorEventListener, tvSensorStatus);
    }

    private void setupClickListeners() {
        Log.d(TAG, "setupClickListeners: Setting up button click listeners");
        btnEditProfiles.setOnClickListener(v -> {
            if (!isResumed) return;
            Intent intent = new Intent(MainActivity.this, ProfileEditorActivity.class);
            startActivity(intent);
        });

        Button manageProfilesButton = findViewById(R.id.manageProfilesButton);
        manageProfilesButton.setOnClickListener(v -> {
            if (!isResumed) return;
            Intent intent = new Intent(MainActivity.this, ProfileListActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.fabAddProfile).setOnClickListener(v -> {
            if (!isResumed) return;
            Intent intent = new Intent(MainActivity.this, ProfileEditorActivity.class);
            startActivity(intent);
        });
    }

    private void checkInitialPermissions() {
        Log.d(TAG, "checkInitialPermissions: Checking required permissions");
        // First check notification policy access
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null && !notificationManager.isNotificationPolicyAccessGranted()) {
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

        // Then check location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            checkBackgroundLocationPermission();
        }
    }

    private void checkBackgroundLocationPermission() {
        Log.d(TAG, "checkBackgroundLocationPermission: Checking background location permission");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                WorkScheduler.scheduleProfileEvaluation(this);
            }
        } else {
            WorkScheduler.scheduleProfileEvaluation(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: Handling permission result for request code: " + requestCode);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkBackgroundLocationPermission();
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
        Log.d(TAG, "showPermissionDeniedDialog: Showing permission denied dialog");
        new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage(message)
                .setPositiveButton("Retry", (dialog, which) -> checkInitialPermissions())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Resuming activity");
        isResumed = true;
        if (isInitialized) {
            sensorManagerHelper.registerSensors();
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: Pausing activity");
        isResumed = false;
        if (isInitialized) {
            sensorManagerHelper.unregisterSensors();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: Destroying activity");
        isResumed = false;
        if (isInitialized) {
            if (sensorManagerHelper != null) {
                sensorManagerHelper.unregisterSensors();
                sensorManagerHelper = null;
            }
            if (sensorEventListener != null) {
                sensorEventListener = null;
            }
        }
        super.onDestroy();
    }
}