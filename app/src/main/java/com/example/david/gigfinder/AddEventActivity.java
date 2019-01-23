package com.example.david.gigfinder;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.david.gigfinder.data.Host;
import com.example.david.gigfinder.data.enums.Genre;
import com.example.david.gigfinder.tools.ColorTools;
import com.example.david.gigfinder.tools.ImageTools;
import com.example.david.gigfinder.tools.Utils;
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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class AddEventActivity extends AppCompatActivity {
    private static final String TAG = "AddEventActivity";
    private static final int PICK_IMAGE = 1;
    private static final int PLACE_PICKER_REQUEST = 2;

    SharedPreferences sharedPreferences;

    private EditText nameField;
    private EditText descriptionField;
    private LinearLayout locationButton;
    private TextView locationButtonText;
    private Spinner genreSpinner;
    private TextView timeFromText;
    private Button pickTimeFromButton;
    private TextView dateFromText;
    private Button pickDateFromButton;
    private TextView timeToText;
    private Button pickTimeToButton;
    private TextView dateToText;
    private Button pickDateToButton;
    private Button addEventButton;

    String[] timeStrings = new String[4];

    private LatLng position;

    String idToken;

    private ArrayAdapter<String> adapter;
    private JSONArray genres;
    private String[] genreStrings;

    /*@Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_add, container, false);
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_event_add);
        //super.onActivityCreated(savedInstanceState);

        //idToken = getArguments().getString("idToken");
        idToken = getIntent().getExtras().getString("idToken");

        sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.shared_prefs), Context.MODE_PRIVATE);

        genreSpinner = findViewById(R.id.add_event_genre);

        if(sharedPreferences.getString("genres","x").equals("x")){
            GetGenres getGenres = new GetGenres();
            getGenres.execute();
        }else{
            showGenres(sharedPreferences.getString("genres","x"));
        }

        nameField = findViewById(R.id.add_event_title);
        descriptionField = findViewById(R.id.add_event_description);
        locationButton = findViewById(R.id.add_event_location_container);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLocationSelection();
            }
        });
        locationButtonText = findViewById(R.id.add_event_location_text);
        //Replaced the Strings with the Genre-Enum


        addEventButton = findViewById(R.id.button_add_event_save);
        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performAddEvent();
            }
        });

        // region Time and Date
        // Time from
        timeFromText = findViewById(R.id.add_event_time_from);
        pickTimeFromButton = findViewById(R.id.button_add_event_select_time_from);
        pickTimeFromButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current Time
                final Calendar c = Calendar.getInstance();
                int mHour = c.get(Calendar.HOUR_OF_DAY);
                int mMinute = c.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddEventActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                String hourString = String.format("%02d", hourOfDay);
                                String minuteString = String.format("%02d", minute);
                                timeStrings[0] = hourString + ":" + minuteString + ":00";
                                timeFromText.setText(hourString + ":" + minuteString + " Uhr");
                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();
            }
        });

        // Date from
        dateFromText = findViewById(R.id.add_event_date_from);
        pickDateFromButton = findViewById(R.id.button_add_event_select_date_from);
        pickDateFromButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current Date
                final Calendar c = Calendar.getInstance();
                final int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(AddEventActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                String dayString = String.format("%02d", dayOfMonth);
                                String monthString = String.format("%02d", (monthOfYear + 1));
                                String yearString = String.format("%04d", year);
                                timeStrings[1] = yearString + "-" + monthString + "-" + dayString;
                                dateFromText.setText(dayString + "." + monthString + "." + yearString);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        // Time to
        timeToText = findViewById(R.id.add_event_time_to);
        pickTimeToButton = findViewById(R.id.button_add_event_select_time_to);
        pickTimeToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current Time
                final Calendar c = Calendar.getInstance();
                int mHour = c.get(Calendar.HOUR_OF_DAY);
                int mMinute = c.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddEventActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                String hourString = String.format("%02d", hourOfDay);
                                String minuteString = String.format("%02d", minute);
                                timeStrings[2] = hourString + ":" + minuteString + ":00";
                                timeToText.setText(hourString + ":" + minuteString + " Uhr");
                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();
            }
        });

        // Date to
        dateToText = findViewById(R.id.add_event_date_to);
        pickDateToButton = findViewById(R.id.button_add_event_select_date_to);
        pickDateToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current Date
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(AddEventActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                String dayString = String.format("%02d", dayOfMonth);
                                String monthString = String.format("%02d", (monthOfYear + 1));
                                String yearString = String.format("%04d", year);
                                timeStrings[3] = yearString + "-" + monthString + "-" + dayString;
                                dateToText.setText(dayString + "." + monthString + "." + yearString);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        /// endregion

        position = null;
    }

    /**
     *  called when the user presses the save-event-button
     */
    private void performAddEvent() {
        if(checkUserInput()) {
            PostEvent postEvent = new PostEvent();
            postEvent.execute(nameField.getText().toString(), descriptionField.getText().toString(),
                    String.valueOf(position.longitude), String.valueOf(position.latitude),
                    timeStrings[1] + "T" + timeStrings[0], timeStrings[3] + "T" + timeStrings[2]);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        /*if (requestCode == PICK_IMAGE) {
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

                    findViewById(R.id.registration_host_image_hint).setVisibility(View.VISIBLE);
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found");
                }


            }
        }*/
        if(requestCode == PLACE_PICKER_REQUEST) {
            if(resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, getApplicationContext());
                position = place.getLatLng();

                String address = position.toString();
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


    private boolean checkUserInput(){

        if(nameField.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(),getString(R.string.error_name),Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Namefield empty.");
            return false;
        }

        if(descriptionField.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_description), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Description empty.");
            return false;
        }

        if(timeFromText.getText().toString().equals(getString(R.string.add_event_time_hint))){
            Toast.makeText(getApplicationContext(), getString(R.string.error_start_time), Toast.LENGTH_SHORT).show();
            return false;
        }

        if(dateFromText.getText().toString().equals(getString(R.string.add_event_date_hint))){
            Toast.makeText(getApplicationContext(), getString(R.string.error_start_date), Toast.LENGTH_SHORT).show();
            return false;
        }

        if(timeToText.getText().toString().equals(getString(R.string.add_event_time_hint))){
            Toast.makeText(getApplicationContext(), getString(R.string.error_end_time), Toast.LENGTH_SHORT).show();
            return false;
        }

        if(dateToText.getText().toString().equals(getString(R.string.add_event_date_hint))){
            Toast.makeText(getApplicationContext(), getString(R.string.error_end_date), Toast.LENGTH_SHORT).show();
            return false;
        }

        Timestamp start = Utils.convertStringToTimestamp(timeStrings[1] + "T" + timeStrings[0]);
        Timestamp end = Utils.convertStringToTimestamp(timeStrings[3] + "T" + timeStrings[2]);

        if(start.after(end)) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_time_order), Toast.LENGTH_SHORT).show();
            return false;
        }


        if(position==null){
            Toast.makeText(getApplicationContext(), getString(R.string.error_location), Toast.LENGTH_SHORT).show();
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

    class PostEvent extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/events");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Authorization", idToken);
                urlConnection.setRequestProperty("Content-Type","application/json");
                urlConnection.setRequestMethod("POST");
                urlConnection.setUseCaches(false);
                urlConnection.setDoOutput(true);

                SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE);
                int hostID = prefs.getInt("userId", 0);

                //Send data
                DataOutputStream os = new DataOutputStream(urlConnection.getOutputStream());
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("hostId", hostID);
                jsonObject.put("title", params[0]);
                jsonObject.put("description", params[1]);
                jsonObject.put("longitude", params[2]);
                jsonObject.put("latitude", params[3]);
                jsonObject.put("start", params[4]);
                jsonObject.put("end", params[5]);
                jsonObject.put("eventGenres", genresToJson(genreStrings));
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
                            Log.d(TAG, "PostEvent: STATUS CODE: " + statusCode);
                            Log.d(TAG, "PostEvent: RESPONESE MESSAGE: " + httpConn.getResponseMessage());
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

                Log.d(TAG, "PostEvent: RESPONSE:" + response.toString());

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
        }
    }
}
