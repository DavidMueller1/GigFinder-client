package com.example.david.gigfinder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.david.gigfinder.adapters.ChatAdapter;

import java.util.ArrayList;

public class ChatFragment extends Fragment {

    private static final String TAG = "APPLOG - ChatFragment";
    private ChatAdapter chatAdapter;
    private FloatingActionButton chatfab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        chatfab = (FloatingActionButton) getView().findViewById(R.id.chatfab);

        ArrayList<String[]> placeholderStrings = new ArrayList<>();
        placeholderStrings.add(new String[]{"Artist Name", "Placeholder message..."});
        placeholderStrings.add(new String[]{"Host Name", "Test message..."});

        chatAdapter = new ChatAdapter(this.getContext(), placeholderStrings);
        ListView listView = (ListView) getView().findViewById(R.id.chatListView);
        listView.setAdapter(chatAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(getActivity(), chatAdapter.getChatImgs().get(position), "chat");
                startActivity(intent, options.toBundle());
            }
        });
    }
}
