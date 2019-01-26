package com.example.david.gigfinder.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.david.gigfinder.R;
import com.example.david.gigfinder.tools.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ProfileEventsAdapter extends ArrayAdapter<String[]> {

    private static final String TAG = "ProfileEventsAdapter";

    public ProfileEventsAdapter(@NonNull Context context, ArrayList<String[]> strings) {
        super(context, R.layout.profile_gig_row, strings);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View customView = layoutInflater.inflate(R.layout.profile_gig_row, parent, false);

        TextView eventName = (TextView) customView.findViewById(R.id.profile_gig_row_name);
        TextView whenString = (TextView) customView.findViewById(R.id.profile_gig_row_time);

        eventName.setText(getItem(position)[0]);

        Date start = Utils.convertStringToDate(getItem(position)[1]);
        if(start.before(Calendar.getInstance().getTime())){
            whenString.setText(R.string.event_alredy_started);
        }else {
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
            whenString.setText(formatter.format(start));
        }



        return customView;
    }
}
