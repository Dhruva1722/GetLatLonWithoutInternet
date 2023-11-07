package com.example.getlatlonwithoutinternet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class LocationDataAdapter extends ArrayAdapter<LocationData> {
private Context context;
private List<LocationData> locationDataList;

public LocationDataAdapter(Context context, List<LocationData> locationDataList){
        super(context, 0, locationDataList);
        this.context = context;
        this.locationDataList = locationDataList;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                        convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
                }

                // Get the LocationData object for this position
                LocationData locationData = locationDataList.get(position);

                // Find and update the views within your item layout
                TextView latitudeTextView = convertView.findViewById(R.id.latitudeTextView);
                TextView longitudeTextView = convertView.findViewById(R.id.longitudeTextView);
                TextView timestampTextView = convertView.findViewById(R.id.timestampTextView);

                if (locationData != null) {
                        latitudeTextView.setText("Latitude: " + locationData.getLatitude());
                        longitudeTextView.setText("Longitude: " + locationData.getLongitude());
                        timestampTextView.setText("Timestamp: " + locationData.getTimestamp());
                }

                return convertView;
        }
}