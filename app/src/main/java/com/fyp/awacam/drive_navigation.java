package com.fyp.awacam;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.fyp.awacam.databinding.ActivityDriveNavigationBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class drive_navigation extends AppCompatActivity {

    private ActivityDriveNavigationBinding binding;
    static final String TAG = "tag";

    static int PORT_NUM = 9999;
    static String SERVERS_IP_ADDRESS = "192.168.0.106";

    static Handler handler;

    Socket socket;

    Context Application_Context;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDriveNavigationBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        SERVERS_IP_ADDRESS = getIntent().getStringExtra("IP_ADDRESS");
        PORT_NUM = getIntent().getIntExtra("PORT_NUM", 9999);

        Toast.makeText(this, "IP :" + SERVERS_IP_ADDRESS + "  Port : " + PORT_NUM, Toast.LENGTH_SHORT).show();

        Application_Context = getApplicationContext();
        context = this;

        Log.d(TAG, "onCreate: Program started");

        /*handler = new Handler(getApplicationContext().getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                View myLayout = getLayoutInflater().inflate(R.layout.drive_info, null, false);

                CardView.LayoutParams layoutParams = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(5, 10, 5, 10);
                myLayout.setLayoutParams(layoutParams);
                TextView name = myLayout.findViewById(R.id.tv_drive_name);
                TextView label = myLayout.findViewById(R.id.tv_drive_label);
                TextView type = myLayout.findViewById(R.id.tv_drive_type);
                TextView format = myLayout.findViewById(R.id.tv_drive_format);

                name.setText(msg.getData().getString("name"));
                if (msg.getData().getString("label").trim().equals("Null")) {
                    label.setText(null);
                } else {
                    label.setText(msg.getData().getString("label"));
                }
                type.append(msg.getData().getString("type"));
                format.append(msg.getData().getString("format"));
                binding.parentConstraint.addView(myLayout);
            }
        };*/

        Thread t1 = new Thread(() -> {
            try {
                socket = new Socket(SERVERS_IP_ADDRESS, PORT_NUM);

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                int data = Integer.parseInt(bufferedReader.readLine());
                Log.d(TAG, "Received port num is " + data);
                socket.close();
                Log.d(TAG, "Prev. Socket Closed");
                socket = new Socket(SERVERS_IP_ADDRESS, data);
                Networking networking = new Networking(drive_navigation.this, Application_Context, context, socket, binding.parentConstraint);
                networking.start();


            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Exception in getDriveNames()>> " + e);
            }
        });
        t1.start();

    }


    private void sendToast(String message) {
        this.runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        });
    }
}