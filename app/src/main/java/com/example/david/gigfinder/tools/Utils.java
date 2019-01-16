package com.example.david.gigfinder.tools;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.david.gigfinder.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public abstract class Utils {
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

}
