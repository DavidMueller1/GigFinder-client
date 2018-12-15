package com.example.david.gigfinder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import java.io.FileNotFoundException;

public class RegistrationArtistActivity extends AppCompatActivity {
    private static final String TAG = "MYLOG_RegistrationArtistActivity";
    private static final int PICK_IMAGE = 1;

    private ImageButton profilePictureButton;
    private EditText nameField;
    private EditText descriptionField;
    private Spinner genreSpinner;
    private Button backgroundColorPickerButton;

    private ColorPicker colorPicker;

    private Bitmap profilePicture;
    private String artistName;
    private String artistDescription;
    private String artistGenre;
    private int artistBackgroundColor;

    private Button registrationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_artist);


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
        String[] spinnerItems = new String[]{getResources().getString(R.string.artist_genre_choose), getResources().getString(R.string.artist_genre_techno), getResources().getString(R.string.artist_genre_schlager), getResources().getString(R.string.artist_genre_rock), getResources().getString(R.string.artist_genre_other)};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, spinnerItems);

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
        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent chooserIntent = Intent.createChooser(pickIntent, getResources().getString(R.string.pick_photo_intent));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {takePhotoIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_IMAGE) {
            if(resultCode == RESULT_OK) {
                Uri path = data.getData();

                try {
                    profilePicture = decodeUri(path);
                    profilePictureButton.setImageBitmap(profilePicture);
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found");
                }


            }
        }
    }

    /**
     * @param selectedImage
     * @return Bitmap of the selected image
     */
    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // The new size
        final int REQUIRED_SIZE = 200;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
    }


    /**
     * Called when the user presses the choose color button
     */
    private void performColorSelection() {
        colorPicker.show();
    }

    private void applyColor(int color) {
        artistBackgroundColor = color;
        findViewById(android.R.id.content).setBackgroundColor(artistBackgroundColor);
    }

    /**
     * Called when the user presses the registrate button
     */
    private void performRegistration() {
        Log.d(TAG, "Checking user input...");
        if(checkUserInputBasic()) {
            Log.d(TAG, "User input ok");
            // TODO redirect to tabview and send data to Server
        }
    }

    /**
     * Checks whether the user input is valid (ex. name not empty)
     */
    private boolean checkUserInputBasic() {
        artistName = nameField.getText().toString();
        if(artistName.equals("")) {
            Toast.makeText(getApplicationContext(),"Namensfeld ist leer.",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Namefield empty.");
            return false;
        }

        artistDescription = descriptionField.getText().toString(); // Description is optional

        artistGenre = genreSpinner.getSelectedItem().toString();
        if(artistGenre.equals(getResources().getString(R.string.artist_genre_choose))) {
            Toast.makeText(getApplicationContext(),"Kein Genre ausgewählt",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "No genre selected.");
            return false;
        }

        /*String colorString = backgroundColorPicker.getText().toString();
        if(!colorString.matches("#(\\d|a|b|c|d|e|f){6}")) {
            Toast.makeText(getApplicationContext(),"Falsches Farbformat (Bsp. #1a2bc3)",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Wrong hex format on color.");
            return false;
        }
        try {
            artistBackgroundColor = Color.parseColor(colorString);
        }
        catch(Exception e) {
            Toast.makeText(getApplicationContext(),"Fehler beim Farbformat. Bitte andere Farbe wählen.",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Error on parsing color.");
            return false;
        }*/

        return true;
    }
}
