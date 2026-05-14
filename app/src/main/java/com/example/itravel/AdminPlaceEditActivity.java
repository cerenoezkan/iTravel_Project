package com.example.itravel;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.itravel.Model.Place;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class AdminPlaceEditActivity extends AppCompatActivity {

    public static final String EXTRA_PLACE_ID = "placeId";

    private String editPlaceId;
    private String existingImageUrl = "";
    private Uri newImageUri = null;

    private DatabaseReference placesRef;
    private TextInputEditText titleEt;
    private TextInputEditText descEt;
    private TextInputEditText latEt;
    private TextInputEditText lonEt;
    private ImageView imageView;
    private MaterialButton deleteBtn;
    private ProgressDialog progressDialog;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    newImageUri = uri;
                    imageView.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isAdminSession(this)) {
            Toast.makeText(this, R.string.admin_area_only, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_admin_place_edit);

        editPlaceId = getIntent().getStringExtra(EXTRA_PLACE_ID);
        boolean editMode = !TextUtils.isEmpty(editPlaceId);

        placesRef = FirebaseDatabase.getInstance(ItravelApp.FIREBASE_RTDB_URL)
                .getReference(ItravelApp.RTDB_NODE_PLACES);

        MaterialToolbar toolbar = findViewById(R.id.admin_edit_toolbar);
        toolbar.setTitle(editMode ? getString(R.string.admin_edit_place_title) : getString(R.string.admin_add_place_title));
        toolbar.setNavigationOnClickListener(v -> finish());

        titleEt = findViewById(R.id.admin_edit_title);
        descEt = findViewById(R.id.admin_edit_description);
        latEt = findViewById(R.id.admin_edit_lat);
        lonEt = findViewById(R.id.admin_edit_lon);
        imageView = findViewById(R.id.admin_edit_image);
        deleteBtn = findViewById(R.id.admin_edit_delete);
        MaterialButton pickBtn = findViewById(R.id.admin_edit_pick_image);
        MaterialButton saveBtn = findViewById(R.id.admin_edit_save);

        progressDialog = new ProgressDialog(this);

        pickBtn.setOnClickListener(v -> pickImage.launch("image/*"));
        saveBtn.setOnClickListener(v -> savePlace(editMode));
        deleteBtn.setOnClickListener(v -> confirmDelete());

        if (editMode) {
            deleteBtn.setVisibility(View.VISIBLE);
            loadExistingPlace();
        }
    }

    private void loadExistingPlace() {
        placesRef.child(editPlaceId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Place p = Place.fromSnapshot(snapshot);
                if (p == null) {
                    Toast.makeText(AdminPlaceEditActivity.this, R.string.place_detail_not_found, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                titleEt.setText(p.getTitle() != null ? p.getTitle() : "");
                descEt.setText(p.getDescription() != null ? p.getDescription() : "");
                latEt.setText(p.getLatitude() != null ? p.getLatitude() : "");
                lonEt.setText(p.getLongitude() != null ? p.getLongitude() : "");
                existingImageUrl = p.getImageUrl() != null ? p.getImageUrl() : "";
                if (!existingImageUrl.isEmpty()) {
                    Glide.with(AdminPlaceEditActivity.this).load(existingImageUrl).centerCrop().into(imageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminPlaceEditActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmDelete() {
        if (TextUtils.isEmpty(editPlaceId)) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.admin_delete_confirm_title)
                .setMessage(R.string.admin_delete_confirm_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.admin_delete, (d, w) -> {
                    progressDialog.setMessage(getString(R.string.admin_deleting));
                    progressDialog.show();
                    placesRef.child(editPlaceId).removeValue()
                            .addOnSuccessListener(unused -> {
                                progressDialog.dismiss();
                                Toast.makeText(AdminPlaceEditActivity.this, R.string.admin_deleted, Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(AdminPlaceEditActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .show();
    }

    private void savePlace(boolean editMode) {
        String title = textOf(titleEt);
        String desc = textOf(descEt);
        String lat = textOf(latEt);
        String lon = textOf(lonEt);

        if (title.isEmpty() || lat.isEmpty() || lon.isEmpty()) {
            Toast.makeText(this, R.string.admin_place_required_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!editMode && newImageUri == null) {
            Toast.makeText(this, R.string.admin_place_image_required, Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage(getString(R.string.admin_saving));
        progressDialog.show();

        if (newImageUri != null) {
            uploadThenSave(editMode, title, desc, lat, lon);
        } else if (editMode) {
            String descOrNull = desc.isEmpty() ? null : desc;
            Place place = new Place(editPlaceId, title, descOrNull, lat, lon, existingImageUrl);
            placesRef.child(editPlaceId).setValue(place)
                    .addOnSuccessListener(unused -> finishOk())
                    .addOnFailureListener(e -> finishError(e.getMessage()));
        }
    }

    private void uploadThenSave(boolean editMode, String title, String desc, String lat, String lon) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("imagePost");
        StorageReference fileRef = storageRef.child(System.currentTimeMillis() + ".jpg");
        fileRef.putFile(newImageUri)
                .addOnSuccessListener((UploadTask.TaskSnapshot taskSnapshot) -> fileRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String url = uri.toString();
                            String descOrNull = desc.isEmpty() ? null : desc;
                            if (editMode) {
                                Place place = new Place(editPlaceId, title, descOrNull, lat, lon, url);
                                placesRef.child(editPlaceId).setValue(place)
                                        .addOnSuccessListener(unused -> finishOk())
                                        .addOnFailureListener(e -> finishError(e.getMessage()));
                            } else {
                                DatabaseReference ref = placesRef.push();
                                String id = ref.getKey();
                                if (id == null) {
                                    finishError(getString(R.string.admin_place_id_error));
                                    return;
                                }
                                Place place = new Place(id, title, descOrNull, lat, lon, url);
                                ref.setValue(place)
                                        .addOnSuccessListener(unused -> finishOk())
                                        .addOnFailureListener(e -> finishError(e.getMessage()));
                            }
                        })
                        .addOnFailureListener(e -> finishError(e.getMessage())))
                .addOnFailureListener(e -> finishError(e.getMessage()));
    }

    private void finishOk() {
        progressDialog.dismiss();
        Toast.makeText(this, R.string.admin_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void finishError(String msg) {
        progressDialog.dismiss();
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private static String textOf(TextInputEditText et) {
        if (et == null || et.getText() == null) {
            return "";
        }
        return et.getText().toString().trim();
    }
}
