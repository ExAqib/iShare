package com.fyp.awacam;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class displayData extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "tag";


    // TODO: Rename and change types of parameters
    private String SERVERS_IP_ADDRESS;
    private int PORT_NUM;

    Socket socket;
    Handler mHandler;
    Context Application_Context;

    public displayData() {
        // Required empty public constructor
    }

    public displayData(String ip, int port) {

    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment displayData.
     */
    // TODO: Rename and change types and number of parameters
    public static displayData newInstance(String param1, int param2) {
        displayData fragment = new displayData();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putInt(ARG_PARAM2, param2);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            SERVERS_IP_ADDRESS = getArguments().getString(ARG_PARAM1);
            PORT_NUM = getArguments().getInt(ARG_PARAM2);
            Log.d(TAG, "IP and port in fragment is "+SERVERS_IP_ADDRESS+" port "+PORT_NUM);
        }

        Application_Context = getActivity().getApplicationContext();
        mHandler = new Handler(Looper.getMainLooper());

        LinearLayout linearLayout= getView().findViewById(R.id.fragmentLinear);

        Thread t1 = new Thread(() -> {
            try {
                socket = new Socket(SERVERS_IP_ADDRESS, PORT_NUM);

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                int data = Integer.parseInt(bufferedReader.readLine());
                Log.d(TAG, "Received port num is " + data);
                socket.close();
                Log.d(TAG, "Prev. Socket Closed");
                socket = new Socket(SERVERS_IP_ADDRESS, data);

                Context context=getContext();


                Networking networking = new Networking(getActivity(), Application_Context, context, socket, linearLayout);
                networking.start();


            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Exception in getDriveNames()>> " + e);
            }
        });
        t1.start();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_display_data, container, false);
    }
}