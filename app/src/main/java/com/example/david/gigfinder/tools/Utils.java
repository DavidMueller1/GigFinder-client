package com.example.david.gigfinder.tools;

import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.example.david.gigfinder.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;

import static android.content.Context.MODE_PRIVATE;

public abstract class Utils {

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


}
