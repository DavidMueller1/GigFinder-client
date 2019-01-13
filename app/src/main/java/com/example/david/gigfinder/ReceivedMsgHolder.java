package com.example.david.gigfinder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.david.gigfinder.data.Message;
import com.google.android.gms.common.util.DataUtils;

public class ReceivedMsgHolder extends RecyclerView.ViewHolder{

    TextView messageText, timeText, nameText;
    ImageView profileImage;

    public ReceivedMsgHolder(View itemView) {
        super(itemView);
        messageText = (TextView) itemView.findViewById(R.id.text_msg_body);
        timeText = (TextView) itemView.findViewById(R.id.text_msg_time);
        nameText = (TextView) itemView.findViewById(R.id.text_msg_name);
        profileImage = (ImageView) itemView.findViewById(R.id.image_msg_profile);
    }

    public void bind(Message message) {
        messageText.setText(message.getMessage());

        // Format the stored timestamp into a readable String using method.
        Context mContext = null;
        timeText.setText(DateUtils.formatDateTime(null, message.getCreatedAt(), 0));
        nameText.setText(message.getName());

        // Insert the profile image from the URL into the ImageView.
        //Utils.displayRoundImageFromUrl(mContext, message.getSender().getProfileUrl(), profileImage);
    }
}
