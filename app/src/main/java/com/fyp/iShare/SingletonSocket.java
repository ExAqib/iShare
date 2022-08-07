package com.fyp.iShare;

import android.util.Log;

import androidx.fragment.app.FragmentManager;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SingletonSocket {

    private static final String TAG = "tag";
    private static Socket socket;

    public static List<String> getNavigationPath() {
        return NavigationPath;
    }

    public static void setNavigationPath() {
        NavigationPath= new ArrayList<>();
    }

    private static List<String> NavigationPath ;

    public static FragmentManager getFragmentManger() {
        return fragmentManger;
    }

    public static void setFragmentManger(FragmentManager fragmentManger) {
        SingletonSocket.fragmentManger = fragmentManger;
    }

    private static  FragmentManager fragmentManger;

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
