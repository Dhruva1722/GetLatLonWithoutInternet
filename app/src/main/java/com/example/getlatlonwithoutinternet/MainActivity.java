package com.example.getlatlonwithoutinternet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    FusedLocationProviderClient mFusedLocationClient;

    TextView latitudeTextView, longitTextView;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private LocationDatabaseHelper dbHelper;
    private boolean locationUpdatesActive = false;
    private Location lastKnownLocation = null;

    Button refreshClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitudeTextView = findViewById(R.id.latVal);
        longitTextView = findViewById(R.id.lonVal);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        Button startStopButton = findViewById(R.id.startStopButton);
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (locationUpdatesActive) {
                    stopLocationUpdates();
                    startStopButton.setText("Start");
                } else {
                    startLocationUpdates();
                    startStopButton.setText("Stop");
                }
            }
        });

        refreshClick = findViewById(R.id.refreshButton);
        refreshClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<LocationData> locationDataList = dbHelper.getAllLocationData();
                LocationDataAdapter adapter = new LocationDataAdapter( MainActivity.this, locationDataList); // Use MainActivity.this as the context
                ListView locationListView = findViewById(R.id.locationListView);
                locationListView.setAdapter(adapter);
            }
        });
        // Initialize dbHelper here
        dbHelper = new LocationDatabaseHelper(this);
    }

    private void startLocationUpdates() {
        if (checkLocationPermissions()) {
            requestNewLocationData();
            locationUpdatesActive = true;
        } else {
            requestLocationPermissions();
        }
    }

    private void stopLocationUpdates() {
        locationUpdatesActive = false;
    }

    private boolean checkLocationPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void getLastLocation() {
        if (checkLocationPermissions()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mFusedLocationClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(Task<Location> task) {
                    Location location = task.getResult();
                    if (location == null) {
                        requestNewLocationData();
                    } else {
                        latitudeTextView.setText("Latitude: " + location.getLatitude());
                        longitTextView.setText("Longitude: " + location.getLongitude());
                        Log.d("------Last Location-------", "===== " + location.getLatitude());
                        Log.d("------Last Location-------", "===== " + location.getLongitude());

                        insertLocationIntoDatabase(location.getLatitude(), location.getLongitude());
                    }
                }
            });
        }
    }
    private void requestNewLocationData() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setNumUpdates(1);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();


                if (lastKnownLocation != null) {
                    float distance = lastKnownLocation.distanceTo(location); // Calculate the distance
                    Log.d("Distance", "Distance: " + distance + " meters");
                }
                lastKnownLocation = location;

                latitudeTextView.setText(" " + location.getLatitude());
                longitTextView.setText(" " + location.getLongitude());

                Log.d("-------New location------", "onLocationResult: " + location.getLatitude());
                Log.d("-------New location------", "onLocationResult: " + location.getLongitude());
                insertLocationIntoDatabase(location.getLatitude(), location.getLongitude());
            }
        }, null);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Location permission denied. Please enable it in your device settings.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void insertLocationIntoDatabase(double latitude, double longitude) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(LocationDatabaseHelper.COLUMN_LATITUDE, latitude);
        values.put(LocationDatabaseHelper.COLUMN_LONGITUDE, longitude);

        long currentTimestampMillis = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedTimestamp = sdf.format(new Date(currentTimestampMillis));
        values.put(LocationDatabaseHelper.COLUMN_TIMESTAMP, formattedTimestamp);

        // Calculate the distance between last known location and the new location
        if (lastKnownLocation != null) {
            Location newLocation = new Location("newLocation");
            newLocation.setLatitude(latitude);
            newLocation.setLongitude(longitude);
            float distance = lastKnownLocation.distanceTo(newLocation);
            values.put(LocationDatabaseHelper.COLUMN_DISTANCE, distance);
        } else {
            values.put(LocationDatabaseHelper.COLUMN_DISTANCE, 0.0); // Default value if lastKnownLocation is null
        }

        long newRowId = db.insert(LocationDatabaseHelper.TABLE_LOCATIONS, null, values);

        if (newRowId != -1) {
            Toast.makeText(this, "Successfully saved location", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed to save location", Toast.LENGTH_LONG).show();
        }

        db.close();
    }
}


//        if (checkLocationPermissions()) {
//            requestNewLocationData();
//
//        } else {
//            getLastLocation();
//            requestLocationPermissions();
//        }



//    private void insertLocationIntoDatabase(double latitude, double longitude) {
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//        ContentValues values = new ContentValues();
//        values.put(LocationDatabaseHelper.COLUMN_LATITUDE, latitude);
//        values.put(LocationDatabaseHelper.COLUMN_LONGITUDE, longitude);
//
//        long currentTimestampMillis = System.currentTimeMillis();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//        String formattedTimestamp = sdf.format(new Date(currentTimestampMillis));
//        values.put(LocationDatabaseHelper.COLUMN_TIMESTAMP, formattedTimestamp);
//
//        long newRowId = db.insert(LocationDatabaseHelper.TABLE_LOCATIONS, null, values);
//
//        if (newRowId != -1) {
//            Toast.makeText(this, "Successfully saved location", Toast.LENGTH_LONG).show();
//        } else {
//            Toast.makeText(this, "Failed to save location", Toast.LENGTH_LONG).show();
//        }
//        db.close();
//    }