package com.example.dreamland;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng currentLocation = new LatLng(37.344814, 126.737185);
        LatLng hospitalLocation1 = new LatLng(37.345599, 126.736434);
        LatLng hospitalLocation2 = new LatLng(37.345505, 126.737647);
        LatLng hospitalLocation3 = new LatLng(37.344994, 126.738387);
        LatLng hospitalLocation4 = new LatLng(37.345113, 126.738569);
        LatLng hospitalLocation5 = new LatLng(37.344021, 126.737689);
        mMap.addMarker(new MarkerOptions()
                .position(hospitalLocation1)
                .title("박승호 내과의원"));

        mMap.addMarker(new MarkerOptions()
                .position(hospitalLocation2)
                .title("Marker in Sydney"));

        mMap.addMarker(new MarkerOptions()
                .position(hospitalLocation3)
                .title("Marker in Sydney"));

        mMap.addMarker(new MarkerOptions()
                .position(hospitalLocation4)
                .title("Marker in Sydney"));

        mMap.addMarker(new MarkerOptions()
                .position(hospitalLocation5)
                .title("Marker in Sydney"));


        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
    }
}