package com.example.itravel.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.itravel.Adapter.UserCommentAdapter;
import com.example.itravel.DetailActivity;
import com.example.itravel.ItravelApp;
import com.example.itravel.Model.Place;
import com.example.itravel.Model.PlaceComment;
import com.example.itravel.Model.User;
import com.example.itravel.Model.UserPlaceComment;
import com.example.itravel.R;
import com.example.itravel.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProfileFragment extends Fragment implements UserCommentAdapter.Listener {

    private ActivityResultLauncher<String> pickImageLauncher;

    private CircularImageView profileAvatar;
    private TextView usernameTextView;
    private TextView emailTextView;
    private TextView commentsEmpty;
    private ProgressBar photoProgress;
    private UserCommentAdapter commentAdapter;

    private DatabaseReference userRef;
    private ValueEventListener userListener;
    private DatabaseReference placesRef;
    private ValueEventListener placesListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadProfilePhoto(uri);
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.profile_toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout && getActivity() != null) {
                SessionManager.signOutAndReturnToRoleSelection(getActivity());
                return true;
            }
            return false;
        });

        profileAvatar = view.findViewById(R.id.profile_avatar);
        usernameTextView = view.findViewById(R.id.username_profile);
        emailTextView = view.findViewById(R.id.email_profile);
        commentsEmpty = view.findViewById(R.id.profile_comments_empty);
        photoProgress = view.findViewById(R.id.profile_photo_progress);

        ImageButton fabPhoto = view.findViewById(R.id.btn_change_photo);
        MaterialButton btnPhotoLabel = view.findViewById(R.id.btn_change_photo_label);
        View.OnClickListener pickPhoto = v -> pickImageLauncher.launch("image/*");
        fabPhoto.setOnClickListener(pickPhoto);
        btnPhotoLabel.setOnClickListener(pickPhoto);

        RecyclerView rvComments = view.findViewById(R.id.rv_my_comments);
        rvComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvComments.setNestedScrollingEnabled(false);
        commentAdapter = new UserCommentAdapter(this);
        rvComments.setAdapter(commentAdapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), R.string.session_expired, Toast.LENGTH_SHORT).show();
            return;
        }

        bindAuthDefaults(user);
        attachUserProfileListener(user.getUid());
        attachUserCommentsListener(user.getUid());
    }

    private void bindAuthDefaults(@NonNull FirebaseUser user) {
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
    }

    private void attachUserProfileListener(@NonNull String userId) {
        userRef = FirebaseDatabase.getInstance(ItravelApp.FIREBASE_RTDB_URL)
                .getReference("Users")
                .child(userId);

        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User profile = snapshot.getValue(User.class);
                if (profile == null) {
                    return;
                }
                if (!TextUtils.isEmpty(profile.username)) {
                    usernameTextView.setText(profile.username);
                }
                if (!TextUtils.isEmpty(profile.email)) {
                    emailTextView.setText(profile.email);
                }
                loadAvatar(profile.image_url);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), R.string.something_wrong, Toast.LENGTH_SHORT).show();
            }
        };
        userRef.addValueEventListener(userListener);
    }

    private void attachUserCommentsListener(@NonNull String userId) {
        placesRef = FirebaseDatabase.getInstance(ItravelApp.FIREBASE_RTDB_URL)
                .getReference(ItravelApp.RTDB_NODE_PLACES);

        placesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<UserPlaceComment> mine = new ArrayList<>();
                for (DataSnapshot placeSnap : snapshot.getChildren()) {
                    String placeId = placeSnap.getKey();
                    if (placeId == null || placeId.isEmpty()) {
                        continue;
                    }
                    Place place = Place.fromSnapshot(placeSnap);
                    String title = place != null ? place.getTitle() : null;
                    String imageUrl = place != null ? place.getImageUrl() : null;

                    DataSnapshot commentsSnap = placeSnap.child("comments");
                    for (DataSnapshot commentSnap : commentsSnap.getChildren()) {
                        PlaceComment comment = PlaceComment.fromSnapshot(commentSnap);
                        if (comment == null) {
                            continue;
                        }
                        if (userId.equals(comment.getUserId())) {
                            mine.add(new UserPlaceComment(placeId, title, imageUrl, comment));
                        }
                    }
                }
                Collections.sort(mine, (a, b) -> Long.compare(
                        b.getComment().getTimestamp(),
                        a.getComment().getTimestamp()));
                commentAdapter.setItems(mine);
                commentsEmpty.setVisibility(mine.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        placesRef.addValueEventListener(placesListener);
    }

    private void uploadProfilePhoto(@NonNull Uri uri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), R.string.session_expired, Toast.LENGTH_SHORT).show();
            return;
        }

        setPhotoLoading(true);
        profileAvatar.setImageURI(uri);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("profile_photos")
                .child(user.getUid() + ".jpg");

        storageRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> saveProfileImageUrl(downloadUri.toString()))
                        .addOnFailureListener(e -> {
                            setPhotoLoading(false);
                            showPhotoError(e);
                        }))
                .addOnFailureListener(e -> {
                    setPhotoLoading(false);
                    showPhotoError(e);
                });
    }

    private void saveProfileImageUrl(@NonNull String url) {
        if (userRef == null) {
            setPhotoLoading(false);
            return;
        }
        userRef.child("image_url").setValue(url)
                .addOnSuccessListener(unused -> {
                    setPhotoLoading(false);
                    loadAvatar(url);
                    Toast.makeText(requireContext(),
                            R.string.profile_photo_updated, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    setPhotoLoading(false);
                    showPhotoError(e);
                });
    }

    private void showPhotoError(@NonNull Exception e) {
        String msg = e.getMessage() != null ? e.getMessage() : "";
        Toast.makeText(requireContext(),
                getString(R.string.profile_photo_failed, msg),
                Toast.LENGTH_LONG).show();
    }

    private void setPhotoLoading(boolean loading) {
        photoProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void loadAvatar(@Nullable String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty() || "default".equalsIgnoreCase(imageUrl.trim())) {
            profileAvatar.setImageResource(R.drawable.default_user);
            return;
        }
        Glide.with(this)
                .load(imageUrl.trim())
                .placeholder(R.drawable.default_user)
                .error(R.drawable.default_user)
                .centerCrop()
                .into(profileAvatar);
    }

    @Override
    public void onCommentClick(@NonNull UserPlaceComment item) {
        DetailActivity.launch(requireContext(), item.getPlaceId());
        if (getActivity() != null) {
            getActivity().overridePendingTransition(R.anim.nav_fade_in, R.anim.nav_fade_out);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userRef != null && userListener != null) {
            userRef.removeEventListener(userListener);
        }
        if (placesRef != null && placesListener != null) {
            placesRef.removeEventListener(placesListener);
        }
    }
}
