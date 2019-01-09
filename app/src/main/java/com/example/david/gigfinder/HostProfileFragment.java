package com.example.david.gigfinder;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.david.gigfinder.data.Artist;
import com.example.david.gigfinder.data.enums.Genre;
import com.example.david.gigfinder.tools.ColorTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

public class HostProfileFragment extends Fragment {
    private static final String TAG = "APPLOG - ArtistProfileFragment";

    private int userID;
    private Button testDeleteBtn;
    private ImageButton imageButton;
    private TextView nameText;
    private TextView descriptionText;
    private TextView genresText;
    private TextView genresLabel; // to change "Genre" to "Genres" if there are multiple
    private String idToken;

    private Artist artist;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        idToken = getArguments().getString("idToken");

        // Test Artist
        ArrayList<Genre> list = new ArrayList<>();
        list.add(Genre.ROCK);
        list.add(Genre.HOUSE);
        artist = new Artist(1, Color.DKGRAY, "TestArtist", "Hallo, ich bin ein Test.", null, null, list);

        testDeleteBtn = getView().findViewById(R.id.deleteBtn);
        imageButton = getView().findViewById(R.id.profile_image);
        nameText = getView().findViewById(R.id.profile_name);
        descriptionText = getView().findViewById(R.id.profile_description);
        genresText = getView().findViewById(R.id.profile_genres);
        genresLabel = getView().findViewById(R.id.profile_genres_label);

        testDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteUser deleteUser = new DeleteUser();
                deleteUser.execute();
            }
        });

        initProfile();

        GetUser getUser = new GetUser();
        getUser.execute();

        super.onActivityCreated(savedInstanceState);
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
        int fontColor = ColorTools.isBrightColor(artist.getColor());

        ViewGroup layout = getView().findViewById(R.id.profile_layout);
        for(int index = 0; index < layout.getChildCount(); ++index) {
            View child = layout.getChildAt(index);
            if(child instanceof TextView) {
                ((TextView) child).setTextColor(fontColor);
            }
        }
    }

    private void updateProfile(String jsonString){
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            JSONObject userProfile = jsonArray.getJSONObject(0);
            nameText.setText(userProfile.getString("name"));
            descriptionText.setText(userProfile.getString("description"));
            userID = userProfile.getInt("id");
            testDeleteBtn.setBackgroundColor(Integer.parseInt(userProfile.getString("backgroundColor")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    class GetUser extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/artists");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Authorization", idToken);
                urlConnection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return response.toString();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "USER PROFILE: " + result);
            updateProfile(result);
        }
    }

    /**
     *
     */
    class DeleteUser extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/artists/"+userID);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Authorization", idToken);
                urlConnection.setRequestMethod("DELETE");

                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return response.toString();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "DELETE ARTIST: " + result);
            getActivity().finish();
        }
    }

}
