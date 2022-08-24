package com.HuimangTech.iShare.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.HuimangTech.iShare.DeviceID;
import com.HuimangTech.iShare.DirectlyReceivePCfile;
import com.HuimangTech.iShare.LoginActivity;
import com.HuimangTech.iShare.SettingsActivity;
import com.HuimangTech.iShare.SingletonSocket;
import com.HuimangTech.iShare.databinding.FragmentHomeBinding;
import com.HuimangTech.iShare.ui.fileTransfer.WAN_Connection;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

public class HomeFragment extends Fragment {

    String[] Permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    static final String TAG = "tag";
    String serverIP = "10.140.49.145";
    boolean NewConnection = true;
    boolean startThread = true;
    boolean manualConnectionState = false;
    int RequestCode = 1122;
    BufferedReader bufferedReader = null;
    PrintWriter printWriter = null;
    Context context;

    ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
                    if (!Environment.isExternalStorageManager()) {
                        Toast.makeText(requireActivity(), "Please Allow permission for storage access!", Toast.LENGTH_LONG).show();
                    }
                }
            }
    );

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (!permissionsGranted()) {
            grantPermissions();
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(TAG, "Current User Found: " + user.getDisplayName());
            binding.tvUsername.setText(user.getDisplayName());
            binding.tvUsername.setVisibility(View.VISIBLE);
        } else {
            binding.tvUsername.setVisibility(View.INVISIBLE);
        }

        binding.btnUser.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });

        binding.btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        });

        binding.btnFileTransfer.setOnClickListener(v -> {

            fileTransferClick(getContext());

        });

        context = requireContext();


        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        manualConnectionState = sharedPreferences.getBoolean("manual", false);
        if (manualConnectionState) {
            binding.edtID.setEnabled(false);
            binding.edtID.setHint("Auto Entered: " + sharedPreferences.getString("ID", ""));
        } else {
            binding.edtID.setEnabled(true);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: triggered");
        if (startThread) {
            startThread = false;
            new Thread(() -> {
                //close any unclosed connection
                if (printWriter != null) {
                    printWriter.println("CLOSE_CONNECTION");
                    printWriter.flush();
                    SingletonSocket.CloseSocket();
                }
                try {
                    String IP_Address = serverIP;
                    int Port_Num = 9999;
                    String id = getDeviceID();

                    DeviceID.deviceID = id;
                    Log.d(TAG, "ID is " + DeviceID.deviceID);

                    requireActivity().runOnUiThread(() -> binding.localID.setText(DeviceID.deviceID));

                    DeviceID.deviceName = Settings.Global.getString(requireActivity().getContentResolver(), "device_name");
                    Log.d(TAG, "deviceName is " + DeviceID.deviceName);

                    Socket socket = SingletonSocket.getSocket();
                    if (socket == null) {
                        Log.d(TAG, "Connecting at " + IP_Address + ":" + Port_Num);
                        socket = new Socket(IP_Address, Port_Num);
                        SingletonSocket.setSocket(socket);
                    }
                    printWriter = new PrintWriter(new OutputStreamWriter(SingletonSocket.getSocket().getOutputStream()));
                    printWriter.println("MOBILE");
                    printWriter.flush();

                    NewConnection = false;

                    printWriter.println(id);
                    printWriter.flush();

                    printWriter.println(DeviceID.deviceName);
                    printWriter.flush();

                    Log.d(TAG, "deviceID and deviceName has benn sent ");

                    // TODO: 8/24/2022  here the Activity is being restarted for some reason

                    bufferedReader = new BufferedReader(new InputStreamReader(SingletonSocket.getSocket().getInputStream()));
                    while (true) {
                        try {
                            String PC_Response = bufferedReader.readLine();
                            Log.d(TAG, "onCreateView:  PC_Response is " + PC_Response);
                            if (PC_Response.equals("RECEIVE_FILE")) {
                                Log.d(TAG, "onCreateView: Receive file from PC");
                                requireActivity().runOnUiThread(() -> {
                                    DirectlyReceivePCfile receiveFile = new DirectlyReceivePCfile(context);
                                    receiveFile.execute("");
                                });
                                startThread = true;
                                break;
                            } else if (PC_Response.equals("ERROR")) {
                                //If User Enters a wrong ID
                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(context, "PC not found", Toast.LENGTH_SHORT).show();
                                });
                            } else if (PC_Response.equals("SUCCESS")) {
                                Intent intent = new Intent(getActivity(), WAN_Connection.class);
                                startActivity(intent);
                                startThread = true;
                                break;
                            }
                            else  {
                                Log.d(TAG, "onStart: Invalid response that is "+PC_Response);

                            }
                        } catch (Exception e) {
                            Log.d(TAG, "Exception  " + e);
                        }
                    }
                } catch (Exception e) {
                    //sendToast(e.toString());
                    e.printStackTrace();
                    Log.d(TAG, "On start Thread Exception  " + e);
                }
            }).start();
        }
    }

    String getDeviceID() {
        String id;
        String fileName = "Unique_ID_file";
        SharedPreferences sp = requireActivity().getSharedPreferences(fileName, Context.MODE_PRIVATE);

        id = sp.getString("ID", null);
        if (id == null) {
            StringBuilder ID = new StringBuilder();
            Log.d(TAG, "No previous ID found ");
            Random random = new Random();
            for (int i = 0; i < 8; i++) {
                ID.append(random.nextInt(9));
                id = ID.toString();

                SharedPreferences.Editor editor = sp.edit();
                editor.putString("ID", id);
                editor.apply();
            }

        }
        return id;
    }

    private void fileTransferClick(Context context) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        manualConnectionState = sharedPreferences.getBoolean("manual", false);

        String IP_Address;
        int Port_Num;
        String partnerId;
        String password;

        if (manualConnectionState) {
            Log.d(TAG, "manualConnectionState: is On");
            IP_Address = sharedPreferences.getString("IP", serverIP);
            Port_Num = Integer.parseInt(sharedPreferences.getString("port", "9999"));
            partnerId = sharedPreferences.getString("ID", "");
            password = sharedPreferences.getString("password", "");

        } else {
            IP_Address = serverIP;
            Port_Num = 9999;
            partnerId = binding.edtID.getText().toString().trim();
            password = "1";
        }
        //if ID is not empty then he will try to connect
        if (!partnerId.equals("")) {
            Thread t1 = new Thread(() -> {
                try {
                    if (SingletonSocket.getSocket() == null) {
                        Log.d(TAG, "fileTransferClickListener: Connecting at " + IP_Address + ":" + Port_Num);

                        Socket socket = new Socket(IP_Address, Port_Num);
                        SingletonSocket.setSocket(socket);
                        NewConnection = true;
                    }
                    if (sendIdPassword(partnerId, password)) {
                        // TODO: 8/24/2022 what is this ?
/*
                            Intent intent = new Intent(getActivity(), WAN_Connection.class);
                            startActivity(intent);*/
                    }
                } catch (Exception e) {
                    //sendToast(e.toString());
                    e.printStackTrace();
                    Log.d(TAG, "Exception  " + e);
                }
            });
            t1.start();
        } else {
            Toast.makeText(getContext(), "ID is Empty", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private boolean sendIdPassword(String id, String password) {
        try {

            if (printWriter == null) {
                printWriter = new PrintWriter(new OutputStreamWriter(SingletonSocket.getSocket().getOutputStream()));
            }
            if (bufferedReader == null) {
                bufferedReader = new BufferedReader(new InputStreamReader(SingletonSocket.getSocket().getInputStream()));
            }

            if (NewConnection) {
                printWriter.println("MOBILE");
                printWriter.flush();
                NewConnection = false;
            }

            printWriter.println("_find_by_id_");
            printWriter.flush();

            printWriter.println(id);
            printWriter.flush();
            /*printWriter.println(password);
            printWriter.flush();*/

            //Log.d(TAG, "ID " + id + " and Password " + password + " send ");
            Log.d(TAG, "ID " + id + " send ");
/*
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
            }*/
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Exception  " + e);
        }

        return false;
    }

    private void grantPermissions() {
        Log.d(TAG, "Granting Permission");
        ActivityCompat.requestPermissions(requireActivity(), Permissions, RequestCode);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Log.d(TAG, "Opening settings for permission ");

                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", requireActivity().getApplicationContext().getPackageName())));
                myActivityResultLauncher.launch(intent);
            } catch (Exception e) {
                Log.d(TAG, "grantPermissions: Exception" + e);
            }
        }

    }

    private boolean permissionsGranted() {
        for (String permission : Permissions) {
            if (ActivityCompat.checkSelfPermission(requireActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permissions not granted i.e " + permission);
                return false;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Log.d(TAG, "Android version > 11 ");

            return Environment.isExternalStorageManager();
        }
        return true;
    }

}

