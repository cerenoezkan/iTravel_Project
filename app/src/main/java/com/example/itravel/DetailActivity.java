package com.example.itravel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.itravel.Model.Place;
import com.example.itravel.Model.PlaceCategory;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_PLACE_ID = "placeId";

    private DatabaseReference placeRef;
    private ValueEventListener placeListener;
    private Place currentPlace;

    public static void launch(@NonNull Context context, @NonNull String placeId) {
        if (TextUtils.isEmpty(placeId)) {
            return;
        }
        Intent i = new Intent(context, DetailActivity.class);
        i.putExtra(EXTRA_PLACE_ID, placeId);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        String placeId = getIntent().getStringExtra(EXTRA_PLACE_ID);
        if (TextUtils.isEmpty(placeId)) {
            Toast.makeText(this, R.string.place_detail_missing_id, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.detail_toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        MaterialButton routeBtn = findViewById(R.id.btn_create_route);
        routeBtn.setOnClickListener(v -> openRouteMap());

        placeRef = FirebaseDatabase.getInstance(ItravelApp.FIREBASE_RTDB_URL)
                .getReference(ItravelApp.RTDB_NODE_PLACES)
                .child(placeId);

        placeListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(DetailActivity.this, R.string.place_detail_not_found, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                Place place = Place.fromSnapshot(snapshot);
                if (place == null) {
                    Toast.makeText(DetailActivity.this, R.string.place_detail_not_found, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                currentPlace = place;
                bindPlace(place);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        placeRef.addValueEventListener(placeListener);
    }

    private void openRouteMap() {
        if (currentPlace == null) {
            return;
        }
        try {
            String latStr = currentPlace.getLatitude();
            String lonStr = currentPlace.getLongitude();
            if (latStr == null || lonStr == null || latStr.trim().isEmpty() || lonStr.trim().isEmpty()) {
                Toast.makeText(this, R.string.route_invalid_destination, Toast.LENGTH_SHORT).show();
                return;
            }
            double lat = Double.parseDouble(latStr.trim());
            double lon = Double.parseDouble(lonStr.trim());

            Intent i = new Intent(this, RouteMapActivity.class);
            i.putExtra(RouteMapActivity.EXTRA_DEST_LAT, lat);
            i.putExtra(RouteMapActivity.EXTRA_DEST_LON, lon);
            String title = currentPlace.getTitle();
            i.putExtra(RouteMapActivity.EXTRA_DEST_TITLE,
                    title != null && !title.isEmpty() ? title : getString(R.string.place_detail_untitled));
            startActivity(i);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.route_invalid_destination, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (placeRef != null && placeListener != null) {
            placeRef.removeEventListener(placeListener);
        }
    }

    private void bindPlace(@NonNull Place place) {
        TextView title = findViewById(R.id.detail_title);
        TextView category = findViewById(R.id.detail_category);
        TextView rating = findViewById(R.id.detail_rating);
        TextView coords = findViewById(R.id.detail_coords);
        TextView about = findViewById(R.id.detail_about);
        ImageView hero = findViewById(R.id.detail_hero_image);

        String t = nz(place.getTitle());
        title.setText(t.isEmpty() ? getString(R.string.place_detail_untitled) : t);

        category.setText(getString(PlaceCategory.labelRes(place.getCategory())));
        if (place.getRating() > 0) {
            rating.setText(getString(R.string.place_rating_format, place.getRating()));
        } else {
            rating.setText(R.string.place_rating_none);
        }

        String lat = nz(place.getLatitude());
        String lon = nz(place.getLongitude());
        String latLine = getString(R.string.place_detail_lat_label) + "  " + (lat.isEmpty() ? "—" : lat);
        String lonLine = getString(R.string.place_detail_lon_label) + "  " + (lon.isEmpty() ? "—" : lon);
        coords.setText(latLine + "\n" + lonLine);

        String desc = place.getDescription();
        if (desc == null || desc.trim().isEmpty()) {
            about.setText(R.string.place_detail_no_description);
            about.setTextColor(ContextCompat.getColor(this, R.color.gray));
        } else {
            about.setText(desc.trim());
            about.setTextColor(ContextCompat.getColor(this, R.color.role_text_primary));
        }

        String img = nz(place.getImageUrl());
        if (!img.isEmpty()) {
            Glide.with(this).load(img).centerCrop().into(hero);
        } else {
            hero.setImageResource(R.drawable.noimage);
        }
    }

    private static String nz(@Nullable String s) {
        return s != null ? s : "";
    }
}
