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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    FusedLocationProviderClient mFusedLocationClient;

    TextView latitudeTextView, longitTextView;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private LocationDatabaseHelper dbHelper;
    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitudeTextView = findViewById(R.id.latVal);
        longitTextView = findViewById(R.id.lonVal);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        dbHelper = new LocationDatabaseHelper(this);


        if (checkLocationPermissions()) {
            getLastLocation();
        } else {
            requestLocationPermissions();
        }
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
                        Log.d("-------------", "===== " + location.getLatitude());
                        Log.d("-------------", "===== " + location.getLongitude());

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
//        locationRequest.setNumUpdates(1);

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
                latitudeTextView.setText(" " + location.getLatitude());
                longitTextView.setText(" " + location.getLongitude());

                Log.d("-------------", "onLocationResult: " + location.getLatitude());
                Log.d("-------------", "onLocationResult: " + location.getLongitude());
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

        long newRowId = db.insert(LocationDatabaseHelper.TABLE_LOCATIONS, null, values);

        if (newRowId != -1) {
            Toast.makeText(this, "Successfull to save  ", Toast.LENGTH_LONG).show();
            startDatabaseInsertion();
        } else {
            Toast.makeText(this, "Fail to save ", Toast.LENGTH_LONG).show();
        }

        db.close();
    }
    private void startDatabaseInsertion() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getLastLocation();
                handler.postDelayed(this, 60 * 1000); // 60 seconds (1 minute)
//                handler.postDelayed(this, 60 * 1000);
            }
        }, 60 * 1000); // Initial delay of 1 minute
    }

}

