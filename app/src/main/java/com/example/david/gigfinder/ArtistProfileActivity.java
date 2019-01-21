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

public class ArtistProfileActivity extends AppCompatActivity {

    private static final String TAG = "ArtistProfileActivity";

    private static final int ID_SOUNDCLOUD = 0;
    private static final int ID_FACEBOOK = 1;
    private static final int ID_TWITTER = 2;
    private static final int ID_YOUTUBE = 3;
    private static final int ID_INSTAGRAM = 4;
    private static final int ID_SPOTIFY = 5;
    private static final int ID_WEB = 6;

    SharedPreferences sharedPreferences;

    private ImageView imageButton;
    private TextView nameText;
    private TextView descriptionText;
    private TextView genresText;
    private Button sendMsgBtn;
    private Button addToFavsBtn;

    // Social Media
    private TextView soundcloudText;
    private TextView facebookText;
    private TextView twitterText;
    private TextView youtubeText;
    private TextView instagramText;
    private TextView spotifyText;
    private TextView webText;

    private FrameLayout progress;

    //private JSONObject hostJson;
    private int userId;
    private int profileUserId;
    String idToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_profile);


        sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE);

        profileUserId = getIntent().getIntExtra("profileUserId", -1);
        idToken = getIntent().getExtras().getString("idToken");
        /*try {
            hostJson = new JSONObject(getIntent().getExtras().getString("host"));
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE);
        userId = prefs.getInt("userId", 0);


//        profileUserId = 30;
        imageButton = findViewById(R.id.profile_artist_profilePicture);
        nameText = findViewById(R.id.profile_artist_name);
        descriptionText = findViewById(R.id.profile_artist_description);
        genresText = findViewById(R.id.profile_artist_genre);

        soundcloudText = findViewById(R.id.profile_soundcloud_text);
        facebookText = findViewById(R.id.profile_facebook_text);
        twitterText = findViewById(R.id.profile_twitter_text);
        youtubeText = findViewById(R.id.profile_youtube_text);
        instagramText = findViewById(R.id.profile_instagram_text);
        spotifyText = findViewById(R.id.profile_spotify_text);
        webText = findViewById(R.id.profile_web_text);

        progress = findViewById(R.id.progressBarHolder);
        sendMsgBtn = findViewById(R.id.sendMsgBtn);
        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ArtistProfileActivity.this, ChatActivity.class);
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

        GetUser getUser = new GetUser();
        getUser.execute(profileUserId + "");
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
        findViewById(R.id.profile_artist_title_bar_form).setBackgroundColor(color);
        sendMsgBtn.setBackgroundColor(color);
        addToFavsBtn.setBackgroundColor(color);


        int titleBarColor = ColorTools.getSecondaryColor(color);
        // happens in MainActivity, otherwise the statusBar changes on chat tab
        getWindow().setStatusBarColor(titleBarColor);

        TextView descriptionLabel = findViewById(R.id.profile_artist_description_label);
        if(ColorTools.isBrightColorBool(color)) {
            descriptionLabel.setTextColor(titleBarColor);
        }
        else {
            descriptionLabel.setTextColor(color);
        }

        descriptionLabel.setTextColor(color);

    }

    private void updateProfile(String result){
        try {
            Log.d(TAG, result);
            JSONObject userProfile = new JSONObject(result);

            GetProfilePicture getProfilePicture = new GetProfilePicture();
            getProfilePicture.execute(userProfile.getInt("profilePictureId") + "");

            final String name = userProfile.getString("name");
            nameText.setText(name);
            descriptionText.setText(userProfile.getString("description"));
            updateColor(Integer.parseInt(userProfile.getString("backgroundColor")));

            String myGenres = "(";
            for(int i=0; i<userProfile.getJSONArray("hostGenres").length(); i++){
                myGenres = myGenres.concat(Utils.genreIdToString(userProfile.getJSONArray("hostGenres").getJSONObject(i).getInt("genreId"), sharedPreferences.getString("genres", "x")));
                if(i < userProfile.getJSONArray("hostGenres").length()-1){
                    myGenres = myGenres.concat(", ");
                }
            }
            myGenres = myGenres.concat(")");
            genresText.setText(myGenres);

            if(sharedPreferences.getString("user", "").equals("artist")) {
                sendMsgBtn.setVisibility(View.GONE);
                addToFavsBtn.setVisibility(View.GONE);
            }
            else {
                sendMsgBtn.setVisibility(View.VISIBLE);
                addToFavsBtn.setVisibility(View.VISIBLE);
            }

            JSONArray socialMedias = userProfile.getJSONArray("artistSocialMedias");

            String socials = sharedPreferences.getString("social medias", "");
            JSONArray socialMediaArrays = new JSONArray(socials);

            for(int i=0; i<socialMedias.length(); i++){
                JSONObject jsonObject = Utils.getSocialMedia(socialMedias.getJSONObject(i).getInt("socialMediaId"), socialMediaArrays);
                //displaySocialMedia(jsonObject.getString("name"), socialMedias.getJSONObject(i).getString("handle"), jsonObject.getString("website"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays a SocialMediaLink
     * @param socialMediaId
     * @param text
     * @param socialMediaLink
     */
    private void displaySocialMedia(int socialMediaId, String text, final String socialMediaLink) {
        LinearLayout container;

        switch(socialMediaId) {
            case ID_SOUNDCLOUD:
                soundcloudText.setText(text);
                container = findViewById(R.id.profile_soundcloud);
                container.setVisibility(View.VISIBLE);
                container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(socialMediaLink));
                        startActivity(browserIntent);
                    }
                });
                break;
            case ID_FACEBOOK:
                facebookText.setText(text);
                container = findViewById(R.id.profile_facebook);
                container.setVisibility(View.VISIBLE);
                container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(socialMediaLink));
                        startActivity(browserIntent);
                    }
                });
                break;
            case ID_TWITTER:
                twitterText.setText(text);
                container = findViewById(R.id.profile_twitter);
                container.setVisibility(View.VISIBLE);
                container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(socialMediaLink));
                        startActivity(browserIntent);
                    }
                });
                break;
            case ID_YOUTUBE:
                youtubeText.setText(text);
                container = findViewById(R.id.profile_youtube);
                container.setVisibility(View.VISIBLE);
                container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(socialMediaLink));
                        startActivity(browserIntent);
                    }
                });
                break;
            case ID_INSTAGRAM:
                instagramText.setText(text);
                container = findViewById(R.id.profile_instagram);
                container.setVisibility(View.VISIBLE);
                container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(socialMediaLink));
                        startActivity(browserIntent);
                    }
                });
                break;
            case ID_SPOTIFY:
                spotifyText.setText(text);
                container = findViewById(R.id.profile_spotify);
                container.setVisibility(View.VISIBLE);
                container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(socialMediaLink));
                        startActivity(browserIntent);
                    }
                });
                break;
            case ID_WEB:
                webText.setText(text);
                container = findViewById(R.id.profile_web);
                container.setVisibility(View.VISIBLE);
                container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(socialMediaLink));
                        startActivity(browserIntent);
                    }
                });
                break;

        }

    };

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
                    .placeholder(ImageTools.PROFILE_PICTURE_PLACEHOLDER)
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
    class GetUser extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            displayLoadingScreen(true);
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/artists/" + params[0]);
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

    class PostReview extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/reviews");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Authorization", idToken);
                urlConnection.setRequestProperty("Content-Type","application/json");
                urlConnection.setRequestMethod("POST");
                urlConnection.setUseCaches(false);
                urlConnection.setDoOutput(true);

                //Send data TODO
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

                Log.d(TAG, "PostReview Response:" + response.toString());

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

        }
    }
}
