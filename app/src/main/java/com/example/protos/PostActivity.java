package com.example.protos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.example.protos.Adapter.CommentsAdapter;
import com.example.protos.Fragments.HomeFragment;
import com.example.protos.Model.Comments;
import com.example.protos.Model.Posts;
import com.example.protos.Model.Users;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;

public class PostActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    DatabaseReference UsersDatabaseReference;
    DatabaseReference PostsDatabaseReference;
    private FirebaseStorage mPostStorage;
    SwipeRefreshLayout swipeRefreshLayout;
    private TextView user_holder, caption, likeCount, creation_date;
    private RecyclerView comment_view;
    private ImageView likeBtn, post_pic, comments_post, save_pic;
    private CircleImageView profile_pic;
    private List<Comments> commentsList;
    private CommentsAdapter commentsAdapter;
    private Posts post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        user_holder = findViewById(R.id.user_holder);
        creation_date = findViewById(R.id.creation_date);
        caption = findViewById(R.id.post_caption);
        profile_pic = findViewById(R.id.profile_pic);
        post_pic = findViewById(R.id.post_pic);
        likeBtn = findViewById(R.id.like_btn);
        likeCount = findViewById(R.id.like_count_tv);
        comments_post = findViewById(R.id.comments_post);
        comment_view = findViewById(R.id.comment_recyclerView);
        save_pic = findViewById(R.id.save_btn);
        mAuth = FirebaseAuth.getInstance();
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        mPostStorage = FirebaseStorage.getInstance();
        UsersDatabaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        PostsDatabaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Protos");
        Bundle bundle = getIntent().getExtras();
        post = bundle.getParcelable("post");

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                finish();
                overridePendingTransition(0, 0);
                getIntent().setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(getIntent());
                swipeRefreshLayout.setRefreshing(false);
            }
        });


        commentsList = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(PostActivity.this, commentsList, post);
        comment_view.setHasFixedSize(true);
        comment_view.setLayoutManager(new LinearLayoutManager(this));
        comment_view.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        comment_view.setAdapter(commentsAdapter);
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new SwipeToDeleteCallback(commentsAdapter, post, commentsList));
        itemTouchHelper.attachToRecyclerView(comment_view);

        user_holder.setText(post.getUsername());
        creation_date.setText(post.getCreation_date());
        caption.setText(post.getCaption());
        Glide.with(PostActivity.this).load(post.getPost_pic()).into(post_pic);
        comments_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostActivity.this, AddComment.class);
                intent.putExtra("post", post);
                startActivity(intent);
            }
        });
        save_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadFile(post.getPost_pic());
                save_pic.setImageDrawable(getDrawable(R.drawable.ic_saved));
                Toast.makeText(PostActivity.this, "Photo saved successfully", Toast.LENGTH_SHORT).show();
            }
        });
        UsersDatabaseReference.child(post.getUser_id()).child("profile_pic").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String profile_picture_url = snapshot.getValue(String.class);
                Glide.with(PostActivity.this).load(profile_picture_url).into(profile_pic);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostsDatabaseReference.child(post.getUser_id()).child(post.getPost_id()).child("Likes").child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", ServerValue.TIMESTAMP);
                            PostsDatabaseReference.child(post.getUser_id()).child(post.getPost_id()).child("Likes").child(mAuth.getUid()).setValue(likesMap);
                        } else {
                            PostsDatabaseReference.child(post.getUser_id()).child(post.getPost_id()).child("Likes").child(mAuth.getUid()).removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        });
        PostsDatabaseReference.child(post.getUser_id()).child(post.getPost_id()).child("Likes").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    likeBtn.setImageDrawable(PostActivity.this.getDrawable(R.drawable.after_liked));
                } else {
                    likeBtn.setImageDrawable(PostActivity.this.getDrawable(R.drawable.before_liked));
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        PostsDatabaseReference.child(post.getUser_id()).child(post.getPost_id()).child("Likes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    likeCount.setText(snapshot.getChildrenCount() + " Likes");
                } else
                    likeCount.setText("0");
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (mAuth.getCurrentUser().getUid().equals(post.getUser_id())) {
            inflater.inflate(R.menu.post_top_option_menu, menu);
        } else
            inflater.inflate(R.menu.top_option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            case R.id.settings:
                startActivity(new Intent(PostActivity.this, Settings.class));
                break;
            case R.id.deletePost:
                AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this);
                builder.setTitle("Delete this post!");
                builder.setMessage("By hitting OK you agree to delete this post you've created.");
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        dialogInterface.cancel();
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
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
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                PostsDatabaseReference.child(mAuth.getUid()).child(post.getPost_id()).removeValue();
                                startActivity(new Intent(PostActivity.this, Profile.class));
                            }
                        }, 1000);
                    }
                });
                builder.show();
                break;
            case R.id.logout:
                mAuth.signOut();
                startActivity(new Intent(PostActivity.this, LoginActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        commentsList.clear();

    }

    @Override
    protected void onResume() {
        super.onResume();
        PostsDatabaseReference.child(post.getUser_id()).child(post.getPost_id()).child("Comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Comments comm = dataSnapshot.getValue(Comments.class);
                    commentsList.add(comm);
                }
                Collections.reverse(commentsList);
                commentsAdapter.notifyDataSetChanged();
                PostsDatabaseReference.child(post.getUser_id()).child(post.getPost_id()).child("Comments").removeEventListener(this);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void downloadFile(String url) {
        DownloadManager mgr = (DownloadManager) PostActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);

        Uri downloadUri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(
                downloadUri);
        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setTitle("Demo")
                .setDescription("Something useful. No, really.")
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, post.getPost_pic());

        mgr.enqueue(request);

    }

}