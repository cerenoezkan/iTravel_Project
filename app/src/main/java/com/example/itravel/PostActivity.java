package com.example.itravel;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.example.itravel.Model.Post;
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

    private final androidx.activity.result.ActivityResultLauncher<CropImageContractOptions> cropImage =
            registerForActivityResult(new CropImageContract(), result -> {
                if (result.isSuccessful()) {
                    imageUrl = result.getUriContent();
                    post_image.setImageURI(imageUrl);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        post_image = findViewById(R.id.post_image);
        placeET = findViewById(R.id.post_edit);
        latET = findViewById(R.id.latitude_edit);
        lonET = findViewById(R.id.longitude_edit);
        btn_cancel = findViewById(R.id.btn_return);
        btn_save = findViewById(R.id.set_post);

        mDatabase = FirebaseDatabase.getInstance();
        reference = mDatabase.getReference().child("Posts");
        storage = FirebaseStorage.getInstance();

        progressDialog = new ProgressDialog(this);

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String place = placeET.getText().toString().trim();
                final String lat = latET.getText().toString().trim();
                final String lon = lonET.getText().toString().trim();

                if (!(place.isEmpty() && lat.isEmpty() && lon.isEmpty() && imageUrl == null)) {
                    progressDialog.setTitle("Uploading ...");
                    progressDialog.show();
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("imagePost");
                    final StorageReference imagePathFile = storageRef.child(imageUrl.getLastPathSegment());
                    imagePathFile.putFile(imageUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imagePathFile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageDownloadLink = uri.toString();
                                    Post post = new Post(placeET.getText().toString(), latET.getText().toString(), lonET.getText().toString(), imageDownloadLink);
                                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                                    DatabaseReference refPost = db.getReference("Posts").push();
                                    refPost.setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }
        });

        post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCropActivity();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void startCropActivity() {
        cropImage.launch(
                new CropImageContractOptions(null, new CropImageOptions())
        );
    }
}