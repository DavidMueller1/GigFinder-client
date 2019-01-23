package com.example.david.gigfinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import com.example.david.gigfinder.adapters.FavAdapter;
import com.example.david.gigfinder.adapters.PastGigsAdapter;
import com.example.david.gigfinder.adapters.UpcomingGigsAdapter;

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
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class GigsFragment extends Fragment {

    private static final String TAG = "APPLOG - GigsFragment";
    private String idToken;
    private UpcomingGigsAdapter upcomingGigsAdapter;
    private PastGigsAdapter pastGigsAdapter;
    int userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gigs, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs = getActivity().getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE);

        idToken = getArguments().getString("idToken");

        ListView upcomingListView = (ListView) getView().findViewById(R.id.upcomingGigsListView);
        ListView pastListView = (ListView) getView().findViewById(R.id.pastGigsListView);

        ArrayList<String[]> futureGigs = new ArrayList<>();
        ArrayList<String[]> pastGigs = new ArrayList<>();

        upcomingGigsAdapter = new UpcomingGigsAdapter(this.getContext(), futureGigs);
        upcomingListView.setAdapter(upcomingGigsAdapter);

        pastGigsAdapter = new PastGigsAdapter(this.getContext(), pastGigs);
        pastListView.setAdapter(pastGigsAdapter);

        try {
            JSONObject jsonObject = new JSONArray(prefs.getString("userProfile","x")).getJSONObject(0);
            userId = jsonObject.getInt("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(idToken.equals("offline")) {
            offlineMode();
        }else{
            //online mode
            GetParticipations getParticipations = new GetParticipations();
            //getParticipations.execute();

            upcomingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getActivity(), EventProfileActivity.class);
                    startActivity(intent);
                }
            });
        }

    }

    /**
     * Checks internet connection and starts offline mode
     */
    private void offlineMode(){
        if(isNetworkAvailable()){
            //Go back to Login
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        }else{
            //Show Popup
        }
    }

    /**
     * Checks if Network is Available
     * @return True if there is an Internet Connection
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    class GetParticipations extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/participants?artist="+userId);
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
            Log.d(TAG, "Participations By This User: " + result);
        }
    }
}
