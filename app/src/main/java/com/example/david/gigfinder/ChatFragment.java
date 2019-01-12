package com.example.david.gigfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.david.gigfinder.adapters.ChatAdapter;

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

public class ChatFragment extends Fragment {

    private static final String TAG = "APPLOG - ChatFragment";
    private ChatAdapter chatAdapter;
    private FloatingActionButton chatfab;
    ArrayList<String[]> chatStrings;
    String idToken;
    String user;

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

        GetReceivers getReceivers = new GetReceivers();
        getReceivers.execute();

        chatStrings = new ArrayList<>();
        chatStrings.add(new String[]{"Artist Name", "Placeholder message...", "0"});
        chatStrings.add(new String[]{"Host Name", "Test message...", "0"});

        chatAdapter = new ChatAdapter(this.getContext(), chatStrings);
        ListView listView = (ListView) getView().findViewById(R.id.chatListView);
        listView.setAdapter(chatAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("idToken", idToken);
                startActivity(intent);
            }
        });
    }

    private void showMessages(String result){
        try {
            JSONArray msgJson = new JSONArray(result);
            /*
            for(int i=0; i<msgJson.length(); i++) {
                if(msgJson.getJSONObject(i).getJSONObject("author").getString("googleIdToken") == idToken) {
                    if(!containsMsgBy(msgJson.getJSONObject(i).getInt("receiverId"))){
                        if(user=="host") {

                        }else{

                        }
                        chatStrings.add(new String[]{})
                    }else{
                        //If msg is newer
                    }
                }else{
                    if(containsMsgBy(msgJson.getJSONObject(i).getInt("authorId"))){

                    }else{
                        //if msg is newer
                    }
                }
            }
            */
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean containsMsgBy(int id){
        for(int i=0; i<chatStrings.size(); i++){
            if(Integer.parseInt(chatStrings.get(i)[3]) == id)
                return true;
        }
        return false;
    }

    /**
     *
     */
    class GetHost extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/messages");
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
            Log.d(TAG, "MESSAGES: " + result);
            showMessages(result);
        }
    }

    /**
     *
     */
    class GetArtist extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/messages");
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
            Log.d(TAG, "MESSAGES: " + result);
            showMessages(result);
        }
    }

    /**
     *
     */
    class GetReceivers extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/receivers");
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
            Log.d(TAG, "MESSAGES: " + result);
            showMessages(result);
        }
    }
}


