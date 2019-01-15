package com.example.david.gigfinder.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.david.gigfinder.R;
import com.example.david.gigfinder.tools.ImageTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ChatAdapter extends ArrayAdapter<String[]> {

    private static final String TAG = "ChatAdapter";

    private ArrayList<ImageView> chatImgs = new ArrayList<ImageView>();
    private ArrayList<TextView> chatNames = new ArrayList<TextView>();

    public ChatAdapter(@NonNull Context context, ArrayList<String[]> strings) {
        super(context, R.layout.chat_row, strings);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View customView = layoutInflater.inflate(R.layout.chat_row, parent, false);

        TextView chatName = (TextView) customView.findViewById(R.id.chatName);
        TextView chatMsg = (TextView) customView.findViewById(R.id.chatMsg);
        ImageView chatImg = (ImageView) customView.findViewById(R.id.chatImg);

        chatName.setText(getItem(position)[0]);
        chatMsg.setText(getItem(position)[1]);

        chatImgs.add(chatImg);
        chatNames.add(chatName);

        if(!getItem(position)[4].equals("empty")) {
            try {
                JSONObject imageProfile = new JSONObject(getItem(position)[4]);

                byte[] decodedString = Base64.decode(imageProfile.getString("image"), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                chatImg.setImageBitmap(decodedByte);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return customView;
    }
}
