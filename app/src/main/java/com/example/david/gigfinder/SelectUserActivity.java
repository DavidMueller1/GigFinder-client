package com.example.david.gigfinder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SelectUserActivity extends AppCompatActivity {

    Button artistButton;
    Button hostButton;

    String googleID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);

        // TODO Get google ID from Login
        googleID = "ThisIsATest";

        artistButton = findViewById(R.id.button_artist);
        artistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startArtistRegistration();
            }
        });

        hostButton = findViewById(R.id.button_location_host);
        hostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startHostRegistration();
            }
        });
    }

    private void startArtistRegistration() {
        Intent intent = new Intent(getApplicationContext(), RegistrationArtistActivity.class);
        intent.putExtra("googleID", googleID);
        startActivity(intent);
    }

    private void startHostRegistration() {
        // TODO Activity für Registrierung des Hosts aufrufen
    }
}
