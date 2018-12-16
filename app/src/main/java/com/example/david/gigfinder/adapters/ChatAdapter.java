package com.example.david.gigfinder.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.david.gigfinder.R;

import java.util.ArrayList;

public class ChatAdapter extends ArrayAdapter<String[]> {

    private static final String TAG = "ChatAdapter";

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

        return customView;
    }
}
