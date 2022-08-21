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
import com.HuimangTech.iShare.DownloadFile;
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

    static final String TAG = "tag";
    boolean NewConnection = true;
    String[] Permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
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

        if (!permissionsGranted()) {
            grantPermissions();
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            //binding.username.setText();
        } else {
            // No user is signed in
        }
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

        fileTransferClickListener(root.getContext());

        context =requireContext();
        Thread t1 = new Thread(() -> {
            try {
                String IP_Address = "192.168.10.99";
                int Port_Num = 9999;
                Log.d(TAG, " Connecting at " + IP_Address + ":" + Port_Num);

                Socket socket = new Socket(IP_Address, Port_Num);
                SingletonSocket.setSocket(socket);

                printWriter = new PrintWriter(new OutputStreamWriter(SingletonSocket.getSocket().getOutputStream()));
                printWriter.println("MOBILE");
                printWriter.flush();

                NewConnection = false;
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
                    }
                    id = ID.toString();

                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("ID", id);
                    editor.apply();
                }
                DeviceID.deviceID = id;
                Log.d(TAG, "ID is " + DeviceID.deviceID);

                String finalId = id;
                requireActivity().runOnUiThread(()->{
                    binding.mobileDataUsage.setText(finalId);
                });


                DeviceID.deviceName = Settings.Global.getString(requireActivity().getContentResolver(), "device_name");
                Log.d(TAG, "deviceName is " + DeviceID.deviceName);

                printWriter.println(id);
                printWriter.flush();

                printWriter.println(DeviceID.deviceName);
                printWriter.flush();

                Log.d(TAG, "deviceId and deviceName Send ");


                bufferedReader = new BufferedReader(new InputStreamReader(SingletonSocket.getSocket().getInputStream()));

                while(true){
                    String PC_Response = bufferedReader.readLine();
                    Log.d(TAG, "onCreateView:  PC_Response is " + PC_Response);
                    if (PC_Response.equals("RECEIVE_FILE")) {
                        Log.d(TAG, "onCreateView: Receive file from PC");
                        requireActivity().runOnUiThread(()->{
                            DirectlyReceivePCfile receiveFile = new DirectlyReceivePCfile(context);
                            receiveFile.execute("");
                        });
                        break;
                    }
                    else if(PC_Response.equals("ERROR")){
                    //If User Enters a wrong ID
                        requireActivity().runOnUiThread(()->{
                            Toast.makeText(context, "PC not found", Toast.LENGTH_SHORT).show();
                        });
                    }
                    else if(PC_Response.equals("SUCCESS")){
                        Intent intent = new Intent(getActivity(), WAN_Connection.class);
                        startActivity(intent);
                        break;
                    }
                }



            } catch (Exception e) {
                //sendToast(e.toString());
                e.printStackTrace();
                Log.d(TAG, "Exception  " + e);
            }
        });
        t1.start();
        return root;
    }

    private void fileTransferClickListener(Context context) {
        binding.btnFileTransfer.setOnClickListener(v -> {

            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(context);
            boolean manualConnectionState = sharedPreferences.getBoolean("manual", false);

            String IP_Address;
            int Port_Num;
            String id;
            String password;

            if (manualConnectionState) {
                Log.d(TAG, "manualConnectionState: is On");
                IP_Address = sharedPreferences.getString("IP", "");
                Port_Num = Integer.parseInt(sharedPreferences.getString("port", ""));
                id = sharedPreferences.getString("ID", "");
                password = sharedPreferences.getString("password", "");
            } else {
                IP_Address = "192.168.10.99";
                Port_Num = 9999;
                id = binding.edtID.getText().toString().trim();
                password = "1";
            }
            //if ID is not empty then he will try to connect
            if (!id.equals("")) {
                Thread t1 = new Thread(() -> {
                    try {
                        if (SingletonSocket.getSocket() == null) {
                            Log.d(TAG, "fileTransferClickListener: Connecting at " + IP_Address + ":" + Port_Num);

                            Socket socket = new Socket(IP_Address, Port_Num);
                            SingletonSocket.setSocket(socket);
                            NewConnection = true;
                        }
                        if (sendIdPassword(id, password)) {

                            /*Intent intent = new Intent(getActivity(), WAN_Connection.class);
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

        });
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

            /*String data = bufferedReader.readLine();
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