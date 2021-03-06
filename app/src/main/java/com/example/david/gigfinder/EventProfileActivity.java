package com.example.david.gigfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.david.gigfinder.adapters.ParticipantAdapter;
import com.example.david.gigfinder.tools.GeoTools;
import com.example.david.gigfinder.tools.Utils;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class EventProfileActivity extends AppCompatActivity {
    private static final String TAG = "EventProfileActivity";

    SharedPreferences prefs;
    private String idToken;
    private String user;
    private int userId;
    private JSONObject hostJson;
    private JSONObject eventJson;

    private ArrayList<String[]> participantStrings;
    private ArrayList<JSONObject> participantJSONObjects;
    private ParticipantAdapter participantAdapter;

    TextView titleText;
    TextView genreText;
    TextView descriptionText;
    TextView timeText;
    TextView dateText;
    LinearLayout gageContainer;
    TextView gageText;
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
        gageContainer = findViewById(R.id.event_gage_container);
        gageText = findViewById(R.id.event_gage_text);
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
                try {
                    intent.putExtra("profileUserId", hostJson.getInt("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
        try {
            String start = eventJson.getString("start");
            if(Utils.convertStringToDate(start).before(Calendar.getInstance().getTime())){
                applyBtn.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        participantStrings = new ArrayList<>();
        participantJSONObjects = new ArrayList<>();

        participantAdapter = new ParticipantAdapter(getApplicationContext(), participantStrings);
        ListView listView = findViewById(R.id.event_participants_list);
        listView.setAdapter(participantAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                long viewId = view.getId();

                if(viewId == R.id.participant_select_button) {
                    Log.d(TAG, "Select button clicked, position: " + position + "    ID: " + id);

                    PutParticipation putParticipation = new PutParticipation();
                    putParticipation.execute(participantJSONObjects.get(position).toString(), "true");

                    int count = parent.getChildCount();

                    for(int i = 0; i < count; i++) {
                        if(i == position) {
                            View v = parent.getChildAt(i);
                            v.findViewById(R.id.participant_cancel_button).setVisibility(View.VISIBLE);
                        }
                        View v = parent.getChildAt(i);
                        v.findViewById(R.id.participant_select_button).setVisibility(View.GONE);

                    }
                }
                else if(viewId == R.id.participant_cancel_button) {
                    Log.d(TAG, "Cancel button clicked, position: " + position + "    ID: " + id);
                    PutParticipation putParticipation = new PutParticipation();
                    putParticipation.execute(participantJSONObjects.get(position).toString(), "false");

                    // TODO Post Unselect
                    int count = parent.getChildCount();

                    for(int i = 0; i < count; i++) {
                        if(i == position) {
                            View v = parent.getChildAt(i);
                            v.findViewById(R.id.participant_cancel_button).setVisibility(View.GONE);
                        }
                        View v = parent.getChildAt(i);
                        v.findViewById(R.id.participant_select_button).setVisibility(View.VISIBLE);
                    }
                }
                else {
                    Log.d(TAG, "Item " + position + " clicked on View " + view.getClass().toString());
                    Intent intent = new Intent(getApplicationContext(), ArtistProfileActivity.class);
                    intent.putExtra("idToken", idToken);
                    intent.putExtra("profileUserId", Integer.parseInt(participantStrings.get(position)[2]));
                    startActivity(intent);
                }
            }
        });
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

        Log.d(TAG, "!!! "+ prefs.getString("genres", "x"));

        String myGenres = "(";
        for(int i=0; i<eventJson.getJSONArray("eventGenres").length(); i++){
            myGenres = myGenres.concat(Utils.genreIdToString(eventJson.getJSONArray("eventGenres").getJSONObject(i).getInt("genreId"), prefs.getString("genres", "x")));
            Log.d(TAG, "!!! "+ eventJson.getJSONArray("eventGenres").getJSONObject(i).getInt("genreId"));
            if(i < eventJson.getJSONArray("eventGenres").length()-1){
                myGenres = myGenres.concat(", ");
            }
        }
        myGenres = myGenres.concat(")");
        genreText.setText(myGenres);

        String time = Utils.getTimeStringFromServerFormat(eventJson.getString("start")) + " Uhr bis " + Utils.getTimeStringFromServerFormat(eventJson.getString("end"));
        timeText.setText(time);
        String startDate = Utils.getDateStringFromServerFormat(eventJson.getString("start"));
        String endDate = Utils.getDateStringFromServerFormat(eventJson.getString("end"));
        String date = startDate;
        if(!endDate.equals(startDate)) {

            date += " bis " + endDate;
        }
        dateText.setText(date);

        String placeName = GeoTools.getAddressFromLatLng(this, new LatLng(eventJson.getDouble("latitude"), eventJson.getDouble("longitude")));
        locationText.setText(placeName);

        double gage = eventJson.getDouble("gage");
        if(gage == 0) {
            gageContainer.setVisibility(View.GONE);
        }
        else {
            if(gage % 1 == 0) {
                gageText.setText(((int) gage) + " €");
            }
            else {
                gageText.setText(gage + " €");
            }
            gageContainer.setVisibility(View.VISIBLE);
        }
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

    /**
     * Displays the hosts name on the button
     */
    private void showHost(String result){

        try {

            hostJson = new JSONObject(result);
            GetParticipants getParticipants = new GetParticipants();
            getParticipants.execute(eventJson.getInt("id") + "");

            testBtn.setText("Gehostet von: " + hostJson.getString("name"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays all participants of the event
     * @param result Contains the participants
     */
    private void showParticipants(String result){
        findViewById(R.id.event_participants_none).setVisibility(View.GONE);
        participantStrings.clear();
        boolean isParticipant = false;
        try {
            JSONArray partJson = new JSONArray(result);
            String isEventHost = userId == hostJson.getInt("id") ? "true" : "false";
            int acceptedPos = -1;

            for(int i = 0; i < partJson.length(); i++) {
                if(partJson.getJSONObject(i).getBoolean("accepted")) {
                    Log.d(TAG, "User at position " + i + " is accepted");
                    acceptedPos = i;
                }
            }

            for(int i = 0; i<partJson.length(); i++){
                participantJSONObjects.add(partJson.getJSONObject(i));
                JSONObject artist = partJson.getJSONObject(i).getJSONObject("artist");
                String name = artist.getString("name");
                Log.d(TAG, "Teilnehmer: " + name);
                String id = String.valueOf(artist.getInt("id"));
                String buttonMode = "select";
                if(acceptedPos != -1) {
                    if(acceptedPos == i) {
                        buttonMode = "cancel";
                    }
                    else {
                        buttonMode = "none";
                    }
                }
                String profilePicId = String.valueOf(artist.getInt("profilePictureId"));
                participantStrings.add(new String[]{name, profilePicId, id, isEventHost, buttonMode, "noPic"}); // name image id isEventHost

                if(artist.getInt("id") == userId) {
                    isParticipant = true;
                }
            }

            ListView listView = findViewById(R.id.event_participants_list);
            int count = listView.getChildCount();

            participantAdapter.notifyDataSetChanged();

            if (participantStrings.isEmpty()) {
                findViewById(R.id.event_participants_none).setVisibility(View.VISIBLE);
            }

            updateProfilePictures();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(user.equals("artist") && !isParticipant) {
            try {
                String start = eventJson.getString("start");
                if(Utils.convertStringToDate(start).after(Calendar.getInstance().getTime())){
                    applyBtn.setVisibility(View.VISIBLE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Displayis the profile pictures of the participants
     */
    private void updateProfilePictures(){
        for(int i = 0; i < participantStrings.size(); i++){
            GetProfilePicture getProfilePicture = new GetProfilePicture();
            getProfilePicture.execute(participantStrings.get(i)[1], String.valueOf(i));
        }
    }

    /**
     * Downloads the profile pictur of an participant
     */
    class GetProfilePicture extends AsyncTask<String, Void, String> {

        int id;

        @Override
        protected String doInBackground(String... params) {
            try {
                id = Integer.parseInt(params[1]);

                URL url = new URL("https://gigfinder.azurewebsites.net/api/pictures/" + params[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Authorization", idToken);
                urlConnection.setRequestMethod("GET");
                urlConnection.setUseCaches(true);
                urlConnection.addRequestProperty("Cache-Control", "max-stale="+getString(R.string.max_stale_online));

                HttpResponseCache cache = HttpResponseCache.getInstalled();

                if (cache != null) {
                    String cacheInfo = "!!! Request count: "
                            + cache.getRequestCount() + ", hit count "
                            + cache.getHitCount() + ", network count "
                            + cache.getNetworkCount() + "   size = "
                            + cache.size() + " <-----------------";
                    Log.w(TAG, cacheInfo);
                }

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
            if (!participantStrings.equals(null)) {
                String base64 = "noPic";
                try {
                    JSONObject imageObject = new JSONObject(result);
                    participantStrings.get(id)[5] = imageObject.getString("image");
                    participantAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Gets the participants of this event from the server
     */
    class GetParticipants extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                Log.d(TAG, params[0]);
                URL url = new URL("https://gigfinder.azurewebsites.net/api/participations?event=" + params[0]);
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
                Log.d(TAG, "Error: " + e.getMessage());
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result){
            Log.d(TAG, "MESSAGES: " + result);
            showParticipants(result);
            /*if(result.equals("[]")) {

            }else{

            }*/
        }
    }

    /**
     * Gets the host of this event from the server
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

    /**
     * Tells the server that the current user is now a participant
     */
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
                os.write(jsonObject.toString().getBytes("UTF-8"));
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
            applyBtn.setVisibility(View.GONE);
            try {
                GetParticipants getParticipants = new GetParticipants();
                getParticipants.execute(eventJson.getInt("id") + "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Tells the server that the current user is no longer a participant
     */
    class PutParticipation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                JSONObject jsonObject = new JSONObject(params[0]);
                Log.d(TAG, jsonObject.toString());

                URL url = new URL("https://gigfinder.azurewebsites.net/api/participations/" + jsonObject.getInt("id"));
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Authorization", idToken);
                urlConnection.setRequestProperty("Content-Type","application/json");
                urlConnection.setRequestMethod("PUT");
                urlConnection.setUseCaches(false);
                urlConnection.setDoOutput(true);

                //Send data
                DataOutputStream os = new DataOutputStream(urlConnection.getOutputStream());
                jsonObject.put("Accepted", Boolean.parseBoolean(params[1]));
                os.write(jsonObject.toString().getBytes("UTF-8"));
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
                            Log.d(TAG, "PutParticipation: STATUS CODE: " + statusCode);
                            Log.d(TAG, "PutParticipation: RESPONESE MESSAGE: " + httpConn.getResponseMessage());
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

                Log.d(TAG, "PutParticipation: RESPONSE:" + response.toString());

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
        }
    }
}
