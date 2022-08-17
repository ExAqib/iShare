package com.fyp.iShare.ui.home;

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

import com.fyp.iShare.Internal_Storage;
import com.fyp.iShare.LoginActivity;
import com.fyp.iShare.SettingsActivity;
import com.fyp.iShare.SingletonSocket;
import com.fyp.iShare.WAN_Connection;
import com.fyp.iShare.databinding.FragmentHomeBinding;

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
    String[] Permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    int RequestCode = 1122;
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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if (!permissionsGranted()) {
            grantPermissions();
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
                        if (socket == null) {
                            Log.d(TAG, "fileTransferClickListener: Connecting at "+IP_Address+":"+Port_Num);
                            socket = new Socket(IP_Address, Port_Num);
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
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

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
            Log.d(TAG, "ID " + id +" send ");

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

    private void grantPermissions() {
        Log.d(TAG, "Granting Permission");
        ActivityCompat.requestPermissions(requireActivity(), Permissions, RequestCode);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Log.d(TAG, "Opening settings for permission ");

                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",requireActivity().getApplicationContext().getPackageName())));
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