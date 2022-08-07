package com.fyp.iShare.ui.devices;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.iShare.R;
import com.fyp.iShare.databinding.FragmentDevicesBinding;

import java.util.ArrayList;
import java.util.List;

public class DevicesFragment extends Fragment implements SavedDevicesAdapter.OnDeviceListener {

    private FragmentDevicesBinding binding;
    private RecyclerView savedRecyclerView, availableRecyclerView;
    private SavedDevicesAdapter adapter;
    private LinearLayout availableDevices, savedDevices;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DevicesViewModel devicesViewModel =
                new ViewModelProvider(this).get(DevicesViewModel.class);

        binding = FragmentDevicesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        List<String> devices = new ArrayList<String>();
        devices.add("Device A");
        devices.add("Device B");
        devices.add("Device C");
        savedRecyclerView = binding.rvSavedDevices;
        savedRecyclerView.setHasFixedSize(true);
        savedRecyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        adapter = new SavedDevicesAdapter(devices, this);
        //adapter.setClickListener(this);
        savedRecyclerView.setAdapter(adapter);

        availableRecyclerView = binding.rvAvailableDevices;
        availableRecyclerView.setHasFixedSize(true);
        availableRecyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        adapter = new SavedDevicesAdapter(devices, this);
        //adapter.setClickListener(this);
        availableRecyclerView.setAdapter(adapter);

        availableDevices = binding.tvAvailableDevices;
        savedDevices = binding.tvSavedDevices;

        availableDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.cnsAvailableRv.getVisibility() == View.VISIBLE) {
                    binding.cnsAvailableRv.setVisibility(getView().GONE);
                    binding.icAvailable.setImageResource(R.drawable.ic_baseline_keyboard_arrow_right_24);
                } else {
                    binding.cnsAvailableRv.setVisibility(getView().VISIBLE);
                    binding.icAvailable.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24);

                }
            }
        });

        savedDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (binding.cnsSavedRv.getVisibility() == View.VISIBLE) {
                    binding.cnsSavedRv.setVisibility(getView().GONE);
                    binding.icSaved.setImageResource(R.drawable.ic_baseline_keyboard_arrow_right_24);

                } else {
                    binding.cnsSavedRv.setVisibility(getView().VISIBLE);
                    binding.icSaved.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24);

                }
            }
        });
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