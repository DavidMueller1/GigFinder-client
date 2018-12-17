package com.example.david.gigfinder.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.david.gigfinder.R;

import java.util.ArrayList;

public class FavAdapter extends ArrayAdapter<String[]> {

    private static final String TAG = "FavAdapter";

    public FavAdapter(@NonNull Context context, ArrayList<String[]> strings) {
        super(context, R.layout.fav_row, strings);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View customView = layoutInflater.inflate(R.layout.fav_row, parent, false);

        TextView favName = (TextView) customView.findViewById(R.id.favName);
        TextView favMsg = (TextView) customView.findViewById(R.id.favMsg);
        ImageView favImg = (ImageView) customView.findViewById(R.id.favImg);

        favName.setText(getItem(position)[0]);
        favMsg.setText(getItem(position)[1]);

        return customView;
    }
}