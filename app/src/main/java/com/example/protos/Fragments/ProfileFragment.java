package com.example.protos.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.protos.Adapter.PostAdapter;
import com.example.protos.Model.Posts;
import com.example.protos.PostActivity;
import com.example.protos.R;
import com.example.protos.Model.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.ContentValues.TAG;

public class ProfileFragment extends Fragment implements PostAdapter.OnProfileItemClickListener {
    private PostAdapter adapter;
    private List<Posts> list;
    private RecyclerView mRecyclerView;
    private FirebaseAuth mAuth;
    private StorageReference mPostStorageRef;
    private StorageReference mUserStorageRef;
    private DatabaseReference UsersDatabaseReference;
    private DatabaseReference PostsDatabaseReference;
    private ImageView profile_pic;
    private TextView username,email;
    private Query query;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile,container,false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.profileRecyclerView);
        mAuth = FirebaseAuth.getInstance();
        username = view.findViewById(R.id.user);
        email = view.findViewById(R.id.email);
        profile_pic= view.findViewById(R.id.feed_profile_pic);
        mPostStorageRef = FirebaseStorage.getInstance().getReference("PostPics");
        mUserStorageRef = FirebaseStorage.getInstance().getReference("ProfilePic");
        UsersDatabaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        PostsDatabaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts");

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));

        list = new ArrayList<>();
        adapter = new PostAdapter(getContext(), list, this);
        mRecyclerView.setAdapter(adapter);
        if (mAuth.getCurrentUser() != null) {
            query = PostsDatabaseReference.child(mAuth.getUid()).orderByChild("creation_date");
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                        Posts post = postSnapshot.getValue(Posts.class);
                        list.add(post);
                    }
                    Collections.reverse(list);
                    adapter.notifyDataSetChanged();
                    query.removeEventListener(this);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                    // ...
                }
            });
        }

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Users post = dataSnapshot.getValue(Users.class);
                username.setText(post.getUsername());
                email.setText(post.getEmail());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        UsersDatabaseReference.child(mAuth.getUid()).addValueEventListener(postListener);

        ValueEventListener postListener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String post = dataSnapshot.getValue(String.class);
                Glide.with(ProfileFragment.this).load(post).into(profile_pic);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        UsersDatabaseReference.child(mAuth.getUid()).child("profile_pic").addValueEventListener(postListener1);
        return view;
    }

    @Override
    public void OnProfileItemClick(int position) {
        Intent intent = new Intent(getContext(), PostActivity.class);
        intent.putExtra("post", (Parcelable) list.get(position));
        startActivity(intent);
    }
}
