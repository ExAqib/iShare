package com.fyp.awacam;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.fyp.awacam.databinding.ActivityConnectBinding;
import com.fyp.awacam.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Connect extends AppCompatActivity {

    private ActivityConnectBinding binding;
    static final String TAG = "tag";
    boolean NewConnection=true;
    Socket socket=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

        binding = ActivityConnectBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        binding.btnConnect.setOnClickListener(v -> {

            String IP_Address = binding.ipAddress.getText().toString().trim();
            int Port_Num = Integer.parseInt(binding.portNumber.getText().toString().trim());

            String id = binding.ID.getText().toString().trim();
            String password = binding.PASSWORD.getText().toString().trim();

            Thread t1 = new Thread(() -> {
                try {
                    /*Wan_Networking networking = new Wan_Networking(WAN_Connection.this, Application_Context, context, socket, binding.parentConstraint);
                            networking.start();*/
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

        binding.btnServer.setOnClickListener(v -> createServerSocket(9999));
    }

    private void goToNextActivity() {
        this.runOnUiThread(() -> {
            startActivity(new Intent(Connect.this,WAN_Connection.class));
        });

    }

    private boolean sendIdPassword(String id, String password) {
        try{
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            if(NewConnection){
                printWriter.println("MOBILE");
                printWriter.flush();
                NewConnection=false;
            }
            printWriter.println(id);
            printWriter.flush();
            printWriter.println(password);
            printWriter.flush();

            Log.d(TAG, "ID " + id + " and Password " + password + " send ");

            String data = bufferedReader.readLine();
            Log.d(TAG, "Received response  "+data);

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
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d(TAG, "Exception  "+e);
        }

        return false;
    }
    public void createServerSocket(int portNum) {
        Thread t1 = new Thread(() ->
        {
            try {
                ServerSocket serverSocket = new ServerSocket(0);
                sendToast("Waiting for client at "+serverSocket.getLocalPort());

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
                Log.d(TAG, "createServerSocket: "+e.toString());
                e.printStackTrace();
            }

        });
        t1.start();
    }
    private void createTestFile()
    {
        String PdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File file = new File(PdfPath, "myResult.pdf");
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            String aqib="qeqweqwe";
            byte [] data= aqib.getBytes();
            outputStream.write(data);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "FileNotFoundException "+e.toString(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        catch (IOException e)
        {
            Toast.makeText(this, "IOException "+e.toString(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    private void sendToast(String message) {
        this.runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        });
    }

}