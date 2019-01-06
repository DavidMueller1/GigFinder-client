package com.example.david.gigfinder;

import android.graphics.Color;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.david.gigfinder.data.Artist;
import com.example.david.gigfinder.data.Event;
import com.example.david.gigfinder.data.Host;
import com.example.david.gigfinder.data.enums.Genre;

import java.sql.Timestamp;
import java.util.ArrayList;

public class EventProfileActivity extends AppCompatActivity {
    private static final String TAG = "EventProfileActivity";

    TextView titleText;
    TextView genreText;
    TextView descriptionText;
    TextView timeText;
    TextView dateText;
    TextView locationText;
    LinearLayout locationContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_profile);

        titleText = findViewById(R.id.event_title);
        genreText = findViewById(R.id.event_genre);
        descriptionText = findViewById(R.id.event_description);
        timeText = findViewById(R.id.event_time_text);
        dateText = findViewById(R.id.event_date_text);
        locationText = findViewById(R.id.event_location_text);
        locationContainer = findViewById(R.id.event_location_container);
        locationContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickLocation();
            }
        });

        // Test Event
        ArrayList<Genre> list = new ArrayList<>();
        list.add(Genre.ROCK);
        list.add(Genre.HOUSE);
        Event testEvent = new Event(1, "Testevent in der gemütlichen Beispielbar",
                "Suche talentierten Drehorgelspieler für Freitag Abend in meiner Bar. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.", list,
                new Timestamp(2019, 1, 20, 20, 15, 0, 0),
                new Timestamp(2019, 1, 21, 3, 30, 0, 0), null);

        displayEvent(testEvent);
    }

    /**
     * Displays the given Event in the Activity
     * @param event
     */
    private void displayEvent(Event event) {
        titleText.setText(event.getTitle());

        String genreString = "(";
        for(Genre g : event.getGenres()) {
            if(!genreString.equals("(")) {
                genreString += ", ";
            }
            genreString += g.toString();
        }
        descriptionText.setText(event.getDescription());
        genreString += ")";
        genreText.setText(genreString);
        String time = event.getTimeFrom().getHours() + ":" + event.getTimeFrom().getMinutes() + " Uhr - " + event.getTimeTo().getHours() + ":" + event.getTimeTo().getMinutes() + " Uhr";
        timeText.setText(time);
        String date = event.getTimeFrom().getDate() + "." + event.getTimeFrom().getMonth() + "." + event.getTimeFrom().getYear();
        dateText.setText(date);
        locationText.setText("TODO");
    }

    /**
     *  Called when the user clicks anywhere on the Location Icon or Text
     */
    private void onClickLocation() {
        Log.d(TAG, "Location clicked");
        // TODO open google map with Location
    }

}
