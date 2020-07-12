package com.aby.maps_abhishek_c0769778;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final int REQUEST_CODE = 1;
    private Marker homeMarker;
    Polygon shape;
    private static final int POLYGON_SIDES = 4;
    List<Marker> markers = new ArrayList();
    private Marker mMarker;
    // location with location manager and listener
    LocationManager locationManager;
    LocationListener locationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                setHomeMarker(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (!hasLocationPermission())
            requestLocationPermission();
        else
            startUpdateLocation();


        // apply long press gesture
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                setMarker(latLng);
            }
            private void setMarker(LatLng latLng) {
                MarkerOptions option = getMarkerOption(latLng);
                Marker marker = mMap.addMarker(option);
                // check if there are already the same number of markers, we clear the map.
                if (markers.size() == POLYGON_SIDES)
                    clearMap();
                markers.add(mMap.addMarker(option));
                if (markers.size() == POLYGON_SIDES)
                    drawShape();
            }

            private void drawShape() {
                PolygonOptions options = new PolygonOptions()
                        .fillColor(Color.GREEN)
                        .strokeColor(Color.RED)
                        .strokeWidth(3);

                for (int i=0; i<POLYGON_SIDES; i++) {
                    options.add(markers.get(i).getPosition());
                }
                shape = mMap.addPolygon(options);
            }

            private void clearMap() {
                for (Marker marker: markers)
                marker.remove();
                markers.clear();
                shape.remove();
                shape = null;
            }
        });
    }

    private MarkerOptions getMarkerOption(LatLng latLng) {
        String [] str = getTitle(latLng);
        MarkerOptions option = new MarkerOptions().position(latLng);
        option.title(str[0]).snippet(str[1]);
        return option;
    }

    private String[] getTitle(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this);
        String [] result = null;
        try {
            Address address = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1).get(0);
            result = getAddress(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String[] getAddress(Address address) {
        StringBuilder title = new StringBuilder("");
        StringBuilder snippet = new StringBuilder("");
        if(address.getSubThoroughfare() != null)
        {
            title.append(address.getSubThoroughfare());
        }
        if(address.getThoroughfare() != null)
        {
            title.append(", " + address.getThoroughfare());
        }
        if(address.getPostalCode() != null)
        {
            title.append(", " + address.getPostalCode());
        }
        if(address.getLocality() != null)
        {
            snippet.append(address.getLocality());
        }
        if(address.getAdminArea() != null)
        {
            snippet.append(", " + address.getAdminArea());
        }
        return new String[]{title.toString(),snippet.toString()};
    }

    private void startUpdateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void setHomeMarker(Location location) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions options = new MarkerOptions().position(userLocation)
                .title("You are here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .snippet("Your Location");
        homeMarker = mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (REQUEST_CODE == requestCode) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
            }
        }
    }
}