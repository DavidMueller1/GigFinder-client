package com.example.david.gigfinder.tools;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.david.gigfinder.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public abstract class Utils {
    //
    public static final String ID_SOUNDCLOUD = "Soundcloud";
    public static final String ID_FACEBOOK = "Facebook";
    public static final String ID_TWITTER = "Twitter";
    public static final String ID_YOUTUBE = "YouTube";
    public static final String ID_INSTAGRAM = "Instagram";
    public static final String ID_SPOTIFY = "Spotify";
    public static final String ID_WEB = "Website";

    private static final String TAG = "Utils";

    public static String genreIdToString(int id, String genres){
        try {
            JSONArray genresJson = new JSONArray(genres);
            for(int i=0; i<genresJson.length(); i++){
                if(genresJson.getJSONObject(i).getInt("id") == id){
                    return genresJson.getJSONObject(i).getString("value");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date dateToString(String dateString){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Timestamp convertStringToTimestamp(String str_date) {
        try {
            String modifiedString = str_date.replace("T", " ");
            Log.d(TAG, "Modified String: " + modifiedString);

            DateFormat formatter;
            formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date date = (Date) formatter.parse(modifiedString);
            java.sql.Timestamp timeStampDate = new Timestamp(date.getTime());
            Log.d(TAG, "Timetsamp out: " + timeStampDate.toString());

            return timeStampDate;
        } catch (ParseException e) {
            System.out.println("Exception :" + e);
            return null;
        }
    }

    public static Date convertStringToDate(String str_date) {
        try {
            String modifiedString = str_date.replace("T", " ");
            Log.d(TAG, "Modified String: " + modifiedString);

            DateFormat formatter;
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = (Date) formatter.parse(modifiedString);

            return date;
        } catch (ParseException e) {
            System.out.println("Exception :" + e);
            return null;
        }
    }

    public static String getTimeStringFromServerFormat(String time) {
        Timestamp timestamp = convertStringToTimestamp(time);
        Date date = new Date();
        date.setTime(timestamp.getTime());
        return new SimpleDateFormat("kk:mm").format(date);
    }

    public static String getDateStringFromServerFormat(String time) {
        Timestamp timestamp = convertStringToTimestamp(time);
        Date date = new Date();
        date.setTime(timestamp.getTime());
        return new SimpleDateFormat("dd.MM.yyyy").format(date);
    }

    /**
     * Returns the Social Media object
     * @param id id of the social media
     * @param jsonArray the social medias object stored in SharedPrefs
     * @return
     */
    public static JSONObject getSocialMedia(int id, JSONArray jsonArray){
        for(int i =0; i<jsonArray.length(); i++){
            try {
                if(jsonArray.getJSONObject(i).getInt("id") == id){
                    return jsonArray.getJSONObject(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static boolean isUserInFavorites(int id, String favorites){
        Log.d(TAG, favorites);
        if(!(favorites.equals("") || favorites.equals("[]"))){
            JSONArray favs = null;
            try {
                favs = new JSONArray(favorites);
            for(int i=0; i<favs.length(); i++){
                Log.d(TAG, String.valueOf(i));
                if(id == favs.getJSONObject(i).getInt("hostId")){
                    return true;
                }
            }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static int idToFavoritesId(int id, String favorites){
        if(!favorites.equals("")){
            JSONArray favs = null;
            try {
                favs = new JSONArray(favorites);
                for(int i=0; i<favs.length(); i++){
                    Log.d(TAG, String.valueOf(i));
                    if(id == favs.getJSONObject(i).getInt("hostId")){
                        return favs.getJSONObject(i).getInt("id");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}
