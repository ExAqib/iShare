package com.fyp.iShare.ui.messages;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.iShare.R;

import java.util.ArrayList;
import java.util.List;

public class chat extends AppCompatActivity {
    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

// TODO: 8/4/2022 testing users, remove later
        User userobj1 = new User("7odaifa", "drawable-v24/send_file_icon.png");
        User userobj2 = new User("M. Aqib", "drawable/ic_dashboard_black_24dp.xml");

        List<UserMessage> messageList = new ArrayList<>();
        UserMessage testobj1 = new UserMessage("hi there", userobj1, 1126);
        UserMessage testobj2 = new UserMessage("whats up", userobj2, 9735);
        UserMessage testobj3 = new UserMessage("all good", userobj1, 4545);
        UserMessage testobj4 = new UserMessage("and you?", userobj1, 4545);

        messageList.add(testobj1);
        messageList.add(testobj2);
        messageList.add(testobj3);
        messageList.add(testobj4);

        mMessageRecycler = findViewById(R.id.recycler_chat);
        mMessageAdapter = new MessageListAdapter(this, messageList);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecycler.setAdapter(mMessageAdapter);

    }
}
