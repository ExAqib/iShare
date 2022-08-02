package com.fyp.iShare.ui.devices;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.iShare.databinding.FragmentDevicesBinding;

import java.util.ArrayList;
import java.util.List;

public class DevicesFragment extends Fragment implements RecyclerAdapter.OnDeviceListener {

    private FragmentDevicesBinding binding;
    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DevicesViewModel devicesViewModel =
                new ViewModelProvider(this).get(DevicesViewModel.class);

        binding = FragmentDevicesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDevices;
        devicesViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        List<String> devices = new ArrayList<String>();
        devices.add("Device A");
        devices.add("Device B");
        devices.add("Device C");
        recyclerView = binding.rvDevices;
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
    public void onDeviceClick(int position) {
        Toast.makeText(getContext(), "clicked", Toast.LENGTH_SHORT).show();
    }
}