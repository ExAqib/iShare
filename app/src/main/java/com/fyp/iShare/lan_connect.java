package com.fyp.iShare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.fyp.iShare.databinding.ActivityConnectBinding;
import com.fyp.iShare.databinding.ActivityLanConnectBinding;

import java.net.Socket;

public class lan_connect extends AppCompatActivity {

    ActivityLanConnectBinding binding;
    static final String TAG = "tag";
    Socket socket=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLanConnectBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.lanConnectButton.setOnClickListener(v -> {

            String IP=binding.lanIpAddressEdt.getText().toString().trim();
            String port= binding.lanPortNumEdt.getText().toString().trim();


            Thread t1 = new Thread(() -> {
                try {

                        socket = new Socket(IP,Integer.parseInt(port));

                        SingletonSocket.setSocket(socket);

                        goToNextActivity();


                } catch (Exception e) {
                    sendToast(e.toString());
                    e.printStackTrace();
                    Log.d(TAG, "Exception  " + e);
                }
            });
            t1.start();


        });
    }

    private void goToNextActivity() {
        this.runOnUiThread(() -> {
            startActivity(new Intent(lan_connect.this,WAN_Connection.class));
        });

    }
    private void sendToast(String message) {
        this.runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        });
    }

}