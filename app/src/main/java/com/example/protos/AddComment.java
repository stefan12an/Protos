package com.example.protos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.protos.Adapter.CommentsAdapter;
import com.example.protos.Model.Comments;
import com.example.protos.Model.Posts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static android.widget.LinearLayout.HORIZONTAL;

public class AddComment extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference UsersDatabaseReference;
    private DatabaseReference PostsDatabaseReference;
    private DividerItemDecoration mDividerItemDecoration;
    private Posts post;
    private EditText add_comment;
    private Button add_comment_btn;
    private RecyclerView comments_Rview;
    private CommentsAdapter commentsAdapter;
    private List<Comments> commentsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comment);
        add_comment = findViewById(R.id.add_comment);
        add_comment_btn = findViewById(R.id.add_comment_btn);
        comments_Rview = findViewById(R.id.comments_Rview);
        mAuth = FirebaseAuth.getInstance();
        UsersDatabaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        PostsDatabaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts");
        Bundle bundle = getIntent().getExtras();
        post = bundle.getParcelable("post");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        commentsList = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(AddComment.this, commentsList, post);

        comments_Rview.setHasFixedSize(true);
        comments_Rview.setLayoutManager(new LinearLayoutManager(this));
        comments_Rview.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        comments_Rview.setAdapter(commentsAdapter);
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


        add_comment_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = add_comment.getText().toString();
                if (!comment.isEmpty()) {
                    PostsDatabaseReference.child(post.getUser_id()).child(post.getPost_id()).child("Comments").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            Map<String, Object> commentsMap = new HashMap<>();
                            String key = PostsDatabaseReference.child(post.getUser_id()).child(post.getPost_id()).child("Comments").push().getKey();
                            commentsMap.put("key", key);
                            commentsMap.put("comment", comment);
                            commentsMap.put("timestamp", ServerValue.TIMESTAMP);
                            commentsMap.put("user_id", mAuth.getUid());
                            PostsDatabaseReference.child(post.getUser_id()).child(post.getPost_id()).child("Comments").child(key).setValue(commentsMap);
                            PostsDatabaseReference.child(post.getUser_id()).child(post.getPost_id()).child("Comments").removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
                } else {
                    Toast.makeText(AddComment.this, "Please add a comment!", Toast.LENGTH_SHORT).show();
                }
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 300);
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
                startActivity(new Intent(AddComment.this, Settings.class));
                break;
            case R.id.logout:
                mAuth.signOut();
                startActivity(new Intent(AddComment.this, LoginActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

}