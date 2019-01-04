package com.example.david.gigfinder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.david.gigfinder.data.Artist;
import com.example.david.gigfinder.data.enums.Genre;
import com.example.david.gigfinder.tools.ImageTools;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

public class RegistrationArtistActivity extends AppCompatActivity {

    private static final String TAG = "MYLOG_RegistrationArtistActivity";
    private static final int PICK_IMAGE = 1;

    private ImageButton profilePictureButton;
    private EditText nameField;
    private EditText descriptionField;
    private Spinner genreSpinner;
    private Button backgroundColorPickerButton;
    private Button registrationButton;

    private ColorPicker colorPicker;

    private Artist artist;
    private Bitmap profilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_artist);

        artist = new Artist();

        profilePictureButton = findViewById(R.id.registration_artist_profilePicture);
        profilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performProfilePictureSelection();
            }
        });

        nameField = findViewById(R.id.registration_artist_name);
        descriptionField = findViewById(R.id.registration_artist_description);
        genreSpinner = findViewById(R.id.registration_artist_genre);
        //Replaced the Strings with the Genre-Enum
        ArrayAdapter<Genre> adapter = new ArrayAdapter<Genre>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, Genre.values());
        genreSpinner.setAdapter(adapter);

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

        colorPicker = new ColorPicker(RegistrationArtistActivity.this, 255, 255, 255);
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
        artist.setColor(color);
        findViewById(android.R.id.content).setBackgroundColor(artist.getColor());

        if(isBrightColor(artist.getColor())) {
            artist.setFontColor(getResources().getColor(R.color.black));
        }
        else {
            artist.setFontColor(getResources().getColor(R.color.white));
        }

        updateFontColor();
    }

    /**
     * Updates the font color of all relevant elements
     */
    private void updateFontColor() {
        int fontColor = artist.getFontColor();

        ViewGroup layout = findViewById(R.id.registration_artist_layout);
        for(int index = 0; index < layout.getChildCount(); ++index) {
            View child = layout.getChildAt(index);
            if(child instanceof TextView) {
                ((TextView) child).setTextColor(fontColor);
            }
            else if(child instanceof EditText) {
                ((EditText) child).setTextColor(fontColor);
            }
        }
    }



    /**
     * Called when the user presses the registrate button
     */
    private void performRegistration() {
        Log.d(TAG, "Checking user input...");
        if(checkUserInputBasic()) {
            Log.d(TAG, "User input ok");
            // TODO Send data to Server and get verification
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
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
        ArrayList genres = new ArrayList();
        genres.add((Genre)genreSpinner.getSelectedItem());
        artist.setGenres(genres);
        if(artist.getGenres().get(0).equals(getResources().getString(R.string.artist_genre_choose))) {
            Toast.makeText(getApplicationContext(),"Kein Genre ausgewählt",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "No genre selected.");
            return false;
        }

        return true;
    }

    /**
     * Checks whether a background color is light enough to use black font
     * @param color
     * @return is the color a bright
     */
    private static boolean isBrightColor(int color) {
        int[] rgb = {Color.red(color), Color.green(color), Color.blue(color)};
        Log.d(TAG, rgb[0] + "");
        // formula for the brightness (returns a value between 0 and 255)
        int brightness = (int) Math.sqrt(rgb[0] * rgb[0] * .299 + rgb[1] * rgb[1] * .587 + rgb[2] * rgb[2] * .114);

        if (brightness >= 110) {
            return true;
        }

        return false;
    }

    class SendRegisterArtist extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/artists");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Authorization", params[0]);
                urlConnection.setRequestMethod("PUT");

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
            Log.d(TAG, "SendRegisterArtist: " + result);
        }
    }
}
