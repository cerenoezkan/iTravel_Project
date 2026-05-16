package com.example.itravel;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class RoleSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_role_selection);

        if (SessionManager.isAdminSession(this)
                && FirebaseAuth.getInstance().getCurrentUser() != null) {
            SessionManager.launchAdminPanel(this);
            return;
        }
        if (SessionManager.isUserRole(this)
                && FirebaseAuth.getInstance().getCurrentUser() != null
                && !SessionManager.isCurrentUserAdminEmail()) {
            SessionManager.launchUserHome(this);
            return;
        }

        WindowInsetsControllerCompat insetsController =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(true);
        insetsController.setAppearanceLightNavigationBars(true);

        View root = findViewById(R.id.role_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            View scroll = findViewById(R.id.role_scroll);
            scroll.setPadding(0, systemBars.top, 0, systemBars.bottom);
            return windowInsets;
        });

        MaterialCardView userBtn = findViewById(R.id.btn_user_login);
        MaterialCardView adminBtn = findViewById(R.id.btn_admin_login);

        userBtn.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));

        adminBtn.setOnClickListener(v ->
                startActivity(new Intent(this, AdminLoginActivity.class)));
    }
}
