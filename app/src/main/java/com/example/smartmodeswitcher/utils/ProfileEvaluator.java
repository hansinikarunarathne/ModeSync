package com.example.smartmodeswitcher.utils;

import android.content.Context;
import android.location.Location;
import android.os.Build;

import com.example.smartmodeswitcher.data.Profile;
import com.example.smartmodeswitcher.location.LocationManagerHelper;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                LocalTime current = LocalTime.parse(currentTime, formatter);
                LocalTime start = LocalTime.parse(startTime, formatter);
                LocalTime end = LocalTime.parse(endTime, formatter);

                if (start.isBefore(end)) {
                    return (!current.isBefore(start)) && (!current.isAfter(end));
                } else {
                    return current.isBefore(start) || current.isAfter(end);
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
