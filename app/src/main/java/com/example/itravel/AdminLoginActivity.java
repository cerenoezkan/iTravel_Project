package com.example.itravel;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdminLoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextInputEditText emailField;
    private TextInputEditText passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        mAuth = FirebaseAuth.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.admin_login_toolbar);
        toolbar.setNavigationOnClickListener(v -> navigateBackToRoleSelection());

        emailField = findViewById(R.id.admin_email);
        passwordField = findViewById(R.id.admin_password);
        MaterialButton signIn = findViewById(R.id.admin_sign_in);
        signIn.setOnClickListener(v -> attemptAdminLogin());
    }

    @Override
    public void onBackPressed() {
        navigateBackToRoleSelection();
    }

    private void navigateBackToRoleSelection() {
        startActivity(new Intent(this, RoleSelectionActivity.class));
        finish();
    }

    private void attemptAdminLogin() {
        String email = emailField != null && emailField.getText() != null
                ? emailField.getText().toString().trim() : "";
        String password = passwordField != null && passwordField.getText() != null
                ? passwordField.getText().toString() : "";

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, R.string.email_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, R.string.email_invalid, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.password_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!SessionManager.ADMIN_EMAIL.equalsIgnoreCase(email.trim())) {
            Toast.makeText(this, R.string.admin_login_wrong_email, Toast.LENGTH_LONG).show();
            return;
        }

        signInLoading(true);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                signInLoading(false);
                if (!task.isSuccessful()) {
                    String msg = task.getException() != null ? task.getException().getMessage() : "";
                    Toast.makeText(AdminLoginActivity.this,
                            getString(R.string.admin_login_failed_prefix) + msg,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                FirebaseUser user = mAuth.getCurrentUser();
                if (user == null || user.getEmail() == null
                        || !SessionManager.ADMIN_EMAIL.equalsIgnoreCase(user.getEmail().trim())) {
                    mAuth.signOut();
                    Toast.makeText(AdminLoginActivity.this, R.string.admin_login_wrong_email, Toast.LENGTH_LONG).show();
                    return;
                }
                SessionManager.setAdminRole(AdminLoginActivity.this);
                startActivity(new Intent(getApplicationContext(), AdminPanelActivity.class));
                finish();
            }
        });
    }

    private void signInLoading(boolean loading) {
        MaterialButton btn = findViewById(R.id.admin_sign_in);
        btn.setEnabled(!loading);
    }
}
