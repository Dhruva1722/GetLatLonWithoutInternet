package com.example.getlatlonwithoutinternet;

public class LocationData {

    private double latitude;
    private double longitude;
    private String timestamp;

    public LocationData(double latitude, double longitude, String timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
