package com.example.itravel;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.itravel.Adapter.AdminPlaceAdapter;
import com.example.itravel.Model.Place;
import com.example.itravel.Model.PlaceCategory;
import com.example.itravel.util.PlaceFilter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminPanelActivity extends AppCompatActivity implements AdminPlaceAdapter.Listener {

    private DatabaseReference placesRef;
    private ValueEventListener placesListener;
    private AdminPlaceAdapter adapter;
    private List<Place> allPlaces = new ArrayList<>();

    @Nullable
    private String selectedCategoryFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isAdminSession(this)) {
            Toast.makeText(this, R.string.admin_area_only, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_admin_panel);

        MaterialToolbar toolbar = findViewById(R.id.admin_panel_toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_admin_logout) {
                logoutAdmin();
                return true;
            }
            return false;
        });

        setupCategoryChips();

        RecyclerView rv = findViewById(R.id.admin_places_rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminPlaceAdapter(this);
        rv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.admin_fab_add);
        fab.setOnClickListener(v -> {
            Intent i = new Intent(this, AdminPlaceEditActivity.class);
            if (selectedCategoryFilter != null) {
                i.putExtra(AdminPlaceEditActivity.EXTRA_CATEGORY, selectedCategoryFilter);
            }
            startActivity(i);
        });

        placesRef = FirebaseDatabase.getInstance(ItravelApp.FIREBASE_RTDB_URL)
                .getReference(ItravelApp.RTDB_NODE_PLACES);

        placesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allPlaces = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Place p = Place.fromSnapshot(child);
                    if (p != null && p.getId() != null && !p.getId().isEmpty()) {
                        allPlaces.add(p);
                    }
                }
                applyFilter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminPanelActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        placesRef.addValueEventListener(placesListener);
    }

    private void setupCategoryChips() {
        ChipGroup group = findViewById(R.id.admin_category_chips);

        Chip allChip = new Chip(this);
        allChip.setText(R.string.admin_filter_all);
        allChip.setCheckable(true);
        allChip.setChecked(true);
        allChip.setTag("");
        group.addView(allChip);

        for (String key : PlaceCategory.ALL_KEYS) {
            Chip chip = new Chip(this);
            chip.setText(getString(PlaceCategory.labelRes(key)));
            chip.setCheckable(true);
            chip.setTag(key);
            group.addView(chip);
        }

        group.setOnCheckedStateChangeListener((chipGroup, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedCategoryFilter = null;
            } else {
                Chip checked = chipGroup.findViewById(checkedIds.get(0));
                if (checked != null) {
                    Object tag = checked.getTag();
                    if (tag instanceof String && !((String) tag).isEmpty()) {
                        selectedCategoryFilter = (String) tag;
                    } else {
                        selectedCategoryFilter = null;
                    }
                }
            }
            applyFilter();
        });
    }

    private void applyFilter() {
        if (selectedCategoryFilter == null) {
            adapter.setPlaces(PlaceFilter.forIstanbul(allPlaces));
        } else {
            adapter.setPlaces(PlaceFilter.byCategory(allPlaces, selectedCategoryFilter));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (placesRef != null && placesListener != null) {
            placesRef.removeEventListener(placesListener);
        }
    }

    private void logoutAdmin() {
        FirebaseAuth.getInstance().signOut();
        SessionManager.clear(this);
        startActivity(new Intent(this, RoleSelectionActivity.class));
        finish();
    }

    @Override
    public void onEdit(@NonNull Place place) {
        Intent i = new Intent(this, AdminPlaceEditActivity.class);
        i.putExtra(AdminPlaceEditActivity.EXTRA_PLACE_ID, place.getId());
        startActivity(i);
    }

    @Override
    public void onDelete(@NonNull Place place) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.admin_delete_confirm_title)
                .setMessage(R.string.admin_delete_confirm_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.admin_delete, (dialog, which) -> {
                    placesRef.child(place.getId()).removeValue()
                            .addOnSuccessListener(unused -> Toast.makeText(AdminPanelActivity.this, R.string.admin_deleted, Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(AdminPanelActivity.this, e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .show();
    }

}
