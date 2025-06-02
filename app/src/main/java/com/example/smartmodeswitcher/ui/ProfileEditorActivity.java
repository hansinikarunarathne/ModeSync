package com.example.smartmodeswitcher.ui;

import static android.content.Intent.getIntent;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartmodeswitcher.utils.ProfileEvaluator;
import com.google.android.material.appbar.MaterialToolbar;

import com.example.smartmodeswitcher.R;
import com.example.smartmodeswitcher.data.Profile;
import com.example.smartmodeswitcher.utils.SharedPrefUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ProfileEditorActivity extends AppCompatActivity {
    private static final int MAP_LOCATION_PICKER_REQUEST = 1;

    private TextInputEditText profileName, startTime, endTime, locationName;
    private AutoCompleteTextView modeSpinner;
    private MaterialButton saveButton;
    private MaterialButton selectLocationButton;
    private int editIndex = -1; // default -1 means add mode
    private Double selectedLatitude;
    private Double selectedLongitude;
    private Calendar calendar;
    private SimpleDateFormat timeFormat;

    private final ActivityResultLauncher<Intent> mapLocationPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedLatitude = result.getData().getDoubleExtra("latitude", 0);
                    selectedLongitude = result.getData().getDoubleExtra("longitude", 0);
                    String address = result.getData().getStringExtra("address");
                    if (address != null && !address.isEmpty()) {
                        locationName.setText(address);
                    } else {
                        locationName.setText(String.format("Selected Location (%.6f, %.6f)", selectedLatitude, selectedLongitude));
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_editor);

        // Initialize calendar and time format
        calendar = Calendar.getInstance();
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Set up toolbar navigation click listener
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        profileName = findViewById(R.id.profileName);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        locationName = findViewById(R.id.locationName);
        saveButton = findViewById(R.id.saveButton);
        modeSpinner = findViewById(R.id.modeSpinner);
        selectLocationButton = findViewById(R.id.selectLocationButton);

        String[] modes = {"SILENT", "VIBRATE", "NORMAL"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, modes);
        modeSpinner.setAdapter(adapter);

        // Set up time picker click listeners
        startTime.setOnClickListener(v -> showTimePickerDialog(true));
        endTime.setOnClickListener(v -> showTimePickerDialog(false));

        // Check if we're editing an existing profile
        editIndex = getIntent().getIntExtra("editIndex", -1);
        String profileJson = getIntent().getStringExtra("editProfile");
        
        if (editIndex != -1 && profileJson != null) {
            try {
                Profile profile = new Gson().fromJson(profileJson, Profile.class);
                
                // Check if this is the active profile
                Profile activeProfile = ProfileEvaluator.getActiveProfile(this);
                if (activeProfile != null && profile.getName().equals(activeProfile.getName())) {
                    Toast.makeText(this, 
                        "Cannot edit currently active profile. Please wait until it becomes inactive.", 
                        Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                
                profileName.setText(profile.getName());
                startTime.setText(profile.getStartTime());
                endTime.setText(profile.getEndTime());
                
                if (profile.getLatitude() != null && profile.getLongitude() != null) {
                    selectedLatitude = profile.getLatitude();
                    selectedLongitude = profile.getLongitude();
                    locationName.setText(profile.getLocationName() != null ? 
                        profile.getLocationName() : 
                        String.format("Selected Location (%.6f, %.6f)", selectedLatitude, selectedLongitude));
                }

                String mode = profile.getMode();
                modeSpinner.setText(mode, false);
            } catch (Exception e) {
                // If there's any error parsing the JSON, fall back to loading from SharedPreferences
                loadProfileData(editIndex);
            }
        }

        selectLocationButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapLocationPickerActivity.class);
            mapLocationPickerLauncher.launch(intent);
        });

        saveButton.setOnClickListener(v -> saveProfile());
    }

    private void showTimePickerDialog(boolean isStartTime) {
        // Get current time or existing time from the field
        String currentTime = isStartTime ? startTime.getText().toString() : endTime.getText().toString();
        int hour, minute;
        
        if (!currentTime.isEmpty()) {
            try {
                String[] timeParts = currentTime.split(":");
                hour = Integer.parseInt(timeParts[0]);
                minute = Integer.parseInt(timeParts[1]);
            } catch (Exception e) {
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                minute = calendar.get(Calendar.MINUTE);
            }
        } else {
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, selectedHour, selectedMinute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                calendar.set(Calendar.MINUTE, selectedMinute);
                String formattedTime = timeFormat.format(calendar.getTime());
                if (isStartTime) {
                    startTime.setText(formattedTime);
                } else {
                    endTime.setText(formattedTime);
                }
            },
            hour,
            minute,
            true // 24-hour format
        );

        timePickerDialog.setTitle(isStartTime ? "Select Start Time" : "Select End Time");
        timePickerDialog.show();
    }

    private void loadProfileData(int index) {
        List<Profile> profileList = SharedPrefUtils.loadProfiles(this);
        if (index >= 0 && index < profileList.size()) {
            Profile profile = profileList.get(index);
            profileName.setText(profile.getName());
            startTime.setText(profile.getStartTime());
            endTime.setText(profile.getEndTime());
            
            if (profile.getLatitude() != null && profile.getLongitude() != null) {
                selectedLatitude = profile.getLatitude();
                selectedLongitude = profile.getLongitude();
                locationName.setText(profile.getLocationName() != null ? 
                    profile.getLocationName() : 
                    String.format("Selected Location (%.6f, %.6f)", selectedLatitude, selectedLongitude));
            }

            String mode = profile.getMode();
            modeSpinner.setText(mode, false);
        }
    }

    private void saveProfile() {
        String name = profileName.getText().toString();
        String mode = modeSpinner.getText().toString();
        String start = startTime.getText().toString();
        String end = endTime.getText().toString();
        String loc = locationName.getText().toString();

        if (name.isEmpty()) {
            Toast.makeText(this, "Profile name required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mode.isEmpty()) {
            Toast.makeText(this, "Please select a mode", Toast.LENGTH_SHORT).show();
            return;
        }

        Profile profile = new Profile(name, mode, start, end, selectedLatitude, selectedLongitude, loc);
        List<Profile> profileList = SharedPrefUtils.loadProfiles(this);

        if (editIndex != -1 && editIndex < profileList.size()) {
            profileList.set(editIndex, profile); // replace existing
        } else {
            profileList.add(profile); // add new
        }

        SharedPrefUtils.saveProfiles(this, profileList);

        Toast.makeText(this, "Profile Saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}
