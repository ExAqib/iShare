package com.HuimangTech.iShare;

import android.util.Log;

import androidx.fragment.app.FragmentManager;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SingletonSocket {

    private static final String TAG = "tag";
    private static Socket socket = null;
    private static List<String> NavigationPath;
    private static FragmentManager fragmentManger;

    private SingletonSocket() {
    }

    public static List<String> getNavigationPath() {
        return NavigationPath;
    }

    public static void setNavigationPath() {
        NavigationPath = new ArrayList<>();
    }

    public static FragmentManager getFragmentManger() {
        return fragmentManger;
    }

    public static void setFragmentManger(FragmentManager fragmentManger) {
        SingletonSocket.fragmentManger = fragmentManger;
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

    public static Socket getSocket() {
        return socket;
    }

    public static void setSocket(Socket socket) {
        SingletonSocket.socket = socket;
    }

    public static void CloseSocket() {
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                Log.d(TAG, "CloseSocket Exception: " + e);
            } finally {
                socket = null;
                Log.d(TAG, "CloseSocket: Done");

            }
        }

    }

}
