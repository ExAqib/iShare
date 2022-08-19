package com.HuimangTech.iShare;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.HuimangTech.iShare.databinding.ActivityConnectBinding;
import com.HuimangTech.iShare.ui.fileTransfer.WAN_Connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Connect extends AppCompatActivity {

    static final String TAG = "tag";
    boolean NewConnection = true;
    Socket socket = null;
    private ActivityConnectBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

        binding = ActivityConnectBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        binding.btnConnectOld.setOnClickListener(v -> {

            String IP_Address = binding.ipAddress.getText().toString().trim();
            int Port_Num = Integer.parseInt(binding.portNumber.getText().toString().trim());

            String id = binding.ID.getText().toString().trim();
            String password = binding.PASSWORD.getText().toString().trim();

            Thread t1 = new Thread(() -> {
                try {
                    if (socket == null) {
                        socket = new Socket(IP_Address, Port_Num);

                    }
                    if (sendIdPassword(id, password)) {
                        SingletonSocket.setSocket(socket);

                        goToNextActivity();

                    }
                } catch (Exception e) {
                    sendToast(e.toString());
                    e.printStackTrace();
                    Log.d(TAG, "Exception  " + e);
                }
            });
            t1.start();


        });

        binding.btnServer.setOnClickListener(v -> new Thread(() -> {

            new UDP_Connect().JoinMultiCastNetwork();
            //ArrayList<String> servers=new UDP_Connect().FindServer(binding.ipAddress.getText().toString().trim());
            //sendToast("Total connections "+servers.size() );

        }).start());

        binding.btnSwitchToLan.setOnClickListener(v -> {
            startActivity(new Intent(Connect.this, lan_connect.class));
        });
    }

    private void goToNextActivity() {
        this.runOnUiThread(() -> {
            startActivity(new Intent(Connect.this, WAN_Connection.class));
        });

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
                sendToast("PC not found.");
            } else if (data.equals("SUCCESS")) {
                Log.d(TAG, "PC found. ID\\Password matched successfully");
                return true;
            } else {
                Log.d(TAG, "Invalid response from server");
                sendToast("Invalid response from server");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Exception  " + e);
        }

        return false;
    }

    public void createServerSocket(int portNum) {
        Thread t1 = new Thread(() ->
        {
            try {
                ServerSocket serverSocket = new ServerSocket(0);
                sendToast("Waiting for client at " + serverSocket.getLocalPort());

                Log.d(TAG, "Server is waiting at port num: " + serverSocket.getLocalPort());
                Socket socket = serverSocket.accept();
                sendToast("Connected");


                Log.d(TAG, "Client Connected");
//                InputStreamReader in= new InputStreamReader(socket.getInputStream());
//                BufferedReader br= new BufferedReader(in);
//
//                String message = br.readLine();
//                Log.d(TAG,"Message received from server is "+message);
//                Toast.makeText(this, "Message received form server ", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                Log.d(TAG, "createServerSocket: " + e);
                e.printStackTrace();
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