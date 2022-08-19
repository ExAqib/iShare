package com.HuimangTech.iShare.ui.devices;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.HuimangTech.iShare.LinkedDevices;
import com.HuimangTech.iShare.R;
import com.HuimangTech.iShare.SingletonSocket;
import com.HuimangTech.iShare.WAN_Connection;
import com.HuimangTech.iShare.databinding.FragmentDevicesBinding;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class DevicesFragment extends Fragment implements SavedDevicesAdapter.OnDeviceListener {

    Socket socket;
    PrintWriter printWriter;
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
        devices.add("No Devices");
        savedRecyclerView = binding.rvSavedDevices;
        savedRecyclerView.setHasFixedSize(true);
        savedRecyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));

        //Getting value from LinkedDevices class
        //adapter = new SavedDevicesAdapter(devices, this);
        adapter = new SavedDevicesAdapter(LinkedDevices.GetAllDeviceNames(), this);
        //adapter.setClickListener(this);
        savedRecyclerView.setAdapter(adapter);

       /* availableRecyclerView = binding.rvAvailableDevices;
        availableRecyclerView.setHasFixedSize(true);
        availableRecyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        adapter = new SavedDevicesAdapter(devices, this);
        //adapter.setClickListener(this);
        availableRecyclerView.setAdapter(adapter);*/

/*
        availableDevices = binding.tvAvailableDevices;
*/
        savedDevices = binding.tvSavedDevices;

     /*   availableDevices.setOnClickListener(new View.OnClickListener() {
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
        });*/

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
    public void onDeviceClick(int position, String DeviceName, String RecyclerView) {
        String DeviceID = LinkedDevices.GetDeviceID(DeviceName);

        if (DeviceName.equals("No Saved Device Available"))
            return;

        Toast.makeText(getContext(), " connecting " + DeviceName + " " + DeviceID, Toast.LENGTH_SHORT).show();

        String ServerIP = "192.168.10.99";
        int ServerPort = 9999;
        Runtime runtime = Runtime.getRuntime();
        Thread t1 = new Thread(() -> {

            try {
                /*
                //for pinging server
                Process  mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 192.168.10.99");
                int mExitValue = mIpAddrProcess.waitFor();
                Log.d("tag", " mExitValue "+mExitValue);
                if(mExitValue==0){
                    sendToast("Server Offline");
                }
                if(RecyclerView.equals("SavedDevicesRecyclerView")){

                }else {

                }*/

                if ((socket = SingletonSocket.getSocket()) == null) {
                    socket = new Socket(ServerIP, ServerPort);
                    SingletonSocket.setSocket(socket);
                    printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

                    SendRequest("MOBILE");
                }
                printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

                if (ConnectPC(DeviceID)) {
                    goToNextActivity();
                }

            } catch (Exception e) {
                sendToast(e.toString());
                Log.d("tag", "Exception  " + e);
            }
        });
        t1.start();

    }

    private void goToNextActivity() {
        requireActivity().runOnUiThread(() -> {
            startActivity(new Intent(requireActivity(), WAN_Connection.class));
        });

    }

    private void sendToast(String message) {
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(requireActivity(), message, Toast.LENGTH_LONG).show();
        });
    }

    private boolean ConnectPC(String id) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            SendRequest("_find_by_id_");
            SendRequest(id);
            Log.d("tag", "ID " + id + " send ");

            String data = bufferedReader.readLine();
            Log.d("tag", "Received response  " + data);

            if (data.equals("ERROR")) {
                Log.d("tag", " PC not found.");
                sendToast("PC not found.");
            } else if (data.equals("SUCCESS")) {
                Log.d("tag", "PC found. ");
                return true;
            } else {
                Log.d("tag", "Invalid response from server" + data);
                sendToast("Invalid response from server");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("tag", "Exception  " + e);
            sendToast(e.getMessage());

        }

        return false;
    }

    void SendRequest(String request) {
        try {
            Log.d("tag", "Sending Request>>" + request);

            printWriter.println(request);
            printWriter.flush();

        } catch (Exception e) {
            Log.d("tag", "Exception  " + e);
            sendToast(e.getMessage());
        }
    }
}