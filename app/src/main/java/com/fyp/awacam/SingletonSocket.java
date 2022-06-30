package com.fyp.awacam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class SingletonSocket {

    private static Socket socket;
    private static BufferedReader bufferedReader;

    private SingletonSocket(){};

    public static void setSocket(Socket socket)  {
        SingletonSocket.socket=socket;

    }


    public static Socket getSocket(){
        return socket;
   }

}
