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

        chatAdapter = new ChatAdapter(this.getContext(), chatStrings);
        ListView listView = (ListView) getView().findViewById(R.id.chatListView);
        listView.setAdapter(chatAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("idToken", idToken);
                intent.putExtra("profileUserId", Integer.valueOf(chatStrings.get(position)[2]));
                startActivity(intent);
            }
        });
    }

    private void showMessages(String result){
        try {
            String chatpartner;
            if(user=="host"){
                chatpartner = "artist";
            }else{
                chatpartner = "host";
            }
            JSONArray msgJson = new JSONArray(result);
            for(int i = 0; i<msgJson.length(); i++){
                String name = msgJson.getJSONObject(i).getJSONObject(chatpartner).getString("name");
                String lastmsg = msgJson.getJSONObject(i).getJSONObject("lastMessage").getString("content");
                String id = String.valueOf(msgJson.getJSONObject(i).getJSONObject(chatpartner).getInt("id"));
                chatStrings.add(new String[]{name, lastmsg, id});
            }
            chatAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
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


