package com.example.itravel;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
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

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailLogin;
    private TextInputEditText passwordLogin;
    private TextView forgetPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailLogin = findViewById(R.id.email_login);
        passwordLogin = findViewById(R.id.password_login);
        forgetPassword = findViewById(R.id.forget_password);
        MaterialButton loginBtn = findViewById(R.id.login_button);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        forgetPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgetPasswordActivity.class)));

        loginBtn.setOnClickListener(v -> userLogin());

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null
                && SessionManager.isUserRole(this)
                && !SessionManager.isCurrentUserAdminEmail()) {
            SessionManager.launchUserHome(this);
        }
    }

    private void userLogin() {
        String email = textOf(emailLogin);
        String password = textOf(passwordLogin);

        if (email.isEmpty()) {
            emailLogin.setError(getString(R.string.email_required));
            emailLogin.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordLogin.setError(getString(R.string.password_required));
            passwordLogin.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLogin.setError(getString(R.string.email_invalid));
            emailLogin.requestFocus();
            return;
        }

        if (SessionManager.ADMIN_EMAIL.equalsIgnoreCase(email)) {
            Toast.makeText(this, R.string.use_admin_entry_for_admin, Toast.LENGTH_LONG).show();
            return;
        }

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.please_wait));
        dialog.show();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                dialog.dismiss();
                if (task.isSuccessful()) {
                    SessionManager.setUserRole(LoginActivity.this);
                    SessionManager.launchUserHome(LoginActivity.this);
                } else {
                    String errorMessage = task.getException() != null
                            ? task.getException().getMessage()
                            : "";
                    Toast.makeText(LoginActivity.this,
                            getString(R.string.login_failed, errorMessage), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private static String textOf(TextInputEditText et) {
        if (et == null || et.getText() == null) {
            return "";
        }
        return et.getText().toString().trim();
    }
}
