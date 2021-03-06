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
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.david.gigfinder.adapters.ChatAdapter;

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

import static android.content.Context.MODE_PRIVATE;

public class ChatFragment extends Fragment {

    private static final String TAG = "ChatFragment";
    private String idToken;
    private String user;

    private TextView noChatsText;
    private ListView listView;
    private FrameLayout progress;

    private ChatAdapter chatAdapter;
    private ArrayList<String[]> chatStrings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs = getActivity().getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE);

        idToken = getArguments().getString("idToken");
        user = prefs.getString("user", "host");

        noChatsText = getView().findViewById(R.id.noChatText);
        progress = getView().findViewById(R.id.progressBarHolder);

        chatStrings = new ArrayList<>();
        chatAdapter = new ChatAdapter(this.getContext(), chatStrings);
        listView = (ListView) getView().findViewById(R.id.chatListView);
        listView.setAdapter(chatAdapter);

        if(idToken.equals("offline")){
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(getContext(),getString(R.string.no_connection),Toast.LENGTH_SHORT).show();
                }
            });
            offlineMode();
        }else {
            //online mode
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra("idToken", idToken);
                        intent.putExtra("name", chatStrings.get(position)[0]);
                        intent.putExtra("pictureId", Integer.parseInt(chatStrings.get(position)[3]));
                        intent.putExtra("profileUserId", Integer.parseInt(chatStrings.get(position)[2]));
                        startActivity(intent);
                    }
                });
        }
        GetReceivers getReceivers = new GetReceivers();
        getReceivers.execute();
    }

    /**
     * Shows the messages from the server
     * @param result
     */
    private void showMessages(String result){
        try {
            String chatpartner;
            if(user.equals("host")){
                chatpartner = "artist";
            }else{
                chatpartner = "host";
            }
            JSONArray msgJson = new JSONArray(result);
            for(int i = 0; i<msgJson.length(); i++){
                String name = msgJson.getJSONObject(i).getJSONObject(chatpartner).getString("name");
                String lastmsg = msgJson.getJSONObject(i).getJSONObject("lastMessage").getString("content");
                String id = String.valueOf(msgJson.getJSONObject(i).getJSONObject(chatpartner).getInt("id"));
                String profilePicId = String.valueOf(msgJson.getJSONObject(i).getJSONObject(chatpartner).getInt("profilePictureId"));
                chatStrings.add(new String[]{name, lastmsg, id, profilePicId, "empty"});
            }
            chatAdapter.notifyDataSetChanged();

            updateProfilePictures();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updating profile pictures from server
     */
    private void updateProfilePictures(){
        for(int i=0; i<chatStrings.size(); i++) {
            GetProfilePicture getProfilePicture = new GetProfilePicture();
            getProfilePicture.execute(chatStrings.get(i)[3], String.valueOf(i));
        }
    }

    /**
     * Displays the loading screen if true
     * @param isLoading
     */
    private void displayLoadingScreen(boolean isLoading) {
        if(isLoading) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progress.setVisibility(View.VISIBLE);
                }
            });
        }
        else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progress.setVisibility(View.GONE);
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

    /**
     * Used to get all chatpartners from the Server
     */
    private class GetReceivers extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                displayLoadingScreen(true);

                URL url = new URL("https://gigfinder.azurewebsites.net/api/receivers");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Authorization", idToken);
                urlConnection.setRequestMethod("GET");
                if(!isNetworkAvailable()){
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
        protected void onPostExecute(String result){
            Log.d(TAG, "MESSAGES: " + result);
            if(result.equals("[]")) {
                displayLoadingScreen(false);
            }else{
                noChatsText.setVisibility(View.GONE);
                showMessages(result);
            }
        }
    }

    /**
     * Used to get profile pictures from server
     * Uses caching
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
            if(chatStrings!=null) {
                chatStrings.get(id)[4] = result;
                chatAdapter.notifyDataSetChanged();
            }
            if(chatStrings.size()==id+1){
                displayLoadingScreen(false);
            }
        }
    }
}


