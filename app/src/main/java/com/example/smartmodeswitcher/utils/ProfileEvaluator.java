package com.example.smartmodeswitcher.utils;

import android.content.Context;
import android.location.Location;
import android.text.TextUtils;
import com.example.smartmodeswitcher.data.Profile;
import com.example.smartmodeswitcher.location.LocationManagerHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileEvaluator {

    public static Profile getActiveProfile(Context context) {
        List<Profile> profiles = SharedPrefUtils.loadProfiles(context);
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        Location currentLocation = LocationManagerHelper.getLastKnownLocation(context);

        for (Profile profile : profiles) {
            boolean isTimeMatch = isTimeInRange(currentTime, profile.getStartTime(), profile.getEndTime());
            boolean isLocationMatch = true;

            if (profile.getLatitude() != null && profile.getLongitude() != null && currentLocation != null) {
                Location profileLocation = new Location("");
                profileLocation.setLatitude(profile.getLatitude());
                profileLocation.setLongitude(profile.getLongitude());
                
                // Check if current location is within 100 meters of profile location
                float distanceInMeters = currentLocation.distanceTo(profileLocation);
                isLocationMatch = distanceInMeters <= 100;
            }

            if (isTimeMatch && isLocationMatch) {
                return profile;
            }
        }

        return null;
    }

    private static boolean isTimeInRange(String currentTime, String startTime, String endTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date current = sdf.parse(currentTime);
            Date start = sdf.parse(startTime);
            Date end = sdf.parse(endTime);

            return current != null && start != null && end != null &&
                    current.after(start) && current.before(end);
        } catch (Exception e) {
            return false;
        }
    }
}
