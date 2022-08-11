package com.fyp.iShare.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fyp.iShare.SettingsActivity;
import com.fyp.iShare.SingletonSocket;
import com.fyp.iShare.WAN_Connection;
import com.fyp.iShare.databinding.FragmentHomeBinding;
import com.fyp.iShare.ui.login.LoginActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class HomeFragment extends Fragment {

    static final String TAG = "tag";
    boolean NewConnection = true;
    Socket socket = null;
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.btnUser.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });

        binding.btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        });

        binding.btnFileTransfer.setOnClickListener(v -> {

            //String IP_Address = binding.ipAddress.getText().toString().trim();
            String IP_Address = "192.168.10.99";
            //int Port_Num = Integer.parseInt(binding.portNumber.getText().toString().trim());
            int Port_Num = 9999;

            //String id = binding.ID.getText().toString().trim();
            String id = binding.edtID.getText().toString().trim();
            // String password = binding.PASSWORD.getText().toString().trim();
            String password = "1";

            Thread t1 = new Thread(() -> {
                try {
                    if (socket == null) {
                        socket = new Socket(IP_Address, Port_Num);

                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        int changedPort = Integer.parseInt(bufferedReader.readLine());
                        Log.d(TAG, "Received port num is " + changedPort);
                        socket.close();
                        Log.d(TAG, "Prev. Socket Closed");

                        socket = new Socket(IP_Address, changedPort);
                    }
                    if (sendIdPassword(id, password)) {
                        SingletonSocket.setSocket(socket);

                        Intent intent = new Intent(getActivity(), WAN_Connection.class);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    //sendToast(e.toString());
                    e.printStackTrace();
                    Log.d(TAG, "Exception  " + e);
                }
            });
            t1.start();


        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private boolean sendIdPassword(String id, String password) {
        try {
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            if (NewConnection) {
                printWriter.println("MOBILE");
                printWriter.flush();
                NewConnection = false;
            }
            printWriter.println(id);
            printWriter.flush();
            printWriter.println(password);
            printWriter.flush();

            Log.d(TAG, "ID " + id + " and Password " + password + " send ");

            String data = bufferedReader.readLine();
            Log.d(TAG, "Received response  " + data);

            if (data.equals("ERROR")) {
                Log.d(TAG, " PC not found. (Wrong ID\\Password)");
                //sendToast("PC not found.");
            } else if (data.equals("SUCCESS")) {
                Log.d(TAG, "PC found. ID\\Password matched successfully");
                return true;
            } else {
                Log.d(TAG, "Invalid response from server");
                //sendToast("Invalid response from server");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Exception  " + e);
        }

        return false;
    }
}