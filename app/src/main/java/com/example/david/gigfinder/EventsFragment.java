package com.example.david.gigfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.david.gigfinder.adapters.PastGigsAdapter;
import com.example.david.gigfinder.adapters.UpcomingGigsAdapter;
import com.example.david.gigfinder.tools.GeoTools;
import com.example.david.gigfinder.tools.Utils;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class EventsFragment extends Fragment {

    private static final String TAG = "APPLOG - EventsFragment";
    private static final int ADD_EVENT = 1;
    private String idToken;
    private UpcomingGigsAdapter upcomingGigsAdapter;
    private PastGigsAdapter pastGigsAdapter;

    ArrayList<JSONObject> futureEventObjects;
    ArrayList<JSONObject> pastEventObjects;

    private ListView upcomingListView;
    private ListView pastListView;

    int userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_events_list, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs = getActivity().getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE);

        idToken = getArguments().getString("idToken");
        try {
            JSONObject jsonObject = new JSONArray(prefs.getString("userProfile","x")).getJSONObject(0);
            userId = jsonObject.getInt("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        GetEvents getEvents = new GetEvents();
        getEvents.execute();

        upcomingListView = getView().findViewById(R.id.upcomingEventsListView);
        pastListView = getView().findViewById(R.id.pastEventsListView);

        futureEventObjects = new ArrayList<>();
        pastEventObjects = new ArrayList<>();

        getView().findViewById(R.id.events_addbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddEventActivity.class);
                intent.putExtra("idToken", idToken);
                startActivityForResult(intent, ADD_EVENT);
            }
        });
    }

    private void updateList(String result) {
        ArrayList<String[]> futureEvents = new ArrayList<>();
        ArrayList<String[]> pastEvents = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(result);
            for(int i=0; i<jsonArray.length(); i++){
                JSONObject event = jsonArray.getJSONObject(i);
                if(Utils.convertStringToTimestamp(event.getString("start")).after(new Timestamp(System.currentTimeMillis()))) {
                    // event is in the future
                    futureEventObjects.add(event);
                    Log.d(TAG, "Event in the future: " + event.toString());
                    String time = Utils.getDateStringFromServerFormat(event.getString("start")) + " um " + Utils.getTimeStringFromServerFormat(event.getString("start")) + " Uhr";
                    futureEvents.add(new String[] {event.getString("title"), time, GeoTools.getAddressFromLatLng(getContext(), new LatLng(event.getDouble("latitude"), event.getDouble("longitude")))});
                }
                else {
                    // event is in the past
                    pastEventObjects.add(event);
                    Log.d(TAG, "Event in the past: " + event.toString());
                    pastEvents.add(new String[] {event.getString("title"), GeoTools.getAddressFromLatLng(getContext(), new LatLng(event.getDouble("latitude"), event.getDouble("longitude")))});
                }
                String[] eString = {};
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "Error getting Events: " + e.getStackTrace().toString());
        }

        upcomingGigsAdapter = new UpcomingGigsAdapter(getContext(), futureEvents);
        upcomingListView.setAdapter(upcomingGigsAdapter);
        upcomingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), EventProfileActivity.class);
                intent.putExtra("idToken", idToken);
                intent.putExtra("Event", futureEventObjects.get(position).toString());
                startActivity(intent);
            }
        });

        pastGigsAdapter = new PastGigsAdapter(getContext(), pastEvents);
        pastListView.setAdapter(pastGigsAdapter);
        pastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), EventProfileActivity.class);
                intent.putExtra("idToken", idToken);
                intent.putExtra("Event", pastEventObjects.get(position).toString());
                startActivity(intent);
            }
        });

    }

    class GetEvents extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/events?host="+userId);
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
            Log.d(TAG, "Events by this user: " + result);
            updateList(result);
        }
    }
}
