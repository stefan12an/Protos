package com.example.protos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.protos.Model.Posts;
import com.example.protos.Model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;

public class Settings extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button saveBtn;
    private Button delBtn;
    private CircleImageView edit_pic;
    private EditText new_user;
    private ProgressBar delBar;
    private FirebaseStorage mPostStorage;
    private StorageReference mUserStorageRef;
    private FirebaseStorage mUserStorage;
    private DatabaseReference UserDatabaseReference;
    private DatabaseReference PostsDatabaseReference;
    private boolean isPhotoSelected = false;
    public Uri img_uri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth = FirebaseAuth.getInstance();
        delBar = (ProgressBar) findViewById(R.id.delBar);
        saveBtn = (Button) findViewById(R.id.saveBtn);
        delBtn = (Button) findViewById(R.id.deleteBtn);
        edit_pic = (CircleImageView) findViewById(R.id.edit_pic);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        delBar.setVisibility(View.INVISIBLE);
        mPostStorage = FirebaseStorage.getInstance();
        mUserStorage = FirebaseStorage.getInstance();
        mUserStorageRef = FirebaseStorage.getInstance().getReference("ProfilePic");
        UserDatabaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        PostsDatabaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts");
        getSupportActionBar().setTitle("Settings");
        UserDatabaseReference.child(mAuth.getUid()).child("profile_pic").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String post = dataSnapshot.getValue(String.class);
                Glide.with(Settings.this).load(post).into(edit_pic);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
            });
        edit_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(Settings.this);
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new_user = (EditText) findViewById(R.id.edit_user);
                String new_username = new_user.getText().toString();
                if (!new_username.isEmpty()) {
                    UserDatabaseReference.child(mAuth.getUid()).child("username").setValue(new_user.getText().toString());
                }
                if (isPhotoSelected) {
                    Fileuploader();
                } else {
                    startActivity(new Intent(Settings.this, Profile.class));
                    Toast.makeText(Settings.this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
                builder.setTitle("Delete your account!");
                builder.setMessage("By hitting OK you agree to delete everything related to your account.");
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        dialogInterface.cancel();
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        delBar.setVisibility(View.VISIBLE);
                        deleteStorageData();

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                deleteDatabaseData();
                                deleteFirebaseUser();
                                delBar.setVisibility(View.INVISIBLE);
                                startActivity(new Intent(Settings.this,LoginActivity.class));
                            }
                        }, 1000);
                    }
                });
                builder.show();
            }
        });
    }

    private void deleteStorageData() {
        PostsDatabaseReference.child(mAuth.getUid()).orderByChild("post_pic").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Posts post = dataSnapshot.getValue(Posts.class);
                    StorageReference photoRef = mPostStorage.getReferenceFromUrl(String.valueOf(post.getPost_pic()));
                    photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // File deleted successfully
                            Log.e(TAG, "onSuccess: deleted file");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Uh-oh, an error occurred!
                            Log.e(TAG, "onFailure: did not delete file");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        UserDatabaseReference.orderByChild("profile_pic").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    if(dataSnapshot.getKey().equals(mAuth.getUid())) {
                        Users user = dataSnapshot.getValue(Users.class);
                        StorageReference photoRef = mUserStorage.getReferenceFromUrl(String.valueOf(user.getProfile_pic()));
                        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // File deleted successfully
                                Log.d(TAG, "onSuccess: deleted file");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Uh-oh, an error occurred!
                                Log.d(TAG, "onFailure: did not delete file");
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    private void deleteDatabaseData() {
        PostsDatabaseReference.child(mAuth.getUid()).removeValue();
        UserDatabaseReference.child(mAuth.getUid()).removeValue();
    }
    private void deleteFirebaseUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User account deleted.");
                        }
                    }
                });
    }

    private void Fileuploader() {
        String uri = img_uri.toString();
        StorageReference ref = mUserStorageRef.child(mAuth.getUid() + "." + uri.substring(uri.lastIndexOf(".") + 1));
        if (img_uri != null) {
            ref.putFile(img_uri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Users user = new Users(uri.toString());
                                    UserDatabaseReference.child(mAuth.getUid()).child("profile_pic").setValue(user.getProfile_pic());
                                    startActivity(new Intent(Settings.this, Profile.class));
                                    Toast.makeText(Settings.this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Settings.this, "Image Upload error", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(Settings.this, "Please select a profile picture", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                img_uri = result.getUri();
                edit_pic.setImageURI(img_uri);
                isPhotoSelected = true;
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, result.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.settings:
                startActivity(new Intent(Settings.this, Settings.class));
                break;
            case R.id.logout:
                mAuth.signOut();
                startActivity(new Intent(Settings.this, LoginActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

}