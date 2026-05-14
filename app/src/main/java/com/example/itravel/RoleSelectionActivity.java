package com.example.itravel;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class RoleSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.role_gradient_top));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.role_gradient_bottom));
        }

        setContentView(R.layout.activity_role_selection);

        WindowInsetsControllerCompat insets = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insets.setAppearanceLightStatusBars(false);
        insets.setAppearanceLightNavigationBars(false);

        MaterialButton userBtn = findViewById(R.id.btn_user_login);
        MaterialButton adminBtn = findViewById(R.id.btn_admin_login);

        userBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            SessionManager.clear(RoleSelectionActivity.this);
            startActivity(new Intent(this, MainActivity.class));
        });

        adminBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            SessionManager.clear(RoleSelectionActivity.this);
            startActivity(new Intent(this, AdminLoginActivity.class));
        });
    }
}
