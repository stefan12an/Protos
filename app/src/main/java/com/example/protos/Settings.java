package com.example.protos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;

import de.hdodenhof.circleimageview.CircleImageView;

public class Settings extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button saveBtn;
    private CircleImageView edit_pic;
    private EditText new_user;
    private StorageReference mStorageRef;
    private DatabaseReference databaseReference;
    public Uri img_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth = FirebaseAuth.getInstance();
        saveBtn = (Button) findViewById(R.id.saveBtn);
        edit_pic = (CircleImageView) findViewById(R.id.edit_pic);
        mStorageRef = FirebaseStorage.getInstance().getReference("ProfilePic");
        databaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        getSupportActionBar().setTitle("Settings");

        edit_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(Settings.this);
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new_user = (EditText) findViewById(R.id.edit_user);
                Fileuploader();
            }
        });
    }

//    private String getExtension(Uri uri) {
//        ContentResolver cr = getContentResolver();
//        MimeTypeMap mimeTypeInfo = MimeTypeMap.getSingleton();
//        return mimeTypeInfo.getExtensionFromMimeType(cr.getType(uri));
//    }

    private void Fileuploader() {
        String uri = img_uri.toString();
        StorageReference ref = mStorageRef.child(mAuth.getUid() + "." + uri.substring(uri.lastIndexOf(".")+1));        ref.putFile(img_uri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                User user = new User(uri.toString());
                                databaseReference.child(mAuth.getUid()).child("profile_pic").setValue(user.getProfile_pic());
                                databaseReference.child(mAuth.getUid()).child("username").setValue(new_user.getText().toString());
                                startActivity(new Intent(Settings.this, Profile.class));
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Settings.this, "Image Upload error", Toast.LENGTH_SHORT);
                    }
                });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK) {
                img_uri = result.getUri();
                edit_pic.setImageURI(img_uri);
            }
            else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Toast.makeText(this,result.getError().getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }

}