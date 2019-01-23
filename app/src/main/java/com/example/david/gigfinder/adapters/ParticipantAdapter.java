package com.example.david.gigfinder.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.CircularProgressDrawable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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

        String name = getItem(position)[0];
        String imgID = getItem(position)[1];
        String id = getItem(position)[2];
        String isEventHost = getItem(position)[3];
        String buttonMode = getItem(position)[4];
        String imgBase64 = getItem(position)[5];

        TextView partName = customView.findViewById(R.id.participant_name);
        ImageView partImg = customView.findViewById(R.id.participant_image);
        Button selectButton = customView.findViewById(R.id.participant_select_button);
        Button canelButton = customView.findViewById(R.id.participant_cancel_button);

        partName.setText(getItem(position)[0]);

        if(isEventHost.equals("false")) {
            selectButton.setVisibility(View.GONE);
        }
        else {
            if(buttonMode.equals("none")) {
                selectButton.setVisibility(View.GONE);
            }
            else if(buttonMode.equals("cancel")) {
                selectButton.setVisibility(View.GONE);
                canelButton.setVisibility(View.VISIBLE);
            }
        }

        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(getContext());
        circularProgressDrawable.setStrokeWidth(10f);
        circularProgressDrawable.setCenterRadius(40f);
        circularProgressDrawable.setColorFilter(new PorterDuffColorFilter(getContext().getResources().getColor(R.color.orange), PorterDuff.Mode.SRC_IN));
        circularProgressDrawable.start();

        if(!imgBase64.equals("noPic")) {
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(circularProgressDrawable)
                    .override(ImageTools.PROFILE_PICTURE_SIZE)
                    .transforms(new CenterCrop(), new RoundedCorners(30));

            Glide.with(getContext())
                    .load(Base64.decode(imgBase64, Base64.DEFAULT))
                    .apply(options)
                    .into(partImg);
        }
        else {
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(circularProgressDrawable)
                    .override(ImageTools.PROFILE_PICTURE_SIZE)
                    .transforms(new CenterCrop(), new RoundedCorners(30));

            Glide.with(getContext())
                    .load(circularProgressDrawable)
                    .apply(options)
                    .into(partImg);
        }



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

        final ListView parentList = (ListView) parent;
        final int finalPosition = position;
        selectButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                parentList.performItemClick(v, finalPosition, 0); // Let the event be handled in onItemClick()
            }
        });

        canelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                parentList.performItemClick(v, finalPosition, 0); // Let the event be handled in onItemClick()
            }
        });

        return customView;
    }
}
