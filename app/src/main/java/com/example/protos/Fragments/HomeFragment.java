package com.example.protos.Fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.transition.Explode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.protos.Adapter.FeedAdapter;
import com.example.protos.Model.Posts;
import com.example.protos.PostActivity;
import com.example.protos.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.ContentValues.TAG;

public class HomeFragment extends Fragment implements FeedAdapter.OnFeedItemClickListener {
    private FeedAdapter adapter;
    private List<Posts> list;
    private RecyclerView mRecyclerView;
    private FirebaseAuth mAuth;
    private StorageReference mPostStorageRef;
    private StorageReference mUserStorageRef;
    private DatabaseReference UsersDatabaseReference;
    private DatabaseReference PostsDatabaseReference;
    private Query query;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.feedRecyclerView);
        mAuth = FirebaseAuth.getInstance();
        mPostStorageRef = FirebaseStorage.getInstance().getReference("PostPics");
        mUserStorageRef = FirebaseStorage.getInstance().getReference("ProfilePic");
        UsersDatabaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        PostsDatabaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts");

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        list = new ArrayList<>();
        adapter = new FeedAdapter(getContext(), list, this);
        mRecyclerView.setAdapter(adapter);
        if (mAuth.getCurrentUser() != null) {
            PostsDatabaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        if (!postSnapshot.getKey().equals(mAuth.getUid())) {
                            query = PostsDatabaseReference.child(postSnapshot.getKey()).orderByChild("creation_date");
                            query.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        Posts post = snapshot.getValue(Posts.class);
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
                    }
                    PostsDatabaseReference.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }
        return view;
    }

    @Override
    public void OnFeedItemClick(int position, ActivityOptions options) {
        Intent intent = new Intent(getContext(), PostActivity.class);
        intent.putExtra("post", (Parcelable) list.get(position));
        startActivity(intent, options.toBundle());
    }
}
