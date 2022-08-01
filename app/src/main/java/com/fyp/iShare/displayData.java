package com.fyp.iShare;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class displayData extends Fragment {

    PrintWriter printWriter;
    InputStream inputStream;
    BufferedReader bufferedReader;
    LinearLayout linearLayout;

    private static final String TAG = "tag";

    Socket socket;

    public displayData() {
        // Required empty public constructor
    }

    public static displayData newInstance(String param1) {
        return new displayData();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called for fragment" );
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart called for fragment" );

        super.onStart();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_display_data, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated called for fragment" );

        linearLayout = getView().findViewById(R.id.fragmentLinear);
        Log.d(TAG, "calling start " );

        start();
    }

    void start() {
        try {
            socket=SingletonSocket.getSocket();
            printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            inputStream = socket.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            Connect connect = new Connect();
            connect.execute();

        } catch (IOException e) {
            Log.d(TAG, "start: Exception" + e);
            e.printStackTrace();
        }
    }

    class Connect extends AsyncTask<Void, Bundle, Bundle> {

        @Override
        protected Bundle doInBackground(Void... voids) {
            setDriveNames();
            return null;
        }

        @Override
        protected void onProgressUpdate(Bundle... values) {
            super.onProgressUpdate(values);

            View myLayout = getLayoutInflater().inflate(R.layout.drive_info, null, false);

            CardView.LayoutParams layoutParams = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(5, 10, 5, 10);
            myLayout.setLayoutParams(layoutParams);

            if (values[0].getString("name").trim().equals("Exception")) {

                View exceptionLayout = getLayoutInflater().inflate(R.layout.exception_file, null, false);
                myLayout.setLayoutParams(layoutParams);
                TextView message = exceptionLayout.findViewById(R.id.exception_message);
                message.setText(values[0].getString("Exception").trim());

                linearLayout.addView(exceptionLayout);
                return;
            }

            TextView name = myLayout.findViewById(R.id.tv_drive_name);
            TextView label = myLayout.findViewById(R.id.tv_drive_label);
            TextView type = myLayout.findViewById(R.id.tv_drive_type);
            TextView format = myLayout.findViewById(R.id.tv_drive_format);

            name.setText(values[0].getString("name"));

            if (values[0].getString("label").trim().equals("Null")) {
                label.setText(null);
            } else {
                label.setText(values[0].getString("label"));
            }
            type.append(values[0].getString("type"));
            format.append(values[0].getString("format"));


            linearLayout.addView(myLayout);

            myLayout.setOnClickListener(v -> {

                SingletonSocket.setNavigationPath();
                Log.d(TAG, "going to next fragment");
                Parameters parameters=new Parameters(v,values[0].getString("name"),bufferedReader,printWriter);

               SingletonSocket.getFragmentManger().beginTransaction()
                        .replace(((ViewGroup)getView().getParent()).getId(), directories.newInstance(null,null,parameters), "findThisFragment")
                        .addToBackStack(null)
                        .commit();


            });
        }

        private void setDriveNames() {
            try {
                sendRequest();
                Log.d(TAG, "Receiving Data from client");

                String data;
                Bundle bundle = new Bundle();
                int loop = 0;

                while (!(data = bufferedReader.readLine()).equals("EndOfStream")) {

                    Log.d(TAG, "Data received is " + data);
                    if (data.equals("Exception")) {
                        data = bufferedReader.readLine();
                        Log.d(TAG, "setDriveNames: Exception>> " + data);
                        bundle.putString("name", "Exception");
                        bundle.putString("Exception", data);
                        publishProgress(bundle);
                        bundle = new Bundle();
                        loop = 0;
                        continue;
                    }

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
                            publishProgress(bundle);
                            bundle = new Bundle();
                            loop = -1;
                            break;
                    }
                    loop++;
                }
                Log.d(TAG, "Socket Data Ended");

                //socket.close();
                //Log.d(TAG, "Socket Closed");
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Exception in getDriveNames()>> " + e);
            }

        }

        void sendRequest() {
            try {

                Log.d(TAG, "Sending Request to Server i.e >>  " + "driveNames");
                printWriter.println("driveNames");
                printWriter.flush();

            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Exception in sendRequest() " + e);
            }
        }

    }

}
