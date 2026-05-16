package com.example.itravel;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.itravel.Model.Place;
import com.example.itravel.Model.PlaceCategory;
import com.example.itravel.util.PlaceFilter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.itravel.util.MapPlaceUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_CATEGORY = "category";

    private static final LatLng ISTANBUL_CENTER = new LatLng(41.0082, 28.9784);
    private static final float ISTANBUL_ZOOM = 11.5f;

    private boolean isPermissionGranted;
    private GoogleMap googleMap;
    private String filterCategory;
    private final Map<String, Place> placeById = new HashMap<>();

    private DatabaseReference placesRef;
    private ValueEventListener placesListener;

    public static void launch(@NonNull Context context, @Nullable String categoryKey) {
        Intent i = new Intent(context, MapActivity.class);
        if (!TextUtils.isEmpty(categoryKey)) {
            i.putExtra(EXTRA_CATEGORY, categoryKey);
        }
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        filterCategory = getIntent().getStringExtra(EXTRA_CATEGORY);

        MaterialToolbar toolbar = findViewById(R.id.map_toolbar);
        if (PlaceCategory.isValid(filterCategory)) {
            toolbar.setTitle(getString(PlaceCategory.labelRes(filterCategory)));
        } else {
            toolbar.setTitle(R.string.map_title);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        placesRef = FirebaseDatabase.getInstance(ItravelApp.FIREBASE_RTDB_URL)
                .getReference(ItravelApp.RTDB_NODE_PLACES);

        checkPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (placesRef != null && placesListener != null) {
            placesRef.removeEventListener(placesListener);
        }
    }

    private boolean checkGooglePlayGranted() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int result = api.isGooglePlayServicesAvailable(this);
        if (result == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(result)) {
            Dialog dialog = api.getErrorDialog(this, result, 201, (DialogInterface.OnCancelListener) dialog1 ->
                    Toast.makeText(MapActivity.this, "Google Play hizmeti kullanılamıyor", Toast.LENGTH_SHORT).show());
            if (dialog != null) {
                dialog.show();
            }
        }
        return false;
    }

    private void checkPermission() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        isPermissionGranted = true;
                        initMap();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        isPermissionGranted = false;
                        initMap();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest request, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void initMap() {
        if (!checkGooglePlayGranted()) {
            Toast.makeText(this, "Google Play hizmeti kullanılamıyor", Toast.LENGTH_SHORT).show();
            return;
        }
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        }
        fragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setZoomGesturesEnabled(true);
        map.getUiSettings().setScrollGesturesEnabled(true);
        map.getUiSettings().setRotateGesturesEnabled(false);
        if (isPermissionGranted) {
            try {
                map.setMyLocationEnabled(true);
            } catch (SecurityException ignored) {
            }
        }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(ISTANBUL_CENTER, ISTANBUL_ZOOM));

        map.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof String) {
                Place place = placeById.get((String) tag);
                if (place != null) {
                    showPlaceSheet(place);
                }
            }
            return true;
        });

        if (placesListener != null) {
            placesRef.removeEventListener(placesListener);
        }

        placesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (googleMap == null) {
                    return;
                }
                googleMap.clear();
                placeById.clear();

                List<Place> all = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Place place = Place.fromSnapshot(child);
                    if (place != null && place.getId() != null && !place.getId().isEmpty()) {
                        all.add(place);
                    }
                }

                List<Place> filtered = PlaceFilter.byCategory(all, filterCategory);
                LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                boolean hasMarker = false;

                for (Place place : filtered) {
                    LatLng latLng = MapPlaceUtils.latLngFromPlace(place);
                    if (latLng == null) {
                        continue;
                    }
                    String placeId = place.getId();
                    placeById.put(placeId, place);

                    String markerTitle = nz(place.getTitle());
                    if (markerTitle.isEmpty()) {
                        markerTitle = getString(R.string.place_detail_untitled);
                    }

                    float hue = PlaceCategory.markerHue(place.getCategory());
                    MarkerOptions options = new MarkerOptions()
                            .title(markerTitle)
                            .position(latLng)
                            .icon(BitmapDescriptorFactory.defaultMarker(hue));
                    Marker marker = googleMap.addMarker(options);
                    if (marker != null) {
                        marker.setTag(placeId);
                        boundsBuilder.include(latLng);
                        hasMarker = true;
                    }
                }

                if (hasMarker && filtered.size() > 1) {
                    try {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120));
                    } catch (Exception e) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ISTANBUL_CENTER, ISTANBUL_ZOOM));
                    }
                } else {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ISTANBUL_CENTER, ISTANBUL_ZOOM));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MapActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        placesRef.addValueEventListener(placesListener);
    }

    private void showPlaceSheet(@NonNull Place place) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View content = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_map_place, null, false);
        dialog.setContentView(content);

        ImageView image = content.findViewById(R.id.sheet_place_image);
        TextView title = content.findViewById(R.id.sheet_place_title);
        Chip category = content.findViewById(R.id.sheet_place_category);
        TextView rating = content.findViewById(R.id.sheet_place_rating);
        MaterialButton details = content.findViewById(R.id.sheet_btn_details);

        String t = nz(place.getTitle());
        title.setText(t.isEmpty() ? getString(R.string.place_detail_untitled) : t);
        category.setText(getString(PlaceCategory.labelRes(place.getCategory())));

        if (place.getRating() > 0) {
            rating.setText(getString(R.string.place_rating_format, place.getRating()));
        } else {
            rating.setText(R.string.place_rating_none);
        }

        String img = nz(place.getImageUrl());
        if (!img.isEmpty()) {
            Glide.with(this).load(img).centerCrop().into(image);
        } else {
            image.setImageResource(R.drawable.noimage);
        }

        details.setOnClickListener(v -> {
            dialog.dismiss();
            if (place.getId() != null) {
                DetailActivity.launch(this, place.getId());
            }
        });

        dialog.show();
    }

    private static String nz(@Nullable String s) {
        return s != null ? s : "";
    }
}
