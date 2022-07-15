package com.fyp.awacam;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class SingletonSocket {

    private static final String TAG = "tag";
    private static Socket socket;
    private static PrintWriter printWriter;


    private SingletonSocket(){};

    public static void setSocket(Socket socket)  {
        SingletonSocket.socket=socket;
    }

    public static void sendRequest(String request) {
        try {
            Log.d(TAG, "Sending Request to Server i.e >>  " + request);
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            printWriter.println(request);
            printWriter.flush();

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Exception in sendRequest() " + e);
        }
    }

    public static Socket getSocket(){
        return socket;
   }

}
