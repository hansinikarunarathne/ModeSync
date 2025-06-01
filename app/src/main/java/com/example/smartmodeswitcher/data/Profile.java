package com.example.smartmodeswitcher.data;

public class Profile {
    private String name;
    private String mode; // "SILENT", "VIBRATE", "NORMAL"
    private String startTime;
    private String endTime;
    private Double latitude; // Location coordinates
    private Double longitude;
    private String locationName; // Optional location name

    public Profile(String name, String mode, String startTime, String endTime, Double latitude, Double longitude, String locationName) {
        this.name = name;
        this.mode = mode;
        this.startTime = startTime;
        this.endTime = endTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
    }

    // Getters and setters
    public String getName() { return name; }
    public String getMode() { return mode; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getLocationName() { return locationName; }

    public void setName(String name) { this.name = name; }
    public void setMode(String mode) { this.mode = mode; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
}
