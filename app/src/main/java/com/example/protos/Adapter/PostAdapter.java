package com.example.protos.Adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.protos.Model.Posts;
import com.example.protos.Model.Users;
import com.example.protos.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Posts> mList;
    private Context context;
    private FirebaseAuth mAuth;
    private StorageReference mPostStorageRef;
    private StorageReference mUserStorageRef;
    private DatabaseReference UsersDatabaseReference;
    private DatabaseReference PostsDatabaseReference;
    public PostAdapter(Context context, List<Posts> mList) {
        this.mList = mList;
        this.context = context;
    }

    @NonNull
    @NotNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.each_post, parent, false);

        return new PostViewHolder(v);
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
        holder.setCaption(post.getCaption());
        holder.setCreation_date(post.getCreation_date());
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);
                holder.setProfile_pic(user.getProfile_pic());
                holder.setUser_holder(user.getUsername());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        UsersDatabaseReference.child(mAuth.getUid()).addValueEventListener(postListener);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        private TextView user_holder;
        private TextView creation_date;
        private TextView caption;
        private CircleImageView profile_pic;
        private ImageView post_pic;
        View mView;

        public PostViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setPost_pic(String urlPost) {
            post_pic = mView.findViewById(R.id.post_pic);
            Glide.with(context).load(urlPost).into(post_pic);
        }

        public void setProfile_pic(String urlProfile) {
            profile_pic = mView.findViewById(R.id.profile_pic);
            Glide.with(context).load(urlProfile).into(profile_pic);
        }

        public void setUser_holder(String Username) {
            user_holder = mView.findViewById(R.id.user_holder);
            user_holder.setText(Username);
        }

        public void setCreation_date(String date) {
            creation_date = mView.findViewById(R.id.creation_date);
            creation_date.setText(date);
        }

        public void setCaption(String post_caption) {
            caption = mView.findViewById(R.id.post_caption);
            caption.setText(post_caption);
        }
    }
}
