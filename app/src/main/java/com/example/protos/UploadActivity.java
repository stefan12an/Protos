package com.example.protos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.protos.Fragments.ProfileFragment;
import com.example.protos.Model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;

import java.io.FileDescriptor;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class UploadActivity extends AppCompatActivity {
    private String username;
    private FirebaseAuth mAuth;
    private CardView mPostBtn;
    private ProgressBar mPostBar;
    private ImageView preview;
    private EditText mCaption;
    private StorageReference mStorageRef;
    private DatabaseReference UsersDatabaseReference;
    private DatabaseReference PostsDatabaseReference;
    public Uri postImageUri;
    long maxid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        mPostBtn = (CardView) findViewById(R.id.postBtn);
        preview = (ImageView) findViewById(R.id.preview);
        mCaption = (EditText) findViewById(R.id.caption);
        mPostBar = (ProgressBar) findViewById(R.id.post_progress_bar);
        mPostBar.setVisibility(View.INVISIBLE);
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference("PostPics");
        UsersDatabaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        PostsDatabaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);
                username = user.getUsername();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        UsersDatabaseReference.child(mAuth.getUid()).addValueEventListener(postListener);
        PostsDatabaseReference.child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    maxid = snapshot.getChildrenCount();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });
        mPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadPost();
            }
        });
    }

    private void SelectImage() {
        Intent intent = CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMaxCropResultSize(1080,1350)
                .setMinCropResultSize(320,566)
                .setMinCropWindowSize(320,566)
                .getIntent(UploadActivity.this);
        startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    private void LoadPost() {
        mPostBar.setVisibility(View.VISIBLE);
        String caption = mCaption.getText().toString();
        String uri = postImageUri.toString();
        String randomId = UUID.randomUUID().toString();
        if (postImageUri != null && !caption.isEmpty()) {
            StorageReference ref = mStorageRef.child(mAuth.getUid()).child(randomId + "." + uri.substring(uri.lastIndexOf(".") + 1));
            ref.putFile(postImageUri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                        Date date = new Date();
                                        String strDate = dateFormat.format(date);

                                        HashMap<String, String> newPost = new HashMap<String, String>();
                                        newPost.put("user_id", mAuth.getUid());
                                        newPost.put("post_id", String.valueOf(maxid + 1));
                                        newPost.put("username", username);
                                        newPost.put("caption", caption);
                                        newPost.put("creation_date", strDate);
                                        newPost.put("post_pic", uri.toString());

                                        PostsDatabaseReference.child(mAuth.getUid()).child(String.valueOf(maxid + 1)).setValue(newPost);
                                        mPostBar.setVisibility(View.INVISIBLE);
                                        startActivity(new Intent(UploadActivity.this,Profile.class));
                                        finish();
                                    }
                                });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UploadActivity.this, "Image Upload error", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(UploadActivity.this, "Please select a profile picture", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = null;
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postImageUri = result.getUri();
                try {
                    bitmap = getBitmapFromUri(postImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                preview.setImageURI(postImageUri);
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(UploadActivity.this, result.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = UploadActivity.this.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
}