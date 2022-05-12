package com.fyp.awacam;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.fyp.awacam.databinding.ActivityDriveNavigationBinding;

import java.io.IOException;
import java.net.Socket;

public class drive_navigation extends AppCompatActivity {

    private ActivityDriveNavigationBinding binding;
    static final String TAG = "tag";

    static final int PORT_NUM = 9999;
    static final String IP_ADDRESS = "192.168.10.99";

    Socket socket;
    static Handler handler;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDriveNavigationBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        final Context Application_Context = getApplicationContext();
        final Context context = this;

        progressBar = new ProgressBar(this);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(false);
        progressBar.setProgress(20);


        Log.d(TAG, "onCreate: Program started");


        Thread t1 = new Thread(() -> {
            try {
                socket = new Socket(IP_ADDRESS, PORT_NUM);
                Networking networking = new Networking(drive_navigation.this, Application_Context, context, socket, binding.parentConstraint);
                networking.start();
//              ConnectClient client = new ConnectClient(socket, PORT_NUM, handler);
//              client.start();

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Exception in drive_navigation  " + e);
            }
        });
        t1.start();
    }
}