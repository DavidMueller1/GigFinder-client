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

public class UpcomingGigsAdapter  extends ArrayAdapter<String[]> {

    private static final String TAG = "UpcomingGigsAdapter";

    public UpcomingGigsAdapter(@NonNull Context context, ArrayList<String[]> strings) {
        super(context, R.layout.upcoming_gig_row, strings);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View customView = layoutInflater.inflate(R.layout.upcoming_gig_row, parent, false);

        TextView gigName = (TextView) customView.findViewById(R.id.upcomingGigName);
        TextView gigLocation = (TextView) customView.findViewById(R.id.upcomingGigLocation);
        TextView gigTime= (TextView) customView.findViewById(R.id.upcomingGigStartTime);
        ImageView gigImg = (ImageView) customView.findViewById(R.id.upcomingGigImg);

        gigName.setText(getItem(position)[0]);
        gigTime.setText(getItem(position)[1]);
        gigLocation.setText(getItem(position)[2]);

        return customView;
    }
}
