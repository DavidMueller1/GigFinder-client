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
import android.widget.TextView;
import android.widget.Toast;

import com.example.david.gigfinder.adapters.FavAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

public class FavoritesFragment extends Fragment {

    private static final String TAG = "FavoritesFragment";
    private SharedPreferences sharedPreferences;
    private String idToken;

    private FavAdapter favAdapter;
    private TextView noFavsText;
    private ArrayList<String[]> favorites;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_prefs), Context.MODE_PRIVATE);
        idToken = getArguments().getString("idToken");

        noFavsText = getView().findViewById(R.id.noFavsText);
        ListView listView = (ListView) getView().findViewById(R.id.favListView);

        favorites = new ArrayList<>();
        favAdapter = new FavAdapter(this.getContext(), favorites);
        listView.setAdapter(favAdapter);

        if(idToken.equals("offline")){
            offlineMode();
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(getContext(),getString(R.string.no_connection),Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            //online mode
            if(isNetworkAvailable()) {
                GetFavorites getFavorites = new GetFavorites();
                getFavorites.execute();
            }

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getActivity(), HostProfileActivity.class);
                    intent.putExtra("idToken", idToken);
                    intent.putExtra("profileUserId", Integer.parseInt(favorites.get(position)[4]));
                    startActivity(intent);
                }
            });
        }
    }

    /**
     * Shows the Artists favorite Hosts
     * @param result
     */
    private void showFavorites(String result) {
        try {
            noFavsText.setVisibility(View.GONE);
            JSONArray favoritesJson = new JSONArray(result);
            for(int i=0; i<favoritesJson.length(); i++){
                String name = favoritesJson.getJSONObject(i).getJSONObject("host").getString("name");
                String description = favoritesJson.getJSONObject(i).getJSONObject("host").getString("description");
                String profilePictureId = String.valueOf(favoritesJson.getJSONObject(i).getJSONObject("host").getInt("profilePictureId"));
                String id = String.valueOf(favoritesJson.getJSONObject(i).getJSONObject("host").getInt("id"));
                favorites.add(new String[]{name, description, profilePictureId, "null", id});
            }
            favAdapter.notifyDataSetChanged();

            sharedPreferences.edit().putString("favorites", favoritesJson.toString()).apply();

            if(favorites.size()>0) {
                updatePictures();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the pictures from server (or cache if offline)
     */
    private void updatePictures() {
        for(int i=0; i<favorites.size(); i++){
            GetProfilePicture getProfilePicture = new GetProfilePicture();
            getProfilePicture.execute(favorites.get(i)[2], String.valueOf(i));
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
            if(!sharedPreferences.getString("favorites", "x").equals("x")){
                showFavorites(sharedPreferences.getString("favorites", "x"));
            }
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

    /**
     * Used to get all of favorite hosts of current artist
     */
    private class GetFavorites extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/favorites");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Authorization", idToken);
                urlConnection.setRequestMethod("GET");
                urlConnection.setUseCaches(false);

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
        protected void onPostExecute(String result){
            Log.d(TAG, "FAVORITES: " + result);
            if(!result.equals("[]") && !result.equals("") && result!=null){
                showFavorites(result);
            }
        }
    }

    /**
     * Used to get the profile pictures of current favorites, if offline using cache
     */
    private class GetProfilePicture extends AsyncTask<String, Void, String> {

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
                if(isNetworkAvailable()) {
                    urlConnection.addRequestProperty("Cache-Control", "max-stale=" + getString(R.string.max_stale_online));
                }else{
                    urlConnection.addRequestProperty("Cache-Control", "max-stale=" + getString(R.string.max_stale_offline));
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
            if (result != null && favorites != null) {
                if(favorites.size()>id) {
                    favorites.get(id)[3] = result;
                    favAdapter.notifyDataSetChanged();
                }
            }
        }
    }
}
