package com.fyp.awacam;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fyp.awacam.databinding.ActivityDriveNavigationBinding;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DecimalFormat;

public class drive_navigation extends AppCompatActivity {

    private ActivityDriveNavigationBinding binding;
    static final String TAG = "tag";

    static final int PORT_NUM = 9999;
    static final String IP_ADDRESS = "192.168.0.113";

    Socket socket;
    static Handler handler;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDriveNavigationBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        final Context Application_Context=getApplicationContext();
        final Context context=this;

        progressBar = new ProgressBar(this);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(false);
        progressBar.setProgress(20);


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
                socket = new Socket(IP_ADDRESS, PORT_NUM);
                Networking networking= new Networking(drive_navigation.this,Application_Context,context,socket,binding.parentConstraint);
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