package com.example.itravel;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.itravel.Model.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        MaterialToolbar toolbar = findViewById(R.id.profile_toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                SessionManager.signOutAndReturnToRoleSelection(ProfileActivity.this);
                return true;
            }
            return false;
        });

        MaterialButton back = findViewById(R.id.btn_back);
        back.setOnClickListener(v -> finish());

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, R.string.session_expired, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        reference = FirebaseDatabase.getInstance(ItravelApp.FIREBASE_RTDB_URL).getReference("Users");
        userID = user.getUid();

        final TextView usernameTextView = findViewById(R.id.username_profile);
        final TextView emailTextView = findViewById(R.id.email_profile);

        String authEmail = user.getEmail();
        if (!TextUtils.isEmpty(authEmail)) {
            emailTextView.setText(authEmail);
        }

        String displayName = user.getDisplayName();
        if (!TextUtils.isEmpty(displayName)) {
            usernameTextView.setText(displayName);
        } else if (!TextUtils.isEmpty(authEmail) && authEmail.contains("@")) {
            usernameTextView.setText(authEmail.substring(0, authEmail.indexOf('@')));
        }

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);
                if (userProfile == null) {
                    return;
                }

                if (!TextUtils.isEmpty(userProfile.username)) {
                    usernameTextView.setText(userProfile.username);
                }

                if (!TextUtils.isEmpty(userProfile.email)) {
                    emailTextView.setText(userProfile.email);
                } else if (!TextUtils.isEmpty(authEmail)) {
                    emailTextView.setText(authEmail);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, R.string.something_wrong, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
