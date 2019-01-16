package com.example.david.gigfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.david.gigfinder.data.Host;
import com.example.david.gigfinder.data.enums.Genre;
import com.example.david.gigfinder.tools.ColorTools;
import com.example.david.gigfinder.tools.ImageTools;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
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
import java.util.List;
import java.util.Locale;

public class RegistrationHostActivity extends AppCompatActivity {
    private static final String TAG = "RegistrationHostActivity";
    private static final int PICK_IMAGE = 1;
    private static final int PLACE_PICKER_REQUEST = 2;

    private TextView profilePictureTitle;
    private TextView profilePictureHint;
    private ImageView profilePictureButton;
    private TextView nameTitle;
    private EditText nameField;
    private TextView descriptionTitle;
    private EditText descriptionField;
    private LinearLayout locationButton;
    private ImageView locationButtonIcon;
    private TextView locationButtonText;
    private TextView genreTitle;
    private Spinner genreSpinner;
    private Button backgroundColorPickerButton;
    private Button registrationButton;

    private FrameLayout progress;

    private ColorPicker colorPicker;

    private Host host;
    private Uri profilePictureUri;
    private LatLng position;

    private boolean pictureChosen;

    String idToken;

    private ArrayAdapter<String> adapter;
    private JSONArray genres;
    private String[] genreStrings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_host);

        idToken = getIntent().getExtras().getString("idToken");

        pictureChosen = false;
        progress = findViewById(R.id.progressBarHolder);

        GetGenres getGenres = new GetGenres();
        getGenres.execute();

        GetSocialMedias getSocialMedias = new GetSocialMedias();
        getSocialMedias.execute();

        host = new Host();

        profilePictureTitle = findViewById(R.id.registration_host_image_title);
        profilePictureHint = findViewById(R.id.registration_host_image_hint);
        profilePictureButton = findViewById(R.id.registration_host_profilePicture);
        profilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performProfilePictureSelection();
            }
        });
        nameTitle = findViewById(R.id.registration_host_name_title);
        nameField = findViewById(R.id.registration_host_name);
        descriptionTitle = findViewById(R.id.registration_host_description_title);
        descriptionField = findViewById(R.id.registration_host_description);
        locationButton = findViewById(R.id.registration_host_location_container);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLocationSelection();
            }
        });
        locationButtonText = findViewById(R.id.registration_host_location_text);
        locationButtonIcon = findViewById(R.id.registration_host_location_icon);
        genreTitle = findViewById(R.id.registration_host_genre_title);
        genreSpinner = findViewById(R.id.registration_host_genre);

        backgroundColorPickerButton = findViewById(R.id.button_registration_host_colorPicker);
        backgroundColorPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performColorSelection();
            }
        });


        registrationButton = findViewById(R.id.button_host_registration);
        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegistration();
            }
        });

        colorPicker = new ColorPicker(RegistrationHostActivity.this, 214, 74, 31);
        colorPicker.enableAutoClose();
        colorPicker.setCallback(new ColorPickerCallback() {
            @Override
            public void onColorChosen(int color) {
                applyColor(color);
            }
        });

        position = null;
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

                    findViewById(R.id.registration_host_image_hint).setVisibility(View.VISIBLE);
                    pictureChosen = true;
                } catch (Exception e) {
                    Log.d(TAG, "File not found");
                }


            }
        }
        else if(requestCode == PLACE_PICKER_REQUEST) {
            if(resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                position = place.getLatLng();

                String address = "";
                List<Address> addresses = null;
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.GERMANY);
                try {
                    addresses = geocoder.getFromLocation(position.latitude, position.longitude, 1);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(addresses.size() > 0) {
                    address = addresses.get(0).getAddressLine(0);
                }
                locationButtonText.setText(address);
                locationButtonText.setTypeface(Typeface.DEFAULT);
            }
        }
    }

    /**
     * Called when the user presses the choose location button
     */
    private void performLocationSelection() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("MYLOG", e.getMessage());
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
        host.setColor(color);
        findViewById(R.id.registration_host_title_bar_form).setBackgroundColor(color);
        TextView title = findViewById(R.id.registration_host_title);
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
        locationButtonIcon.setImageTintList(ColorStateList.valueOf(color));
        genreTitle.setTextColor(color);
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

            SendRegisterHost sendRegisterHost = new SendRegisterHost();
            sendRegisterHost.execute(host.getName(), host.getDescription(), String.valueOf(host.getColor()), "" + position.latitude, "" + position.longitude, Base64.encodeToString(imageByteArray, Base64.DEFAULT)); //TODO params
        }
    }

    private boolean checkUserInputBasic(){
        // Check whether a profile picture is selected
        if(!pictureChosen) {
            Toast.makeText(getApplicationContext(),"Bitte ein Profilbild wählen.",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "No profile picture");
            return false;
        }

        // Check whether name field is empty
        host.setName(nameField.getText().toString());
        if(host.getName().equals("")) {
            Toast.makeText(getApplicationContext(),"Namensfeld ist leer.",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Namefield empty.");
            return false;
        }

        // Description is optional (can be empty)
        host.setDescription(descriptionField.getText().toString());

        if(position == null) {
            Toast.makeText(getApplicationContext(),"Keine Location ausgewählt",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "No Location choosen.");
            return false;
        }

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

    class SendRegisterHost extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            displayLoadingScreen(true);
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/hosts");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Authorization", idToken);
                urlConnection.setRequestProperty("Content-Type","application/json");
                urlConnection.setRequestMethod("POST");
                urlConnection.setUseCaches(false);
                urlConnection.setDoOutput(true);

                //Send data
                DataOutputStream os = new DataOutputStream(urlConnection.getOutputStream());
                JSONObject jsonObject = new JSONObject();JSONObject imageJson = new JSONObject();
                imageJson.put("Image", params[5]);
                jsonObject.put("name", params[0]);
                jsonObject.put("description", params[1]);
                jsonObject.put("backgroundColor", params[2]);
                jsonObject.put("latitude", params[3]);
                jsonObject.put("longitude", params[4]);
                jsonObject.put("hostGenres", genresToJson(genreStrings));
                jsonObject.put("profilePicture", imageJson);
                //jsonObject.put("image", params[4]);
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
                            Log.d(TAG, "SendRegisterHost: STATUS CODE: " + statusCode);
                            Log.d(TAG, "SendRegisterHost: RESPONESE MESSAGE: " + httpConn.getResponseMessage());
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

                Log.d(TAG, "SendRegisterHost: RESPONSE:" + response.toString());

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
            displayLoadingScreen(false);
            try {
                JSONObject user = new JSONObject(result);
                JSONArray jsonArray = new JSONArray();
                jsonArray.put(user);
                SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE).edit();
                editor.putInt("userId", user.getInt("id"));
                editor.putString("userProfile", jsonArray.toString());
                editor.putString("user", "host");
                editor.putInt("userColor", user.getInt("backgroundColor"));
                editor.apply();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(RegistrationHostActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("idToken", idToken);
            startActivity(intent);
            finish();
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

        }
    }

}
