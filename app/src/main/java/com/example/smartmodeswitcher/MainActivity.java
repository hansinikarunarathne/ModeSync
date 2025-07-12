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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.cardview.widget.CardView;
import com.example.smartmodeswitcher.mode.WorkScheduler;
import com.example.smartmodeswitcher.sensors.SensorEventListenerImpl;
import com.example.smartmodeswitcher.sensors.SensorManagerHelper;
import com.example.smartmodeswitcher.ui.ProfileEditorActivity;
import com.example.smartmodeswitcher.ui.ProfileListActivity;
import com.example.smartmodeswitcher.ui.TodayEventsActivity;
import com.example.smartmodeswitcher.utils.CalendarHelper;
import android.os.Handler;
import android.os.Looper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import java.lang.StringBuilder;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private SensorManagerHelper sensorManagerHelper;
    private SensorEventListenerImpl sensorEventListener;
    private TextView tvContext, tvValues, tvSensorStatus, tvCalendarEvent;
    private TextView tvValueAccelometer, tvValueGyroscope, tvValueLightSensor, tvValueProximity;
    private Button btnEditProfiles;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CardView contextCard, sensorCard, calendarCard;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 101;
    private static final int CALENDAR_PERMISSION_REQUEST_CODE = 102;
    private boolean isInitialized = false;
    private boolean isResumed = false;
    private CalendarHelper calendarHelper;
    private Handler handler;
    private static final long CALENDAR_UPDATE_INTERVAL = 60000; // Update every minute

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Initializing activity");
        setContentView(R.layout.activity_main);

        initializeViews();
        setupClickListeners();
        setupSwipeRefresh();
        calendarHelper = new CalendarHelper(this);
        handler = new Handler(Looper.getMainLooper());
        
        // Check permissions in sequence
        checkInitialPermissions();
        isInitialized = true;
    }

    private void initializeViews() {
        Log.d(TAG, "initializeViews: Setting up views");
        tvContext = findViewById(R.id.tvContext);
        tvValueAccelometer = findViewById(R.id.accelerometerValues);
        tvValueGyroscope = findViewById(R.id.gyroscopeValue);
        tvValueLightSensor = findViewById(R.id.LightSnsorValues);
        tvValueProximity = findViewById(R.id.proximityValues);
        tvCalendarEvent = findViewById(R.id.tvCalendarEvent);
        btnEditProfiles = findViewById(R.id.btnEditProfiles);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        
        // Initialize cards
        contextCard = findViewById(R.id.contextCard);
        calendarCard = findViewById(R.id.calendarCard);

        sensorEventListener = new SensorEventListenerImpl(this, tvContext, tvValueAccelometer, tvValueGyroscope, tvValueLightSensor, tvValueProximity);
        sensorManagerHelper = new SensorManagerHelper(this, sensorEventListener);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Refresh sensor data
            sensorManagerHelper.refreshSensors();
            
            // Refresh calendar data
            updateCalendarEvent();
            
            // Stop refresh animation after a short delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                swipeRefreshLayout.setRefreshing(false);
            }, 1000);
        });
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

        findViewById(R.id.btnViewTodayEvents).setOnClickListener(v -> {
            if (!isResumed) return;
            Intent intent = new Intent(MainActivity.this, TodayEventsActivity.class);
            startActivity(intent);
        });

        // Add card click listeners
        contextCard.setOnClickListener(v -> {
            // Show detailed context information
            showContextDetails();
        });

        calendarCard.setOnClickListener(v -> {
            // Show detailed calendar information
            showCalendarDetails();
        });
    }

    private void showContextDetails() {
        new AlertDialog.Builder(this)
            .setTitle("Context Details")
            .setMessage("Current context information and details about how it was determined.")
            .setPositiveButton("OK", null)
            .show();
    }

    private void showSensorDetails() {
        new AlertDialog.Builder(this)
            .setTitle("Sensor Details")
            .setMessage("Detailed information about active sensors and their current values.")
            .setPositiveButton("OK", null)
            .show();
    }

    private void showCalendarDetails() {
        new AlertDialog.Builder(this)
            .setTitle("Calendar Details")
            .setMessage("Detailed information about current and upcoming calendar events.")
            .setPositiveButton("OK", null)
            .show();
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

        // Check calendar permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CALENDAR},
                    CALENDAR_PERMISSION_REQUEST_CODE);
        } else {
            startCalendarUpdates();
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

    private void startCalendarUpdates() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                updateCalendarEvent();
                handler.postDelayed(this, CALENDAR_UPDATE_INTERVAL);
            }
        });
    }

    private void updateCalendarEvent() {
        CalendarHelper.CalendarEvent currentEvent = calendarHelper.getCurrentEvent();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String eventText = "No events scheduled for today";
        if (currentEvent != null) {
            // Only handle events that are currently active
            long currentTime = System.currentTimeMillis();
            if (currentTime >= currentEvent.startTime && currentTime <= currentEvent.endTime) {
                // Display the current event
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String startTime = timeFormat.format(new Date(currentEvent.startTime));
                String endTime = timeFormat.format(new Date(currentEvent.endTime));

                eventText = String.format("Current Event: %s\nTime: %s - %s\nCalendar: %s",
                        currentEvent.title, startTime, endTime, currentEvent.calendarName);

                tvCalendarEvent.setText(eventText);

                // Determine if we should set silent mode based on event properties
                boolean shouldSetSilent = shouldSetSilentMode(currentEvent);

                // Set device mode based on event type
                if (notificationManager != null && notificationManager.isNotificationPolicyAccessGranted()) {
                    if (shouldSetSilent) {
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
                        Log.d(TAG, "Setting silent mode for event: " + currentEvent.title);
                    } else {
                        WorkScheduler.scheduleProfileEvaluation(this);
                        Log.d(TAG, "Using normal profile evaluation for event: " + currentEvent.title);
                    }
                }
            } else {
                // Not currently in an event, show no events message
                tvCalendarEvent.setText("No events scheduled for today");
                if (notificationManager != null && notificationManager.isNotificationPolicyAccessGranted()) {
                    WorkScheduler.scheduleProfileEvaluation(this);
                }
            }
        } else {
            // No event at all, show no events message
            tvCalendarEvent.setText("No events scheduled for today");
            if (notificationManager != null && notificationManager.isNotificationPolicyAccessGranted()) {
                WorkScheduler.scheduleProfileEvaluation(this);
            }
        }
    }

    private boolean shouldSetSilentMode(CalendarHelper.CalendarEvent event) {
        // Check if the event has attendees (indicates a meeting)
        boolean hasAttendees = event.description != null && 
                             (event.description.contains("attendee") || 
                              event.description.contains("participant") ||
                              event.description.contains("invitee"));

        // Check if the event has a meeting link (indicates a virtual meeting)
        boolean hasMeetingLink = event.description != null && 
                               (event.description.contains("meet.google.com") ||
                                event.description.contains("zoom.us") ||
                                event.description.contains("teams.microsoft.com") ||
                                event.description.contains("webex.com"));

        // Check if the event has a specific duration (typical for meetings)
        long duration = event.endTime - event.startTime;
        boolean isTypicalMeetingDuration = duration >= 15 * 60 * 1000 && // 15 minutes
                                         duration <= 120 * 60 * 1000;    // 2 hours

        // Check for important personal events based on title or description
        boolean isImportantEvent = (event.title != null && 
            (event.title.toLowerCase().contains("doctor") ||
             event.title.toLowerCase().contains("appointment") ||
             event.title.toLowerCase().contains("interview") ||
             event.title.toLowerCase().contains("exam") ||
             event.title.toLowerCase().contains("presentation") ||
             event.title.toLowerCase().contains("meeting"))) ||
             (event.description != null &&
             (event.description.toLowerCase().contains("doctor") ||
              event.description.toLowerCase().contains("appointment") ||
              event.description.toLowerCase().contains("interview") ||
              event.description.toLowerCase().contains("exam") ||
              event.description.toLowerCase().contains("presentation") ||
              event.description.toLowerCase().contains("meeting")));

        // Set silent mode if any of the indicators suggest it's a meeting or an important event
        return hasAttendees || hasMeetingLink || isTypicalMeetingDuration || isImportantEvent;
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

        if (requestCode == CALENDAR_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCalendarUpdates();
            } else {
                showPermissionDeniedDialog("Calendar access is required to manage device modes based on events.");
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
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

