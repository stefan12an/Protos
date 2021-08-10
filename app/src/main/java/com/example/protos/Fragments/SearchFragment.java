package com.example.protos.Fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.protos.Adapter.SearchAdapter;
import com.example.protos.Model.Users;
import com.example.protos.PostActivity;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class SearchFragment extends Fragment implements SearchAdapter.OnSearchItemClickListener {
    private FirebaseAuth mAuth;
    private StorageReference mPostStorageRef;
    private StorageReference mUserStorageRef;
    private DatabaseReference UsersDatabaseReference;
    private DatabaseReference PostsDatabaseReference;
    private List<Users> mList;
    private SearchAdapter searchAdapter;
    private RecyclerView recyclerView;
    private SearchView searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        mAuth = FirebaseAuth.getInstance();
        mPostStorageRef = FirebaseStorage.getInstance().getReference("PostPics");
        mUserStorageRef = FirebaseStorage.getInstance().getReference("ProfilePic");
        UsersDatabaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        PostsDatabaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts");
        recyclerView = view.findViewById(R.id.searchRecyclerView);
        searchView = view.findViewById(R.id.searchView);
        mList = new ArrayList<>();
        searchAdapter = new SearchAdapter(getContext(), mList, this);
        recyclerView.setAdapter(searchAdapter);
        UsersDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        mList.add(ds.getValue(Users.class));
                    }
                }
                searchAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        if(searchView!= null){
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    search(newText);
                    return true;
                }
            });
        }
        return view;
    }

    private void search(String newText){
        ArrayList<Users> myList = new ArrayList<>();
        for(Users object:mList){
            if(object.getUsername().toLowerCase().contains(newText.toLowerCase())){
                myList.add(object);
            }
        }
        SearchAdapter searchAdapter = new SearchAdapter(getContext(),myList,this);
        recyclerView.setAdapter(searchAdapter);
    }

    @Override
    public void OnSearchItemClick(int position) {
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("user", mList.get(position));
        profileFragment.setArguments(bundle);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, profileFragment)
                .commit();
    }
}