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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Connect extends AppCompatActivity {

    private ActivityConnectBinding binding;
    static final String TAG = "tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConnectBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        binding.btnConnect.setOnClickListener(v -> {
            String IP_Address = binding.ipAddress.getText().toString();
            Log.d(TAG, "IP : " + IP_Address);
            int Port_Num = Integer.parseInt(binding.portNumber.getText().toString());
            Log.d(TAG, "PORt " + Port_Num);

            String id = binding.ID.getText().toString();
            Log.d(TAG, "ID : " + id);
            String password = binding.PASSWORD.getText().toString();
            Log.d(TAG, "password : " + password);


            Intent intent = new Intent(Connect.this, WAN_Connection.class);
            intent.putExtra("IP_ADDRESS", IP_Address);
            intent.putExtra("PORT_NUM", Port_Num);
            intent.putExtra("ID", id);
            intent.putExtra("PASSWORD", password);
            startActivity(intent);

        });

        binding.btnServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createServerSocket(9999);
            }
        });
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