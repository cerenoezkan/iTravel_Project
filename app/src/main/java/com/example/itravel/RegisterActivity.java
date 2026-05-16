package com.example.itravel;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.itravel.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditTextFields fields;
    private FirebaseAuth mAuth;

  private static class EditTextFields {
        TextInputEditText username;
        TextInputEditText email;
        TextInputEditText password;
        TextInputEditText repassword;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fields = new EditTextFields();
        fields.username = findViewById(R.id.username_add);
        fields.email = findViewById(R.id.email_add);
        fields.password = findViewById(R.id.password_add);
        fields.repassword = findViewById(R.id.repassword);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        });

        MaterialButton btnRegister = findViewById(R.id.register_button);
        btnRegister.setOnClickListener(v -> registerUser());

        mAuth = FirebaseAuth.getInstance();
    }

    private void registerUser() {
        String username = textOf(fields.username);
        String email = textOf(fields.email);
        String password = textOf(fields.password);
        String rePassword = textOf(fields.repassword);

        if (username.isEmpty()) {
            fields.username.setError(getString(R.string.username_required));
            fields.username.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            fields.email.setError(getString(R.string.email_required));
            fields.email.requestFocus();
            return;
        }

        if (SessionManager.ADMIN_EMAIL.equalsIgnoreCase(email)) {
            Toast.makeText(this, R.string.admin_email_reserved, Toast.LENGTH_LONG).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            fields.email.setError(getString(R.string.email_invalid));
            fields.email.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            fields.password.setError(getString(R.string.password_required));
            fields.password.requestFocus();
            return;
        }

        if (password.length() < 6) {
            fields.password.setError(getString(R.string.password_min_length));
            fields.password.requestFocus();
            return;
        }

        if (!password.equals(rePassword)) {
            fields.repassword.setError(getString(R.string.password_mismatch));
            fields.repassword.requestFocus();
            return;
        }

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.please_wait));
        dialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            dialog.dismiss();
                            String msg = task.getException() != null ? task.getException().getMessage() : "";
                            Toast.makeText(RegisterActivity.this,
                                    getString(R.string.register_failed, msg), Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                            dialog.dismiss();
                            Toast.makeText(RegisterActivity.this, R.string.session_expired, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (firebaseUser == null) {
                            dialog.dismiss();
                            Toast.makeText(RegisterActivity.this, R.string.session_expired, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        firebaseUser.updateProfile(
                                new UserProfileChangeRequest.Builder().setDisplayName(username).build());

                        User user = new User(username, email, "", "default");
                        FirebaseDatabase.getInstance(ItravelApp.FIREBASE_RTDB_URL).getReference("Users")
                                .child(firebaseUser.getUid())
                                .setValue(user)
                                .addOnCompleteListener(saveTask -> {
                                    dialog.dismiss();
                                    if (saveTask.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, R.string.register_success, Toast.LENGTH_SHORT).show();
                                        SessionManager.setUserRole(RegisterActivity.this);
                                        SessionManager.launchUserHome(RegisterActivity.this);
                                    } else {
                                        String msg = saveTask.getException() != null
                                                ? saveTask.getException().getMessage() : "";
                                        Toast.makeText(RegisterActivity.this,
                                                getString(R.string.profile_save_failed, msg), Toast.LENGTH_LONG).show();
                                    }
                                });
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
