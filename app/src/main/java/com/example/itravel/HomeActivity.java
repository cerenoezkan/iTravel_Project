package com.example.itravel;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.itravel.Adapter.PlaceAdapter;
import com.example.itravel.Model.Place;
import com.example.itravel.Model.User;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;
    private RecyclerView rv_posts;
    private List<Place> places;
    private PlaceAdapter placeAdapter;
    Button weather, map, addPost, logout;

    FloatingActionButton profile, update_profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // init components
        rv_posts = findViewById(R.id.rv_posts);
        rv_posts.setLayoutManager(new LinearLayoutManager(this));
        rv_posts.setHasFixedSize(true);

        // recycler fetch and send data.
        FirebaseDatabase pdb = FirebaseDatabase.getInstance(ItravelApp.FIREBASE_RTDB_URL);
        DatabaseReference pref = pdb.getReference(ItravelApp.RTDB_NODE_PLACES);

        pref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                places = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Place place = Place.fromSnapshot(child);
                    if (place != null && place.getId() != null && !place.getId().isEmpty()) {
                        places.add(place);
                    }
                }
                placeAdapter = new PlaceAdapter(places);
                rv_posts.setAdapter(placeAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        logout = findViewById(R.id.logout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SessionManager.signOutAndReturnToRoleSelection(HomeActivity.this);
            }
        });

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Session expired, please login again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
            return;
        }

        if (!SessionManager.isUserRole(this)) {
            if (SessionManager.isAdminSession(this)) {
                startActivity(new Intent(this, AdminPanelActivity.class));
            } else {
                Toast.makeText(this, "Session expired, please login again.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
            finish();
            return;
        }

        reference = FirebaseDatabase.getInstance(ItravelApp.FIREBASE_RTDB_URL).getReference("Users");
        userID = user.getUid();

        addPost = findViewById(R.id.btn_post);
        addPost.setVisibility(View.GONE);

        map = findViewById(R.id.btn_map);
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MapActivity.class));
            }
        });

        weather = findViewById(R.id.weather);
        if (weather != null) {
            weather.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeActivity.this, WeatherActivity.class);
                    startActivity(intent);

                }
            });
        }

        FloatingActionsMenu floatingMenu = findViewById(R.id.fab_menu);
        if (floatingMenu != null) {
            floatingMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
                @Override
                public void onMenuExpanded() {
                    View homeLayout = findViewById(R.id.main_home_layout);
                    if (homeLayout != null) {
                        Drawable bg = homeLayout.getBackground();
                        if (bg != null) {
                            bg.setAlpha(128);
                        }
                    }
                }

                @Override
                public void onMenuCollapsed() {
                    View homeLayout = findViewById(R.id.main_home_layout);
                    if (homeLayout != null) {
                        Drawable bg = homeLayout.getBackground();
                        if (bg != null) {
                            bg.setAlpha(64);
                        }
                    }
                }
            });
        }

        profile = findViewById(R.id.view_profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            }
        });

        update_profile = findViewById(R.id.profile_setting);
        if (update_profile != null) {
            update_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            User userProfile = snapshot.getValue(User.class);

                            if (userProfile != null) {

                                String username = userProfile.username;
                                String email = userProfile.email;
                                String phone = userProfile.phone;

                                Intent i = new Intent(v.getContext(), EditProfileActivity.class);

                                i.putExtra("username", username);
                                i.putExtra("email", email);
                                i.putExtra("phone", phone);

                                startActivity(i);
                            } else {
                                Toast.makeText(HomeActivity.this, "User profile not found.", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(HomeActivity.this, "Something wrong happened!", Toast.LENGTH_SHORT).show();
                        }
                    });


                }

            });
        }

    }

}
