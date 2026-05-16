package com.example.itravel;

import android.Manifest;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class RouteMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_DEST_LAT = "dest_lat";
    public static final String EXTRA_DEST_LON = "dest_lon";
    public static final String EXTRA_DEST_TITLE = "dest_title";

    private static final LatLng ISTANBUL_CENTER = new LatLng(41.0082, 28.9784);
    private static final float ISTANBUL_ZOOM = 11.5f;
    private static final double DRIVING_SPEED_KMH = 35.0;

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng userLatLng;
    private LatLng destLatLng;
    private String destTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_map);

        destTitle = getIntent().getStringExtra(EXTRA_DEST_TITLE);
        if (destTitle == null) {
            destTitle = getString(R.string.route_destination);
        }

        double lat = getIntent().getDoubleExtra(EXTRA_DEST_LAT, Double.NaN);
        double lon = getIntent().getDoubleExtra(EXTRA_DEST_LON, Double.NaN);
        if (Double.isNaN(lat) || Double.isNaN(lon)) {
            Toast.makeText(this, R.string.route_invalid_destination, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        destLatLng = new LatLng(lat, lon);

        MaterialToolbar toolbar = findViewById(R.id.route_toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setTitle(destTitle);

        TextView titleView = findViewById(R.id.route_dest_title);
        titleView.setText(destTitle);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocationAndMap();
    }

    private void requestLocationAndMap() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        initMapFragment();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(RouteMapActivity.this, R.string.route_location_denied, Toast.LENGTH_LONG).show();
                        initMapFragment();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest request, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void initMapFragment() {
        setLoading(true);
        SupportMapFragment fragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.route_map_container, fragment)
                .commit();
        fragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ISTANBUL_CENTER, ISTANBUL_ZOOM));

        applyMapInsets();

        try {
            googleMap.setMyLocationEnabled(true);
        } catch (SecurityException ignored) {
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    } else {
                        userLatLng = ISTANBUL_CENTER;
                    }
                    drawRoute();
                    setLoading(false);
                })
                .addOnFailureListener(e -> {
                    userLatLng = ISTANBUL_CENTER;
                    drawRoute();
                    setLoading(false);
                });
    }

    private void applyMapInsets() {
        if (googleMap == null) {
            return;
        }
        MaterialCardView panel = findViewById(R.id.route_bottom_panel);
        View toolbar = findViewById(R.id.route_toolbar);
        panel.post(() -> {
            int top = toolbar.getHeight();
            int bottom = panel.getHeight() + dp(20);
            googleMap.setPadding(0, top, 0, bottom);
        });
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void drawRoute() {
        if (googleMap == null || userLatLng == null || destLatLng == null) {
            return;
        }

        googleMap.clear();

        googleMap.addMarker(new MarkerOptions()
                .position(userLatLng)
                .title(getString(R.string.route_you))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        googleMap.addMarker(new MarkerOptions()
                .position(destLatLng)
                .title(destTitle)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        googleMap.addPolyline(new PolylineOptions()
                .add(userLatLng, destLatLng)
                .width(12f)
                .color(Color.parseColor("#14502E"))
                .geodesic(true));

        float[] results = new float[1];
        Location.distanceBetween(
                userLatLng.latitude, userLatLng.longitude,
                destLatLng.latitude, destLatLng.longitude,
                results);
        double distanceKm = results[0] / 1000.0;
        int minutes = (int) Math.max(1, Math.round((distanceKm / DRIVING_SPEED_KMH) * 60.0));

        TextView distanceTv = findViewById(R.id.route_distance);
        TextView durationTv = findViewById(R.id.route_duration);
        distanceTv.setText(getString(R.string.route_distance, distanceKm));
        durationTv.setText(getString(R.string.route_duration, minutes));

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(userLatLng)
                .include(destLatLng)
                .build();
        try {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, dp(48)));
        } catch (Exception e) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ISTANBUL_CENTER, ISTANBUL_ZOOM));
        }
    }

    private void setLoading(boolean loading) {
        ProgressBar bar = findViewById(R.id.route_progress);
        if (bar != null) {
            bar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }
}
