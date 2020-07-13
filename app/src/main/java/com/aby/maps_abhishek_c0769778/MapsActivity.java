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
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnPolygonClickListener, GoogleMap.OnPolylineClickListener{

    private GoogleMap mMap;

    private static final int REQUEST_CODE = 1;
    private Marker homeMarker;
    Polygon shape;
    private static final int POLYGON_SIDES = 4;
    List<Marker> markersList = new ArrayList();
    List<Polyline> polylinesList = new ArrayList<>();
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

        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);

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

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                setMarker(latLng);
            }
            private void setMarker(LatLng latLng) {
//                if (markersList.size() < POLYGON_SIDES) {
                    MarkerOptions options = getMarkerOption(latLng);
                    Marker marker = mMap.addMarker(options);
                    drawPolyline(marker);
                    mMarker = marker;
                    markersList.add(marker);
//                }
                if (markersList.size() == POLYGON_SIDES)
                    clearMap();
                //markersList.add(mMap.addMarker(options));
                if (markersList.size() == POLYGON_SIDES)
                    drawShape();
            }

            private void addPolyLines(List<LatLng> latLngList) {
                for (int i = 0; i < 4; i++) {
                    if (i == 0) {
                        drawPolyline(latLngList.get(3), latLngList.get(i));
                    } else {
                        drawPolyline(latLngList.get(i), latLngList.get(i - 1));
                    }
                }
            }

            private void drawPolyline(LatLng latLng, LatLng latLng1) {
                PolylineOptions polylineOptions = new PolylineOptions()
                        .color(Color.RED)
                        .width(20)
                        .add(latLng, latLng1);
                Polyline polyline = mMap.addPolyline(polylineOptions);
                polyline.setClickable(true);
                polylinesList.add(polyline);
            }

            private void drawPolyline(Marker marker) {
                if (mMarker != null) {
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .color(Color.RED)
                            .width(20)
                            .add(marker.getPosition(), mMarker.getPosition());
                    Polyline polyline = mMap.addPolyline(polylineOptions);
                    polyline.setClickable(true);
                    polylinesList.add(polyline);
                }
            }

            private void drawShape() {
                PolygonOptions options = new PolygonOptions()
                        .fillColor(Color.argb(75, 0, 255, 0))
                        .strokeColor(Color.RED);

                for (int i=0; i<POLYGON_SIDES; i++) {
                    options.add(markersList.get(i).getPosition());
                }
                shape = mMap.addPolygon(options);
                shape.setClickable(true);
            }

            private void clearMap() {
                for (Marker marker: markersList)
                marker.remove();

                for (Polyline polyline : polylinesList) {
                    polyline.remove();
                }
                polylinesList.clear();

                markersList.clear();
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

    @Override
    public void onPolygonClick(Polygon polygon) {
        Toast.makeText(this, "Polygon clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        Toast.makeText(this, "Polyline clicked", Toast.LENGTH_SHORT).show();
    }
}