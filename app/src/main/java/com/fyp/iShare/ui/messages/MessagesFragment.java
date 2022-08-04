package com.fyp.iShare.ui.messages;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.iShare.databinding.FragmentMessagesBinding;
import com.fyp.iShare.ui.downloads.RecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MessagesFragment extends Fragment implements ChannelsListAdapter.OnChannelListener{

    private FragmentMessagesBinding binding;
    private RecyclerView recyclerView;
    private ChannelsListAdapter adapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MessagesViewModel messagesViewModel =
                new ViewModelProvider(this).get(MessagesViewModel.class);

        binding = FragmentMessagesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        User userobj1 = new User("7odaifa", "drawable-v24/send_file_icon.png");
        User userobj2 = new User("M. Aqib", "drawable/ic_dashboard_black_24dp.xml");

        List<User> messageList = new ArrayList<>();

        messageList.add(userobj1);
        messageList.add(userobj2);



        recyclerView = binding.rvChannels;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        adapter = new ChannelsListAdapter(messageList, this);

        //adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        /*textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), chat.class);
                startActivity(intent);
            }
        });*/
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onChannelClick(int position) {

    }
}