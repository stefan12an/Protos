package com.example.protos.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.protos.AddComment;
import com.example.protos.Model.Comments;
import com.example.protos.Model.Posts;
import com.example.protos.Model.Users;
import com.example.protos.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private Activity context;
    private DatabaseReference UsersDatabaseReference;
    private DatabaseReference PostsDatabaseReference;
    private List<Comments> commentsList;
    private boolean ok;
    private Posts post;

    public CommentsAdapter(Activity context, List<Comments> commentsList, Posts post) {
        this.context = context;
        this.post = post;
        this.commentsList = commentsList;
    }

    @NonNull
    @NotNull
    @Override
    public CommentsViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.each_comment, parent, false);

        return new CommentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull CommentsViewHolder holder, int position) {
        UsersDatabaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        PostsDatabaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts");
        Comments comment = commentsList.get(position);
        holder.setComment(comment.getComment());

        UsersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (comment.getUser_id().equals(userSnapshot.getKey())) {
                        Users user = userSnapshot.getValue(Users.class);
                        holder.setProfile_pic(user.getProfile_pic());
                        holder.setUser_holder(user.getUsername() + ":");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        holder.like_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostsDatabaseReference.child(post.getUser_id()).child(post.getPost_id()).child("Comments").child(comment.getKey()).child("Comm_Likes").child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", ServerValue.TIMESTAMP);
                            PostsDatabaseReference.child(post.getUser_id()).child(post.getPost_id()).child("Comments").child(comment.getKey()).child("Comm_Likes").child(mAuth.getUid()).setValue(likesMap);
                        } else {
                            PostsDatabaseReference.child(post.getUser_id()).child(post.getPost_id()).child("Comments").child(comment.getKey()).child("Comm_Likes").child(mAuth.getUid()).removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        });

        PostsDatabaseReference.child(post.getUser_id()).child(post.getPost_id()).child("Comments").child(comment.getKey()).child("Comm_Likes").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.like_pic.setImageDrawable(context.getDrawable(R.drawable.after_liked));
                } else {
                    holder.like_pic.setImageDrawable(context.getDrawable(R.drawable.before_liked));
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        PostsDatabaseReference.child(post.getUser_id()).child(post.getPost_id()).child("Comments").child(comment.getKey()).child("Comm_Likes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int count = (int) snapshot.getChildrenCount();
                    holder.setCommLikes(count);
                } else
                    holder.setCommLikes(0);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    public Context getContext() {
        return context;
    }

    public class CommentsViewHolder extends RecyclerView.ViewHolder {
        private TextView mComment,username,comm_likes;
        private CircleImageView profile_pic;
        private ImageView like_pic;
        private View mView;

        public CommentsViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            mView = itemView;
            like_pic = mView.findViewById(R.id.like_comment);
        }

        public void setCommLikes(int count) {
            comm_likes = mView.findViewById(R.id.comm_like_count);
            comm_likes.setText(String.valueOf(count));
        }

        public void setComment(String comment) {
            mComment = mView.findViewById(R.id.comment_tv);
            mComment.setText(comment);
        }

        public void setProfile_pic(String urlProfile) {
            profile_pic = mView.findViewById(R.id.comment_profile_tv);
            Glide.with(context).load(urlProfile).into(profile_pic);
        }

        public void setUser_holder(String Username) {
            username = mView.findViewById(R.id.comment_username_tv);
            username.setText(Username);
        }
    }

    public void deleteItem(int position, Posts post) {
//        Comments mRecentlyDeletedItem = commentsList.get(position);
//        int mRecentlyDeletedItemPosition = position;
        PostsDatabaseReference.child(post.getUser_id()).child(post.getPost_id()).child("Comments").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                        Comments comment = dataSnapshot.getValue(Comments.class);
                        if (commentsList.get(position).getKey().equals(dataSnapshot.getKey()) && post.getUser_id().equals(mAuth.getUid())) {
                            PostsDatabaseReference.child(post.getUser_id()).child(post.getPost_id()).child("Comments").child(commentsList.get(position).getKey()).removeValue();

                            PostsDatabaseReference.child(post.getUser_id()).child(post.getPost_id()).child("Comments").removeEventListener(this);
                            commentsList.remove(position);
                            notifyItemRemoved(position);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public boolean isOwner(Posts post) {
        ok = Objects.equals(mAuth.getUid(), post.getUser_id());
        return ok;
    }
}
