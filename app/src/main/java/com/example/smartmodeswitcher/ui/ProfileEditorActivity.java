package com.example.smartmodeswitcher.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartmodeswitcher.R;
import com.example.smartmodeswitcher.data.Profile;
import com.example.smartmodeswitcher.utils.SharedPrefUtils;

import java.util.List;

public class ProfileEditorActivity extends AppCompatActivity {

    private EditText profileName, startTime, endTime, location;
    private Spinner modeSpinner;
    private Button saveButton;
    private int editIndex = -1; // default -1 means add mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_editor);

        profileName = findViewById(R.id.profileName);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        location = findViewById(R.id.location);
        saveButton = findViewById(R.id.saveButton);
        modeSpinner = findViewById(R.id.modeSpinner);

        String[] modes = {"SILENT", "VIBRATE", "NORMAL"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeSpinner.setAdapter(adapter);

        // Check if we're editing an existing profile
        editIndex = getIntent().getIntExtra("editIndex", -1);
        if (editIndex != -1) {
            loadProfileData(editIndex);
        }

        saveButton.setOnClickListener(v -> saveProfile());
    }

    private void loadProfileData(int index) {
        List<Profile> profileList = SharedPrefUtils.loadProfiles(this);
        if (index >= 0 && index < profileList.size()) {
            Profile profile = profileList.get(index);
            profileName.setText(profile.getName());
            startTime.setText(profile.getStartTime());
            endTime.setText(profile.getEndTime());
            location.setText(profile.getLocation());

            String mode = profile.getMode();
            int spinnerPosition = ((ArrayAdapter<String>) modeSpinner.getAdapter()).getPosition(mode);
            modeSpinner.setSelection(spinnerPosition);
        }
    }

    private void saveProfile() {
        String name = profileName.getText().toString();
        String mode = modeSpinner.getSelectedItem().toString();
        String start = startTime.getText().toString();
        String end = endTime.getText().toString();
        String loc = location.getText().toString();

        if (name.isEmpty()) {
            Toast.makeText(this, "Profile name required", Toast.LENGTH_SHORT).show();
            return;
        }

        Profile profile = new Profile(name, mode, start, end, loc);
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
