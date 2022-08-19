package com.HuimangTech.iShare.ui.messages;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.HuimangTech.iShare.databinding.FragmentMessagesBinding;

import java.util.ArrayList;
import java.util.List;

public class MessagesFragment extends Fragment implements ContactsListAdapter.OnContactListener {

    private FragmentMessagesBinding binding;
    private RecyclerView recyclerView;
    private ContactsListAdapter adapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMessagesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        User userobj1 = new User("7odaifa", "drawable-v24/ic_dashboard_black_24dp.png");
        User userobj2 = new User("M. Aqib", "drawable/ic_dashboard_black_24dp.xml");
        User userobj3 = new User("Abdullah", "drawable/ic_dashboard_black_24dp.xml");

        List<User> messageList = new ArrayList<>();

        messageList.add(userobj1);
        messageList.add(userobj2);
        messageList.add(userobj3);

        recyclerView = binding.rvContacts;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        adapter = new ContactsListAdapter(messageList, this);
        recyclerView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onContactClick(int position) {
        Intent intent = new Intent(getActivity(), chat.class);
        startActivity(intent);
    }
}