package com.testlabic.datenearu.Activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.soundcloud.android.crop.Crop;
import com.testlabic.datenearu.Models.ModelUser;
import com.testlabic.datenearu.R;
import com.testlabic.datenearu.Utils.Constants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Callable;

import cn.pedant.SweetAlert.SweetAlertDialog;
import jp.wasabeef.blurry.Blurry;

public class EditActivity extends AppCompatActivity {
    
    String imageId = "image1";

     private static final String default_uri_dp="https://firebasestorage.googleapis.com/v0/b/datenearu.appspot.com/o/profile.jpeg?alt=media&token=d50ac046-46b4-480d-a612-e3d5c8519717";
    private static final String TAG = EditActivity.class.getSimpleName();
    TextView name, age, about;
    ImageView image1, nameWrap,image2,image3;
    ProgressBar bar1, bar2, bar3;
    Switch blur;
    CircularImageView remove_dp1,remove_dp2,remove_dp3;
    String cityLabel, gender;
    Boolean detailsSetup = false;
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    StorageReference displaypics_Ref;
    UploadTask uploadTask;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        name = findViewById(R.id.name);
        age = findViewById(R.id.age);
        about = findViewById(R.id.about);
        View dp1=findViewById(R.id.edit_dp_1);
        View dp2=findViewById(R.id.edit_dp_2);
        View dp3=findViewById(R.id.edit_dp_3);
        image1 = dp1.findViewById(R.id.image1);
        image2 = dp2.findViewById(R.id.image1);
        image3 = dp3.findViewById(R.id.image1);
        bar1 = dp1.findViewById(R.id.progress_bar);
        bar2 = dp2.findViewById(R.id.progress_bar);
        bar3 = dp3.findViewById(R.id.progress_bar);
        blur = findViewById(R.id.blurProfile);
        nameWrap = findViewById(R.id.name_wrap);
        remove_dp1=dp1.findViewById(R.id.remove_dp);
        remove_dp2=dp2.findViewById(R.id.remove_dp);
        remove_dp3=dp3.findViewById(R.id.remove_dp);



        //remove dp code
        remove_dp1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SweetAlertDialog sweetAlertDialog=new SweetAlertDialog(EditActivity.this,SweetAlertDialog.WARNING_TYPE);
                sweetAlertDialog.setTitle("Image");
                sweetAlertDialog.setContentText("Are you sure?");
                sweetAlertDialog.setConfirmButton("Yes", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        image1.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.profile));
                        uri_update_db(default_uri_dp,"image1");
                        sweetAlertDialog.dismiss();
                    }
                });
                sweetAlertDialog.setCancelText("No");
                sweetAlertDialog.show();
            }
        });
        remove_dp2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SweetAlertDialog sweetAlertDialog=new SweetAlertDialog(EditActivity.this,SweetAlertDialog.WARNING_TYPE);
                sweetAlertDialog.setTitle("Image");
                sweetAlertDialog.setContentText("Are you sure?");
                sweetAlertDialog.setConfirmButton("Yes", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        image2.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.profile));
                        uri_update_db(default_uri_dp,"image2");
                        sweetAlertDialog.dismiss();
                    }
                });
                sweetAlertDialog.setCancelText("No");
                sweetAlertDialog.show();
            }
        });
        remove_dp3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SweetAlertDialog sweetAlertDialog=new SweetAlertDialog(EditActivity.this,SweetAlertDialog.WARNING_TYPE);
                sweetAlertDialog.setTitle("Image");
                sweetAlertDialog.setContentText("Are you sure?");
                sweetAlertDialog.setConfirmButton("Yes", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        image3.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.profile));
                        uri_update_db(default_uri_dp,"image3");
                        sweetAlertDialog.dismiss();
                    }
                });
                sweetAlertDialog.setCancelText("No");
                sweetAlertDialog.show();
            }
        });


        //open gallery  or camera code on image click
        image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SweetAlertDialog sweetAlertDialog=new SweetAlertDialog(EditActivity.this,SweetAlertDialog.NORMAL_TYPE);
                sweetAlertDialog.setTitle("Image");
                sweetAlertDialog.setContentText("Change picture by clicking on any one of them");
                sweetAlertDialog.setConfirmButton("Gallery", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                       
                        Crop.pickImage(EditActivity.this );
                        sweetAlertDialog.dismiss();
                     
                        imageId = "imageUrl";
                    }
                });
               
                sweetAlertDialog.show();
               
            }
        });
        image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SweetAlertDialog sweetAlertDialog=new SweetAlertDialog(EditActivity.this,SweetAlertDialog.NORMAL_TYPE);
                sweetAlertDialog.setTitle("Image");
                sweetAlertDialog.setContentText("Change picture by clicking on any one of them");
                sweetAlertDialog.setConfirmButton("Gallery", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        Crop.pickImage(EditActivity.this );
                       
                        sweetAlertDialog.dismiss();
                        imageId = "image2";
                    }
                });
                sweetAlertDialog.show();
            }
        });
        image3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SweetAlertDialog sweetAlertDialog=new SweetAlertDialog(EditActivity.this,SweetAlertDialog.NORMAL_TYPE);
                sweetAlertDialog.setTitle("Image");
                sweetAlertDialog.setContentText("Change picture by clicking on any one of them");
                sweetAlertDialog.setConfirmButton("Gallery", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        Crop.pickImage(EditActivity.this );
                        sweetAlertDialog.dismiss();
                        imageId = "image3";
                    }
                });
              
                sweetAlertDialog.show();
              
            }
        });




        blur.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    if(!detailsSetup)
                        Toast.makeText(EditActivity.this, "Wait a moment and try again please!", Toast.LENGTH_SHORT).show();
                    else
                    blurProfile();
                }
                else {
                    if(!detailsSetup)
                        Toast.makeText(EditActivity.this, "Wait a moment and try again please!", Toast.LENGTH_SHORT).show();
                    else
                    unBlurProfile();
                }
            }
        });
        setUpDetails();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(data.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        }
       
    }
    
    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }
    
    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Crop.getOutput(result));
                switch (imageId) {
                    case "image2":
                        displaypics_Ref = storageRef.child("Display_Pics/" + Constants.uid + "/2.jpg");
                        image2.setImageURI(Crop.getOutput(result));
                        bar2.setVisibility(View.VISIBLE);
                        bar1.setVisibility(View.INVISIBLE);
                        bar3.setVisibility(View.INVISIBLE);
    
                        break;
                    case "image3":
                        displaypics_Ref = storageRef.child("Display_Pics/" + Constants.uid + "/3.jpg");
                        image3.setImageURI(Crop.getOutput(result));
                        bar3.setVisibility(View.VISIBLE);
                        bar1.setVisibility(View.INVISIBLE);
                        bar2.setVisibility(View.INVISIBLE);
    
                        break;
                    default:
                        displaypics_Ref = storageRef.child("Display_Pics/" + Constants.uid + "/1.jpg");
                        image1.setImageURI(Crop.getOutput(result));
                        bar1.setVisibility(View.VISIBLE);
                        bar2.setVisibility(View.INVISIBLE);
                        bar3.setVisibility(View.INVISIBLE);
    
                        break;
                }
                uploaddp(bitmap, imageId);
            } catch (IOException e) {
                e.printStackTrace();
            }
    
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    
    public void uploaddp(Bitmap bitmap, final String image_id){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        byte[] data1 = baos.toByteArray();
        uploadTask = displaypics_Ref.putBytes(data1);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.e(TAG, "Failed!");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                 Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                       
                        // Continue with the task to get the download URL
                        return displaypics_Ref.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            String uri=downloadUri.toString();
                            uri_update_db(uri,image_id);
                           
                        } else {
                            Toast.makeText(getApplicationContext(),"Unable to get Uri",Toast.LENGTH_LONG).show();
                            // Handle failures
                            // ...
                        }
                    }
                });
            }
        });

    }

    public void uri_update_db(String uri,String image_id){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().getRef().child(Constants.userInfo).child(Constants.uid);
        HashMap<String, Object> updateimage = new HashMap<>();
        updateimage.put(image_id, uri);
        ref.updateChildren(updateimage).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                bar1.setVisibility(View.INVISIBLE);
                bar2.setVisibility(View.INVISIBLE);
                bar3.setVisibility(View.INVISIBLE);
    
            }
        });
    }


    private void unBlurProfile() {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.userInfo).child(Constants.uid);

        // first update userinfo then update child under city label node.

        final HashMap<String, Object> updateBlur = new HashMap<>();
        updateBlur.put(Constants.isBlur, false);
        reference.updateChildren(updateBlur).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference()
                        .child(Constants.cityLabels).child(cityLabel).child(gender).child(Constants.uid);
                ref2.updateChildren(updateBlur).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        startActivity(new Intent(EditActivity.this, EditActivity.class));
                        finish();
                    }
                });

            }
        });

    }

    private void blurProfile() {
        Blurry.with(EditActivity.this).capture(image1).into(image1);
//        Blurry.with(EditActivity.this).capture(nameWrap).into(nameWrap);
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.userInfo).child(Constants.uid);

        // first update userinfo then update child under city label node.

        final HashMap<String, Object> updateBlur = new HashMap<>();
        updateBlur.put(Constants.isBlur, true);
        reference.updateChildren(updateBlur).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference()
                        .child(Constants.cityLabels).child(cityLabel).child(gender).child(Constants.uid);
                ref2.updateChildren(updateBlur);

            }
        });
    
        name.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        float radius = name.getTextSize() / 3;
        BlurMaskFilter filter = new BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL);
        name.getPaint().setMaskFilter(filter);
    }

    private void setUpDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().getRef().child(Constants.userInfo).child(Constants.uid);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(ModelUser.class) != null) {
                    ModelUser user = dataSnapshot.getValue(ModelUser.class);
                    if (user != null && user.getUserName() != null) {
                        name.setText(user.getUserName());
                        age.setText(String.valueOf(user.getNumeralAge()));
                        Glide.with(EditActivity.this).load(user.getImage1()).into(image1);
                        Glide.with(EditActivity.this).load(user.getImage2()).into(image2);
                        Glide.with(EditActivity.this).load(user.getImage3()).into(image3);
                        cityLabel = user.getCityLabel();
                        cityLabel = cityLabel.replace(", ", "_");
                        gender = user.getGender();
                        detailsSetup = true;
                        if(user.getIsBlur())
                        {
                           blur.setChecked(true);
                           blurProfile();
                        }
                        else blur.setChecked(false);
                    }


                    if (user != null && user.getAbout() != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            about.setText(Html.fromHtml(user.getAbout(), Html.FROM_HTML_MODE_COMPACT));
                        } else {
                            about.setText(Html.fromHtml(user.getAbout()));
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
