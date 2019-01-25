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

import com.example.david.gigfinder.adapters.FavAdapter;

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

public class FavoritesFragment extends Fragment {

    private static final String TAG = "APPLOG - FavoritesFragment";
    private SharedPreferences sharedPreferences;

    private FavAdapter favAdapter;
    private String idToken;
    private TextView noFavsText;
    ArrayList<String[]> favorites;

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
        }else {
            //online mode
            GetFavorites getFavorites = new GetFavorites();
            getFavorites.execute();

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getActivity(), HostProfileActivity.class);
                    intent.putExtra("idToken", idToken);
                    intent.putExtra("host", favorites.get(position)[2]);
                    try {
                        JSONObject jsonObject = new JSONObject(favorites.get(position)[2]);
                        intent.putExtra("profileUserId", String.valueOf(jsonObject.getInt("id")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    startActivity(intent);
                }
            });
        }
    }

    private void showFavorites(String result) {
        try {
            JSONArray favoritesJson = new JSONArray(result);
            for(int i=0; i<favoritesJson.length(); i++){
                String name = favoritesJson.getJSONObject(i).getJSONObject("host").getString("name");
                String description = favoritesJson.getJSONObject(i).getJSONObject("host").getString("description");
                String profilePictureId = String.valueOf(favoritesJson.getJSONObject(i).getJSONObject("host").getInt("profilePictureId"));
                favorites.add(new String[]{name, description, profilePictureId, "null"});
            }
            favAdapter.notifyDataSetChanged();

            sharedPreferences.edit().putString("favorites", favoritesJson.toString());

            updatePictures();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

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
     *
     */
    class GetFavorites extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/favorites");
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
        protected void onPostExecute(String result){
            Log.d(TAG, "FAVORITES: " + result);
            if(result.equals("[]")){

            }else{
                noFavsText.setVisibility(View.GONE);
                showFavorites(result);
            }
        }
    }

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
            favorites.get(id)[3] = result;
            favAdapter.notifyDataSetChanged();
        }
    }
}
