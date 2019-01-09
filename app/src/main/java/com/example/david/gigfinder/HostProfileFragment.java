package com.example.david.gigfinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.content.res.AppCompatResources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.david.gigfinder.data.Artist;
import com.example.david.gigfinder.data.enums.Genre;
import com.example.david.gigfinder.tools.ColorTools;
import com.google.android.gms.maps.model.LatLng;

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
import java.util.List;
import java.util.Locale;

public class HostProfileFragment extends Fragment {
    private static final String TAG = "APPLOG - HostProfileFragment";

    SharedPreferences sharedPreferences;

    private int userID;
    private Button testDeleteBtn;
    private ImageView imageButton;
    private TextView nameText;
    private TextView descriptionText;
    private LinearLayout locationContainer;
    private ImageView locationIcon;
    private TextView locationText;
    private TextView genresText;
    //private TextView genresLabel; // to change "Genre" to "Genres" if there are multiple
    private String idToken;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_host_profile, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        idToken = getArguments().getString("idToken");

        sharedPreferences = getContext().getSharedPreferences("List", Context.MODE_PRIVATE);

        // Test Artist
        /*ArrayList<Genre> list = new ArrayList<>();
        list.add(Genre.ROCK);
        list.add(Genre.HOUSE);
        artist = new Artist(1, Color.DKGRAY, "TestArtist", "Hallo, ich bin ein Test.", null, null, list);
           */

        testDeleteBtn = getView().findViewById(R.id.deleteBtn);
        imageButton = getView().findViewById(R.id.profile_host_profilePicture);
        nameText = getView().findViewById(R.id.profile_host_name);
        descriptionText = getView().findViewById(R.id.profile_host_description);
        locationText = getView().findViewById(R.id.profile_host_location_text);
        locationIcon = getView().findViewById(R.id.profile_host_location_icon);
        locationContainer = getView().findViewById(R.id.profile_host_location_container);
        genresText = getView().findViewById(R.id.profile_host_genre);
        //genresLabel = getView().findViewById(R.id.profile_genres_label);

        testDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteUser deleteUser = new DeleteUser();
                deleteUser.execute();
            }
        });

        GetUser getUser = new GetUser();
        getUser.execute();

        super.onActivityCreated(savedInstanceState);
    }


    /**
     * Updates the color of all relevant elements
     */
    private void updateColor(int color) {
        int fontColor = ColorTools.isBrightColor(color);
        nameText.setTextColor(fontColor);
        genresText.setTextColor(fontColor);
        getView().findViewById(R.id.profile_host_title_bar_form).setBackgroundColor(color);

        float deltaValue = 0.15f;
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        if(hsv[2] < deltaValue) {
            hsv[2] += deltaValue;
        }
        else {
            hsv[2] -= deltaValue;
        }

        int titleBarColor = Color.HSVToColor(hsv);
        getActivity().getWindow().setStatusBarColor(titleBarColor);

        TextView descriptionLabel = getView().findViewById(R.id.profile_host_description_label);
        if(ColorTools.isBrightColorBool(color)) {
            descriptionLabel.setTextColor(titleBarColor);
        }
        else {
            descriptionLabel.setTextColor(color);
        }

        descriptionLabel.setTextColor(color);
        locationIcon.setImageTintList(ColorStateList.valueOf(color));


        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("titleBarColor", titleBarColor);
        editor.putInt("userColor", color);
        editor.commit();
    }

    private void updateProfile(String jsonString){
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            JSONObject userProfile = jsonArray.getJSONObject(0);
            final String name = userProfile.getString("name");
            nameText.setText(name);
            descriptionText.setText(userProfile.getString("description"));
            userID = userProfile.getInt("id");
            updateColor(Integer.parseInt(userProfile.getString("backgroundColor")));
            testDeleteBtn.setBackgroundColor(Integer.parseInt(userProfile.getString("backgroundColor")));
            final float lat = Float.parseFloat(userProfile.getString("latitude"));
            final float lng = Float.parseFloat(userProfile.getString("longitude"));
            locationContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)", lat, lng, lat, lng, name);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(intent);
                }
            });

            String addressString = "Keine Addresse gefunden";

            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

            List<Address> addresses = null;

            try {
                addresses = geocoder.getFromLocation(lat, lng, 1);
            } catch (IOException ioException) {
                // Catch network or other I/O problems.
                Log.d(TAG, "Network error");

            } catch (IllegalArgumentException illegalArgumentException) {
                // Catch invalid latitude or longitude values.
                Log.d(TAG, "Coordinate error");

            }

            // Handle case where no address was found.
            if (addresses == null || addresses.size()  == 0) {
                Log.d(TAG, "No Address found");
            }
            else {
                Address address = addresses.get(0);
                ArrayList<String> addressFragments = new ArrayList<String>();

                // Fetch the address lines using getAddressLine
                for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressFragments.add(address.getAddressLine(i));
                }

                addressString = address.getAddressLine(0);
                locationText.setText(addressString);
            }


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
            getView().findViewById(R.id.progressBarHolder).setVisibility(View.GONE);
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/hosts");
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
            getView().findViewById(R.id.progressBarHolder).setVisibility(View.GONE);
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
                URL url = new URL("https://gigfinder.azurewebsites.net/api/hosts/"+userID);
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
            Log.d(TAG, "DELETE HOST: " + result);
            getActivity().finish();
        }
    }

}
