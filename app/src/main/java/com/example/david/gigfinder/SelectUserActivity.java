package com.example.david.gigfinder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SelectUserActivity extends AppCompatActivity {

    Button artistButton;
    Button hostButton;

    String idToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);

        idToken = getIntent().getExtras().getString("idToken");

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
        intent.putExtra("idToken", idToken);
        startActivity(intent);
    }

    private void startHostRegistration() {
        Intent intent = new Intent(getApplicationContext(), RegistrationHostActivity.class);
        intent.putExtra("idToken", idToken);
        intent.putExtra("mode", 0); // = Profile Registration
        startActivity(intent);
    }
}
