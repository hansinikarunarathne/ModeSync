package com.example.smartmodeswitcher.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartmodeswitcher.R;
import com.example.smartmodeswitcher.data.Profile;
import com.example.smartmodeswitcher.utils.SharedPrefUtils;
import com.example.smartmodeswitcher.utils.ProfileEvaluator;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class ProfileListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProfileAdapter adapter;
    private List<Profile> allProfiles;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_list);

        // Set up toolbar navigation click listener
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.profileRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up tab layout
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterProfiles(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Add FAB click listener
        findViewById(R.id.fabAddProfile).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileListActivity.this, ProfileEditorActivity.class);
            startActivity(intent);
        });

        loadProfiles();
    }

    private void loadProfiles() {
        allProfiles = SharedPrefUtils.loadProfiles(this);
        adapter = new ProfileAdapter(this, new ArrayList<>(), new ProfileAdapter.OnProfileActionListener() {
            @Override
            public void onEdit(Profile profile, int position) {
                Profile activeProfile = ProfileEvaluator.getActiveProfile(ProfileListActivity.this);
                if (activeProfile != null && profile.getName().equals(activeProfile.getName())) {
                    Toast.makeText(ProfileListActivity.this, 
                        "Cannot edit currently active profile. Please wait until it becomes inactive.", 
                        Toast.LENGTH_LONG).show();
                    return;
                }
                
                Intent intent = new Intent(ProfileListActivity.this, ProfileEditorActivity.class);
                intent.putExtra("editProfile", new Gson().toJson(profile));
                intent.putExtra("editIndex", allProfiles.indexOf(profile));
                startActivity(intent);
            }

            @Override
            public void onDelete(Profile profile, int position) {
                int actualPosition = allProfiles.indexOf(profile);
                allProfiles.remove(actualPosition);
                SharedPrefUtils.saveProfiles(ProfileListActivity.this, allProfiles);
                filterProfiles(tabLayout.getSelectedTabPosition());
            }
        });
        recyclerView.setAdapter(adapter);
        filterProfiles(tabLayout.getSelectedTabPosition());
    }

    private void filterProfiles(int tabPosition) {
        List<Profile> filteredProfiles = new ArrayList<>();
        Profile activeProfile = ProfileEvaluator.getActiveProfile(this);

        switch (tabPosition) {
            case 0: // All Profiles
                filteredProfiles.addAll(allProfiles);
                break;
            case 1: // Active
                // Show only the currently active profile
                if (activeProfile != null) {
                    filteredProfiles.add(activeProfile);
                }
                break;
            case 2: // Inactive
                // Show all profiles except the currently active one
                for (Profile profile : allProfiles) {
                    if (activeProfile == null || !profile.getName().equals(activeProfile.getName())) {
                        filteredProfiles.add(profile);
                    }
                }
                break;
        }

        adapter.updateProfiles(filteredProfiles);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfiles();
    }
}