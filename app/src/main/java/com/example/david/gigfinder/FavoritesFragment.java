package com.example.david.gigfinder;

import android.content.Intent;
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

        idToken = getArguments().getString("idToken");

        noFavsText = getView().findViewById(R.id.noFavsText);

        favorites = new ArrayList<>();

        favAdapter = new FavAdapter(this.getContext(), favorites);
        ListView listView = (ListView) getView().findViewById(R.id.favListView);
        listView.setAdapter(favAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), HostProfileActivity.class);
                intent.putExtra("idToken", idToken);
                intent.putExtra("host", favorites.get(position)[2]);
                startActivity(intent);
            }
        });

        GetFavorites getFavorites = new GetFavorites();
        getFavorites.execute();
    }

    private void showFavorites(String result) {
        try {
            JSONArray favoritesJson = new JSONArray(result);
            for(int i=0; i<favoritesJson.length(); i++){
                String name = favoritesJson.getJSONObject(i).getJSONObject("host").getString("name");
                String description = favoritesJson.getJSONObject(i).getJSONObject("host").getString("description");
                favorites.add(new String[]{name, description, favoritesJson.getJSONObject(i).getJSONObject("host").toString()});
            }
            favAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
}
