package com.example.protos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.protos.Fragments.HomeFragment;
import com.example.protos.Fragments.NotificationFragment;
import com.example.protos.Fragments.ProfileFragment;
import com.example.protos.Fragments.SearchFragment;
import com.example.protos.Model.Users;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import nl.joery.animatedbottombar.AnimatedBottomBar;

import static android.content.ContentValues.TAG;

public class Profile extends AppCompatActivity {
    private FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    BottomNavigationView bottomNavigationView;
    AnimatedBottomBar meow;
    Fragment selectedFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        meow = (AnimatedBottomBar) findViewById(R.id.bottomNavigationView);
        databaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new HomeFragment()).commit();
        getSupportActionBar().setTitle("Protos");
        meow.setOnTabSelected(new Function1<AnimatedBottomBar.Tab, Unit>() {
            @Override
            public Unit invoke(AnimatedBottomBar.Tab tab) {
                selectedFragment = null;
                switch (tab.getId()) {
                    case R.id.miSearch:
                        getSupportActionBar().setElevation(4);
                        selectedFragment = new SearchFragment();
                        break;
                    case R.id.miHome:
                        getSupportActionBar().setElevation(4);
                        selectedFragment = new HomeFragment();
                        break;
                    case R.id.miNotifications:
                        getSupportActionBar().setElevation(4);
                        selectedFragment = new NotificationFragment();
                        break;
                    case R.id.miProfile:
                        getSupportActionBar().setElevation(0);
                        selectedFragment = new ProfileFragment();
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, selectedFragment).commit();

                if(selectedFragment instanceof ProfileFragment) {
                    ValueEventListener postListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get Post object and use the values to update the UI
                            Users post = dataSnapshot.getValue(Users.class);
                            getSupportActionBar().setTitle(post.getUsername());
                            // ..
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Getting Post failed, log a message
                            Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                        }
                    };
                    databaseReference.child(mAuth.getUid()).addValueEventListener(postListener);
                }else{
                    getSupportActionBar().setTitle("Protos");
                }
                    return null;
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
            case R.id.settings:
                startActivity(new Intent(Profile.this, Settings.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}