package com.example.itravel;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.itravel.Adapter.CategoryHomeAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity implements CategoryHomeAdapter.Listener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, R.string.session_expired, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
            return;
        }

        if (!SessionManager.isUserRole(this)) {
            if (SessionManager.isAdminSession(this)) {
                SessionManager.launchAdminPanel(this);
                return;
            } else {
                Toast.makeText(this, R.string.session_expired, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
            finish();
            return;
        }

        MaterialButton logout = findViewById(R.id.logout);
        logout.setOnClickListener(v -> SessionManager.signOutAndReturnToRoleSelection(HomeActivity.this));

        RecyclerView rvCategories = findViewById(R.id.rv_categories);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setHasFixedSize(true);
        rvCategories.setItemAnimator(null);
        rvCategories.setAdapter(new CategoryHomeAdapter(this));

        MaterialButton viewProfile = findViewById(R.id.btn_view_profile);
        viewProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    @Override
    public void onCategoryClick(@NonNull String categoryKey) {
        Intent i = new Intent(this, CategoryPlacesActivity.class);
        i.putExtra(CategoryPlacesActivity.EXTRA_CATEGORY, categoryKey);
        startActivity(i);
    }

    @Override
    public void onMapClick(@NonNull String categoryKey) {
        MapActivity.launch(this, categoryKey);
    }
}
