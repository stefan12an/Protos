package com.example.protos.Adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.protos.Model.Posts;
import com.example.protos.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Posts> mList;
    private Context context;
    private FirebaseAuth mAuth;
    private StorageReference mPostStorageRef;
    private StorageReference mUserStorageRef;
    private DatabaseReference UsersDatabaseReference;
    private DatabaseReference PostsDatabaseReference;
    private OnProfileItemClickListener mOnProfileItemClickListener;

    public PostAdapter(Context context, List<Posts> mList, OnProfileItemClickListener onProfileItemClickListener) {
        this.mList = mList;
        this.context = context;
        this.mOnProfileItemClickListener = onProfileItemClickListener;
    }

    @NonNull
    @NotNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.each_post, parent, false);

        return new PostViewHolder(v, mOnProfileItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PostViewHolder holder, int position) {
        mAuth = FirebaseAuth.getInstance();
        mPostStorageRef = FirebaseStorage.getInstance().getReference("PostPics");
        mUserStorageRef = FirebaseStorage.getInstance().getReference("ProfilePic");
        UsersDatabaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        PostsDatabaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts");
        Posts post = mList.get(position);
        holder.setPost_pic(post.getPost_pic());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView post_pic;
        View mView;
        OnProfileItemClickListener onProfileItemClickListener;

        public PostViewHolder(@NonNull @NotNull View itemView, OnProfileItemClickListener onProfileItemClickListener) {
            super(itemView);
            mView = itemView;
            this.onProfileItemClickListener = onProfileItemClickListener;
            itemView.setOnClickListener(this);
        }

        public void setPost_pic(String urlPost) {
            post_pic = mView.findViewById(R.id.feed_post_pic);
            RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.rectangle);
            Glide.with(context).load(urlPost).apply(requestOptions).into(post_pic);
        }

        @Override
        public void onClick(View v) {
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation((Activity) context, post_pic, "robot");
            mOnProfileItemClickListener.OnProfileItemClick(getAbsoluteAdapterPosition(), options);
        }
    }
    public interface OnProfileItemClickListener{
        void OnProfileItemClick(int position, ActivityOptions options);
    }
}
