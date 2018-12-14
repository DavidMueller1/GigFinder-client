package com.example.david.gigfinder;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class RegistrationArtistActivity extends AppCompatActivity {
    private static final String TAG = "DEBUGLOG_RegistrationArtistActivity";

    EditText nameField;
    EditText descriptionField;
    Spinner genreSpinner;
    EditText backgroundColorPicker; // TODO use color picker

    String artistName;
    String artistDescription;
    String artistGenre;
    int artistBackgroundColor;

    Button registrationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_artist);

        nameField = findViewById(R.id.registration_artist_name);
        descriptionField = findViewById(R.id.registration_artist_description);
        genreSpinner = findViewById(R.id.registration_artist_genre);
        backgroundColorPicker = findViewById(R.id.registration_artist_color);

        String[] spinnerItems = new String[]{getResources().getString(R.string.artist_genre_choose), getResources().getString(R.string.artist_genre_techno), getResources().getString(R.string.artist_genre_schlager), getResources().getString(R.string.artist_genre_rock), getResources().getString(R.string.artist_genre_other)};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, spinnerItems);
        genreSpinner.setAdapter(adapter);


        registrationButton = findViewById(R.id.button_artist_registration);
        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegistration();
            }
        });
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

        String genreString = genreSpinner.getSelectedItem().toString();
        if(genreString.equals(getResources().getString(R.string.artist_genre_choose))) {
            Toast.makeText(getApplicationContext(),"Kein Genre ausgewählt",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "No genre selected.");
            return false;
        }

        String colorString = backgroundColorPicker.getText().toString();
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
        }

        return true;
    }
}
