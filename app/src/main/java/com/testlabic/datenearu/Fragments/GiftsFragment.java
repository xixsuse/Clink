package com.testlabic.datenearu.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jpardogo.android.googleprogressbar.library.GoogleProgressBar;
import com.testlabic.datenearu.ClickedUser;
import com.testlabic.datenearu.Models.ModelGift;
import com.testlabic.datenearu.Models.ModelNotification;
import com.testlabic.datenearu.R;
import com.testlabic.datenearu.Utils.Constants;

import java.util.Date;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 */
public class GiftsFragment extends Fragment {
    
    
    private DatabaseReference reference;
    private ChildEventListener listener;
    private View rootView;
    private SwipeMenuListView listView;
    private GoogleProgressBar bar;
    private FirebaseListAdapter<ModelGift> adapter;
    public GiftsFragment() {
        // Required empty public constructor
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_gifts, container, false);
        listView = rootView.findViewById(R.id.listView);
        bar = rootView.findViewById(R.id.progress_bar);
        MoveGiftsToRead();
        return rootView;
    }
    private void MoveGiftsToRead() {
        
        reference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.Gifts)
                .child(Constants.uid).child(Constants.unread);
        listener =  reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.getValue() != null) {
                    ModelGift notification = dataSnapshot.getValue(ModelGift.class);
                    String pushKey = dataSnapshot.getKey();
                    if (notification != null&&pushKey != null) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                .child(Constants.Gifts)
                                .child(Constants.uid).child(Constants.read).child(pushKey);
                        
                        reference.setValue(notification).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                
                                dataSnapshot.getRef().setValue(null);
                                
                            }
                        });
                    }
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
                
                bar.setVisibility(View.GONE);
                
                Query query = FirebaseDatabase.getInstance().getReference()
                        .child(Constants.Gifts)
                        .child(Constants.uid)
                        .child(Constants.read).orderByChild("timeStamp");
                FirebaseListOptions<ModelGift> options = new FirebaseListOptions.Builder<ModelGift>()
                        .setQuery(query, ModelGift.class)
                        .setLayout(R.layout.sample_notif)
                        .build();
                adapter = new FirebaseListAdapter<ModelGift>(options) {
                    @Override
                    protected void populateView(View v, ModelGift model, int position) {
                        
                        TextView txt = v.findViewById(R.id.txt);
                       
                        
                            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/SF-Pro-Display-Regular.otf");
                            txt.setTypeface(tf);
                            txt.setTextColor(getResources().getColor(R.color.black));
                        
                        TextView time = v.findViewById(R.id.time);
                        ImageView photo = v.findViewById(R.id.image);
                        
                        String message = model.getGiftSendersName() + " has sent you a "+ model.getGiftType() + " Tap to view profile!";
                        
                        txt.setText(message);
                        time.setText(setTime((Long) model.getTimeStamp().get(Constants.timeStamp)));
                        Glide.with(getActivity()).load(model.getGiftSendersImageUrl()).into(photo);
                    }
                };
                listView.setAdapter(adapter);
                setSwipeMenu();
                adapter.startListening();
            }
            private String setTime(long timestampCreatedLong) {
                Date dateObj = new Date(timestampCreatedLong);
                long epoch = dateObj.getTime();
                CharSequence teste = DateUtils.getRelativeTimeSpanString(epoch, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
                return (String) teste;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
            
        });
    }
    
    private void setSwipeMenu() {
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            
            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(getResources().getColor(R.color.sweet_green)));
                // set item width
                openItem.setWidth((180));
                // set item title
                openItem.setTitle("See Profile");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);
                
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth((180));
                // set a icon
                deleteItem.setIcon(R.drawable.ic_action_del);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

// set creator
        listView.setMenuCreator(creator);
        listView.setSwipeDirection(SwipeMenuListView.DIRECTION_RIGHT);
        
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // show the profile of the request sender
                        Intent i = new Intent(getActivity(), ClickedUser.class);
                        i.putExtra(Constants.comingFromNotif, true);
                        i.putExtra(Constants.clickedUid,adapter.getItem(position).getGiftSendersUid());
                        startActivity(i);
                        // Toast.makeText(getActivity(), "Accepting request!", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        // delete
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
        
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listView.smoothOpenMenu(position);
                
            }
        });
        
    }
    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null)
            adapter.startListening();
    }
    
    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null)
            adapter.stopListening();
        
        if(listener!=null)
            reference.removeEventListener(listener);
    }
    
}