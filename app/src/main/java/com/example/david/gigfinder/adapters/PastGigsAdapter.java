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

public class PastGigsAdapter extends ArrayAdapter<String[]> {

    private static final String TAG = "UpcomingGigsAdapter";

    public PastGigsAdapter(@NonNull Context context, ArrayList<String[]> strings) {
        super(context, R.layout.past_gig_row, strings);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View customView = layoutInflater.inflate(R.layout.past_gig_row, parent, false);

        TextView gigName = (TextView) customView.findViewById(R.id.pastGigName);
        TextView gigLocation = (TextView) customView.findViewById(R.id.pastGigLocation);
        TextView gigStatus = customView.findViewById(R.id.gigs_status_text);
        //ImageView gigImg = (ImageView) customView.findViewById(R.id.pastGigImg);

        gigName.setText(getItem(position)[0]);
        gigLocation.setText(getItem(position)[1]);
        String statusText = "";
        int statusColor = getContext().getResources().getColor(R.color.black);
        switch(getItem(position)[2]) {
            case "loading":
                statusText = getContext().getResources().getString(R.string.gigs_loading);
                statusColor = getContext().getResources().getColor(R.color.darkGrey);
                break;
            case "accepted":
                statusText = getContext().getResources().getString(R.string.gigs_accepted);
                statusColor = getContext().getResources().getColor(R.color.darkGreen);
                break;
            case "canceled":
                statusText = getContext().getResources().getString(R.string.gigs_canceled);
                statusColor = getContext().getResources().getColor(R.color.darkRed);
                break;
            case "none":
                statusText = "";
                break;
        }
        gigStatus.setText(statusText);
        gigStatus.setTextColor(statusColor);

        return customView;
    }
}
