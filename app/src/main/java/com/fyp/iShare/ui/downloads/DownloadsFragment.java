package com.fyp.iShare.ui.downloads;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.iShare.databinding.FragmentDownloadsBinding;
import com.fyp.iShare.ui.downloads.RecyclerAdapter;
import com.fyp.iShare.ui.messages.chat;

import java.util.ArrayList;
import java.util.List;

public class DownloadsFragment extends Fragment implements RecyclerAdapter.OnFileListener {

    private FragmentDownloadsBinding binding;
    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DownloadsViewModel downloadsViewModel =
                new ViewModelProvider(this).get(DownloadsViewModel.class);

        binding = FragmentDownloadsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        List<String> devices = new ArrayList<String>();
        devices.add("File1");
        devices.add("File2");
        devices.add("File3");
        recyclerView = binding.rvDownloads;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        adapter = new RecyclerAdapter(devices, this);

        //adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onFileClick(int position) {

    }
}