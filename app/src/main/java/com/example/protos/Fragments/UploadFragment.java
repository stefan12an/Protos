package com.example.protos.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.protos.Profile;
import com.example.protos.R;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

public class UploadFragment extends Fragment {
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);
        mPostBtn = (CardView) view.findViewById(R.id.postBtn);
        preview = (ImageView) view.findViewById(R.id.preview);
        mCaption = (EditText) view.findViewById(R.id.caption);
        mPostBar = (ProgressBar) view.findViewById(R.id.post_progress_bar);
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
                if(snapshot.exists()){
                    maxid=snapshot.getChildrenCount();
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
        return view;
    }

    private void SelectImage() {
        Intent intent = CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.OFF)
                .setMinCropResultSize(512, 512)
                .getIntent(getContext());
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
                                        newPost.put("post_id",String.valueOf(maxid+1));
                                        newPost.put("username", username);
                                        newPost.put("caption", caption);
                                        newPost.put("creation_date", strDate);
                                        newPost.put("post_pic", uri.toString());

                                        PostsDatabaseReference.child(mAuth.getUid()).child(String.valueOf(maxid+1)).setValue(newPost);
                                        mPostBar.setVisibility(View.INVISIBLE);
                                        Fragment profileFragment = new ProfileFragment();
                                        getFragmentManager().beginTransaction().replace(R.id.flFragment, profileFragment).commit();
                                    }
                                });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Image Upload error", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getContext(), "Please select a profile picture", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postImageUri = result.getUri();
                preview.setImageURI(postImageUri);
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(getContext(), result.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}

