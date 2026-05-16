package com.example.itravel.fragment;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.itravel.DetailActivity;
import com.example.itravel.ItravelApp;
import com.example.itravel.Model.Place;
import com.example.itravel.Model.PlaceCategory;
import com.example.itravel.R;
import com.example.itravel.util.MapPlaceUtils;
import com.example.itravel.util.PlaceFilter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
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

public class MapBrowseFragment extends Fragment implements OnMapReadyCallback {

    private static final LatLng ISTANBUL_CENTER = new LatLng(41.0082, 28.9784);
    private static final float DEFAULT_ZOOM = 13.5f;
    private static final int MAX_MARKERS = 45;

    private GoogleMap googleMap;
    private boolean locationPermissionGranted;
    private LatLng userLocation;

    private final Map<String, Place> placeById = new HashMap<>();
    private List<Place> allPlaces = new ArrayList<>();

    private DatabaseReference placesRef;
    private ValueEventListener placesListener;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean markersRefreshing;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map_browse, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        SupportMapFragment mapFragment = new SupportMapFragment();
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.map_fragment_container, mapFragment)
                .commit();
        mapFragment.getMapAsync(this);

        requestLocationAndLoadPlaces();
    }

    private void requestLocationAndLoadPlaces() {
        Dexter.withContext(requireContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        locationPermissionGranted = true;
                        fetchUserLocation();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        locationPermissionGranted = false;
                        attachPlacesListener();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest request,
                                                                     PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void fetchUserLocation() {
        if (!locationPermissionGranted) {
            attachPlacesListener();
            return;
        }
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            if (googleMap != null) {
                                googleMap.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(userLocation, DEFAULT_ZOOM));
                            }
                        }
                        attachPlacesListener();
                    })
                    .addOnFailureListener(e -> attachPlacesListener());
        } catch (SecurityException e) {
            attachPlacesListener();
        }
    }

    private void attachPlacesListener() {
        placesRef = FirebaseDatabase.getInstance(ItravelApp.FIREBASE_RTDB_URL)
                .getReference(ItravelApp.RTDB_NODE_PLACES);

        if (placesListener != null) {
            placesRef.removeEventListener(placesListener);
        }

        placesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Place> loaded = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Place place = Place.fromSnapshot(child);
                    if (place != null && place.getId() != null && !place.getId().isEmpty()) {
                        loaded.add(place);
                    }
                }
                allPlaces = PlaceFilter.forIstanbul(loaded);
                refreshMarkers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        placesRef.addValueEventListener(placesListener);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        if (locationPermissionGranted) {
            try {
                map.setMyLocationEnabled(true);
            } catch (SecurityException ignored) {
            }
        }

        LatLng initial = userLocation != null ? userLocation : ISTANBUL_CENTER;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(initial, DEFAULT_ZOOM));

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

        map.setOnCameraIdleListener(() -> {
            if (googleMap != null && !markersRefreshing) {
                refreshMarkers();
            }
        });

        refreshMarkers();
    }

    private void refreshMarkers() {
        if (googleMap == null || allPlaces.isEmpty()) {
            return;
        }

        markersRefreshing = true;
        googleMap.clear();
        placeById.clear();

        LatLng center = googleMap.getCameraPosition().target;

        float zoom = googleMap.getCameraPosition().zoom;
        double radius = MapPlaceUtils.radiusMetersForZoom(zoom);
        List<Place> nearby = MapPlaceUtils.nearbyPlaces(allPlaces, center, radius, MAX_MARKERS);

        if (nearby.isEmpty()) {
            nearby = MapPlaceUtils.nearbyPlaces(allPlaces, center, radius * 2.5, MAX_MARKERS);
        }

        for (Place place : nearby) {
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
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .title(markerTitle)
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(hue)));
            if (marker != null) {
                marker.setTag(placeId);
            }
        }
        markersRefreshing = false;
    }

    private void showPlaceSheet(@NonNull Place place) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_map_place, null, false);
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
                DetailActivity.launch(requireContext(), place.getId());
                requireActivity().overridePendingTransition(R.anim.nav_fade_in, R.anim.nav_fade_out);
            }
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (placesRef != null && placesListener != null) {
            placesRef.removeEventListener(placesListener);
        }
        googleMap = null;
    }

    private static String nz(@Nullable String s) {
        return s != null ? s : "";
    }
}
