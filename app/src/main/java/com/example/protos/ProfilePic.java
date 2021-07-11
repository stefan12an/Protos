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
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfilePic extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button upBtn;
    private Button lodBtn;
    private Button sendText;
    private ImageView preview;
    private StorageReference mStorageRef;
    private EditText test;
    private int index = 0;
    private DatabaseReference databaseReference;
    public Uri imguri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountsetup);
        mAuth = FirebaseAuth.getInstance();
        upBtn = (Button) findViewById(R.id.uploadBtn);
        lodBtn = (Button) findViewById(R.id.loadBtn);
        preview = (ImageView) findViewById(R.id.preview);
        mStorageRef = FirebaseStorage.getInstance().getReference("Images");
        databaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users");

        lodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Filechooser();
            }
        });
        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fileuploader();
            }
        });
    }

    private String getExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeInfo = MimeTypeMap.getSingleton();
        return mimeTypeInfo.getExtensionFromMimeType(cr.getType(uri));
    }

    private void Fileuploader() {
        String id = databaseReference.push().getKey();
        String unique = String.valueOf(index);
        StorageReference ref = mStorageRef.child(id + unique + "." + getExtension(imguri));
        index++;
        ref.putFile(imguri)
                .addOnSuccessListener((new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Toast.makeText(ProfilePic.this, "Image Uploaded succesfully", Toast.LENGTH_SHORT);
                    }
                }))
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfilePic.this, "Image Upload error", Toast.LENGTH_SHORT);
                    }
                });
    }


    private void Filechooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imguri = data.getData();
            preview.setImageURI(imguri);
        }
    }

}