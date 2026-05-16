package com.example.itravel;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordActivity extends AppCompatActivity {

    private TextInputEditText emailEditText;
    private MaterialButton resetPasswordButton;
    private ProgressBar progressBar;
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        fAuth = FirebaseAuth.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.forget_toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        emailEditText = findViewById(R.id.email_reset);
        resetPasswordButton = findViewById(R.id.reset);
        progressBar = findViewById(R.id.progress_bar);

        resetPasswordButton.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = emailEditText != null && emailEditText.getText() != null
                ? emailEditText.getText().toString().trim() : "";

        if (email.isEmpty()) {
            Toast.makeText(this, R.string.email_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, R.string.email_invalid, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        fAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toast.makeText(ForgetPasswordActivity.this, R.string.reset_link_sent, Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(ForgetPasswordActivity.this,
                            getString(R.string.reset_link_failed, e.getMessage()),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void setLoading(boolean loading) {
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        resetPasswordButton.setEnabled(!loading);
    }
}
