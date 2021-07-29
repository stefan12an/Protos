package com.example.protos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.app.Notification;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.androidisland.views.ArcBottomNavigationView;
import com.example.protos.Fragments.HomeFragment;
import com.example.protos.Fragments.ProfileFragment;
import com.example.protos.Fragments.SearchFragment;
import com.example.protos.Fragments.UploadFragment;
import com.example.protos.Model.Users;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import nl.joery.animatedbottombar.AnimatedBottomBar;

import static android.content.ContentValues.TAG;

public class Profile extends AppCompatActivity {
    private FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    BottomNavigationView bottomNavigationView;
    AnimatedBottomBar meow;
    private final static int ID_HOME = 1;
    private final static int ID_UPLOAD = 2;
    private final static int ID_PROFILE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        meow = (AnimatedBottomBar) findViewById(R.id.bottomNavigationView);
        databaseReference = FirebaseDatabase.getInstance("https://protos-dde67-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        //bottomNavigationView = findViewById(R.id.bottomNavigationView);
        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new HomeFragment()).commit();
        //bottomNavigationView.setOnItemSelectedListener(navListner);
        getSupportActionBar().setTitle("Protos");
        meow.setOnTabSelected(new Function1<AnimatedBottomBar.Tab, Unit>() {
            @Override
            public Unit invoke(AnimatedBottomBar.Tab tab) {
                Fragment selectedFragment = null;
                switch (tab.getId()) {
                    case R.id.miSearch:
                        getSupportActionBar().setElevation(4);
                        selectedFragment = new SearchFragment();
                        break;
                    case R.id.miHome:
                        getSupportActionBar().setElevation(4);
                        selectedFragment = new HomeFragment();
                        getSupportActionBar().setTitle("Protos");
                        break;
                    case R.id.miUpload:
                        getSupportActionBar().setElevation(4);
                        selectedFragment = new UploadFragment();
                        getSupportActionBar().setTitle("Protos");
                        break;
                    case R.id.miProfile:
                        getSupportActionBar().setElevation(0);
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
                        selectedFragment = new ProfileFragment();
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, selectedFragment).commit();
                return null;
            }
        });
    }
//    private NavigationBarView.OnItemSelectedListener navListner =
//            new NavigationBarView.OnItemSelectedListener() {
//                @Override
//                public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
//                    Fragment selectedFragment = null;
//                    switch (item.getItemId()) {
//                        case R.id.miHome:
//                            selectedFragment = new HomeFragment();
//                            getSupportActionBar().setTitle("Protos");
//                            break;
//                        case R.id.miUpload:
//                            selectedFragment = new UploadFragment();
//                            getSupportActionBar().setTitle("Protos");
//                            break;
//                        case R.id.miProfile:
//                            ValueEventListener postListener = new ValueEventListener() {
//                                @Override
//                                public void onDataChange(DataSnapshot dataSnapshot) {
//                                    // Get Post object and use the values to update the UI
//                                    Users post = dataSnapshot.getValue(Users.class);
//                                    getSupportActionBar().setTitle(post.getUsername());
//                                    // ..
//                                }
//
//                                @Override
//                                public void onCancelled(DatabaseError databaseError) {
//                                    // Getting Post failed, log a message
//                                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
//                                }
//                            };
//                            databaseReference.child(mAuth.getUid()).addValueEventListener(postListener);
//                            selectedFragment = new ProfileFragment();
//                            break;
//                    }
//                    getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, selectedFragment).commit();
//                    return true;
//                }
//            };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.notifications:
                startActivity(new Intent(Profile.this, NotificationActivity.class));
                break;
            case R.id.settings:
                startActivity(new Intent(Profile.this, Settings.class));
                break;
            case R.id.logout:
                mAuth.signOut();
                startActivity(new Intent(Profile.this, LoginActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}