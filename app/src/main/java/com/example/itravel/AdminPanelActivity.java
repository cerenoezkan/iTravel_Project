package com.example.itravel;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.itravel.Adapter.AdminPlaceAdapter;
import com.example.itravel.Model.Place;
import com.google.android.material.appbar.MaterialToolbar;
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

        RecyclerView rv = findViewById(R.id.admin_places_rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminPlaceAdapter(this);
        rv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.admin_fab_add);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AdminPlaceEditActivity.class)));

        placesRef = FirebaseDatabase.getInstance(ItravelApp.FIREBASE_RTDB_URL)
                .getReference(ItravelApp.RTDB_NODE_PLACES);

        placesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Place> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Place p = Place.fromSnapshot(child);
                    if (p != null && p.getId() != null && !p.getId().isEmpty()) {
                        list.add(p);
                    }
                }
                adapter.setPlaces(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminPanelActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
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

    @Override
    public void onBackPressed() {
        logoutAdmin();
    }
}
