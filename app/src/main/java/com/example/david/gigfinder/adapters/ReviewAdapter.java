package com.example.david.gigfinder.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.CircularProgressDrawable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.david.gigfinder.R;
import com.example.david.gigfinder.tools.ImageTools;

import java.util.ArrayList;

public class ReviewAdapter extends ArrayAdapter<String[]> {
    private static final String TAG = "ReviewAdapter";


    public ReviewAdapter(@NonNull Context context, ArrayList<String[]> strings) {
        super(context, R.layout.participant_row, strings);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View customView = layoutInflater.inflate(R.layout.review_row, parent, false);

        float rating = Float.parseFloat(getItem(position)[0]);
        String comment = getItem(position)[1];

        RatingBar ratingBar = customView.findViewById(R.id.review_row_rating_bar);
        TextView commentText = customView.findViewById(R.id.review_row_comment);

        ratingBar.setRating(rating);
        commentText.setText(comment);

        return customView;
    }
}
