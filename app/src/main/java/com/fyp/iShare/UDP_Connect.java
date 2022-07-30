package com.fyp.iShare;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Enumeration;

public class UDP_Connect {

    int SOCKET_TIME_OUT=3000;
    private static final String TAG = "tag";

    String ServerResponseMessage = "iShare_Server_REQUEST_RESPONSE";
    byte[] sendData = "DISCOVER_iShare_Server_REQUEST".getBytes();
    ArrayList<String> listeners;

    UDP_Connect() {

    }

    ArrayList<String> FindServer(String ip){
        try {
            //testUdpBroadCast(ip);

            listeners = new ArrayList<>();
            Log.d(TAG, "UDP Connect Called");
            int SERVER_PORT = 9999;
            String IP = "255.255.255.255"; //255.255.2552.255

            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            socket.setSoTimeout(SOCKET_TIME_OUT);

            //Try the 255.255.255.255 first
            try {
                Log.d(TAG, " Sending Packet at  " + IP + " for port " + SERVER_PORT);
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(IP), SERVER_PORT);
                socket.send(sendPacket);
                Log.d(TAG,   "Request packet sent to: " + IP);

            } catch (Exception e) {
                Log.d(TAG, "Exception " + e);
            }

            // Broadcast the message over all the network interfaces
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue; // Don't want to broadcast to the loopback interface
                }

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();

                    if (broadcast == null) {
                        continue;
                    }

                    // Send the broadcast package!
                    try {
                        Log.d(TAG, " Sending Packet to  " + interfaceAddress.getBroadcast() + " at port " + SERVER_PORT);

                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, SERVER_PORT);
                        socket.send(sendPacket);
                    } catch (Exception e) {
                        Log.d(TAG, "Exception " + e);

                    }
                }

            }

            Log.d(TAG, "BroadCast send! Now waiting for a reply!");
            Response(socket);
            socket.close();

            Log.d(TAG, "Found "+listeners.size()+" connections");
            return (listeners);


        } catch (Exception ex) {
            Log.d(TAG, "Exception " + ex);
            return (listeners);
        }
    }

    void Response(DatagramSocket socket) {
        //Wait for a response
        byte[] serverResponseMessageBytes = ServerResponseMessage.getBytes();
        DatagramPacket receivePacket = new DatagramPacket(serverResponseMessageBytes, serverResponseMessageBytes.length);

        while (true) {
            try {
                socket.receive(receivePacket);

                //We have a response
                String message = new String(receivePacket.getData()).trim();

                String serverIP = receivePacket.getAddress().getHostAddress();
                Log.d(TAG, "Received Response from server: " + serverIP +" i.e "+ message);

                //Check if the response is correct
                if (message.equals(ServerResponseMessage) && !listeners.contains(serverIP)) {

                    //DO SOMETHING WITH THE SERVER'S IP (for example, store it in your controller)
                    Log.d(TAG, "Found server " + message);
                    listeners.add(serverIP);
                }

            } catch (SocketTimeoutException e) {
                Log.d(TAG, "Timeout Exception Reached" + e);
                break;
            } catch (Exception e) {
                Log.d(TAG, "Exception" + e);
                break;
            }
        }

    }

    void testUdpBroadCast(String IP) throws Exception {

        if (IP == null) {
            IP = "192.168.0.255";
        }
        int PORT = 9999;
        DatagramSocket myUDPSocket = new DatagramSocket();
        //myUDPSocket.setSoTimeout(3000);
        myUDPSocket.setBroadcast(true);

        try {

            Log.d(TAG, " Sending Packet to  " + IP + " at port " + PORT);

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(IP), PORT);
            myUDPSocket.send(sendPacket);
            Log.d(TAG, "Request packet sent to: " + IP);

            myUDPSocket.close();
            Log.d(TAG, "Socket Closed " );


        } catch (Exception e) {
            Log.d(TAG, "Exception  in sendUdpBroadCast() fx. " + e);
        }

    }
}
