package com.example.smartmodeswitcher.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartmodeswitcher.R;
import com.example.smartmodeswitcher.data.Profile;
import com.example.smartmodeswitcher.utils.SharedPrefUtils;
import com.google.gson.Gson;

import java.util.List;

public class ProfileListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProfileAdapter adapter;
    private List<Profile> profileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_list);

        recyclerView = findViewById(R.id.profileRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadProfiles();
    }

    private void loadProfiles() {
        profileList = SharedPrefUtils.loadProfiles(this);
        adapter = new ProfileAdapter(this, profileList, new ProfileAdapter.OnProfileActionListener() {
            @Override
            public void onEdit(Profile profile, int position) {
                Intent intent = new Intent(ProfileListActivity.this, ProfileEditorActivity.class);
                intent.putExtra("editProfile", new Gson().toJson(profile));
                intent.putExtra("editIndex", position);
                startActivity(intent);
            }

            @Override
            public void onDelete(Profile profile, int position) {
                profileList.remove(position);
                SharedPrefUtils.saveProfiles(ProfileListActivity.this, profileList);
                adapter.notifyItemRemoved(position);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfiles();
    }
}