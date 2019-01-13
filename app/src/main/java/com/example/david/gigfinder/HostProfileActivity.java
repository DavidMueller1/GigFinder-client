package com.example.david.gigfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.david.gigfinder.tools.ColorTools;
import com.example.david.gigfinder.tools.GeoTools;
import com.example.david.gigfinder.tools.ImageTools;
import com.example.david.gigfinder.tools.Utils;
import com.google.android.gms.common.util.Strings;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Locale;

public class HostProfileActivity extends AppCompatActivity {

    private static final String TAG = "HostProfileActivity";

    SharedPreferences sharedPreferences;

    private ImageView imageButton;
    private TextView nameText;
    private TextView descriptionText;
    private LinearLayout locationContainer;
    private ImageView locationIcon;
    private TextView locationText;
    private TextView genresText;
    private Button sendMsgBtn;
    private Button addToFavsBtn;

    private FrameLayout progress;

    //private JSONObject hostJson;
    private int userId;
    private int profileUserId;
    String idToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_profile);


        sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE);

        profileUserId = Integer.parseInt(getIntent().getExtras().getString("profileUserId"));
        idToken = getIntent().getExtras().getString("idToken");
        /*try {
            hostJson = new JSONObject(getIntent().getExtras().getString("host"));
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        //SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", -1);

//        profileUserId = 28;

        imageButton = findViewById(R.id.profile_host_profilePicture);
        nameText = findViewById(R.id.profile_host_name);
        descriptionText = findViewById(R.id.profile_host_description);
        locationText = findViewById(R.id.profile_host_location_text);
        locationIcon = findViewById(R.id.profile_host_location_icon);
        locationContainer = findViewById(R.id.profile_host_location_container);
        genresText = findViewById(R.id.profile_host_genre);

        progress = findViewById(R.id.progressBarHolder);
        sendMsgBtn = findViewById(R.id.sendMsgBtn);
        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HostProfileActivity.this, ChatActivity.class);
                intent.putExtra("idToken", idToken);

                intent.putExtra("profileUserId", profileUserId);

                startActivity(intent);
            }
        });

        addToFavsBtn = findViewById(R.id.addToFavsBtn);
        addToFavsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToFavorites();
                // TODO check if already in favorites
            }
        });

        GetHost getHost = new GetHost();
        getHost.execute(profileUserId + "");
    }

    /**
     * Adds the Host to Favorites
     */
    private void addToFavorites() {
        PostFavorite postFavorite = new PostFavorite();
        postFavorite.execute(); //TODO Ids;
    }

    /**
     * Deletes the Host from Favorites
     */
    private void deleteFromFavorites() {
        DeleteFavorite deleteFavorite = new DeleteFavorite();
        deleteFavorite.execute();
    }

    /**
     * Updates the color of all relevant elements
     */
    private void updateColor(int color) {
        int fontColor = ColorTools.isBrightColor(color);
        nameText.setTextColor(fontColor);
        genresText.setTextColor(fontColor);
        findViewById(R.id.profile_host_title_bar_form).setBackgroundColor(color);
        sendMsgBtn.setBackgroundColor(color);
        addToFavsBtn.setBackgroundColor(color);


        int titleBarColor = ColorTools.getSecondaryColor(color);
        // happens in MainActivity, otherwise the statusBar changes on chat tab
        getWindow().setStatusBarColor(titleBarColor);

        TextView descriptionLabel = findViewById(R.id.profile_host_description_label);
        if(ColorTools.isBrightColorBool(color)) {
            descriptionLabel.setTextColor(titleBarColor);
        }
        else {
            descriptionLabel.setTextColor(color);
        }

        descriptionLabel.setTextColor(color);
        locationIcon.setImageTintList(ColorStateList.valueOf(color));

    }

    private void updateProfile(String jsonString){
        try {
            JSONObject userProfile = new JSONObject(jsonString);

            GetProfilePicture getProfilePicture = new GetProfilePicture();
            getProfilePicture.execute(userProfile.getInt("profilePictureId") + "");

            final String name = userProfile.getString("name");
            nameText.setText(name);
            descriptionText.setText(userProfile.getString("description"));
            updateColor(Integer.parseInt(userProfile.getString("backgroundColor")));
            final float lat = Float.parseFloat(userProfile.getString("latitude"));
            final float lng = Float.parseFloat(userProfile.getString("longitude"));

            String myGenres = "(";
            for(int i=0; i<userProfile.getJSONArray("hostGenres").length(); i++){
                myGenres = myGenres.concat(Utils.genreIdToString(userProfile.getJSONArray("hostGenres").getJSONObject(i).getInt("genreId"), sharedPreferences.getString("genres", "x")));
                if(i < userProfile.getJSONArray("hostGenres").length()-1){
                    myGenres = myGenres.concat(", ");
                }
            }
            myGenres = myGenres.concat(")");
            genresText.setText(myGenres);

            addToFavsBtn.setVisibility(View.GONE);
            if(sharedPreferences.getString("user", "").equals("artist")) {
                sendMsgBtn.setVisibility(View.VISIBLE);
            }
            else {
                sendMsgBtn.setVisibility(View.GONE);
            }

            /*SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE).edit();
            editor.putInt("userId", userID);
            editor.apply();*/
            //TODO: We should probably cache everything here

            locationContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)", lat, lng, lat, lng, name);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(intent);
                }
            });

            String address = GeoTools.getAddressFromLatLng(getApplicationContext(), new LatLng(lat, lng));
            locationText.setText(address);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void displayProfilePicture(String result) {
        try {
            JSONObject imageProfile = new JSONObject(result);

            ViewGroup.LayoutParams params = imageButton.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            imageButton.setBackground(null);
            imageButton.setLayoutParams(params);
            imageButton.setImageTintList(null);

            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(ImageTools.PROFILE_PICTURE_PLACEHOLDER) // TODO default image
                    .override(ImageTools.PROFILE_PICTURE_SIZE)
                    .transforms(new CenterCrop(), new RoundedCorners(30));

            Glide.with(getApplicationContext())
                    .load(Base64.decode(imageProfile.getString("image"), Base64.DEFAULT))
                    .apply(options)
                    .into(imageButton);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void displayLoadingScreen(boolean isLoading) {
        if(isLoading) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progress.setVisibility(View.VISIBLE);
                }
            });
        }
        else {
            runOnUiThread(new Runnable() {
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
    class GetHost extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            displayLoadingScreen(true);
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/hosts/" + params[0]);
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

    class GetProfilePicture extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/pictures/" + params[0]);
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
            displayProfilePicture(result);
            displayLoadingScreen(false);
        }
    }


    class PostFavorite extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/favorites");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Authorization", idToken);
                urlConnection.setRequestProperty("Content-Type","application/json");
                urlConnection.setRequestMethod("POST");
                urlConnection.setUseCaches(false);
                urlConnection.setDoOutput(true);

                //Send data
                DataOutputStream os = new DataOutputStream(urlConnection.getOutputStream());
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ArtistId", userId);
                jsonObject.put("HostId", profileUserId);
                os.writeBytes(jsonObject.toString());
                os.close();

                //Get response
                InputStream is = null;
                try {
                    is = urlConnection.getInputStream();
                } catch (IOException ioe) {
                    if (urlConnection instanceof HttpURLConnection) {
                        HttpURLConnection httpConn = (HttpURLConnection) urlConnection;
                        int statusCode = httpConn.getResponseCode();
                        if (statusCode != 200) {
                            is = httpConn.getErrorStream();
                            Log.d(TAG, "PostFavorite: STATUS CODE: " + statusCode);
                            Log.d(TAG, "PostFavorite: RESPONESE MESSAGE: " + httpConn.getResponseMessage());
                            Log.d(TAG, httpConn.getURL().toString());
                        }
                    }
                }

                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();

                Log.d(TAG, "PostFavorite: RESPONSE:" + response.toString());

                return response.toString();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            //TODO Show that the host is now a favorite
        }
    }

    class DeleteFavorite extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/favorites/"+"this ID"); //TODO Id
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
            //TODO Update GUI
        }
    }
}
