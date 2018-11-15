package com.testlabic.datenearu.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.testlabic.datenearu.R;

import Fragments.Home;
import Fragments.Messages;
import Fragments.Profile;
import adapter.TablayoutAdapter_Home;

public class MainActivity extends AppCompatActivity {
    
    private FirebaseAuth mAuth;
    private BottomBar bottomBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomBar = findViewById(R.id.bottomBar);
        mAuth = FirebaseAuth.getInstance();
        
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

       
        /*
        on create switch to the home fragment
         */
        
        changeFragment(new Home());
        
        //setCustomFontAndStyle(tabLayout1, 0);
        
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(int tabId) {
                
                if (tabId == R.id.tab_message) {
                    // switch to messages fragment
                    changeFragment(new Messages());
                } else if (tabId == R.id.tab_home) {
                    // switch to messages fragment
                    changeFragment(new Home());
                }

                else if (tabId == R.id.tab_profile) {
                    // switch to messages fragment
                    changeFragment(new Profile());
                }
                
            }
        });
        
    }
    
    public void changeFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_container, fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                
                if (firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(MainActivity.this, SignIn.class));
                    finish();
                }
                
            }
        });
        
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        
        /*
        move to home fragment if on any other fragment, else exit
         */
        
        
    }
}










