package com.example.david.gigfinder;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import com.example.david.gigfinder.data.Message;
import com.example.david.gigfinder.tools.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *
 */
public class SentMsgHolder extends RecyclerView.ViewHolder {

    private TextView messageText, timeText;

    /**
     * Constructor
     * @param itemView
     */
    public SentMsgHolder(View itemView) {
        super(itemView);
        messageText = (TextView) itemView.findViewById(R.id.text_msg_body);
        timeText = (TextView) itemView.findViewById(R.id.text_msg_time);
    }

    /**
     * Binds the view to the message
     * @param message
     */
    public void bind(Message message) {
        messageText.setText(message.getMessage());

        // Formatating the Timestamp
        Date date = Utils.convertStringToDate(message.getCreatedAt());
        SimpleDateFormat dt1;
        if (!DateUtils.isToday(date.getTime())) {
            dt1 = new SimpleDateFormat("dd MMMM", Locale.GERMAN);
        }else{
            dt1 = new SimpleDateFormat("HH:mm");
        }

        timeText.setText(dt1.format(date));
    }
}
