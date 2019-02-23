package com.testlabic.datenearu.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.testlabic.datenearu.R;
import com.testlabic.datenearu.Utils.Constants;

/**
 * A simple {@link Fragment} subclass.
 */
public class ClickedUserImage1 extends Fragment {
    
    private static final String TAG = ClickedUserImage1.class.getSimpleName();
    String imageUrl;
    
    public ClickedUserImage1() {
        // Required empty public constructor
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_clicked_user_image1,container,false);
        final ImageView imageView = view.findViewById(R.id.photos);
        if (getArguments() != null) {
            imageUrl = getArguments().getString(Constants.imageUrl);
            boolean isBlur = getArguments().getBoolean(Constants.isBlur);
            //Log.e(TAG, "Image url : reached here "+imageUrl);
            if(!isBlur)
            Glide.with(getContext()).load
                    ( imageUrl).into(imageView);
    
            else
            {
                RequestOptions myOptions = new RequestOptions()
                        .override(15, 15)
                        .diskCacheStrategy(DiskCacheStrategy.NONE);

                Glide.with(getContext())
                        .load( imageUrl)
                        .apply(myOptions)
                        .into(imageView);
                /*Glide.with(getContext()).load
                        ( imageUrl).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }
    
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                        Blurry.with(getActivity())
                                .capture(imageView)
                                .into((ImageView) imageView);
                        return true;
                    }
                }).into(imageView);*/

            }
            
        }
        return view;
    }
    
}
