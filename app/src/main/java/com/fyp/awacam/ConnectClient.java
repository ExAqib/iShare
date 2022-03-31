package com.fyp.awacam;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectClient extends Thread {
    public static final String TAG = "tag";
    Socket socket;
    int portNumber;
    Handler uiThreadHandler;

    private InputStream inputStream;
    private BufferedReader bufferedReader;

    ConnectClient(Socket socket, int Port, Handler handler) {
        this.socket = socket;
        this.portNumber = Port;
        this.uiThreadHandler = handler;
        Log.d(TAG, "Client Connected");
    }

    @Override
    public void run() {
        setDriveNames();
    }

    boolean sendTestMessage(String message) {
        try {
            if (message == "") {
                message = "This is testing message from server ";
            }

            Log.d(TAG, "Sending Message to Client i.e >>  " + message);
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            printWriter.println(message);
            printWriter.flush();

            //Log.d(TAG,"printWriter closed");
            //printWriter.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Exception in sendTestMessage() " + e);
            return false;
        }
    }

    private void setDriveNames() {
        try {
            Log.d(TAG, "Receiving Data from client");
            inputStream = socket.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String data;

            Message message = Message.obtain();
            Bundle bundle = new Bundle();

            int loop = 0;
            while ((data = bufferedReader.readLine()) != null) {

                Log.d(TAG, "Data received is " + data);

                switch (loop) {
                    case 0:
                        bundle.putString("name", data);
                        break;
                    case 1:
                        bundle.putString("label", data);
                        break;
                    case 2:
                        bundle.putString("type", data);
                        break;
                    case 3:
                        bundle.putString("format", data);
                        message.setData(bundle);
                        uiThreadHandler.sendMessage(message);
                        bundle = new Bundle();
                        message = Message.obtain();
                        loop = -1;
                        break;
                }
                loop++;
            }
            Log.d(TAG, "Socket Data Ended");

            //socket.close();
            //Log.d(TAG, "Socket Closed");

//            byte[] buffer = new byte[1024];
//            int read;
//            while ((read = is.read(buffer)) != -1) {
//                String output = new String(buffer, 0, read);
//                Log.d(TAG, "Client's Message is " + output);
//            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Exception in getDriveNames()>> " + e);
        }

    }

}
