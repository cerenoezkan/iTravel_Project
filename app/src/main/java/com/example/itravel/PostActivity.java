package com.example.itravel;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.itravel.Model.Place;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class PostActivity extends AppCompatActivity {

    private ImageView post_image;
    private EditText placeET, lonET, latET;
    private Button btn_save, btn_cancel;

    Uri imageUrl = null;
    ProgressDialog progressDialog;

    FirebaseDatabase mDatabase;
    DatabaseReference reference;
    FirebaseStorage storage;

    // FOTOĞRAF SEÇME
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            imageUrl = uri;
                            post_image.setImageURI(uri);
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

        setContentView(R.layout.activity_post);

        post_image = findViewById(R.id.post_image);
        placeET = findViewById(R.id.post_edit);
        latET = findViewById(R.id.latitude_edit);
        lonET = findViewById(R.id.longitude_edit);
        final EditText descriptionET = findViewById(R.id.description_edit);
        btn_cancel = findViewById(R.id.btn_return);
        btn_save = findViewById(R.id.set_post);

        mDatabase = FirebaseDatabase.getInstance(ItravelApp.FIREBASE_RTDB_URL);
        reference = mDatabase.getReference(ItravelApp.RTDB_NODE_PLACES);
        storage = FirebaseStorage.getInstance();

        progressDialog = new ProgressDialog(this);

        // KAYDET BUTONU
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String place = placeET.getText().toString().trim();
                final String lat = latET.getText().toString().trim();
                final String lon = lonET.getText().toString().trim();

                if (place.isEmpty() || lat.isEmpty() || lon.isEmpty()) {
                    Toast.makeText(PostActivity.this,
                            "Mekân adı, enlem ve boylam zorunludur.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (imageUrl == null) {
                    Toast.makeText(PostActivity.this,
                            "Lütfen bir fotoğraf seçin.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialog.setTitle("Uploading...");
                progressDialog.show();

                StorageReference storageRef =
                        FirebaseStorage.getInstance()
                                .getReference()
                                .child("imagePost");

                final StorageReference imagePathFile =
                        storageRef.child(System.currentTimeMillis() + ".jpg");

                imagePathFile.putFile(imageUrl)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                imagePathFile.getDownloadUrl()
                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {

                                                String imageDownloadLink = uri.toString();

                                                String desc = descriptionET.getText().toString().trim();

                                                DatabaseReference refPost = reference.push();
                                                String placeId = refPost.getKey();
                                                if (placeId == null) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(PostActivity.this,
                                                            "Kayıt anahtarı oluşturulamadı.",
                                                            Toast.LENGTH_SHORT).show();
                                                    return;
                                                }

                                                Place place = new Place(
                                                        placeId,
                                                        placeET.getText().toString().trim(),
                                                        desc.isEmpty() ? null : desc,
                                                        latET.getText().toString().trim(),
                                                        lonET.getText().toString().trim(),
                                                        imageDownloadLink,
                                                        com.example.itravel.Model.PlaceCategory.CITY_ISTANBUL,
                                                        com.example.itravel.Model.PlaceCategory.HISTORICAL,
                                                        0d
                                                );

                                                refPost.setValue(place)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                progressDialog.dismiss();

                                                                Intent intent =
                                                                        new Intent(getApplicationContext(),
                                                                                AdminPanelActivity.class);

                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(PostActivity.this,
                                                                    "Veritabanı kaydı başarısız: " + e.getMessage(),
                                                                    Toast.LENGTH_LONG).show();
                                                            e.printStackTrace();
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            progressDialog.dismiss();
                                            Toast.makeText(PostActivity.this,
                                                    "İndirme bağlantısı alınamadı: " + e.getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                            e.printStackTrace();
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(PostActivity.this,
                                    "Yükleme başarısız (Storage kuralları / ağ): " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        });
            }
        });

        // FOTOĞRAF SEÇME
        post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage.launch("image/*");
            }
        });

        // GERİ DÖN
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent =
                        new Intent(getApplicationContext(), AdminPanelActivity.class);

                startActivity(intent);
                finish();
            }
        });
    }
}