package com.example.david.gigfinder;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.david.gigfinder.data.Artist;
import com.example.david.gigfinder.data.enums.Genre;

import java.util.ArrayList;
import java.util.Arrays;

import static com.example.david.gigfinder.data.enums.Genre.HIPHOP;
import static com.example.david.gigfinder.data.enums.Genre.ROCK;

public class ProfileFragment extends Fragment {
    private static final String TAG = "APPLOG - ProfileFragment";

    private ImageButton imageButton;
    private TextView nameText;
    private TextView descriptionText;
    private TextView genresText;
    private TextView genresLabel; // to change "Genre" to "Genres" if there are multiple


    private Artist artist;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Test Artist
        ArrayList<Genre> list = new ArrayList<>();
        list.add(Genre.ROCK);
        list.add(Genre.HOUSE);
        artist = new Artist(1, "TestArtist", "Hallo, ich bin ein Test.", null, null, list, Color.DKGRAY, Color.WHITE);

        imageButton = getView().findViewById(R.id.profile_image);
        nameText = getView().findViewById(R.id.profile_name);
        descriptionText = getView().findViewById(R.id.profile_description);
        genresText = getView().findViewById(R.id.profile_genres);
        genresLabel = getView().findViewById(R.id.profile_genres_label);

        initProfile();
    }

    private void initProfile() {
        getView().setBackgroundColor(artist.getColor());
        updateFontColor();

        nameText.setText(artist.getName());
        descriptionText.setText(artist.getDescription());

        String genreString = "";
        for(Genre g : artist.getGenres()) {
            if(!genreString.equals("")) {
                genreString += ", ";
            }
            genreString += g.toString();
        }
        genresText.setText(genreString);

        if(artist.getGenres().size() > 1) {
            genresLabel.setText(getResources().getString(R.string.profile_genre_multiple));
        }
        else {
            genresLabel.setText(getResources().getString(R.string.profile_genre_single));
        }
    }

    /**
     * Updates the font color of all relevant elements
     */
    private void updateFontColor() {
        int fontColor = artist.getFontColor();

        ViewGroup layout = getView().findViewById(R.id.profile_layout);
        for(int index = 0; index < layout.getChildCount(); ++index) {
            View child = layout.getChildAt(index);
            if(child instanceof TextView) {
                ((TextView) child).setTextColor(fontColor);
            }
        }
    }
}
