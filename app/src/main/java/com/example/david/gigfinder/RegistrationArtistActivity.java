package com.example.david.gigfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.david.gigfinder.data.Artist;
import com.example.david.gigfinder.tools.ColorTools;
import com.example.david.gigfinder.tools.ImageTools;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

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

import static com.example.david.gigfinder.GigFinderFirebaseMessagingService.sendDeviceToken;

public class RegistrationArtistActivity extends AppCompatActivity {

    private static final String TAG = "MYLOG_RegistrationArtistActivity";
    private static final int PICK_IMAGE = 1;

    private TextView profilePictureTitle;
    private TextView profilePictureHint;
    private ImageView profilePictureButton;
    private TextView nameTitle;
    private EditText nameField;
    private TextView descriptionTitle;
    private EditText descriptionField;
    private TextView genreTitle;
    private Spinner genreSpinner;
    private TextView socialMediaTitle;
    private EditText soundcloudField;
    private EditText facebookField;
    private EditText twitterField;
    private EditText youtubeField;
    private EditText instagramField;
    private EditText spotifyField;
    private EditText webField;
    private Button backgroundColorPickerButton;
    private Button registrationButton;

    private FrameLayout progress;

    private ColorPicker colorPicker;

    private Artist artist;
    private Uri profilePictureUri;
    private String idToken;

    private boolean pictureChosen;

    private ArrayAdapter<String> adapter;

    //Genres
    private JSONArray genres;
    private String[] myGenres;

    //Social Media
    private JSONArray socialMedias;
    private JSONArray mySocialMedias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_artist);

        idToken = getIntent().getExtras().getString("idToken");

        pictureChosen = false;
        progress = findViewById(R.id.progressBarHolder);

        GetGenres getGenres = new GetGenres();
        getGenres.execute();

        GetSocialMedias getSocialMedias = new GetSocialMedias();
        getSocialMedias.execute();

        artist = new Artist();
        mySocialMedias = new JSONArray();

        profilePictureTitle = findViewById(R.id.registration_artist_image_title);
        profilePictureHint = findViewById(R.id.registration_artist_image_hint);
        profilePictureButton = findViewById(R.id.registration_artist_profilePicture);
        profilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performProfilePictureSelection();
            }
        });
        nameTitle = findViewById(R.id.registration_artist_name_title);
        nameField = findViewById(R.id.registration_artist_name);
        descriptionTitle = findViewById(R.id.registration_artist_description_title);
        descriptionField = findViewById(R.id.registration_artist_description);
        genreTitle = findViewById(R.id.registration_artist_genre_title);
        genreSpinner = findViewById(R.id.registration_artist_genre);
        //Replaced the Strings with the Genre-Enum

        socialMediaTitle = findViewById(R.id.registration_artist_social_media_title);
        soundcloudField = findViewById(R.id.registration_soundcloud);
        facebookField = findViewById(R.id.registration_facebook);
        twitterField = findViewById(R.id.registration_twitter);
        youtubeField = findViewById(R.id.registration_youtube);
        instagramField = findViewById(R.id.registration_instagram);
        spotifyField = findViewById(R.id.registration_spotify);
        webField = findViewById(R.id.registration_web);

        backgroundColorPickerButton = findViewById(R.id.button_registration_colorPicker);
        backgroundColorPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performColorSelection();
            }
        });


        registrationButton = findViewById(R.id.button_artist_registration);
        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegistration();
            }
        });

        colorPicker = new ColorPicker(RegistrationArtistActivity.this, 214, 74, 31);
        colorPicker.enableAutoClose();
        colorPicker.setCallback(new ColorPickerCallback() {
            @Override
            public void onColorChosen(int color) {
                applyColor(color);
            }
        });
    }

    /**
     * Called when the user presses the profile picture button
     * User can choose between camera and gallery
     */
    private void performProfilePictureSelection() {
        /*Intent pickIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent chooserIntent = Intent.createChooser(pickIntent, getResources().getString(R.string.pick_photo_intent));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {takePhotoIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);*/
        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickIntent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_IMAGE) {
            if(resultCode == RESULT_OK) {
                Uri path = data.getData();

                try {
                    //profilePicture = ImageTools.decodeUri(path, getContentResolver());
                    //profilePictureFile = new File(path.getPath());
                    profilePictureUri = path;

                    ViewGroup.LayoutParams params = profilePictureButton.getLayoutParams();
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    profilePictureButton.setBackground(null);
                    profilePictureButton.setLayoutParams(params);
                    /*profilePictureButton.setImageBitmap(profilePicture);*/
                    profilePictureButton.setImageTintList(null);
                    RequestOptions options = new RequestOptions()
                            .centerCrop()
                            .placeholder(ImageTools.PROFILE_PICTURE_PLACEHOLDER)
                            .override(ImageTools.PROFILE_PICTURE_SIZE)
                            .transforms(new CenterCrop(), new RoundedCorners(30));

                    Glide.with(getApplicationContext())
                            .load(path)
                            .apply(options)
                            .into(profilePictureButton);

                    findViewById(R.id.registration_artist_image_hint).setVisibility(View.VISIBLE);
                    pictureChosen = true;
                } catch (Exception e) {
                    Log.d(TAG, "File not found");
                }
            }
        }
    }

    /**
     * Called when the user presses the choose color button
     */
    private void performColorSelection() {
        colorPicker.show();
    }

    /**
     * Called when the user chose a color
     */
    private void applyColor(int color) {
        int textColor = ColorTools.isBrightColor(color);
        artist.setColor(color);
        findViewById(R.id.registration_artist_title_bar_form).setBackgroundColor(color);
        TextView title = findViewById(R.id.registration_artist_title);
        title.setTextColor(textColor);
        getWindow().setStatusBarColor(ColorTools.getSecondaryColor(color));
        registrationButton.setBackgroundTintList(ColorStateList.valueOf(color));
        registrationButton.setTextColor(textColor);
        backgroundColorPickerButton.setBackgroundTintList(ColorStateList.valueOf(color));
        backgroundColorPickerButton.setTextColor(textColor);

        profilePictureTitle.setTextColor(color);
        profilePictureHint.setTextColor(color);
        nameTitle.setTextColor(color);
        descriptionTitle.setTextColor(color);
        genreTitle.setTextColor(color);
        socialMediaTitle.setTextColor(color);
    }

    /**
     * Called when the user presses the registrate button
     */
    private void performRegistration() {
        Log.d(TAG, "Checking user input...");
        if(checkUserInputBasic()) {
            Log.d(TAG, "User input ok");

            byte[] imageByteArray = null;
            try {
                imageByteArray = ImageTools.uriToByteArray(profilePictureUri, getApplicationContext());
            } catch (IOException e) {
                Log.d(TAG, "Uri not found");
                Toast.makeText(getApplicationContext(),"Uri not found",Toast.LENGTH_SHORT).show();

            }

            SendRegisterArtist sendRegisterArtist = new SendRegisterArtist();
            sendRegisterArtist.execute(artist.getName(), artist.getDescription(), String.valueOf(artist.getColor()), Base64.encodeToString(imageByteArray, Base64.DEFAULT));
        }
    }

    /**
     * Checks whether the user input is valid (ex. name not empty)
     */
    private boolean checkUserInputBasic() {
        // Check whether a profile picture is selected
        if(!pictureChosen) {
            Toast.makeText(getApplicationContext(),"Bitte ein Profilbild wählen.",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "No profile picture");
            return false;
        }

        // Check whether name field is empty
        artist.setName(nameField.getText().toString());
        if(artist.getName().equals("")) {
            Toast.makeText(getApplicationContext(),"Namensfeld ist leer.",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Namefield empty.");
            return false;
        }

        // Description is optional (can be empty)
        artist.setDescription(descriptionField.getText().toString());

        // TODO multiple genres selectable
        myGenres = new String[1]; //Change length to num of selected genres
        myGenres[0] = genreSpinner.getSelectedItem().toString();
        //TODO just fill this list with selected genres

        postSocialMedia();

        return true;
    }

    private void showGenres(String result){
        try {
            genres = new JSONArray(result);
            String[] genreStrings = new String[genres.length()];
            for(int i=0; i<genres.length(); i++){
                genreStrings[i] = genres.getJSONObject(i).getString("value");
            }
            adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, genreStrings);
            genreSpinner.setAdapter(adapter);
        } catch (JSONException e) {
             e.printStackTrace();
        }
    }

    private JSONArray genresToJson(String[] genreStrings) throws JSONException {
        JSONArray genresJson = new JSONArray();
        for(String i : genreStrings){
           for(int j=0; j<genres.length(); j++){
               if(genres.getJSONObject(j).getString("value").equals(i)){
                   JSONObject genreObject = new JSONObject();
                   genreObject.put("genreId", genres.getJSONObject(j).getInt("id"));
                   genresJson.put(genreObject);
               }
           }
        }
        Log.d(TAG, genresJson.toString());
        return genresJson;
    }

    private void postSocialMedia(){
            if(!soundcloudField.getText().toString().equals("")){
                pickSocialMedia("Soundcloud", soundcloudField.getText().toString());
            }
            if(!facebookField.getText().toString().equals("")){
                pickSocialMedia("Facebook", facebookField.getText().toString());
            }
            if(!twitterField.getText().toString().equals("")){
                pickSocialMedia("Twitter", twitterField.getText().toString());
            }
            if(!youtubeField.getText().toString().equals("")){
                pickSocialMedia("YouTube", youtubeField.getText().toString());
            }
            if(!instagramField.getText().toString().equals("")){
                //pickSocialMedia("Soundcloud", soundcloudField.getText().toString()); TODO
            }
            if(!spotifyField.getText().toString().equals("")){
                pickSocialMedia("Spotify", spotifyField.getText().toString());
            }
            if(!webField.getText().toString().equals("")){
                pickSocialMedia("Website", webField.getText().toString());
            }
    }

    private void pickSocialMedia(String name, String handle){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("SocialMediaId", getSocialMediaId(name));
            jsonObject.put("Handle", handle);
            mySocialMedias.put(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private int getSocialMediaId(String name){
        for(int i = 0; i< socialMedias.length(); i++){
            try {
                if(socialMedias.getJSONObject(i).getString("name").equals(name)){
                    return socialMedias.getJSONObject(i).getInt("id");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return 0;
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

    class SendRegisterArtist extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            displayLoadingScreen(true);
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/artists");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Authorization", idToken);
                urlConnection.setRequestProperty("Content-Type","application/json");
                urlConnection.setRequestMethod("POST");
                urlConnection.setUseCaches(false);
                urlConnection.setDoOutput(true);


                //Send data
                DataOutputStream os = new DataOutputStream(urlConnection.getOutputStream());
                JSONObject jsonObject = new JSONObject();
                JSONObject imageJson = new JSONObject();
                imageJson.put("Image", params[3]);
                jsonObject.put("name", params[0]);
                jsonObject.put("description", params[1]);
                jsonObject.put("backgroundColor", params[2]);
                jsonObject.put("profilePicture", imageJson);
                jsonObject.put("artistGenres", genresToJson(myGenres));
                if(mySocialMedias.length()>0){
                    jsonObject.put("artistSocialMedias", mySocialMedias);
                    Log.d(TAG, mySocialMedias.toString());
                }
                os.writeBytes(jsonObject.toString());
                os.close();

                //Get response
                InputStream is = null;
                try {
                    is = urlConnection.getInputStream();
                } catch (IOException ioe) {
                    displayLoadingScreen(false);
                    if (urlConnection instanceof HttpURLConnection) {
                        HttpURLConnection httpConn = (HttpURLConnection) urlConnection;
                        int statusCode = httpConn.getResponseCode();
                        if (statusCode != 200) {
                            is = httpConn.getErrorStream();
                            Log.d(TAG, "SendRegisterArtist: STATUS CODE: " + statusCode);
                            Log.d(TAG, "SendRegisterArtist: RESPONSE MESSAGE: " + httpConn.getResponseMessage());
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

                Log.d(TAG, "SendRegisterArtist: RESPONSE:" + response.toString());

                return response.toString();
            } catch (ProtocolException e) {
                displayLoadingScreen(false);
                e.printStackTrace();
            } catch (MalformedURLException e) {
                displayLoadingScreen(false);
                e.printStackTrace();
            } catch (IOException e) {
                displayLoadingScreen(false);
                e.printStackTrace();
            } catch (JSONException e) {
                displayLoadingScreen(false);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if(!result.equals("")) {
                try {
                    JSONObject user = new JSONObject(result);
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(user);
                    SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE).edit();
                    editor.putInt("userId", user.getInt("id"));
                    editor.putString("userProfile", jsonArray.toString());
                    editor.putString("user", "artist");
                    editor.putInt("userColor", user.getInt("backgroundColor"));
                    editor.apply();
                    //TODO: We should probably cache everything here

                    sendDeviceToken(getBaseContext());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(RegistrationArtistActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("idToken", idToken);
                displayLoadingScreen(false);
                startActivity(intent);
                finish();
            }
            displayLoadingScreen(false);
        }
    }

    class GetGenres extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/genres");
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
            Log.d(TAG, "GENRES: " + result);

            SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE).edit();
            editor.putString("genres", result);
            editor.apply();
            showGenres(result);
        }
    }

    class GetSocialMedias extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/socialmedias");
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
            Log.d(TAG, "Social Medias: " + result);

            SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE).edit();
            editor.putString("social medias", result);
            editor.apply();

            try {
                socialMedias = new JSONArray(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
