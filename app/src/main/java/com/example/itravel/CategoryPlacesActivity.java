package com.example.itravel;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.itravel.Adapter.PlaceAdapter;
import com.example.itravel.Model.Place;
import com.example.itravel.Model.PlaceCategory;
import com.example.itravel.util.PlaceFilter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CategoryPlacesActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY = "category";

    private String categoryKey;
    private DatabaseReference placesRef;
    private ValueEventListener placesListener;
    private PlaceAdapter adapter;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_places);

        categoryKey = getIntent().getStringExtra(EXTRA_CATEGORY);
        if (!PlaceCategory.isValid(categoryKey)) {
            finish();
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.category_toolbar);
        toolbar.setTitle(PlaceCategory.labelRes(categoryKey));
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_map) {
                MapActivity.launch(this, categoryKey);
                overridePendingTransition(R.anim.nav_fade_in, R.anim.nav_fade_out);
                return true;
            }
            return false;
        });

        RecyclerView rv = findViewById(R.id.rv_places);
        emptyView = findViewById(R.id.empty_places);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PlaceAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        placesRef = FirebaseDatabase.getInstance(ItravelApp.FIREBASE_RTDB_URL)
                .getReference(ItravelApp.RTDB_NODE_PLACES);

        placesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Place> all = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Place p = Place.fromSnapshot(child);
                    if (p != null && p.getId() != null && !p.getId().isEmpty()) {
                        all.add(p);
                    }
                }
                List<Place> filtered = PlaceFilter.byCategory(all, categoryKey);
                adapter = new PlaceAdapter(filtered);
                rv.setAdapter(adapter);
                emptyView.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                emptyView.setVisibility(View.VISIBLE);
            }
        };
        placesRef.addValueEventListener(placesListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (placesRef != null && placesListener != null) {
            placesRef.removeEventListener(placesListener);
        }
    }
}
