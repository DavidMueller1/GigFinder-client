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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.david.gigfinder.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParticipantAdapter extends ArrayAdapter<String[]> {

    private static final String TAG = "ParticipantAdapter";

    private ArrayList<ImageView> participantImgs = new ArrayList<ImageView>();
    private ArrayList<TextView> participantNames = new ArrayList<TextView>();

    public ParticipantAdapter(@NonNull Context context, ArrayList<String[]> strings) {
        super(context, R.layout.participant_row, strings);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View customView = layoutInflater.inflate(R.layout.participant_row, parent, false);

        TextView partName = customView.findViewById(R.id.participant_name);
        ImageView partImg = customView.findViewById(R.id.chatImg);

        partName.setText(getItem(position)[0]);

        participantImgs.add(partImg);
        participantNames.add(partName);

        /*if(!getItem(position)[4].equals("empty")) {
            try {
                JSONObject imageProfile = new JSONObject(getItem(position)[4]);

                byte[] decodedString = Base64.decode(imageProfile.getString("image"), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                partImg.setImageBitmap(decodedByte);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }*/

        Button button = customView.findViewById(R.id.participant_select_button);
        final ListView parentList = (ListView) parent;
        final int finalPosition = position;
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                parentList.performItemClick(v, finalPosition, 0); // Let the event be handled in onItemClick()
            }
        });

        return customView;
    }
}
