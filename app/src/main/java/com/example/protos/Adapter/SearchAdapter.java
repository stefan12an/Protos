package com.example.protos.Adapter;

import android.app.ActivityOptions;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.protos.Model.Posts;
import com.example.protos.Model.Users;
import com.example.protos.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private List<Users> mList;
    private Context context;
    private FirebaseAuth mAuth;
    private StorageReference mPostStorageRef;
    private StorageReference mUserStorageRef;
    private DatabaseReference UsersDatabaseReference;
    private DatabaseReference PostsDatabaseReference;
    OnSearchItemClickListener mOnSearchItemClickListener;

    public SearchAdapter(Context context, List<Users> mList, OnSearchItemClickListener onSearchItemClickListener){
        this.mList = mList;
        this.context = context;
        this.mOnSearchItemClickListener = onSearchItemClickListener;
    }

    @NonNull
    @NotNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.user_card, parent, false);

        return new SearchViewHolder(v, mOnSearchItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SearchViewHolder holder, int position) {
        mAuth = FirebaseAuth.getInstance();
        mPostStorageRef = FirebaseStorage.getInstance().getReference("PostPics");
        mUserStorageRef = FirebaseStorage.getInstance().getReference("ProfilePic");
        UsersDatabaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        PostsDatabaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts");
        Users user = mList.get(position);
        holder.setProfile_pic(user.getProfile_pic());
        holder.setSearchUsername(user.getUsername());
        holder.setDescription(user.getDate_of_birth());

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        OnSearchItemClickListener mOnSearchItemClickListener;
        CircleImageView profile_pic;
        TextView username,description;
        View mView;
        public SearchViewHolder(@NonNull @NotNull View itemView, OnSearchItemClickListener mOnSearchItemClickListener) {
            super(itemView);
            mView = itemView;
            this.mOnSearchItemClickListener = mOnSearchItemClickListener;
            itemView.setOnClickListener(this);

        }

        public void setProfile_pic(String urlPic) {
            profile_pic = mView.findViewById(R.id.searchProfilePic);
            Glide.with(context).load(urlPic).into(profile_pic);
        }

        public void setSearchUsername(String user){
            username = mView.findViewById(R.id.searchUsername);
            username.setText(user);
        }

        public void setDescription(String description){
            this.description = mView.findViewById(R.id.searchDescription);
            this.description.setText(description);
        }

        @Override
        public void onClick(View v) {
            mOnSearchItemClickListener.OnSearchItemClick(getAbsoluteAdapterPosition());
        }

    }
    public interface OnSearchItemClickListener {
        void OnSearchItemClick(int position);
    }
}
