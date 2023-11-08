package com.example.getlatlonwithoutinternet;

public class LocationData {

    private double latitude;
    private double longitude;
    private String timestamp;

    private float distance;

    public LocationData(double latitude, double longitude, String timestamp, float distance) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.distance = distance;
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


    public float getDistance() {
        return distance;
    }
}
