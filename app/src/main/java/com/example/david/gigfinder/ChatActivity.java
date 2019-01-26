package com.example.david.gigfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.david.gigfinder.adapters.MessageListAdapter;
import com.example.david.gigfinder.data.Artist;
import com.example.david.gigfinder.data.Message;
import com.example.david.gigfinder.data.User;

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
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.example.david.gigfinder.tools.Utils.convertStringToTimestamp;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private int receiverId;
    private int authorId;

    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private ArrayList<Message> messageList = new ArrayList<Message>();
    private LinearLayout nameboxLayout;
    private ImageView backBtn;
    private ImageView chatImg;
    private Button sendBtn;
    private EditText chatText;
    private TextView chatName;

    private String idToken;
    private String user;
    private String name;
    private byte[] picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE);

        idToken = getIntent().getExtras().getString("idToken");
        receiverId = getIntent().getExtras().getInt("profileUserId");
        name = getIntent().getExtras().getString("name");

        authorId = prefs.getInt("userId", 0);
        user = prefs.getString("user", "null");

        GetMessages getMessages = new GetMessages();
        getMessages.execute();

        chatName = (TextView) findViewById(R.id.chatName);
        chatName.setText(name);

        chatImg = (ImageView) findViewById(R.id.chatImg);
        try {
            JSONObject imageProfile = new JSONObject(getIntent().getExtras().getString("picture"));
            picture = Base64.decode(imageProfile.getString("image"), Base64.DEFAULT);
            chatImg.setImageBitmap(BitmapFactory.decodeByteArray(picture, 0, picture.length));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        chatText = (EditText) findViewById(R.id.edittext_chatbox);

        backBtn = (ImageView) findViewById(R.id.backImg);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        sendBtn = (Button) findViewById(R.id.button_chatbox_send);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chatText.getText().toString() != "") {
                    PostMessage postMessage = new PostMessage();
                    postMessage.execute(chatText.getText().toString());
                }
            }
        });

        nameboxLayout = (LinearLayout) findViewById(R.id.layout_namebox);
        nameboxLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user.equals("host")) {
                    Intent intent = new Intent(ChatActivity.this, ArtistProfileActivity.class);
                    intent.putExtra("profileUserId", receiverId);
                    intent.putExtra("idToken", idToken);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(ChatActivity.this, HostProfileActivity.class);
                    intent.putExtra("profileUserId", receiverId);
                    intent.putExtra("idToken", idToken);
                    startActivity(intent);
                }
            }
        });

        mMessageRecycler = (RecyclerView) findViewById(R.id.reyclerview_message_list);
        mMessageAdapter = new MessageListAdapter(this, messageList);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecycler.setAdapter(mMessageAdapter);

    }

    private void showMessages(String messages){
        try {
            JSONArray messagesArray = new JSONArray(messages);
            for(int i=0; i<messagesArray.length();i++){
                //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                //Date date = format.parse(messagesArray.getJSONObject(i).getString("created"));
                //long l = date.getTime();
                Timestamp timestamp = convertStringToTimestamp(messagesArray.getJSONObject(i).getString("created"));
                if(messagesArray.getJSONObject(i).getInt("authorId")==receiverId){
                    messageList.add(new Message(messagesArray.getJSONObject(i).getString("content"), name, false, timestamp.getTime(), picture));

                }else{
                    messageList.add(new Message(messagesArray.getJSONObject(i).getString("content"), "me", true, timestamp.getTime(), null));
                }
            }
            sortMessages();
            mMessageAdapter.notifyDataSetChanged();
            mMessageRecycler.scrollToPosition(messageList.size()-1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sortMessages(){
        messageList.sort(new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                Timestamp t1 = new Timestamp(o1.getCreatedAt());
                Timestamp t2 = new Timestamp(o2.getCreatedAt());
                return t1.compareTo(t2);
            }
        });
    }

    /**
     *
     */
    class PostMessage extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://gigfinder.azurewebsites.net/api/messages");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Authorization", idToken);
                urlConnection.setRequestProperty("Content-Type","application/json;charset=utf-8");
                urlConnection.setRequestMethod("POST");
                urlConnection.setUseCaches(false);
                urlConnection.setDoOutput(true);

                //Send data
                DataOutputStream os = new DataOutputStream(urlConnection.getOutputStream());
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("AuthorId", authorId);
                jsonObject.put("ReceiverId", receiverId);
                jsonObject.put("Content", params[0]);

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
                            Log.d(TAG, "PostMessage: STATUS CODE: " + statusCode);
                            Log.d(TAG, "PostMessage: RESPONESE MESSAGE: " + httpConn.getResponseMessage());
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

                Log.d(TAG, "PostMessage: RESPONSE:" + response.toString());

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
            chatText.getText().clear();
            JSONObject msg = null;
            try {
                msg = new JSONObject(result);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date date = format.parse(msg.getString("created"));
                long l = date.getTime();
                messageList.add(new Message(msg.getString("content"), "me", true, l, null));
                mMessageAdapter.notifyDataSetChanged();
                mMessageRecycler.scrollToPosition(messageList.size()-1);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     */
    class GetMessages extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {

                URL url = new URL("https://gigfinder.azurewebsites.net/api/messages?receiver=" + receiverId);
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
            Log.d(TAG, "Messages: " + result);
            showMessages(result);
        }
    }
}
