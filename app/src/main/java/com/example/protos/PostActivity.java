package com.example.protos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.protos.Fragments.HomeFragment;
import com.example.protos.Model.Posts;
import com.example.protos.Model.Users;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;

public class PostActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    DatabaseReference UsersDatabaseReference;
    DatabaseReference PostsDatabaseReference;
    private TextView user_holder;
    private TextView creation_date;
    private TextView caption, likeCount;
    private ImageView likeBtn;
    private CircleImageView profile_pic;
    private ImageView post_pic;
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
        mAuth = FirebaseAuth.getInstance();
        UsersDatabaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        PostsDatabaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Protos");
        Bundle bundle = getIntent().getExtras();
        Posts post = bundle.getParcelable("post");

        user_holder.setText(post.getUsername());
        creation_date.setText(post.getCreation_date());
        caption.setText(post.getCaption());
        Glide.with(PostActivity.this).load(post.getPost_pic()).into(post_pic);

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
                }else
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
                startActivity(new Intent(PostActivity.this, Settings.class));
                break;
            case R.id.logout:
                mAuth.signOut();
                startActivity(new Intent(PostActivity.this, LoginActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}