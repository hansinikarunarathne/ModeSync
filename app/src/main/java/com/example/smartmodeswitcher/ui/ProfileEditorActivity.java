package com.example.smartmodeswitcher.ui;

import static android.content.Intent.getIntent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

import com.example.smartmodeswitcher.R;
import com.example.smartmodeswitcher.data.Profile;
import com.example.smartmodeswitcher.utils.SharedPrefUtils;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ProfileEditorActivity extends AppCompatActivity {
    private static final int MAP_LOCATION_PICKER_REQUEST = 1;

    private EditText profileName, startTime, endTime, locationName;
    private AutoCompleteTextView modeSpinner;
    private MaterialButton saveButton;
    private MaterialButton selectLocationButton;
    private int editIndex = -1; // default -1 means add mode
    private Double selectedLatitude;
    private Double selectedLongitude;

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

        // Check if we're editing an existing profile
        editIndex = getIntent().getIntExtra("editIndex", -1);
        if (editIndex != -1) {
            loadProfileData(editIndex);
        }

        selectLocationButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapLocationPickerActivity.class);
            mapLocationPickerLauncher.launch(intent);
        });

        saveButton.setOnClickListener(v -> saveProfile());
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
            int spinnerPosition = ((ArrayAdapter<String>) modeSpinner.getAdapter()).getPosition(mode);
            modeSpinner.setSelection(spinnerPosition);
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
