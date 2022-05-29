package com.fyp.awacam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.fyp.awacam.databinding.ActivityWanConnectionBinding;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class WAN_Connection extends AppCompatActivity {

    private ActivityWanConnectionBinding binding;
    private static final String TAG = "Tag";

    static int PORT_NUM = 9999;
    static String SERVERS_IP_ADDRESS = "192.168.0.106";

    Socket socket;

    Context Application_Context;
    Context context;

    String ID;
    String Password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWanConnectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SERVERS_IP_ADDRESS = getIntent().getStringExtra("IP_ADDRESS");
        PORT_NUM = getIntent().getIntExtra("PORT_NUM", 9999);
        ID = getIntent().getStringExtra("ID");
        Password = getIntent().getStringExtra("PASSWORD");

        Application_Context = getApplicationContext();
        context = this;



        Log.d(TAG, "onCreate: Program started");
        Thread t1 = new Thread(() -> {
            try {
                socket = new Socket(SERVERS_IP_ADDRESS, PORT_NUM);

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                int data = Integer.parseInt(bufferedReader.readLine());
                Log.d(TAG, "Received port num is " + data);
                socket.close();
                Log.d(TAG, "Prev. Socket Closed");
                socket = new Socket(SERVERS_IP_ADDRESS, data);
                if (sendIdPassword(ID, Password)) {
                    Wan_Networking networking = new Wan_Networking(WAN_Connection.this, Application_Context, context, socket, binding.parentConstraint);
                    networking.start();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Exception in getDriveNames()>> " + e);
            }
        });
        t1.start();
    }

    private boolean sendIdPassword(String id, String password) {
        try{
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            printWriter.println("MOBILE");
            printWriter.flush();
            printWriter.println(id);
            printWriter.flush();
            printWriter.println(password);
            printWriter.flush();

            Log.d(TAG, "ID " + id + " and Password " + password + " send ");

            String data = bufferedReader.readLine();
            Log.d(TAG, "Received response  "+data);

            if (data.equals("ERROR")) {
                Log.d(TAG, "Wrong ID\\Password");
                sendToast("Wrong ID\\Password");
            } else if (data.equals("SUCCESS")) {
                Log.d(TAG, " ID\\Password matched successfully");
                return true;
            } else {
                Log.d(TAG, "Invalid response from server");
                sendToast("Invalid response from server");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d(TAG, "Exception  "+e);
        }

        return false;
    }

    private void sendToast(String message) {
        this.runOnUiThread(() -> {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        });
    }

}