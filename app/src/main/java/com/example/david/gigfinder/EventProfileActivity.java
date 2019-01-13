package com.example.david.gigfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.david.gigfinder.data.Artist;
import com.example.david.gigfinder.data.Event;
import com.example.david.gigfinder.data.Host;
import com.example.david.gigfinder.data.enums.Genre;
import com.example.david.gigfinder.tools.GeoTools;
import com.example.david.gigfinder.tools.Utils;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.RuntimeRemoteException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventProfileActivity extends AppCompatActivity {
    private static final String TAG = "EventProfileActivity";

    SharedPreferences prefs;

    private Event event;
    private String idToken;
    private String user;
    private int userId;
    private JSONObject hostJson;
    private JSONObject eventJson;

    TextView titleText;
    TextView genreText;
    TextView descriptionText;
    TextView timeText;
    TextView dateText;
    TextView locationText;
    LinearLayout locationContainer;

    Button testBtn;
    Button applyBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_profile);

        prefs = getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE);

        idToken = getIntent().getExtras().getString("idToken");
        userId = prefs.getInt("userId", 0);
        user = prefs.getString("user", "host");

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

        if(getIntent().hasExtra("Event")) {
            try {
                eventJson = new JSONObject(getIntent().getExtras().getString("Event"));
                GetHost getHost = new GetHost();
                getHost.execute();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // TODO move the following code (including the callback method) to where the Event is generated

        // region Test Event
        GeoDataClient mGeoDataClient = Places.getGeoDataClient(this);
        Task<PlaceBufferResponse> placeResult = mGeoDataClient.getPlaceById("ChIJQwJTzpl1nkcR2vIR4mH1Bfw");
        placeResult.addOnCompleteListener(mUpdatePlaceDetailsCallback);

        testBtn = findViewById(R.id.testBtn);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventProfileActivity.this, HostProfileActivity.class);
                intent.putExtra("idToken", idToken);
                //intent.putExtra("host", hostJson.toString());
                startActivity(intent);
            }
        });

        applyBtn = findViewById(R.id.applyBtn);
        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostParticipation postParticipation = new PostParticipation();
                postParticipation.execute();
            }
        });
        if(user.equals("host")){
            applyBtn.setVisibility(View.GONE);
        }
    }

    /**
     *  Callback for after the Place is found by its ID
     */
    private OnCompleteListener<PlaceBufferResponse> mUpdatePlaceDetailsCallback
            = new OnCompleteListener<PlaceBufferResponse>() {
        @Override
        public void onComplete(Task<PlaceBufferResponse> task) {
            try {
                PlaceBufferResponse places = task.getResult();

                // Get the Place object from the buffer.
                Place place = places.get(0);

                // Some example Genres
                ArrayList<Genre> list = new ArrayList<>();
                list.add(Genre.ROCK);
                list.add(Genre.HOUSE);

                // Generate a Test Event
                event = new Event(1, "Testevent in der gemütlichen Beispielbar",
                        "Suche talentierten Drehorgelspieler für Freitag Abend in meiner Bar. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.",
                        list, place.getLatLng(), new Timestamp(2019, 1, 20, 20, 15, 0, 0),
                        new Timestamp(2019, 1, 21, 3, 30, 0, 0), null);

                displayEvent();

            } catch (RuntimeRemoteException e) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete.", e);
                return;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    // endregion

    /**
     * Displays the given Event in the Activity
     */
    private void displayEvent() throws JSONException { // TODO handle with the jsonevent?
        titleText.setText(eventJson.getString("title"));
        descriptionText.setText(eventJson.getString("description"));

        String myGenres = "(";
        for(int i=0; i<eventJson.getJSONArray("eventGenres").length(); i++){
            myGenres = myGenres.concat(Utils.genreIdToString(eventJson.getJSONArray("eventGenres").getJSONObject(i).getInt("genreId"), prefs.getString("genres", "x")));
            if(i < eventJson.getJSONArray("eventGenres").length()-1){
                myGenres = myGenres.concat(", ");
            }
        }
        myGenres = myGenres.concat(")");
        genreText.setText(myGenres);
        String time = event.getTimeFrom().getHours() + ":" + event.getTimeFrom().getMinutes() + " Uhr - " + event.getTimeTo().getHours() + ":" + event.getTimeTo().getMinutes() + " Uhr";
        timeText.setText(time);
        String date = event.getTimeFrom().getDate() + "." + event.getTimeFrom().getMonth() + "." + event.getTimeFrom().getYear();
        dateText.setText(date);
        String placeName = GeoTools.getAddressFromLatLng(this, event.getLocation());
        locationText.setText(placeName);
    }

    /**
     *  Called when the user clicks anywhere on the Location Icon or Text
     */
    private void onClickLocation() {
        try {
            float lat = Float.parseFloat(eventJson.getString("latitude"));
            float lng = Float.parseFloat(eventJson.getString("longitude"));
            String title = eventJson.getString("title");
            String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)", lat, lng, lat, lng, title);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);
        }
        catch (JSONException e) {

        }
    }

    private void showHost(String result){
        try {
            hostJson = new JSONObject(result);
            testBtn.setText("Hosted By: " + hostJson.getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    class GetHost extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/hosts/" + eventJson.getInt("hostId"));
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
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "USER PROFILE: " + result);
            showHost(result);
        }
    }

    class PostParticipation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/participations");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Authorization", idToken);
                urlConnection.setRequestProperty("Content-Type","application/json");
                urlConnection.setRequestMethod("POST");
                urlConnection.setUseCaches(false);
                urlConnection.setDoOutput(true);

                //Send data
                DataOutputStream os = new DataOutputStream(urlConnection.getOutputStream());
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("EventId", eventJson.getInt("id"));
                jsonObject.put("ArtistId", userId);
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
                            Log.d(TAG, "PostParticipation: STATUS CODE: " + statusCode);
                            Log.d(TAG, "PostParticipation: RESPONESE MESSAGE: " + httpConn.getResponseMessage());
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

                Log.d(TAG, "PostParticipation: RESPONSE:" + response.toString());

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
            applyBtn.setClickable(false);
        }
    }
}
