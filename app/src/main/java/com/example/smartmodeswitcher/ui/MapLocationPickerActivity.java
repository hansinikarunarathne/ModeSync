package com.example.smartmodeswitcher.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.smartmodeswitcher.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapLocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng selectedLocation;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_location_picker);

        geocoder = new Geocoder(this, Locale.getDefault());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        FloatingActionButton fabCurrentLocation = findViewById(R.id.fabCurrentLocation);
        fabCurrentLocation.setOnClickListener(v -> getCurrentLocation());

        FloatingActionButton fabConfirm = findViewById(R.id.fabConfirm);
        fabConfirm.setOnClickListener(v -> {
            if (selectedLocation != null) {
                getAddressFromLocation(selectedLocation);
            } else {
                Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAddressFromLocation(LatLng location) {
        try {
            List<Address> addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String placeName = null;
                
                // Try to get the most specific place name available
                if (address.getFeatureName() != null && !address.getFeatureName().equals(address.getAddressLine(0))) {
                    placeName = address.getFeatureName();
                } else if (address.getLocality() != null) {
                    placeName = address.getLocality();
                } else if (address.getSubLocality() != null) {
                    placeName = address.getSubLocality();
                } else if (address.getAddressLine(0) != null) {
                    placeName = address.getAddressLine(0);
                }

                Intent resultIntent = new Intent();
                resultIntent.putExtra("latitude", location.latitude);
                resultIntent.putExtra("longitude", location.longitude);
                if (placeName != null && !placeName.isEmpty()) {
                    resultIntent.putExtra("address", placeName);
                }
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                // If no address found, just return coordinates
                Intent resultIntent = new Intent();
                resultIntent.putExtra("latitude", location.latitude);
                resultIntent.putExtra("longitude", location.longitude);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        } catch (IOException e) {
            // If geocoding fails, just return coordinates
            Intent resultIntent = new Intent();
            resultIntent.putExtra("latitude", location.latitude);
            resultIntent.putExtra("longitude", location.longitude);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng;
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng));
        });

        if (checkLocationPermission()) {
            getCurrentLocation();
        } else {
            requestLocationPermission();
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void getCurrentLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            selectedLocation = currentLocation;
                            mMap.clear();
                            mMap.addMarker(new MarkerOptions().position(currentLocation));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        } else {
                            Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            requestLocationPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
} 