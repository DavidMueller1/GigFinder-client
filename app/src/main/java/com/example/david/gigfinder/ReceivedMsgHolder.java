package com.example.david.gigfinder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.david.gigfinder.data.Message;
import com.example.david.gigfinder.tools.Utils;
import com.google.android.gms.common.util.DataUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReceivedMsgHolder extends RecyclerView.ViewHolder{

    private TextView messageText, timeText, nameText;
    private ImageView profileImage;

    /**
     * Constructor
     * @param itemView
     */
    public ReceivedMsgHolder(View itemView) {
        super(itemView);
        messageText = (TextView) itemView.findViewById(R.id.text_msg_body);
        timeText = (TextView) itemView.findViewById(R.id.text_msg_time);
        nameText = (TextView) itemView.findViewById(R.id.text_msg_name);
        profileImage = (ImageView) itemView.findViewById(R.id.image_msg_profile);
    }

    /**
     * Used to bind the message to the view
     * @param message
     */
    public void bind(Message message) {
        messageText.setText(message.getMessage());

        Date date = Utils.convertStringToDate(message.getCreatedAt());
        SimpleDateFormat dt1;
        if (!DateUtils.isToday(date.getTime())) {
            dt1 = new SimpleDateFormat("dd MMMM", Locale.GERMAN);
        }else{
            dt1 = new SimpleDateFormat("HH:mm");
        }

        timeText.setText(dt1.format(date));
        nameText.setText(message.getName());
        profileImage.setImageBitmap(BitmapFactory.decodeByteArray(message.getPicture(), 0, message.getPicture().length));

    }
}
