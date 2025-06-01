package com.example.smartmodeswitcher.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartmodeswitcher.R;
import com.example.smartmodeswitcher.data.Profile;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {
    public interface OnProfileActionListener {
        void onEdit(Profile profile, int position);
        void onDelete(Profile profile, int position);
    }

    private List<Profile> profiles;
    private Context context;
    public OnProfileActionListener listener;

    public ProfileAdapter(Context context, List<Profile> profiles, OnProfileActionListener listener) {
        this.context = context;
        this.profiles = profiles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        Profile profile = profiles.get(position);
        holder.profileName.setText(profile.getName());
        holder.profileMode.setText(profile.getMode());
        
        // Robust time display
        String start = profile.getStartTime();
        String end = profile.getEndTime();
        if (start != null && !start.trim().isEmpty() && end != null && !end.trim().isEmpty()) {
            holder.timeRange.setText(start + " - " + end);
        } else {
            holder.timeRange.setText("No time set");
        }
        holder.timeRange.setVisibility(View.VISIBLE);

        // Display location information
        if (profile.getLatitude() != null && profile.getLongitude() != null) {
            String locationText = profile.getLocationName() != null && !profile.getLocationName().isEmpty() ?
                    profile.getLocationName() :
                    String.format("(%.6f, %.6f)", profile.getLatitude(), profile.getLongitude());
            holder.location.setText(locationText);
        } else {
            holder.location.setText("No location set");
        }
        holder.location.setVisibility(View.VISIBLE);

        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProfileEditorActivity.class);
            intent.putExtra("editIndex", position);
            context.startActivity(intent);
        });
        holder.deleteButton.setOnClickListener(v -> listener.onDelete(profile, position));
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        TextView profileName, profileMode, timeRange, location;
        MaterialButton editButton, deleteButton;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            profileName = itemView.findViewById(R.id.profileName);
            profileMode = itemView.findViewById(R.id.profileMode);
            timeRange = itemView.findViewById(R.id.timeRange);
            location = itemView.findViewById(R.id.location);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}