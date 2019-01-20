package com.testlabic.datenearu.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabSelectListener;
import com.startapp.android.publish.adsCommon.StartAppAd;
import com.startapp.android.publish.adsCommon.StartAppSDK;
import com.testlabic.datenearu.ChatUtils.ChatMessage;
import com.testlabic.datenearu.MatchAlgoUtils.SeriousOrCasual;
import com.testlabic.datenearu.Models.ModelNotification;
import com.testlabic.datenearu.NewUserSetupUtils.LocationUpdateService;
import com.testlabic.datenearu.NewUserSetupUtils.NewUserSetup;
import com.testlabic.datenearu.R;
import com.testlabic.datenearu.TransitionUtils.pagerTransition;
import com.testlabic.datenearu.Utils.Constants;

import java.util.HashMap;

import com.testlabic.datenearu.Fragments.Messages;
import com.testlabic.datenearu.Fragments.NotificationFragment;
import com.testlabic.datenearu.Fragments.Profile;

public class MainActivity extends AppCompatActivity {
    
    private static final String TAG = MainActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    private BottomBar bottomBar;
    private int count = 0;
    private int messagesUnread = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //initialize the startappSDK
    
        StartAppSDK.init(this, "211455651", false);
        
        StartAppAd.disableSplash();
        
        bottomBar = findViewById(R.id.bottomBar);
        mAuth = FirebaseAuth.getInstance();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    
        boolean refresh = getIntent().getBooleanExtra(Constants.refresh, false);
       
        /*
        on create switch to the home fragment
         */
        
        changeFragment(new pagerTransition());
        
        //setCustomFontAndStyle(tabLayout1, 0);
        
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(int tabId) {
                
                if (tabId == R.id.tab_message) {
                    // switch to messages fragment
                    changeFragment(new Messages());
                    BottomBarTab nearby = bottomBar.getTabWithId(R.id.tab_message);
                    nearby.removeBadge();
                } else if (tabId == R.id.tab_home) {
                    // switch to messages fragment
                    changeFragment(new pagerTransition());
                } else if (tabId == R.id.tab_profile) {
                    // switch to messages fragment
                    changeFragment(new Profile());
                } else if (tabId == R.id.tab_notif) {
                    changeFragment(new NotificationFragment());
                    BottomBarTab nearby = bottomBar.getTabWithId(R.id.tab_notif);
                    nearby.removeBadge();
                }
                
            }
        });
        
    }
    
    private void checkForNotification() {
        count = 0;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.Notifications)
                .child(Constants.uid).child(Constants.unread);
        
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.getValue(ModelNotification.class) != null) {
                    count++;
                }
            }
            
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            
            }
            
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            
            }
            
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            
            }
        });
        
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        /*
                        change the notification icon.
                         */
                bottomBar = findViewById(R.id.bottomBar);
                BottomBarTab nearby = bottomBar.getTabWithId(R.id.tab_notif);
                if (count > 0) {
                    
                    nearby.setBadgeCount(count);

// Remove the badge when you're done with it.
                } else
                    nearby.removeBadge();
                
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            
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
        Constants.uid = FirebaseAuth.getInstance().getUid();
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                
                if (firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(MainActivity.this, SignIn.class));
                    finish();
                }
                else {
                    Constants.uid = firebaseAuth.getUid();
                    checkForNotification();
                    checkForNewMessages();
                    updateStatus(Constants.online);
                    //giveXPoints();
                }
                
            }
        });
        
    }
    
    private void checkForNewMessages() {
        messagesUnread = 0;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.CHATS + Constants.unread)
                .child(Constants.uid + Constants.unread);
        
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.getValue(ChatMessage.class) != null) {
                    messagesUnread++;
                }
            }
            
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            
            }
            
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            
            }
            
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            
            }
        });
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int unreads = (int) dataSnapshot.getChildrenCount();
                
                if (unreads > 0) {
                    BottomBarTab nearby = bottomBar.getTabWithId(R.id.tab_message);
                    if (messagesUnread > 0) {
        
                        nearby.setBadgeCount(messagesUnread);

// Remove the badge when you're done with it.
                    } else
                        nearby.removeBadge();
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            
            }
        });
    }
    
    @Override
    protected void onStart() {
        super.onStart();
       // startActivity(new Intent(MainActivity.this, MainActivity.class));
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        updateStatus(Constants.offline);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
       // updateStatus(Constants.offline);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(Constants.uid!=null)
        updateStatus(Constants.offline);
    }
    
    private void updateStatus(final String status) {
        HashMap<String, Object> updateStatus = new HashMap<>();
        updateStatus.put(Constants.status, status);
        if(Constants.uid==null)
            return;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.usersStatus)
                .child(Constants.uid);
        reference.updateChildren(updateStatus).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if(status.equals(Constants.online))
                {
                
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
    
    public void giveXPoints()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.xPoints)
                .child(Constants.uid);
        HashMap<String, Object> updatePoints = new HashMap<>();
        updatePoints.put(Constants.xPoints, 1000);
        reference.updateChildren(updatePoints);
    
    }
}










