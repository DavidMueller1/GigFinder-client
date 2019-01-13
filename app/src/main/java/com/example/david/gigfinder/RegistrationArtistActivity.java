package com.example.david.gigfinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.david.gigfinder.data.Artist;
import com.example.david.gigfinder.data.enums.Genre;
import com.example.david.gigfinder.tools.ColorTools;
import com.example.david.gigfinder.tools.ImageTools;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

public class RegistrationArtistActivity extends AppCompatActivity {

    private static final String TAG = "MYLOG_RegistrationArtistActivity";
    private static final int PICK_IMAGE = 1;

    private TextView profilePictureTitle;
    private ImageView profilePictureButton;
    private TextView nameTitle;
    private EditText nameField;
    private TextView descriptionTitle;
    private EditText descriptionField;
    private TextView genreTitle;
    private Spinner genreSpinner;
    private Button backgroundColorPickerButton;
    private Button registrationButton;

    private ColorPicker colorPicker;

    private Artist artist;
    private Bitmap profilePicture;
    private String idToken;

    private ArrayAdapter<String> adapter;
    private JSONArray genres;
    private String[] genreStrings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_artist);

        idToken = getIntent().getExtras().getString("idToken");

        GetGenres getGenres = new GetGenres();
        getGenres.execute();

        artist = new Artist();

        profilePictureTitle = findViewById(R.id.registration_artist_image_title);
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
                    profilePicture = ImageTools.decodeUri(path, getContentResolver());

                    ViewGroup.LayoutParams params = profilePictureButton.getLayoutParams();
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    profilePictureButton.setBackground(null);
                    profilePictureButton.setLayoutParams(params);
                    profilePictureButton.setImageBitmap(profilePicture);
                    profilePictureButton.setImageTintList(null);

                    findViewById(R.id.registration_artist_image_hint).setVisibility(View.VISIBLE);
                } catch (FileNotFoundException e) {
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
        nameTitle.setTextColor(color);
        descriptionTitle.setTextColor(color);
        genreTitle.setTextColor(color);
    }

    /**
     * Called when the user presses the registrate button
     */
    private void performRegistration() {
        Log.d(TAG, "Checking user input...");
        if(checkUserInputBasic()) {
            Log.d(TAG, "User input ok");

            SendRegisterArtist sendRegisterArtist = new SendRegisterArtist();
            sendRegisterArtist.execute(artist.getName(), artist.getDescription(), String.valueOf(artist.getColor()));
        }
    }

    /**
     * Checks whether the user input is valid (ex. name not empty)
     */
    private boolean checkUserInputBasic() {
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
        genreStrings = new String[1]; //Change length to num of selected genres
        genreStrings[0] = genreSpinner.getSelectedItem().toString();
        //TODO just fill this list with selected genres

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

    class SendRegisterArtist extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
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
                jsonObject.put("name", params[0]);
                jsonObject.put("description", params[1]);
                jsonObject.put("backgroundColor", params[2]);
                jsonObject.put("artistGenres", genresToJson(genreStrings));
                //jsonObject.put("image", params[4]);
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
                            Log.d(TAG, "SendRegisterArtist: STATUS CODE: " + statusCode);
                            Log.d(TAG, "SendRegisterArtist: RESPONESE MESSAGE: " + httpConn.getResponseMessage());
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
            if(!result.equals("")) {
                try {
                    JSONObject user = new JSONObject(result);
                    SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE).edit();
                    editor.putInt("userId", user.getInt("id"));
                    editor.putString("user", "artist");
                    editor.putInt("userColor", user.getInt("backgroundColor"));
                    editor.apply();
                    //TODO: We should probably cache everything here
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(RegistrationArtistActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("idToken", idToken);
                intent.putExtra("user", "artist");
                startActivity(intent);
                finish();
            }
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
}
