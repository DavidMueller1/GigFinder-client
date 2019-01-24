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
        TextView gigStatus = customView.findViewById(R.id.gigs_status_text);
        ImageView gigImg = (ImageView) customView.findViewById(R.id.upcomingGigImg);

        gigName.setText(getItem(position)[0]);
        gigTime.setText(getItem(position)[1]);
        gigLocation.setText(getItem(position)[2]);
        String statusText = "";
        int statusColor = getContext().getResources().getColor(R.color.black);
        switch(getItem(position)[3]) {
            case "loading":
                statusText = getContext().getResources().getString(R.string.gigs_loading);
                statusColor = getContext().getResources().getColor(R.color.darkGrey);
                break;
            case "pending":
                statusText = getContext().getResources().getString(R.string.gigs_pending);
                statusColor = getContext().getResources().getColor(R.color.darkYellow);
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
