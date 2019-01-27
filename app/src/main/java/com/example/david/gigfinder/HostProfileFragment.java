package com.example.david.gigfinder;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.david.gigfinder.adapters.ReviewAdapter;
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
import java.util.ArrayList;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class HostProfileFragment extends Fragment {
    private static final String TAG = "APPLOG - HostProfileFragment";

    private static final int RESULT_EDIT_PROFILE = 1;
    private static final int RESULT_PICK_IMAGE = 2;

    private SharedPreferences sharedPreferences;

    private int userID;
    private Button testDeleteBtn;
    private Button testSignOutBtn;
    private ImageView imageButton;
    private TextView nameText;
    private TextView descriptionText;
    private LinearLayout locationContainer;
    private ImageView locationIcon;
    private TextView locationText;
    private TextView genresText;
    private String idToken;
    private RatingBar ratingBar;

    private RelativeLayout showAllReviewsButton;
    private ListView reviewListView;

    // Social Media
    private TextView soundcloudText;
    private TextView facebookText;
    private TextView twitterText;
    private TextView youtubeText;
    private TextView instagramText;
    private TextView spotifyText;
    private TextView webText;

    private FrameLayout progress;

    private ArrayList<String[]> reviewStrings;
    private ReviewAdapter reviewAdapter;
    boolean isReviewListExpanded;

    private String profilePictureString;
    private JSONObject profilePictureObject;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_host_profile, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        idToken = getArguments().getString("idToken");

        sharedPreferences = getContext().getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE);

        testDeleteBtn = getView().findViewById(R.id.deleteBtn);
        testSignOutBtn = getView().findViewById(R.id.signOutBtn);
        imageButton = getView().findViewById(R.id.profile_host_profilePicture);
        nameText = getView().findViewById(R.id.profile_host_name);
        descriptionText = getView().findViewById(R.id.profile_host_description);
        locationText = getView().findViewById(R.id.profile_host_location_text);
        locationIcon = getView().findViewById(R.id.profile_host_location_icon);
        locationContainer = getView().findViewById(R.id.profile_host_location_container);
        genresText = getView().findViewById(R.id.profile_host_genre);

        soundcloudText = getView().findViewById(R.id.profile_soundcloud_text);
        facebookText = getView().findViewById(R.id.profile_facebook_text);
        twitterText = getView().findViewById(R.id.profile_twitter_text);
        youtubeText = getView().findViewById(R.id.profile_youtube_text);
        instagramText = getView().findViewById(R.id.profile_instagram_text);
        spotifyText = getView().findViewById(R.id.profile_spotify_text);
        webText = getView().findViewById(R.id.profile_web_text);

        ratingBar = getView().findViewById(R.id.profile_artist_rating_bar);


        isReviewListExpanded = false;
        reviewListView = getView().findViewById(R.id.profile_artist_review_list);

        showAllReviewsButton = getView().findViewById(R.id.profile_artist_button_show_all);
        showAllReviewsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayReviewList();
            }
        });

        progress = getView().findViewById(R.id.progressBarHolder);


        reviewStrings = new ArrayList<>();

        reviewAdapter = new ReviewAdapter(getContext(), reviewStrings);
        reviewListView.setAdapter(reviewAdapter);

        updateProfile(sharedPreferences.getString("userProfile", "x"));

        if(idToken.equals("offline")) {
            offlineMode();
        }else{
            //online mode
            displayLoadingScreen(true);

            testDeleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   delteUserDialog();
                }
            });

            testSignOutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signOut();
                }
            });
        }

        super.onActivityCreated(savedInstanceState);
    }

    private void performProfilePictureSelection() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickIntent, RESULT_PICK_IMAGE);
    }

    /**
     * Updates the color of all relevant elements
     */
    private void updateColor(int color) {
        int fontColor = ColorTools.isBrightColor(color);
        nameText.setTextColor(fontColor);
        genresText.setTextColor(fontColor);
        testSignOutBtn.setTextColor(fontColor);
        testDeleteBtn.setTextColor(fontColor);


        int titleBarColor = ColorTools.getSecondaryColor(color);
        // happens in MainActivity, otherwise the statusBar changes on chat tab
        //getActivity().getWindow().setStatusBarColor(titleBarColor);

        TextView descriptionLabel = getView().findViewById(R.id.profile_host_description_label);
        TextView socialMediaLabel = getView().findViewById(R.id.profile_host_social_media_label);
        TextView reviewLabel = getView().findViewById(R.id.profile_artist_review_label);

        if(ColorTools.isSuperBrightColorBool(color)) { // Check if the color is too bright for the white background
            getView().findViewById(R.id.profile_host_title_bar_form).setBackgroundColor(titleBarColor);
            testDeleteBtn.setBackgroundTintList(ColorStateList.valueOf(titleBarColor));
            testSignOutBtn.setBackgroundTintList(ColorStateList.valueOf(titleBarColor));
            locationIcon.setImageTintList(ColorStateList.valueOf(titleBarColor));
            ratingBar.setProgressTintList(ColorStateList.valueOf(titleBarColor));
            descriptionLabel.setTextColor(titleBarColor);
            socialMediaLabel.setTextColor(titleBarColor);
            reviewLabel.setTextColor(titleBarColor);
        }
        else {
            getView().findViewById(R.id.profile_host_title_bar_form).setBackgroundColor(color);
            testDeleteBtn.setBackgroundTintList(ColorStateList.valueOf(titleBarColor));
            testSignOutBtn.setBackgroundTintList(ColorStateList.valueOf(titleBarColor));
            locationIcon.setImageTintList(ColorStateList.valueOf(color));
            ratingBar.setProgressTintList(ColorStateList.valueOf(color));
            descriptionLabel.setTextColor(color);
            socialMediaLabel.setTextColor(color);
            reviewLabel.setTextColor(color);
        }



        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("titleBarColor", titleBarColor);
        editor.putInt("userColor", color);
        editor.commit();
    }

    /**
     * Updates the Profile using the cached Json Object
     * @param jsonString
     */
    private void updateProfile(String jsonString){
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            JSONObject userProfile = jsonArray.getJSONObject(0);

            GetProfilePicture getProfilePicture = new GetProfilePicture();
            getProfilePicture.execute(userProfile.getInt("profilePictureId") + "");

            GetReview getReview = new GetReview();
            getReview.execute();

            final String name = userProfile.getString("name");
            nameText.setText(name);
            descriptionText.setText(userProfile.getString("description"));
            userID = userProfile.getInt("id");
            updateColor(Integer.parseInt(userProfile.getString("backgroundColor")));
            final float lat = Float.parseFloat(userProfile.getString("latitude"));
            final float lng = Float.parseFloat(userProfile.getString("longitude"));

            String myGenres = "(";
            for(int i=0; i<userProfile.getJSONArray("hostGenres").length(); i++){
                myGenres = myGenres.concat(Utils.genreIdToString(userProfile.getJSONArray("hostGenres").getJSONObject(i).getInt("genreId"),
                        sharedPreferences.getString("genres", "x")));
                if(i < userProfile.getJSONArray("hostGenres").length()-1){
                    myGenres = myGenres.concat(", ");
                }
            }
            myGenres = myGenres.concat(")");
            genresText.setText(myGenres);

            SharedPreferences.Editor editor = getActivity().getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE).edit();
            editor.putInt("userId", userID);
            editor.apply();
            //TODO: We should probably cache everything here

            locationContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)", lat, lng, lat, lng, name);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(intent);
                }
            });

            String address = GeoTools.getAddressFromLatLng(getContext(), new LatLng(lat, lng));
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
     * @param socialMedia The name of the Social Media Platform (e.g. facebook)
     * @param text The "Name" of the account
     * @param socialMediaLink The Link to the userprofile
     */
    private void displaySocialMedia(String socialMedia, final String text, final String socialMediaLink) {
        LinearLayout container;
        final Uri link = Uri.parse(socialMediaLink + text);
        Log.d(TAG, "Link: " + link.toString());
        getView().findViewById(R.id.profile_host_social_media_label).setVisibility(View.VISIBLE);

        switch(socialMedia) {
            case Utils.ID_SOUNDCLOUD:
                soundcloudText.setText(text);
                container = getView().findViewById(R.id.profile_soundcloud);
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
                container = getView().findViewById(R.id.profile_facebook);
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
                container = getView().findViewById(R.id.profile_twitter);
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
                container = getView().findViewById(R.id.profile_youtube);
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
                container = getView().findViewById(R.id.profile_instagram);
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
                container = getView().findViewById(R.id.profile_spotify);
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
                container = getView().findViewById(R.id.profile_web);
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
     * Updates the Reviews in the Profile
     * @param result Contains a JsonArray with the Reviews
     */
    private void displayReviews(String result) {
        try {
            JSONArray jsonArray = new JSONArray(result);
            int arraySize = jsonArray.length();
            float sum = 0f;
            reviewStrings.clear();
            for(int i = 0; i < arraySize; i++) {
                JSONObject reviewJson = jsonArray.getJSONObject(i);

                float rating =  (float) reviewJson.getInt("rating") / 2;
                Log.d(TAG, "Float: " + rating);
                String comment = reviewJson.getString("comment");

                sum += rating;

                reviewStrings.add(new String[]{String.valueOf(rating), comment});
            }

            reviewAdapter.notifyDataSetChanged();
            ratingBar.setRating(sum / arraySize);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows and hides the full list of reviews
     */
    private void displayReviewList() {
        if(!isReviewListExpanded) {
            ObjectAnimator animation = ObjectAnimator.ofFloat(getView().findViewById(R.id.review_triangle_left), "rotation", -180f, 0f);
            animation.setDuration(400);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            animation.start();
            ObjectAnimator animation2 = ObjectAnimator.ofFloat(getView().findViewById(R.id.review_triangle_right), "rotation", 180f, 0f);
            animation2.setDuration(400);
            animation2.setInterpolator(new AccelerateDecelerateInterpolator());
            animation2.start();

            ((TextView) getView().findViewById(R.id.profile_artist_show_all)).setText(getString(R.string.profile_review_button_hide_all));

            reviewListView.setVisibility(View.VISIBLE);
            isReviewListExpanded = true;
        }
        else {
            ObjectAnimator animation = ObjectAnimator.ofFloat(getView().findViewById(R.id.review_triangle_left), "rotation", 0f, -180f);
            animation.setDuration(400);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            animation.start();
            ObjectAnimator animation2 = ObjectAnimator.ofFloat(getView().findViewById(R.id.review_triangle_right), "rotation", 0f, 180f);
            animation2.setDuration(400);
            animation2.setInterpolator(new AccelerateDecelerateInterpolator());
            animation2.start();

            ((TextView) getView().findViewById(R.id.profile_artist_show_all)).setText(getString(R.string.profile_review_button_show_all));

            reviewListView.setVisibility(View.GONE);
            isReviewListExpanded = false;
        }
    }

    /**
     * Displays the Website dialog
     * @param link The link to the external website
     */
    private void openWebsiteDialog(final Uri link){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
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
                Uri uri;
                if(!link.toString().startsWith("http://") && !link.toString().startsWith("https://")){
                    uri = Uri.parse("http://" + link.toString());
                }else{
                    uri = link;
                }
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(browserIntent);
                dialog.cancel();
            }
        });
    }

    /**
     * Asks the user if he wants to delete the profile
     */
    private void delteUserDialog(){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        View mView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();

        Button cancelBtn = (Button) mView.findViewById(R.id.cancelBtn);
        Button proceedBtn = (Button) mView.findViewById(R.id.proceedBtn);
        TextView dialogText = (TextView) mView.findViewById(R.id.custom_dialoge_text);
        TextView dialogTitle = (TextView) mView.findViewById(R.id.custom_dialoge_title);

        dialogText.setText("Sind Sie sicher, dass die ihr Profil dauerhaft löschen möchten");
        dialogTitle.setText("Profil Löschen");

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        proceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNetworkAvailable()) {
                    DeleteUser deleteUser = new DeleteUser();
                    deleteUser.execute();
                }
                dialog.cancel();
            }
        });
    }

    /**
     * Displays the profile picture
     * @param result Contains a JSONObject of the image class
     */
    private void displayProfilePicture(String result) {
        try {
            profilePictureObject = new JSONObject(result);

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

            profilePictureString = profilePictureObject.getString("image");

            Glide.with(getContext())
                    .load(Base64.decode(profilePictureString, Base64.DEFAULT))
                    .apply(options)
                    .into(imageButton);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        displayLoadingScreen(false);
    }

    /**
     * Displays and hides a rotating loading animation
     * @param isLoading Is the laoding screen visible?
     */
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
     * Deletes SharedPrefs and opens the LoginActivity which then SignsOut the user
     */
    private void signOut() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().apply();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.putExtra("SignOut", true);
        startActivity(intent);
        getActivity().finish();
    }

    /**
     * Checks internet connection and starts offline mode
     */
    private void offlineMode(){
        if(isNetworkAvailable()){
            //Go back to Login
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        }else{
            //Show Popup
        }
    }

    /**
     * Checks if Network is Available
     * @return True if there is an Internet Connection
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Gets the profile picture from the server and GUI update
     */
    private class GetProfilePicture extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/pictures/" + params[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Authorization", idToken);
                urlConnection.setRequestMethod("GET");
                if(isNetworkAvailable()) {
                    urlConnection.addRequestProperty("Cache-Control", "max-stale=" + getString(R.string.max_stale_online));
                }else{
                    urlConnection.addRequestProperty("Cache-Control", "max-stale=" + getString(R.string.max_stale_offline));
                }

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
        }
    }


    /**
     * Tells the server to delete the user
     */
    private class DeleteUser extends AsyncTask<String, Void, String> {

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
            if (result != null) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                Log.d(TAG, "DELETE HOST: " + result);
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.putExtra("SignOut", true);
                startActivity(intent);

                try {
                    HttpResponseCache cache = HttpResponseCache.getInstalled();
                    cache.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Toast.makeText(getActivity().getApplicationContext(), "Profil Gelöscht", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }

    /**
     * Gets the reviews of the user from the server
     */
    private class GetReview extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/reviews?host=" + userID);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Authorization", idToken);
                urlConnection.setRequestMethod("GET");
                if(!isNetworkAvailable()){
                    urlConnection.addRequestProperty("Cache-Control", "max-stale=" + getString(R.string.max_stale_offline));
                }

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
            Log.d(TAG, "USER REVIEWS: " + result);
            displayReviews(result);
        }
    }

}
