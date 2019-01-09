package com.example.david.gigfinder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.david.gigfinder.adapters.FavAdapter;
import com.example.david.gigfinder.adapters.PastGigsAdapter;
import com.example.david.gigfinder.adapters.UpcomingGigsAdapter;

import java.util.ArrayList;

public class GigsFragment extends Fragment {

    private static final String TAG = "APPLOG - GigsFragment";
    private UpcomingGigsAdapter upcomingGigsAdapter;
    private PastGigsAdapter pastGigsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gigs, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        ListView upcomingListView = (ListView) getView().findViewById(R.id.upcomingGigsListView);
        ListView pastListView = (ListView) getView().findViewById(R.id.pastGigsListView);

        ArrayList<String[]> placeholderStrings = new ArrayList<>();
        placeholderStrings.add(new String[]{"Event Name", "20.04.2019", "Geschwister-Scholl-Platz 1"});
        placeholderStrings.add(new String[]{"Event Name", "20.04.2019", "Geschwister-Scholl-Platz 1"});

        ArrayList<String[]> placeholderStrings2 = new ArrayList<>();
        placeholderStrings2.add(new String[]{"Event Name", "Geschwister-Scholl-Platz 1"});

        upcomingGigsAdapter = new UpcomingGigsAdapter(this.getContext(), placeholderStrings);
        upcomingListView.setAdapter(upcomingGigsAdapter);
        upcomingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), EventProfileActivity.class);
                startActivity(intent);
            }
        });

        pastGigsAdapter = new PastGigsAdapter(this.getContext(), placeholderStrings2);
        pastListView.setAdapter(pastGigsAdapter);
    }
}
