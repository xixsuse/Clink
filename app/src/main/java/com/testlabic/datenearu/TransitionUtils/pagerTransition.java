package com.testlabic.datenearu.TransitionUtils;

import android.app.Activity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;
import com.startapp.android.publish.ads.banner.Banner;
import com.takusemba.spotlight.OnSpotlightStateChangedListener;
import com.takusemba.spotlight.OnTargetStateChangedListener;
import com.takusemba.spotlight.Spotlight;
import com.takusemba.spotlight.shape.Circle;
import com.takusemba.spotlight.target.SimpleTarget;
import com.testlabic.datenearu.Activities.SignIn;
import com.testlabic.datenearu.ArchitectureUtils.ViewModels.CityLabelModel;
import com.testlabic.datenearu.ArchitectureUtils.ViewModels.PointLabelModel;
import com.testlabic.datenearu.ArchitectureUtils.ViewModels.PrefsModel;
import com.testlabic.datenearu.BillingUtils.PurchasePacks;
import com.testlabic.datenearu.Models.LatLong;
import com.testlabic.datenearu.Models.ModelPrefs;
import com.testlabic.datenearu.Models.ModelSubscr;
import com.testlabic.datenearu.Models.ModelUser;
import com.testlabic.datenearu.R;
import com.testlabic.datenearu.Utils.Constants;
import com.testlabic.datenearu.Utils.Levenshtein;
import com.testlabic.datenearu.Utils.locationUpdater;
import com.testlabic.datenearu.WaveDrawable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Aviharsh on 2016/9/18.
 */
public class pagerTransition extends Fragment {
    
    private static final String TAG = pagerTransition.class.getSimpleName();
    private TextView indicatorTv;
    private ViewPager viewPager;
    private View rootView;
    private HashMap<String, Object> updateMap = null;
    private HashMap<String, Object> updateMinAge;
    private HashMap<String, Object> updateMaxAge;
    private ShowcaseView showcaseView;
    private int counter = 0;
    private String interestedGender;
    private List<CommonFragment> fragments = new ArrayList<>(); // 供ViewPager使用
    private ArrayList<ModelUser> displayArrayList;
    private TextView changeLocation;
    private FragmentStatePagerAdapter adapter;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ImageButton filterList;
    private LatLong currentUsersLatLong;
    private ModelPrefs prefs;
    private TextView points;
    private ImageView bottle;
    private DatabaseReference ref;
    private String curUsersMatchSeq;
    private Boolean fetchPrefsCalledOnce = false;
    private ImageView emptyView;
    private ChildEventListener childEventListener;
    private String city;
    private ImageView emptyViewno;
    private TextView emptyViewtext;
    private ArrayList<String> contactsUid;
    
    public pagerTransition() {
        // Required empty public constructor
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        
        rootView = inflater.inflate(R.layout.activity_pager_transition, viewPager, false);
        contactsUid = new ArrayList<>();
        sharedPreferences = getActivity().getSharedPreferences(Constants.userDetailsOff, MODE_PRIVATE);
        interestedGender = sharedPreferences.getString(Constants.userIntrGender, "female");
        curUsersMatchSeq = sharedPreferences.getString(Constants.matchAlgo, "ABCDEF");
        city = sharedPreferences.getString(Constants.cityLabel, null);
        editor = sharedPreferences.edit();
        filterList = rootView.findViewById(R.id.filter);
        ImageView hideAd = rootView.findViewById(R.id.hideAd);
        changeLocation = rootView.findViewById(R.id.changeLocation);
        points = rootView.findViewById(R.id.points);
        bottle = rootView.findViewById(R.id.fill_bottle);
        emptyView = rootView.findViewById(R.id.emptyView);
        emptyViewno = rootView.findViewById(R.id.emptyViewno);
        emptyViewtext = rootView.findViewById(R.id.emptyViewtext);

        bottle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(),PurchasePacks.class));
            }
        });
        Drawable mWaveDrawable = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mWaveDrawable = new WaveDrawable(getContext().getDrawable(R.drawable.nohere));
        }
        ((WaveDrawable) mWaveDrawable).setWaveAmplitude(30);
        ((WaveDrawable) mWaveDrawable).setWaveLength(580);
        ((WaveDrawable) mWaveDrawable).setWaveSpeed(12);
        //((WaveDrawable) mWaveDrawable).setLevel(20);
        ((WaveDrawable) mWaveDrawable).setIndeterminate(true);
        emptyView.setImageDrawable(mWaveDrawable);
        
        emptyView.setVisibility(View.VISIBLE);
        Constants.uid = FirebaseAuth.getInstance().getUid();
        if (Constants.uid != null) {
            setUpPoints();
            fetchPreferences();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.Messages)
                    .child(Constants.uid)
                    .child(Constants.contacts);
            reference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    if(dataSnapshot.getKey()!=null)
                        contactsUid .add(String.valueOf(dataSnapshot.getKey()));
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
                
                }
    
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
        
                }
            });
        }
        
        changeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), locationUpdater.class), 87);
            }
        });
        
        filterList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog();
            }
        });
        
        hideAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Banner banner = rootView.findViewById(R.id.startAppBanner);
                banner.hideBanner();
            }
        });
        
        setShowcaseView();
        
        //setupCityLabelLocation
        
        // Obtain a new or prior instance of ViewModel from the
        // ViewModelProviders utility class.
        if (Constants.uid != null) {
            CityLabelModel viewModel = ViewModelProviders.of(this).get(CityLabelModel.class);
            
            LiveData<DataSnapshot> liveData = viewModel.getDataSnapshotLiveData();
            
            liveData.observe(this, new Observer<DataSnapshot>() {
                @Override
                public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        // Log.e(TAG, "Setting value using architecture " + dataSnapshot.getValue());
                        if (dataSnapshot.getValue() != null) {
                            String value = dataSnapshot.getValue(String.class);
                            
                            if (value != null) {
                                editor.putString(Constants.cityLabel, value).apply();
                                changeLocation.setText(Constants.decrypt(value));
                            }
                        }
                    }
                }
            });
        }
        return rootView;
    }
    
    private void nothere(boolean b) {
        if (b) {
            emptyViewtext.setVisibility(View.VISIBLE);
            emptyViewno.setVisibility(View.VISIBLE);
        }
        else
        {
            emptyViewtext.setVisibility(View.INVISIBLE);
            emptyViewno.setVisibility(View.INVISIBLE);
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        //Log.e(TAG, "Paused pagerTransition");
        cleanup();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 87 && resultCode == Activity.RESULT_OK) {
            //Log.e(TAG, "onActivityResult!");
            boolean refresh = data.getBooleanExtra(Constants.refresh, false);
            if (refresh) {
                fetchPrefsCalledOnce = false;
                fetchPreferences();
            }
        }
        
    }
    
    public void setShowcaseView() {
        final SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean isFirstRun = wmbPreference.getBoolean("SHOW18", true);
        if (isFirstRun) {//make isFirstRun
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    //first target
                    int[] imagelocation1 = {0, 0};
                    filterList.getLocationInWindow(imagelocation1);
                    final float imageX1 = imagelocation1[0] + filterList.getMeasuredWidth() / 2f;
                    final float imageY1 = imagelocation1[1] + filterList.getMeasuredHeight() / 2f;
                    final float imageRadius1 = 100f;
                    //Log.e(TAG, String.valueOf(filterList.getMeasuredWidth()) + "  " + String.valueOf(imageY));
                    final SimpleTarget simpleTarget1 = new SimpleTarget.Builder((Activity) getContext())
                            .setPoint(imageX1, imageY1)
                            .setShape(new Circle(100f)) // or RoundedRectangle()
                            .setTitle(getString(R.string.filter_showcase))
                            .setDescription(getString(R.string.filters_desc))
                            .setOverlayPoint(100f, imageY1 + imageRadius1 + 100f)
                            .setOnSpotlightStartedListener(new OnTargetStateChangedListener<SimpleTarget>() {
                                @Override
                                public void onStarted(SimpleTarget target) {
                                    // do something
                                }
                                
                                @Override
                                public void onEnded(SimpleTarget target) {
                                    // do something
                                }
                            })
                            .build();
                    //second target
                    int[] imagelocation2 = {0, 0};
                    changeLocation.getLocationInWindow(imagelocation2);
                    final float imageX2 = imagelocation2[0] + changeLocation.getMeasuredWidth() / 2f;
                    final float imageY2 = imagelocation2[1] + changeLocation.getMeasuredHeight() / 2f;
                    final float imageRadius2 = 200f;
                    final SimpleTarget simpleTarget2 = new SimpleTarget.Builder((Activity) getContext())
                            .setPoint(imageX2, imageY2)
                            .setShape(new Circle(100f)) // or RoundedRectangle()
                            .setTitle(getString(R.string.location_title))
                            .setDescription(getString(R.string.location_description))
                            .setOverlayPoint(100f, imageY2 + imageRadius2 + 100f)
                            .setOnSpotlightStartedListener(new OnTargetStateChangedListener<SimpleTarget>() {
                                @Override
                                public void onStarted(SimpleTarget target) {
                                    // do something
                                }
                                
                                @Override
                                public void onEnded(SimpleTarget target) {
                                    // do something
                                }
                            })
                            .build();
                    //third target
                    int[] imagelocation3 = {0, 0};
                    bottle.getLocationInWindow(imagelocation3);
                    final float imageX3 = imagelocation3[0] + bottle.getMeasuredWidth() / 2f;
                    final float imageY3 = imagelocation3[1] + bottle.getMeasuredHeight() / 2f;
                    final float imageRadius3 = 400f;
                    final SimpleTarget simpleTarget3 = new SimpleTarget.Builder((Activity) getContext())
                            .setPoint(imageX3, imageY3)
                            .setShape(new Circle(100f)) // or RoundedRectangle()
                            .setTitle(getString(R.string.drops_showcase))
                            .setDescription(getString(R.string.drops_description))
                            .setOverlayPoint(100f, imageY3 + imageRadius3 + 100f)
                            .setOnSpotlightStartedListener(new OnTargetStateChangedListener<SimpleTarget>() {
                                @Override
                                public void onStarted(SimpleTarget target) {
                                    // do something
                                }
                                
                                @Override
                                public void onEnded(SimpleTarget target) {
                                    // do something
                                }
                            })
                            .build();
                    Spotlight.with((Activity) getContext())
                            .setOverlayColor(R.color.background)
                            .setDuration(1000L)
                            .setAnimation(new DecelerateInterpolator(2f))
                            .setTargets(simpleTarget1, simpleTarget2, simpleTarget3)
                            .setClosedOnTouchedOutside(true)
                            .setOnSpotlightStateListener(new OnSpotlightStateChangedListener() {
                                @Override
                                public void onStarted() {
                                    //Toast.makeText(getContext(), "spotlight is started", Toast.LENGTH_SHORT).show();
                                }
                                
                                @Override
                                public void onEnded() {
                                    SharedPreferences.Editor editor = wmbPreference.edit();
                                    editor.putBoolean("SHOW18", false).apply();
                                    editor.putBoolean("SHOW20", true).apply();
                                    //Toast.makeText(getContext(), "spotlight is ended", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .start();
                }
            });
            
        }
        
    }
    
    private void setUpPoints() {
        PointLabelModel viewModel = ViewModelProviders.of(this).get(PointLabelModel.class);
        LiveData<DataSnapshot> liveData = viewModel.getDataSnapshotLiveData();
        
        liveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                ModelSubscr modelSubscr = null;
                if (dataSnapshot != null) {
                    modelSubscr = dataSnapshot.getValue(ModelSubscr.class);
                }
                if (modelSubscr != null) {
                    int current = modelSubscr.getXPoints();
                    String set = String.valueOf(current) + " drops";
                    points.setText(set);
                    
                    //code for bottle image
                    /*if (current >= 0 && current <= 50) {
                        bottle.setImageResource(R.drawable.bottle_1);
                    } else if (current > 50 && current <= 100) {
                        bottle.setImageResource(R.drawable.bottle_2);
                    } else if (current > 100 && current <= 150) {
                        bottle.setImageResource(R.drawable.bottle_3);
                    } else if (current > 150 && current <= 200) {
                        bottle.setImageResource(R.drawable.bottle_5);
                    } else if (current > 250 && current <= 300) {
                        bottle.setImageResource(R.drawable.bottle_5);
                    } else if (current > 350 && current <= 400) {
                        bottle.setImageResource(R.drawable.bottle_6);
                    } else if (current > 450 && current <= 500) {
                        bottle.setImageResource(R.drawable.bottle_7);
                    } else if (current > 550 && current <= 600) {
                        bottle.setImageResource(R.drawable.bottle_8);
                    } else if (current > 650 && current <= 700) {
                        bottle.setImageResource(R.drawable.bottle_9);
                    } else if (current > 750 && current <= 800) {
                        bottle.setImageResource(R.drawable.bottle_10);
                    } else if (current > 850 && current <= 900) {
                        bottle.setImageResource(R.drawable.bottle_11);
                    } else if (current > 950 && current <= 1000) {
                        bottle.setImageResource(R.drawable.bottle_12);
                    } else if (current > 1050 && current <= 1100) {
                        bottle.setImageResource(R.drawable.bottle_13);
                    } else if (current > 1150 && current <= 1200) {
                        bottle.setImageResource(R.drawable.bottle_14);
                    } else if (current > 1250 && current <= 1300) {
                        bottle.setImageResource(R.drawable.bottle_15);
                    } else {
                        bottle.setImageResource(R.drawable.bottle_16);
                    }*/
                    
                    points.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            
                            startActivity(new Intent(getActivity(), PurchasePacks.class));
                        }
                    });
                }
            }
        });
        
    }
    
    private void fetchPreferences() {
        
        // Log.e(TAG, "The boolean is "+fetchPrefsCalledOnce);
        PrefsModel prefsModel = ViewModelProviders.of(this).get(PrefsModel.class);
        LiveData<DataSnapshot> snapshotLiveData = prefsModel.getDataSnapshotLiveData();
        
        snapshotLiveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null && !fetchPrefsCalledOnce) {
                    //
                    //Log.e(TAG, "Fetch Prefs called again");
                    prefs = dataSnapshot.getValue(ModelPrefs.class);
                    if (prefs != null) {
                        fetchPrefsCalledOnce = true;
                        curUsersMatchSeq = prefs.getMatchAlgo();
                        editor.putString(Constants.userIntrGender, prefs.getPreferedGender()).apply();
                        city = sharedPreferences.getString(Constants.cityLabel, null);
                        if (city == null) {
                            CityLabelModel viewModel = ViewModelProviders.of(pagerTransition.this).get(CityLabelModel.class);
                            
                            LiveData<DataSnapshot> liveData = viewModel.getDataSnapshotLiveData();
                            
                            liveData.observe(pagerTransition.this, new Observer<DataSnapshot>() {
                                @Override
                                public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                                    if (dataSnapshot != null) {
                                        if (dataSnapshot.getValue() != null) {
                                            String value = dataSnapshot.getValue(String.class);
                                            editor.putString(Constants.cityLabel, value).apply();
                                            downloadList();
                                        }
                                    }
                                }
                            });
                        } else
                            downloadList();
                    }
                }
            }
        });
        
        // get lat long
        DatabaseReference latLong = FirebaseDatabase.getInstance().getReference()
                .child(Constants.userInfo)
                .child(Constants.uid)
                .child(Constants.location);
        latLong.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    currentUsersLatLong = dataSnapshot.getValue(LatLong.class);
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        
    }
    
    private void showFilterDialog() {
        
        final LayoutInflater factory = LayoutInflater.from(getContext());
        final View filterDialogView = factory.inflate(R.layout.filter_dialog, null);
        /*final AlertDialog deleteDialog = new AlertDialog.Builder(getContext()).create();
        deleteDialog.setView(filterDialogView);
        deleteDialog.show();*/
        
        final SweetAlertDialog dialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Filters")
                .setConfirmText("Save")
                .setCustomView(filterDialogView);
        
        RadioGroup genderGroup = filterDialogView.findViewById(R.id.genderRadio);
        RadioButton maleRButton = filterDialogView.findViewById(R.id.maleRadio);
        RadioButton femaleRButton = filterDialogView.findViewById(R.id.femaleRadio);
        final TextView distance = filterDialogView.findViewById(R.id.distance_value);
        
        if (prefs != null) {
            if (prefs.getPreferedGender().equals(Constants.male)) {
                maleRButton.setChecked(true);
                femaleRButton.setChecked(false);
            } else {
                femaleRButton.setChecked(true);
                maleRButton.setChecked(false);
            }
        }
        
        genderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.maleRadio) {
                    //update preferences
                    DatabaseReference refPrefs = FirebaseDatabase.getInstance().getReference()
                            .child(Constants.userPreferences)
                            .child(Constants.uid);
                    
                    HashMap<String, Object> updateMap = new HashMap<>();
                    updateMap.put(Constants.preferedGender, Constants.male);
                    // Toast.makeText(getActivity(), "Getting new results", Toast.LENGTH_SHORT).show();
                    refPrefs.updateChildren(updateMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            editor.putString(Constants.userIntrGender, Constants.male).apply();
                            fetchPrefsCalledOnce = false;
                            fetchPreferences();
                            dialog.dismiss();
                        }
                    });
                    
                } else {
                    //update preferences
                    DatabaseReference refPrefs = FirebaseDatabase.getInstance().getReference()
                            .child(Constants.userPreferences)
                            .child(Constants.uid);
                    
                    HashMap<String, Object> updateMap = new HashMap<>();
                    updateMap.put(Constants.preferedGender, Constants.female);
                    // Toast.makeText(getActivity(), "Getting new results", Toast.LENGTH_SHORT).show();
                    refPrefs.updateChildren(updateMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            editor.putString(Constants.userIntrGender, Constants.female).apply();
                            fetchPrefsCalledOnce = false;
                            fetchPreferences();
                            dialog.dismiss();
                        }
                    });
                }
            }
        });
        RangeSeekBar age_seek = filterDialogView.findViewById(R.id.age_seekbar);
        SeekBar distance_seek = filterDialogView.findViewById(R.id.distance_seekbar);
        
        final TextView tvMin = filterDialogView.findViewById(R.id.minAge);
        final TextView tvMax = filterDialogView.findViewById(R.id.maxAge);
        final TextView tvAgeRange = filterDialogView.findViewById(R.id.ageRange);

// set final value listener
        if (prefs != null) {
            int maxAge = prefs.getMaxAge();
            int minAge = prefs.getMinAge();
            String age_range = "(" + minAge + " - " + maxAge + " yrs)";
            tvAgeRange.setText(age_range);
            tvMin.setText("18");
            tvMax.setText("70");
            
            age_seek.setProgressColor(getResources().getColor(R.color.appcolor));
            age_seek.setValue(minAge, maxAge);
            
        }
        
        age_seek.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                
                view.setProgressColor(getResources().getColor(R.color.appcolor));
                tvMin.setText((int) leftValue + "");
                tvMax.setText((int) rightValue + "");
                
                updateMinAge = new HashMap<>();
                updateMinAge.put("minAge", leftValue);
                
                updateMaxAge = new HashMap<>();
                updateMaxAge.put("maxAge", rightValue);
                
                Log.e("CRS=>", String.valueOf(leftValue) + " : " + String.valueOf(rightValue));
                
                //update the preferences
                
                final DatabaseReference refPrefs = FirebaseDatabase.getInstance().getReference()
                        .child(Constants.userPreferences)
                        .child(Constants.uid);
                
                refPrefs.updateChildren(updateMinAge).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        refPrefs.updateChildren(updateMaxAge).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                            
                            }
                        });
                        
                    }
                });
            }
            
            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {
            
            }
            
            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {
            
            }
        });
        
        double dist = prefs != null ? prefs.getDistanceLimit() : 0.0;
        distance.setText(String.valueOf((int) dist) + " km");
        distance_seek.setProgress((int) dist/50);
        
        distance_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                
                distance.setText(String.valueOf((int) (double) progress * 50) + " km");
                // update the database
                
                updateMap = new HashMap<>();
                updateMap.put("distanceLimit", (double) progress*50);
                
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            
            }
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            
            }
        });
        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(final SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                if (updateMap != null) {
                    final DatabaseReference refPrefs = FirebaseDatabase.getInstance().getReference()
                            .child(Constants.userPreferences)
                            .child(Constants.uid);
                    refPrefs.updateChildren(updateMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //sweetAlertDialog.dismissWithAnimation();
                            fetchPrefsCalledOnce = false;
                            fetchPreferences();
                            
                        }
                    });
                } else {
                    fetchPrefsCalledOnce = false;
                    fetchPreferences();
                }
                
            }
        });
        dialog.show();
        Button btn = dialog.findViewById(R.id.confirm_button);
        btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_4_dialogue));
        
    }
    
    private void fillViewPager() {
        
        indicatorTv = (TextView) rootView.findViewById(R.id.indicator_tv);
        viewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
        
        viewPager.setPageTransformer(false, new CustPagerTransformer(getActivity()));
        
        for (int i = 0; i < displayArrayList.size(); i++) {
            fragments.add(new CommonFragment());
        }
       // Log.e(TAG, "The size of display array list is "+ displayArrayList.size());
        if (getActivity() != null) {
            cleanup();
            adapter = new FragmentStatePagerAdapter(getActivity().getSupportFragmentManager()) {
                @NonNull
                @Override
                public Fragment getItem(int position) {
                    /*if(position>fragments.size())
                    {
                        position--;
                    }*/
                    CommonFragment fragment = fragments.get(position);
                    fragment.bindData(displayArrayList.get(position));
                    return fragment;
                
                    }
                
                @Override
                public int getCount() {
                   // Log.e(TAG, "Returning count as "+ displayArrayList.size());
                    return displayArrayList.size();
                }
            };
            
            viewPager.setAdapter(adapter);
            //remove listener after setting adapter
           
            
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }
                
                @Override
                public void onPageSelected(int position) {
                    updateIndicatorTv();
                }
                
                @Override
                public void onPageScrollStateChanged(int state) {
                
                }
            });
            updateIndicatorTv();
        }
        
    }
    
    private void downloadList() {
        
        displayArrayList = new ArrayList<>();
        displayArrayList.clear();
        Log.e(TAG, "Download list called with " + city);
        interestedGender = sharedPreferences.getString(Constants.userIntrGender, null);
        
        if (city == null || interestedGender == null) {
            fetchPrefsCalledOnce = false;
            fetchPreferences();
        } else {
            
            ref = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.cityLabels).child(Constants.encrypt(city)).child(interestedGender);
            
            //Log.e(TAG, "download list called "+ ref);
            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    if (dataSnapshot.getValue() != null) {
                        //Log.e(TAG, "Child added!");
                        ModelUser item = dataSnapshot.getValue(ModelUser.class);
                        if (item != null) {
                            filterList(item);
                        }
                    }
                }
                
                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Log.e(TAG, "The child changed triggeres");
                }
                
                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    Log.e(TAG, "The child removed triggeres");
                   //remove the child from list and call adapter.notify
                    /*if (dataSnapshot.getValue() != null) {
                        ModelUser item = dataSnapshot.getValue(ModelUser.class);
                        if(displayArrayList.size()>0)
                        {
                            for (Iterator<ModelUser> iterator = displayArrayList.iterator(); iterator.hasNext(); ) {
                                ModelUser user = iterator.next();
                                if (item != null && user.getUid().equals(item.getUid())){
                                    iterator.remove();
                                    if(adapter!=null)
                                        adapter.notifyDataSetChanged();
                                }
                                
                            }
                        }
                    }*/
                    downloadList();
                }
                
                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                
                }
            };
            ref.addChildEventListener(childEventListener);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //populate adapter
                    
                    //sort the arraylist here using LevenshteinDistance algorithm
                    
                    //Log.e(TAG, "Called");
                    
                    if (!dataSnapshot.exists())
                        nothere(true);
                    else
                        nothere(false);
                    //sortWithMatch();
                    emptyView.setVisibility(View.INVISIBLE);
                    /*Collections.sort(displayArrayList, new Comparator<ModelUser>() {
                        @Override
                        public int compare(ModelUser v1, ModelUser v2) {
                            double sub = v1.getMatchIndex() - (v2.getMatchIndex());
                            //Log.e(TAG, "The sub is "+ v1.getUserName() + " second name "+v2.getUserName()+" sum is " +sub);
                            return (int) sub;
                        }
                    });*/
                    Collections.shuffle(displayArrayList);
                    fillViewPager();
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                
                }
            });
        }
    }
    
    private void sortWithMatch() {
        
        final Levenshtein l = new Levenshtein();
        
        for (final ModelUser user : displayArrayList) {
            
            //  Log.e(TAG, "The match seq is "+ curUsersMatchSeq);
            if (user.getMatchAlgo() != null) {
                double s = l.distance(curUsersMatchSeq, user.getMatchAlgo());
                user.setMatchIndex(s);
                // Log.e(TAG, "The match index is "+ s);
            }
        }
        
    }
    
    private void filterList(ModelUser item) {
        //pass the item through the filters and then add them to the list for adaper;
        //1. Age filter
        // Log.e(TAG, "Filtering with "+String.valueOf(prefs));
        if (prefs != null && item.getNumeralAge() >= prefs.getMinAge() && item.getNumeralAge() <= prefs.getMaxAge()
                && distanceBetweenThem(item.getLocation()) <= prefs.getDistanceLimit()&&item.getUid()!=null &&!(contactsUid.contains(item.getUid()))) {
            
            if(item.getUid()!=null) {
                if(!item.getUid().equals(Constants.uid)) {
                    displayArrayList.add(item);
                }
            }
            else {
                displayArrayList.add(item);
                if(adapter!=null)
                    adapter.notifyDataSetChanged();
            }
           
        }
    }
    
    private double distanceBetweenThem(LatLong location) {
        
          if(currentUsersLatLong!=null&&location!=null) {
              return distance(currentUsersLatLong.getLatitude(), currentUsersLatLong.getLongitude(), location.getLatitude(), location.getLongitude());
          }
        // LevenshteinDistance.Compute(baseString, stringtoTest)
        else
        return 0.0;
    }

          /*
    Calculating the distances in kilometer
     */
    
    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        //Log.e(TAG, "The distance is " + dist);
        return (dist);
    }
    
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
    
    /**
     * 更新指示器
     */
    private void updateIndicatorTv() {
        int totalNum = viewPager.getAdapter().getCount();
        int currentItem = viewPager.getCurrentItem() + 1;
        indicatorTv.setText(Html.fromHtml("<font color='#12edf0'>" + currentItem + "</font>  /  " + totalNum));
    }
    
    @Override
    public void onResume() {
        super.onResume();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null)
            startActivity(new Intent(getActivity(), SignIn.class));
        else {
            Constants.uid = FirebaseAuth.getInstance().getUid();
        }
        
    }
    
    public void cleanup() {
        // We're being destroyed, let go of our mListener and forget about all of the mModels
        if (ref != null && childEventListener != null)
            ref.removeEventListener(childEventListener);
        
    }
    
    @Override
    public void onDestroy() {
        cleanup();
        super.onDestroy();
    }
}
