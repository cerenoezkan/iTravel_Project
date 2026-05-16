package com.example.itravel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.itravel.Adapter.PlaceCommentAdapter;
import com.example.itravel.Adapter.SimilarPlaceAdapter;
import com.example.itravel.util.SimilarPlacesFinder;
import com.example.itravel.Model.Place;
import com.example.itravel.Model.PlaceCategory;
import com.example.itravel.Model.PlaceComment;
import com.example.itravel.Model.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailActivity extends AppCompatActivity
        implements PlaceCommentAdapter.Listener, SimilarPlaceAdapter.Listener {

    public static final String EXTRA_PLACE_ID = "placeId";

    private String placeId;
    private DatabaseReference placeRef;
    private DatabaseReference commentsRef;
    private ValueEventListener placeListener;
    private ValueEventListener commentsListener;
    private ValueEventListener allPlacesListener;
    private Place currentPlace;

    private DatabaseReference allPlacesRef;
    private final List<Place> allPlacesCache = new ArrayList<>();

    private View similarCard;
    private SimilarPlaceAdapter similarAdapter;
    private PlaceCommentAdapter commentAdapter;
    private TextView commentsEmpty;
    private LinearLayout starRatingContainer;
    private int selectedStars;
    private TextInputEditText commentInput;
    private MaterialButton submitCommentBtn;

    @Nullable
    private String editingCommentId;
    @Nullable
    private String resolvedUsername;

    public static void launch(@NonNull Context context, @NonNull String placeId) {
        if (TextUtils.isEmpty(placeId)) {
            return;
        }
        Intent i = new Intent(context, DetailActivity.class);
        i.putExtra(EXTRA_PLACE_ID, placeId);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        placeId = getIntent().getStringExtra(EXTRA_PLACE_ID);
        if (TextUtils.isEmpty(placeId)) {
            Toast.makeText(this, R.string.place_detail_missing_id, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.detail_toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        MaterialButton routeBtn = findViewById(R.id.btn_create_route);
        routeBtn.setOnClickListener(v -> openRouteMap());

        commentsEmpty = findViewById(R.id.comments_empty);
        starRatingContainer = findViewById(R.id.comment_star_rating);
        setupStarRatingInput();
        commentInput = findViewById(R.id.comment_input);
        submitCommentBtn = findViewById(R.id.btn_submit_comment);

        RecyclerView rvComments = findViewById(R.id.rv_comments);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setNestedScrollingEnabled(false);
        String uid = currentUserId();
        commentAdapter = new PlaceCommentAdapter(uid, uid != null ? this : null);
        rvComments.setAdapter(commentAdapter);

        submitCommentBtn.setOnClickListener(v -> onSubmitCommentClicked());

        similarCard = findViewById(R.id.detail_similar_card);
        RecyclerView rvSimilar = findViewById(R.id.rv_similar_places);
        rvSimilar.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvSimilar.setNestedScrollingEnabled(false);
        similarAdapter = new SimilarPlaceAdapter(this);
        rvSimilar.setAdapter(similarAdapter);

        placeRef = FirebaseDatabase.getInstance(ItravelApp.FIREBASE_RTDB_URL)
                .getReference(ItravelApp.RTDB_NODE_PLACES)
                .child(placeId);
        commentsRef = placeRef.child("comments");

        attachPlaceListener();
        attachCommentsListener();
        attachAllPlacesListener();
    }

    private void attachAllPlacesListener() {
        allPlacesRef = FirebaseDatabase.getInstance(ItravelApp.FIREBASE_RTDB_URL)
                .getReference(ItravelApp.RTDB_NODE_PLACES);
        allPlacesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allPlacesCache.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Place place = Place.fromSnapshot(child);
                    if (place != null && place.getId() != null && !place.getId().isEmpty()) {
                        allPlacesCache.add(place);
                    }
                }
                refreshSimilarPlaces();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Similar section optional; ignore silently
            }
        };
        allPlacesRef.addValueEventListener(allPlacesListener);
    }

    private void refreshSimilarPlaces() {
        if (currentPlace == null || similarAdapter == null || similarCard == null) {
            return;
        }
        List<Place> similar = SimilarPlacesFinder.find(currentPlace, allPlacesCache);
        similarAdapter.setPlaces(similar);
        similarCard.setVisibility(similar.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onPlaceClick(@NonNull Place place) {
        if (place.getId() == null || place.getId().isEmpty()) {
            return;
        }
        DetailActivity.launch(this, place.getId());
        overridePendingTransition(R.anim.nav_fade_in, R.anim.nav_fade_out);
    }

    private void setupStarRatingInput() {
        starRatingContainer.removeAllViews();
        int starSize = dp(40);
        int starMargin = dp(6);
        for (int i = 1; i <= 5; i++) {
            ImageView star = new ImageView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(starSize, starSize);
            if (i > 1) {
                lp.setMarginStart(starMargin);
            }
            star.setLayoutParams(lp);
            star.setScaleType(ImageView.ScaleType.FIT_CENTER);
            star.setClickable(true);
            star.setFocusable(true);
            star.setContentDescription(getString(R.string.place_detail_rating) + " " + i);
            final int value = i;
            star.setOnClickListener(v -> setSelectedStars(value));
            starRatingContainer.addView(star);
        }
        setSelectedStars(0);
    }

    private void setSelectedStars(int stars) {
        selectedStars = Math.max(0, Math.min(5, stars));
        for (int i = 0; i < starRatingContainer.getChildCount(); i++) {
            ImageView star = (ImageView) starRatingContainer.getChildAt(i);
            star.setImageResource(i < selectedStars
                    ? R.drawable.ic_star_filled
                    : R.drawable.ic_star_outline);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void attachPlaceListener() {
        placeListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(DetailActivity.this, R.string.place_detail_not_found, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                Place place = Place.fromSnapshot(snapshot);
                if (place == null) {
                    Toast.makeText(DetailActivity.this, R.string.place_detail_not_found, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                currentPlace = place;
                bindPlace(place);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        placeRef.addValueEventListener(placeListener);
    }

    private void attachCommentsListener() {
        commentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<PlaceComment> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    PlaceComment comment = PlaceComment.fromSnapshot(child);
                    if (comment != null && comment.getId() != null) {
                        list.add(comment);
                    }
                }
                Collections.sort(list, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                commentAdapter.setComments(list);
                commentsEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        commentsRef.addValueEventListener(commentsListener);
    }

    private void onSubmitCommentClicked() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showLoginRequiredDialog();
            return;
        }

        String text = commentInput.getText() != null
                ? commentInput.getText().toString().trim() : "";
        int stars = selectedStars;

        if (stars < 1) {
            Toast.makeText(this, R.string.comment_stars_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (text.isEmpty()) {
            Toast.makeText(this, R.string.comment_text_required, Toast.LENGTH_SHORT).show();
            return;
        }

        submitCommentBtn.setEnabled(false);
        resolveUsername(user, username -> saveComment(user.getUid(), username, text, stars));
    }

    private void saveComment(@NonNull String userId, @NonNull String username,
                             @NonNull String text, int stars) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("username", username);
        payload.put("comment", text);
        payload.put("stars", stars);
        payload.put("timestamp", ServerValue.TIMESTAMP);

        final boolean isUpdate = !TextUtils.isEmpty(editingCommentId);
        final DatabaseReference target;
        if (isUpdate) {
            target = commentsRef.child(editingCommentId);
        } else {
            String newId = commentsRef.push().getKey();
            if (newId == null) {
                submitCommentBtn.setEnabled(true);
                Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_SHORT).show();
                return;
            }
            target = commentsRef.child(newId);
        }

        target.updateChildren(payload)
                .addOnSuccessListener(unused -> {
                    submitCommentBtn.setEnabled(true);
                    clearComposer();
                    Toast.makeText(this,
                            isUpdate ? R.string.comment_updated : R.string.comment_saved,
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    submitCommentBtn.setEnabled(true);
                    Toast.makeText(this,
                            getString(R.string.comment_save_failed, e.getMessage()),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void clearComposer() {
        if (commentInput.getText() != null) {
            commentInput.getText().clear();
        }
        setSelectedStars(0);
        editingCommentId = null;
        submitCommentBtn.setText(R.string.comments_submit);
    }

    private void resolveUsername(@NonNull FirebaseUser user, @NonNull UsernameCallback callback) {
        if (!TextUtils.isEmpty(resolvedUsername)) {
            callback.onUsername(resolvedUsername);
            return;
        }
        String display = user.getDisplayName();
        if (!TextUtils.isEmpty(display)) {
            resolvedUsername = display;
            callback.onUsername(resolvedUsername);
            return;
        }
        String email = user.getEmail();
        if (!TextUtils.isEmpty(email) && email.contains("@")) {
            resolvedUsername = email.substring(0, email.indexOf('@'));
            callback.onUsername(resolvedUsername);
            return;
        }

        FirebaseDatabase.getInstance(ItravelApp.FIREBASE_RTDB_URL)
                .getReference("Users")
                .child(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        User profile = task.getResult().getValue(User.class);
                        if (profile != null && !TextUtils.isEmpty(profile.username)) {
                            resolvedUsername = profile.username;
                            callback.onUsername(resolvedUsername);
                            return;
                        }
                    }
                    resolvedUsername = getString(R.string.comment_anonymous);
                    callback.onUsername(resolvedUsername);
                });
    }

    private void showLoginRequiredDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.comments_login_required_title)
                .setMessage(R.string.comments_login_required_message)
                .setNegativeButton(R.string.comments_cancel, null)
                .setPositiveButton(R.string.comments_login_action, (d, w) ->
                        startActivity(new Intent(this, LoginActivity.class)))
                .show();
    }

    @Override
    public void onEdit(@NonNull PlaceComment comment) {
        String uid = currentUserId();
        if (uid == null || comment.getUserId() == null || !uid.equals(comment.getUserId())) {
            return;
        }
        editingCommentId = comment.getId();
        if (commentInput.getText() != null) {
            commentInput.setText(comment.getComment());
        }
        setSelectedStars(comment.getStars());
        submitCommentBtn.setText(R.string.comments_update);
        findViewById(R.id.detail_comment_composer_card).requestFocus();
    }

    @Override
    public void onDelete(@NonNull PlaceComment comment) {
        if (comment.getId() == null) {
            return;
        }
        String uid = currentUserId();
        if (uid == null || comment.getUserId() == null || !uid.equals(comment.getUserId())) {
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.comment_delete_title)
                .setMessage(R.string.comment_delete_message)
                .setNegativeButton(R.string.comments_cancel, null)
                .setPositiveButton(R.string.comment_delete, (d, w) ->
                        commentsRef.child(comment.getId()).removeValue()
                                .addOnSuccessListener(unused -> {
                                    if (comment.getId().equals(editingCommentId)) {
                                        clearComposer();
                                    }
                                    Toast.makeText(this, R.string.comment_deleted, Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this,
                                        getString(R.string.comment_save_failed, e.getMessage()),
                                        Toast.LENGTH_LONG).show()))
                .show();
    }

    private void openRouteMap() {
        if (currentPlace == null) {
            return;
        }
        try {
            String latStr = currentPlace.getLatitude();
            String lonStr = currentPlace.getLongitude();
            if (latStr == null || lonStr == null || latStr.trim().isEmpty() || lonStr.trim().isEmpty()) {
                Toast.makeText(this, R.string.route_invalid_destination, Toast.LENGTH_SHORT).show();
                return;
            }
            double lat = Double.parseDouble(latStr.trim());
            double lon = Double.parseDouble(lonStr.trim());

            Intent i = new Intent(this, RouteMapActivity.class);
            i.putExtra(RouteMapActivity.EXTRA_DEST_LAT, lat);
            i.putExtra(RouteMapActivity.EXTRA_DEST_LON, lon);
            String title = currentPlace.getTitle();
            i.putExtra(RouteMapActivity.EXTRA_DEST_TITLE,
                    title != null && !title.isEmpty() ? title : getString(R.string.place_detail_untitled));
            startActivity(i);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.route_invalid_destination, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (placeRef != null && placeListener != null) {
            placeRef.removeEventListener(placeListener);
        }
        if (commentsRef != null && commentsListener != null) {
            commentsRef.removeEventListener(commentsListener);
        }
        if (allPlacesRef != null && allPlacesListener != null) {
            allPlacesRef.removeEventListener(allPlacesListener);
        }
    }

    private void bindPlace(@NonNull Place place) {
        TextView title = findViewById(R.id.detail_title);
        TextView category = findViewById(R.id.detail_category);
        TextView rating = findViewById(R.id.detail_rating);
        TextView coords = findViewById(R.id.detail_coords);
        TextView about = findViewById(R.id.detail_about);
        ImageView hero = findViewById(R.id.detail_hero_image);

        String t = nz(place.getTitle());
        title.setText(t.isEmpty() ? getString(R.string.place_detail_untitled) : t);

        category.setText(getString(PlaceCategory.labelRes(place.getCategory())));
        if (place.getRating() > 0) {
            rating.setText(getString(R.string.place_rating_format, place.getRating()));
        } else {
            rating.setText(R.string.place_rating_none);
        }

        String lat = nz(place.getLatitude());
        String lon = nz(place.getLongitude());
        String latLine = getString(R.string.place_detail_lat_label) + "  " + (lat.isEmpty() ? "—" : lat);
        String lonLine = getString(R.string.place_detail_lon_label) + "  " + (lon.isEmpty() ? "—" : lon);
        coords.setText(latLine + "\n" + lonLine);

        String desc = place.getDescription();
        if (desc == null || desc.trim().isEmpty()) {
            about.setText(R.string.place_detail_no_description);
            about.setTextColor(ContextCompat.getColor(this, R.color.gray));
        } else {
            about.setText(desc.trim());
            about.setTextColor(ContextCompat.getColor(this, R.color.role_text_primary));
        }

        String img = nz(place.getImageUrl());
        if (!img.isEmpty()) {
            Glide.with(this).load(img).centerCrop().into(hero);
        } else {
            hero.setImageResource(R.drawable.noimage);
        }
        refreshSimilarPlaces();
    }

    @Nullable
    private String currentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    private static String nz(@Nullable String s) {
        return s != null ? s : "";
    }

    private interface UsernameCallback {
        void onUsername(@NonNull String username);
    }
}
