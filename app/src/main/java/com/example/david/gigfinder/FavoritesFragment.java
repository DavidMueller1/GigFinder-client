package com.example.david.gigfinder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.david.gigfinder.adapters.ChatAdapter;
import com.example.david.gigfinder.adapters.FavAdapter;

import java.util.ArrayList;

public class FavoritesFragment extends Fragment {

    private static final String TAG = "APPLOG - FavoritesFragment";
    private FavAdapter favAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        ArrayList<String[]> placeholderStrings = new ArrayList<>();
        placeholderStrings.add(new String[]{"Artist Name", "Placeholder message..."});
        placeholderStrings.add(new String[]{"Host Name", "Test message..."});

        favAdapter = new FavAdapter(this.getContext(), placeholderStrings);
        ListView listView = (ListView) getView().findViewById(R.id.favListView);
        listView.setAdapter(favAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ArtistProfileActivity.class);
                startActivity(intent);
            }
        });
    }
}
