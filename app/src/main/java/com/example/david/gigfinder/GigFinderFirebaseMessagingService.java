package com.example.david.gigfinder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

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

import static android.support.constraint.Constraints.TAG;
import static com.example.david.gigfinder.LoginActivity.ID_TOKEN;

public class GigFinderFirebaseMessagingService extends FirebaseMessagingService {
    public static final String DEVICE_TOKEN = "DeviceToken";
    public static final String USER_ID = "userId";
    public static final String CHANNEL_ID = "com.example.david.gigfinder.main";

    private int messageCounter = 0;

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d(TAG, "Refreshed token: " + s);

        // write token s in sharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE).edit();
        editor.putString(DEVICE_TOKEN, s);
        editor.apply();

        // send token s to server
        sendDeviceToken(getBaseContext());
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            // handle data

            //if (/* Check if data needs to be processed by long running job */ true) {
            //    // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
            //    scheduleJob();
            //} else {
            //    // Handle message within 10 seconds
            //    handleNow();
            //}
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            if (messageCounter == 0)
                initNotificationChannels();

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_music_gig)
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setColor(getColor(R.color.darkOrange))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.notify(messageCounter, mBuilder.build());
            messageCounter++;
        }
    }

    private void initNotificationChannels() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channelOne = new NotificationChannel(CHANNEL_ID, getString(R.string.channel_name), NotificationManager.IMPORTANCE_DEFAULT);
        channelOne.setDescription(getString(R.string.channel_description));
        channelOne.enableLights(true);
        mNotificationManager.createNotificationChannel(channelOne);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    public static void sendDeviceToken(Context context) {
        SendDeviceToken sendDeviceToken = new SendDeviceToken(context);
        sendDeviceToken.execute();
    }

    static class SendDeviceToken extends AsyncTask<String, Void, String> {
        Context context;

        public SendDeviceToken(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.shared_prefs), Context.MODE_PRIVATE);
                if (!prefs.contains(DEVICE_TOKEN) || !prefs.contains(ID_TOKEN) || !prefs.contains(USER_ID))
                    return "";
                String deviceToken = prefs.getString(DEVICE_TOKEN, null);
                String idToken = prefs.getString(ID_TOKEN, null);
                int userID = prefs.getInt(USER_ID, 0);

                URL url = new URL("https://gigfinder.azurewebsites.net/api/devicetoken/" + userID);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Authorization", idToken);
                urlConnection.setRequestProperty("Content-Type","application/json");
                urlConnection.setRequestMethod("PUT");
                urlConnection.setUseCaches(false);
                urlConnection.setDoOutput(true);

                //Send data
                DataOutputStream os = new DataOutputStream(urlConnection.getOutputStream());
                os.writeBytes("\"" + deviceToken + "\"");
                os.close();

                //Get response
                InputStream is = null;
                try {
                    is = urlConnection.getInputStream();
                } catch (IOException ioe) {
                    if (urlConnection instanceof HttpURLConnection) {
                        HttpURLConnection httpConn = (HttpURLConnection)urlConnection;
                        int statusCode = httpConn.getResponseCode();
                        if (statusCode != 200) {
                            is = httpConn.getErrorStream();
                            Log.d(TAG, "SendDeviceToken: STATUS CODE: " + statusCode);
                            Log.d(TAG, "SendDeviceToken: RESPONSE MESSAGE: " + httpConn.getResponseMessage());
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

                Log.d(TAG, "SendDeviceToken: RESPONSE:" + response.toString());

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

        }
    }
}
