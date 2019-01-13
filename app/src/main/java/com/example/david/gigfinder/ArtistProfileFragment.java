package com.example.david.gigfinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import static android.content.Context.MODE_PRIVATE;

public class ArtistProfileFragment extends Fragment {
    private static final String TAG = "APPLOG - ArtistProfileFragment";

    SharedPreferences sharedPreferences;

    private int userID;
    private Button testDeleteBtn;
    private ImageView imageButton;
    private TextView nameText;
    private TextView descriptionText;
    private TextView genresText;
    //private TextView genresLabel; // to change "Genre" to "Genres" if there are multiple
    private String idToken;

    private FrameLayout progress;

    //private Artist artist;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artist_profile, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        idToken = getArguments().getString("idToken");

        sharedPreferences = getContext().getSharedPreferences(getString(R.string.shared_prefs), Context.MODE_PRIVATE);

        // Test Artist
        /*ArrayList<Genre> list = new ArrayList<>();
        list.add(Genre.ROCK);
        list.add(Genre.HOUSE);
        artist = new Artist(1, Color.DKGRAY, "TestArtist", "Hallo, ich bin ein Test.", null, null, list);
           */
        testDeleteBtn = getView().findViewById(R.id.deleteBtn);
        imageButton = getView().findViewById(R.id.profile_artist_profilePicture);
        nameText = getView().findViewById(R.id.profile_artist_name);
        descriptionText = getView().findViewById(R.id.profile_artist_description);
        genresText = getView().findViewById(R.id.profile_artist_genre);
        //genresLabel = getView().findViewById(R.id.profile_genres_label);


        progress = getView().findViewById(R.id.progressBarHolder);

        testDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteUser deleteUser = new DeleteUser();
                deleteUser.execute();
            }
        });

        //
        //initProfile();

        GetUser getUser = new GetUser();
        getUser.execute();

        super.onActivityCreated(savedInstanceState);
    }

    /*private void initProfile() {
        //updateColor();

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

        /*if(artist.getGenres().size() > 1) {
            genresLabel.setText(getResources().getString(R.string.profile_genre_multiple));
        }
        else {
            genresLabel.setText(getResources().getString(R.string.profile_genre_single));
        }
    }*/

    /**
     * Updates the color of all relevant elements
     */
    private void updateColor(int color) {
        int fontColor = ColorTools.isBrightColor(color);
        nameText.setTextColor(fontColor);
        genresText.setTextColor(fontColor);
        getView().findViewById(R.id.profile_artist_title_bar_form).setBackgroundColor(color);

        int titleBarColor = ColorTools.getSecondaryColor(color);
        // happens in MainActivity, otherwise the statusBar changes on chat tab
        //getActivity().getWindow().setStatusBarColor(titleBarColor);

        TextView descriptionLabel = getView().findViewById(R.id.profile_artist_description_label);
        if(ColorTools.isBrightColorBool(color)) {
            descriptionLabel.setTextColor(titleBarColor);
        }
        else {
            descriptionLabel.setTextColor(color);
        }

        descriptionLabel.setTextColor(color);


        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("titleBarColor", titleBarColor);
        editor.putInt("userColor", color);
        editor.commit();

        /*ViewGroup layout = getView().findViewById(R.id.profile_layout);
        for(int index = 0; index < layout.getChildCount(); ++index) {
            View child = layout.getChildAt(index);
            if(child instanceof TextView) {
                ((TextView) child).setTextColor(fontColor);
            }
        }*/


    }

    private void updateProfile(String jsonString){
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            JSONObject userProfile = jsonArray.getJSONObject(0);
            Log.d(TAG, userProfile.toString());
            nameText.setText(userProfile.getString("name"));
            descriptionText.setText(userProfile.getString("description"));
            userID = userProfile.getInt("id");
            updateColor(Integer.parseInt(userProfile.getString("backgroundColor")));
            testDeleteBtn.setBackgroundColor(Integer.parseInt(userProfile.getString("backgroundColor")));

            SharedPreferences.Editor editor = getActivity().getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE).edit();
            editor.putInt("userId", userID);
            editor.apply();
            //TODO: We should probably cache everything here

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void displayLoadingScreen(boolean isLoading) {
        if(isLoading) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progress.setVisibility(View.VISIBLE);
                }
            });
        }
        else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progress.setVisibility(View.GONE);
                }
            });
        }
    }

    /**
     *
     */
    class GetUser extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            displayLoadingScreen(true);
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
            displayLoadingScreen(false);
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
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.commit();
            Log.d(TAG, "DELETE ARTIST: " + result);
            getActivity().finish();
        }
    }

}
