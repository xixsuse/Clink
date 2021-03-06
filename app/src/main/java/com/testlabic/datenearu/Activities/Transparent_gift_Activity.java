package com.testlabic.datenearu.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cooltechworks.views.ScratchImageView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jackpocket.scratchoff.ScratchoffController;
import com.skyfishjy.library.RippleBackground;
import com.testlabic.datenearu.ClickedUser;
import com.testlabic.datenearu.Models.ModelGift;
import com.testlabic.datenearu.R;
import com.testlabic.datenearu.Utils.Constants;

public class Transparent_gift_Activity extends Activity {

    ModelGift modelGift;
    ImageView imagePerson;
    ImageView royal_bottle;
    TextView namePerson;
    ImageView premium_bottle;
    RelativeLayout completeScreen;
    boolean tapTwice = false;
    ImageView regular_bottle;
    ImageView bottletype;
    DatabaseReference moveReadRef;
    ChildEventListener childEventListener;
    TextView points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transparent_gift_);
        modelGift = (ModelGift) getIntent().getSerializableExtra(Constants.giftModel);
        premium_bottle = findViewById(R.id.premium_bottle);
        points=findViewById(R.id.points);
        moveGiftToRead();
        completeScreen = findViewById(R.id.scratch_view_behind);
        completeScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.e("Trans", "The value of tap twice is "+ tapTwice);
                if (tapTwice) {
                    removeListeners();
                    finish();
                } else {
                    tapTwice = true;
                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tapTwice = false;
                        }
                    }, 2000);
                }
            }
        });
        regular_bottle = findViewById(R.id.royal_bottle);
        royal_bottle = findViewById(R.id.regular_bottle);
        bottletype=findViewById(R.id.bottletype);
        //scratchImageView=findViewById(R.id.sample_image);
        //scratchImageView = new ScratchImageView(this);
        imagePerson = findViewById(R.id.imageperson);
        namePerson = findViewById(R.id.nameperson);
        final RippleBackground rippleBackground = (RippleBackground)findViewById(R.id.content);
        rippleBackground.startRippleAnimation();
        String url = modelGift.getGiftSendersImageUrl();
        Glide.with(this).load(url).apply(RequestOptions.circleCropTransform()).into(imagePerson);
        String message = modelGift.getGiftSendersName() + " has sent you a gift (Tap to see Profile) ";

        namePerson.setText(message);
        namePerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Transparent_gift_Activity.this, ClickedUser.class);
                i.putExtra(Constants.comingFromNotif, true);
                i.putExtra(Constants.clickedUid, modelGift.getGiftSendersUid());
                finish();
                startActivity(i);
            }
        });
        imagePerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Transparent_gift_Activity.this, ClickedUser.class);
                i.putExtra(Constants.comingFromNotif, true);
                i.putExtra(Constants.clickedUid, modelGift.getGiftSendersUid());
                finish();
                startActivity(i);
            }
        });


        switch (modelGift.getGiftType())
        {
            case "Regular Wine" : bottletype.setImageResource(R.drawable.regularwine);

                points.setText("+200 drops");
                royal_bottle.setVisibility(View.GONE);
                premium_bottle.setVisibility(View.GONE);
                regular_bottle.setVisibility(View.VISIBLE);
                break;


            case "Premium Wine" : bottletype.setImageResource(R.drawable.premium_bottle);

                points.setText("+500 drops");
                royal_bottle.setVisibility(View.GONE);
                regular_bottle.setVisibility(View.GONE);
                premium_bottle.setVisibility(View.VISIBLE);
                break;


            case "Royal Wine" :bottletype.setImageResource(R.drawable.royal_bottle);

                points.setText("+1000 drops");
                premium_bottle.setVisibility(View.INVISIBLE);
                regular_bottle.setVisibility(View.INVISIBLE);
                royal_bottle.setVisibility(View.VISIBLE);

                break;


        }

        ScratchoffController controller = new ScratchoffController(this)
                .setThresholdPercent(0.20d)
                .setTouchRadiusDip(this, 30)
                .setFadeOnClear(true)
                .setClearOnThresholdReached(true)
                .setCompletionCallback(new Runnable() {
                    @Override
                    public void run() {

                        /*AlphaAnimation anim2 = new AlphaAnimation(0.0f, 1.0f);
                        switch (modelGift.getGiftType())
                        {
                            case "Regular Wine" : bottletype.setImageResource(R.drawable.regular_bottle);

                                anim2.setStartOffset(100);
                                anim2.setDuration(1000);
                                //anim1.setRepeatCount(10);
                                //anim1.setRepeatMode(Animation.ZORDER_BOTTOM);
                                //premium_bottle.startAnimation(anim2);
                                royal_bottle.setVisibility(View.GONE);
                                premium_bottle.setVisibility(View.GONE);
                                regular_bottle.setVisibility(View.VISIBLE);
                                break;


                            case "Premium Wine" : bottletype.setImageResource(R.drawable.premium_bottle);

                                anim2.setStartOffset(100);
                                anim2.setDuration(1000);
                                //anim1.setRepeatCount(10);
                                //anim1.setRepeatMode(Animation.ZORDER_BOTTOM);
                                //premium_bottle.startAnimation(anim2);
                                royal_bottle.setVisibility(View.GONE);
                                regular_bottle.setVisibility(View.GONE);
                                premium_bottle.setVisibility(View.VISIBLE);
                                break;


                            case "Royal Wine" :bottletype.setImageResource(R.drawable.royal_bottle);

                                anim2.setStartOffset(100);
                                anim2.setDuration(1000);
                                //anim1.setRepeatCount(10);
                                //anim1.setRepeatMode(Animation.ZORDER_BOTTOM);
                                //premium_bottle.startAnimation(anim2);
                                premium_bottle.setVisibility(View.INVISIBLE);
                                regular_bottle.setVisibility(View.INVISIBLE);
                                royal_bottle.setVisibility(View.VISIBLE);

                                break;


                        }*/

                        KonfettiView viewKonfetti = findViewById(R.id.viewKonfetti);
                        viewKonfetti.build()
                                .addColors(getApplicationContext().getResources().getColor(R.color.appcolor),
                                        getApplicationContext().getResources().getColor(R.color.yellow),
                                        getApplicationContext().getResources().getColor(R.color.appcolor))
                                .setDirection(0.0, 359.0)
                                .setSpeed(1f, 5f)
                                .setFadeOutEnabled(true)
                                .setTimeToLive(2000L)
                                .addShapes(Shape.RECT, Shape.CIRCLE)
                                .addSizes(new Size(12, 5))
                                .setPosition(-100f, 1000f, -50f, -50f)
                                .streamFor(300, 5000L);
                        
                        
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                removeListeners();
                                finish();
                            }
                        }, 8000);
                    }
                })
                .attach(findViewById(R.id.scratch_view), findViewById(R.id.scratch_view_behind));
       

    }
    private void removeListeners() {
    
        moveReadRef.removeEventListener(childEventListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        removeListeners();
    }

    private void moveGiftToRead() {
        moveReadRef = FirebaseDatabase.getInstance().getReference()
                .child(Constants.Gifts)
                .child(Constants.uid).child(Constants.unread);


        childEventListener = moveReadRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.getValue() != null) {
                    ModelGift notification = dataSnapshot.getValue(ModelGift.class);
                    String pushKey = dataSnapshot.getKey();
                    if (notification != null && pushKey != null) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                .child(Constants.Gifts)
                                .child(Constants.uid).child(Constants.read).child(pushKey);
                        //Log.e("Trans", "THe ref to push to reaad" + reference);
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
    }
}
