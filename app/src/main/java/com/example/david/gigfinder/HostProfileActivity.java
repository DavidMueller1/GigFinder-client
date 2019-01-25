package com.example.david.gigfinder;

import android.app.AlertDialog;
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

    // Social Media
    private TextView soundcloudText;
    private TextView facebookText;
    private TextView twitterText;
    private TextView youtubeText;
    private TextView instagramText;
    private TextView spotifyText;
    private TextView webText;

    private Button sendMsgBtn;
    private Button addToFavsBtn;

    private FrameLayout progress;

    //private JSONObject hostJson;
    private int userId;
    private int profileUserId;
    private String picture;
    String idToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_profile);

        idToken = getIntent().getExtras().getString("idToken");

        sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE);
        profileUserId = getIntent().getExtras().getInt("profileUserId");

        userId = sharedPreferences.getInt("userId", -1);

        imageButton = findViewById(R.id.profile_host_profilePicture);
        nameText = findViewById(R.id.profile_host_name);
        descriptionText = findViewById(R.id.profile_host_description);
        locationText = findViewById(R.id.profile_host_location_text);
        locationIcon = findViewById(R.id.profile_host_location_icon);
        locationContainer = findViewById(R.id.profile_host_location_container);
        genresText = findViewById(R.id.profile_host_genre);

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
                Intent intent = new Intent(HostProfileActivity.this, ChatActivity.class);
                intent.putExtra("idToken", idToken);
                intent.putExtra("profileUserId", profileUserId);
                intent.putExtra("name", nameText.getText().toString());
                intent.putExtra("picture", picture);
                startActivity(intent);
            }
        });

        addToFavsBtn = findViewById(R.id.addToFavsBtn);
        addToFavsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToFavorites();
            }
        });

        GetHost getHost = new GetHost();
        getHost.execute(String.valueOf(profileUserId));
    }

    /**
     * Adds the Host to Favorites
     */
    private void addToFavorites() {
        // TODO check if already in favorites
        PostFavorite postFavorite = new PostFavorite();
        postFavorite.execute();
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
        TextView socialMediaLabel = findViewById(R.id.profile_host_social_media_label);
        if(ColorTools.isBrightColorBool(color)) {
            descriptionLabel.setTextColor(titleBarColor);
            socialMediaLabel.setTextColor(titleBarColor);
        }
        else {
            descriptionLabel.setTextColor(color);
            socialMediaLabel.setTextColor(color);
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

            if(sharedPreferences.getString("user", "").equals("artist")) {
                sendMsgBtn.setVisibility(View.VISIBLE);
                addToFavsBtn.setVisibility(View.VISIBLE);
            }
            else {
                sendMsgBtn.setVisibility(View.GONE);
                addToFavsBtn.setVisibility(View.GONE);
            }

            if(Utils.isUserInFavorites(profileUserId, sharedPreferences.getString("favorites", ""))){
                addToFavsBtn.setClickable(false);
                //TODO Change design
            }

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

            JSONArray socialMedias = userProfile.getJSONArray("hostSocialMedias");

            String socials = sharedPreferences.getString("social medias", "");
            JSONArray socialMediaArrays = new JSONArray(socials);

            for(int i=0; i<socialMedias.length(); i++){
                JSONObject jsonObject = Utils.getSocialMedia(socialMedias.getJSONObject(i).getInt("socialMediaId"), socialMediaArrays);
                displaySocialMedia(jsonObject.getString("name"), socialMedias.getJSONObject(i).getString("handle"), jsonObject.getString("website"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays a SocialMediaLink
     * @param socialMedia
     * @param text
     * @param socialMediaLink
     */
    private void displaySocialMedia(String socialMedia, final String text, final String socialMediaLink) {
        LinearLayout container;
        final Uri link = Uri.parse(socialMediaLink + text);

        switch(socialMedia) {
            case Utils.ID_SOUNDCLOUD:
                soundcloudText.setText(text);
                container = findViewById(R.id.profile_soundcloud);
                container.setVisibility(View.VISIBLE);
                container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, link);
                        startActivity(browserIntent);
                    }
                });
                break;
            case Utils.ID_FACEBOOK:
                facebookText.setText(text);
                container = findViewById(R.id.profile_facebook);
                container.setVisibility(View.VISIBLE);
                container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, link);
                        startActivity(browserIntent);
                    }
                });
                break;
            case Utils.ID_TWITTER:
                twitterText.setText(text);
                container = findViewById(R.id.profile_twitter);
                container.setVisibility(View.VISIBLE);
                container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, link);
                        startActivity(browserIntent);
                    }
                });
                break;
            case Utils.ID_YOUTUBE:
                youtubeText.setText(text);
                container = findViewById(R.id.profile_youtube);
                container.setVisibility(View.VISIBLE);
                container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, link);
                        startActivity(browserIntent);
                    }
                });
                break;
            case Utils.ID_INSTAGRAM:
                instagramText.setText(text);
                container = findViewById(R.id.profile_instagram);
                container.setVisibility(View.VISIBLE);
                container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, link);
                        startActivity(browserIntent);
                    }
                });
                break;
            case Utils.ID_SPOTIFY:
                spotifyText.setText(text);
                container = findViewById(R.id.profile_spotify);
                container.setVisibility(View.VISIBLE);
                container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, link);
                        startActivity(browserIntent);
                    }
                });
                break;
            case Utils.ID_WEB:
                webText.setText(text);
                container = findViewById(R.id.profile_web);
                container.setVisibility(View.VISIBLE);
                container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openWebsiteDialog(Uri.parse(text));
                    }
                });
                break;

        }

    };

    /**
     * Displays the Website dialog
     * @param link
     */
    private void openWebsiteDialog(final Uri link){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        View mView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();

        Button cancelBtn = (Button) mView.findViewById(R.id.cancelBtn);
        Button proceedBtn = (Button) mView.findViewById(R.id.proceedBtn);
        TextView websiteText = (TextView) mView.findViewById(R.id.custom_dialoge_text);
        TextView dialogTitle = (TextView) mView.findViewById(R.id.custom_dialoge_title);

        websiteText.setText(getString(R.string.website_dialog_1) + link.toString() + getString(R.string.website_dialog_2));
        dialogTitle.setText(getString(R.string.website_dialog_title));

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        proceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, link);
                startActivity(browserIntent);
            }
        });
    }

    private void displayProfilePicture(String result) {
        try {
            picture = result;
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

                Log.d(TAG, String.valueOf(userId));

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
