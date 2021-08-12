package com.example.protos.Adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.protos.AddComment;
import com.example.protos.Fragments.ProfileFragment;
import com.example.protos.Model.Posts;
import com.example.protos.Model.Users;
import com.example.protos.R;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {
    private List<Posts> mList;
    private Context context;
    private FirebaseAuth mAuth;
    private StorageReference mPostStorageRef;
    private StorageReference mUserStorageRef;
    private boolean status = false;
    private DatabaseReference UsersDatabaseReference;
    private DatabaseReference PostsDatabaseReference;
    private OnFeedItemClickListener mOnFeedItemClickListener;


    public FeedAdapter(Context context, List<Posts> mList, OnFeedItemClickListener onFeedItemClickListener) {
        this.mList = mList;
        this.context = context;
        this.mOnFeedItemClickListener = onFeedItemClickListener;
    }

    @NonNull
    @NotNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.feed_post, parent, false);

        return new FeedViewHolder(v, mOnFeedItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull FeedViewHolder holder, int position) {
        mAuth = FirebaseAuth.getInstance();
        mPostStorageRef = FirebaseStorage.getInstance().getReference("PostPics");
        mUserStorageRef = FirebaseStorage.getInstance().getReference("ProfilePic");
        UsersDatabaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        PostsDatabaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts");
        Posts post = mList.get(position);
        holder.setPost_pic(post.getPost_pic());
        holder.setCaption(post.getCaption());
        holder.setCreation_date(post.getCreation_date());
        UsersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (post.getUser_id().equals(userSnapshot.getKey())) {
                        Users user = userSnapshot.getValue(Users.class);
                        holder.setProfile_pic(user.getProfile_pic());
                        holder.setUser_holder(user.getUsername());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        holder.profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnFeedItemClickListener.OnProfilePicClick(holder.getAbsoluteAdapterPosition());
            }
        });

        holder.user_holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnFeedItemClickListener.OnProfilePicClick(holder.getAbsoluteAdapterPosition());
            }
        });

        holder.comments_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddComment.class);
                intent.putExtra("post", post);
                context.startActivity(intent);
            }
        });

        holder.like_pic.setOnClickListener(new View.OnClickListener() {
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

        holder.share_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                Uri subject = Uri.parse(post.getPost_pic());
//                String shareBody = post.getCaption();
                sendIntent.putExtra(Intent.EXTRA_STREAM, subject);
//                sendIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                sendIntent.setType("image/*");
                context.startActivity(Intent.createChooser(sendIntent, "COX cei ala COX"));
            }
        });

        PostsDatabaseReference.child(post.getUser_id()).child(post.getPost_id()).child("Likes").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
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

        PostsDatabaseReference.child(post.getUser_id()).child(post.getPost_id()).child("Likes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int count = (int) snapshot.getChildrenCount();
                    holder.setPostLikes(count);
                } else
                    holder.setPostLikes(0);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class FeedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView user_holder;
        private TextView creation_date;
        private TextView caption;
        private TextView postLikes;
        private CircleImageView profile_pic;
        private ImageView post_pic, like_pic, comments_pic, share_post;
        OnFeedItemClickListener onFeedItemClickListener;
        View mView;

        public FeedViewHolder(@NonNull @NotNull View itemView, OnFeedItemClickListener onFeedItemClickListener) {
            super(itemView);
            mView = itemView;
            this.onFeedItemClickListener = onFeedItemClickListener;
            like_pic = mView.findViewById(R.id.like_btn);
            comments_pic = mView.findViewById(R.id.feed_comments_post);
            share_post = mView.findViewById(R.id.share_post);
            profile_pic = mView.findViewById(R.id.feed_profile_pic);
            user_holder = mView.findViewById(R.id.feed_user_holder);
            itemView.setOnClickListener(this);
        }

        public void setPostLikes(int count) {
            postLikes = mView.findViewById(R.id.like_count_tv);
            postLikes.setText(count + " Likes");
        }

        public void setPost_pic(String urlPost) {
            post_pic = mView.findViewById(R.id.feed_post_pic);
            Glide.with(context).load(urlPost).into(post_pic);
        }

        public void setProfile_pic(String urlProfile) {
            Glide.with(context).load(urlProfile).into(profile_pic);
        }

        public void setUser_holder(String Username) {
            user_holder.setText(Username);
        }

        public void setCreation_date(String date) {
            creation_date = mView.findViewById(R.id.feed_creation_date);
            creation_date.setText(date);
        }

        public void setCaption(String post_caption) {
            caption = mView.findViewById(R.id.feed_post_caption);
            caption.setText(post_caption);
        }

        @Override
        public void onClick(View v) {
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation((Activity) context, post_pic, "robot");
            onFeedItemClickListener.OnFeedItemClick(getAbsoluteAdapterPosition(), options);
        }
    }

    public interface OnFeedItemClickListener {
        void OnFeedItemClick(int position, ActivityOptions options);
        void OnProfilePicClick(int position);
    }

}
