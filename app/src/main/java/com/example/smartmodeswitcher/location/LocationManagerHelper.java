package com.example.smartmodeswitcher.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import androidx.core.app.ActivityCompat;

public class LocationManagerHelper {

    public static Location getLastKnownLocation(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
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
