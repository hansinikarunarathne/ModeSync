package com.example.smartmodeswitcher.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

public class LocationManagerHelper {

    @SuppressLint("MissingPermission")
    public static Location getLastKnownLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = null;

        if (locationManager != null) {
            for (String provider : locationManager.getProviders(true)) {
                Location loc = locationManager.getLastKnownLocation(provider);
                if (loc != null) {
                    location = loc;
                    break;
                }
            }
        }

        return location;
    }
}
