package com.example.protos.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.protos.Model.Comments;
import com.example.protos.Model.Users;
import com.example.protos.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {

    private Activity context;
    DatabaseReference UsersDatabaseReference;
    private List<Comments> commentsList;

    public CommentsAdapter(Activity context, List<Comments> commentsList) {
        this.context = context;
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

        Comments comments = commentsList.get(position);
            holder.setmComment(comments.getComment());

        UsersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (comments.getUser_id().equals(userSnapshot.getKey())) {
                        Users user = userSnapshot.getValue(Users.class);
                        holder.setProfile_pic(user.getProfile_pic());
                        holder.setUser_holder(user.getUsername()+":");
                    }
                }
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

    public class CommentsViewHolder extends RecyclerView.ViewHolder {
        TextView mComment;
        TextView username;
        CircleImageView profile_pic;
        View mView;
        public CommentsViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setmComment(String comment){
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
}
