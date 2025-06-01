package com.example.smartmodeswitcher.data;

public class Profile {
    private String name;
    private String mode; // "SILENT", "VIBRATE", "NORMAL"
    private String startTime;
    private String endTime;
    private String location; // Optional

    public Profile(String name, String mode, String startTime, String endTime, String location) {
        this.name = name;
        this.mode = mode;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
    }

    // Getters and setters
    public String getName() { return name; }
    public String getMode() { return mode; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getLocation() { return location; }

    public void setName(String name) { this.name = name; }
    public void setMode(String mode) { this.mode = mode; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setLocation(String location) { this.location = location; }
}
