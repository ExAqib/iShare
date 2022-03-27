package com.fyp.awacam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fyp.awacam.databinding.ActivityDriveNavigationBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class drive_navigation extends AppCompatActivity {

    private ActivityDriveNavigationBinding binding;
    static final String TAG = "tag";

    int l=1;

    static final int PORT_NUM = 9999;
    static final String IP_ADDRESS = "192.168.0.113";
    Socket socket;

    static Handler handler;

    static int ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriveNavigationBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        /*handler = new Handler(getApplicationContext().getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                View myLayout = getLayoutInflater().inflate(R.layout.drive_info, null, false);

                CardView.LayoutParams layoutParams = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(5, 10, 5, 10);
                myLayout.setLayoutParams(layoutParams);
                TextView name = myLayout.findViewById(R.id.tv_drive_name);
                TextView label = myLayout.findViewById(R.id.tv_drive_label);
                TextView type = myLayout.findViewById(R.id.tv_drive_type);
                TextView format = myLayout.findViewById(R.id.tv_drive_format);

                name.setText(msg.getData().getString("name"));
                if (msg.getData().getString("label").trim().equals("Null")) {
                    label.setText(null);
                } else {
                    label.setText(msg.getData().getString("label"));
                }
                type.append(msg.getData().getString("type"));
                format.append(msg.getData().getString("format"));
                binding.parentConstraint.addView(myLayout);
            }
        };*/

        Thread t1 = new Thread(() -> {
            try {
                socket = new Socket(IP_ADDRESS, PORT_NUM);

                Connect connect = new Connect();
                connect.execute(socket);

//                    ConnectClient client = new ConnectClient(socket, PORT_NUM, handler);
//                    client.start();

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Exception in While(true)" + e);
            }
        });
        t1.start();
    }

    class Connect extends AsyncTask<Socket, Bundle, Bundle> {

        @Override
        protected Bundle doInBackground(Socket... sockets) {
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

            myLayout.setId(ID);
            ID++;
            binding.parentConstraint.addView(myLayout);

            myLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: ");
                    DirectoryProcessor directoryProcessor = new DirectoryProcessor(v);
                    directoryProcessor.execute(values[0].getString("name"));
                }

            });
        }

        @Override
        protected void onPostExecute(Bundle s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: ");
        }

        private void sendRequest(String request) {
            try {
                Log.d(TAG, "Sending request to Server i.e >>  " + request);
                PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                printWriter.println(request);
                printWriter.flush();

                //Log.d(TAG,"printWriter closed");
                //printWriter.close();

            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Exception in sendRequest() " + e);
            }
        }

        private void setDriveNames() {
            try {
                sendRequest("driveNames");
                Log.d(TAG, "Receiving Data from client");
                InputStream inputStream = socket.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String data;
                Bundle bundle = new Bundle();

                int loop = 0;
                while (!(data = bufferedReader.readLine()).equals("EndOfStream")) {

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

    }

    class DirectoryProcessor extends AsyncTask<String, Bundle, Bundle> {
        View v;

        public DirectoryProcessor(View v) {
            this.v = v;
        }

        @Override
        protected Bundle doInBackground(String... strings) {

            Log.d(TAG, "doInBackground: ");
            getDirectories(strings[0]);

            return null;
        }

        @Override
        protected void onProgressUpdate(Bundle... values) {
            super.onProgressUpdate(values);

            if (l==1)
            {
                l++;
                return;
            }
            View myLayout = getLayoutInflater().inflate(R.layout.directory, null, false);

            CardView.LayoutParams layoutParams = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(5, 10, 5, 10);
            myLayout.setLayoutParams(layoutParams);

            TextView directoryName = myLayout.findViewById(R.id.directory_name);
            directoryName.setText(values[0].getString("name"));

            myLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SubDirectoryProcessor subDirectoryProcessor=new SubDirectoryProcessor(v,directoryName.getText().toString());
                    subDirectoryProcessor.execute(socket);
                }
            });

            runOnUiThread(() -> {
                LinearLayout layout = v.findViewById(R.id.show_directory);
                layout.addView(myLayout);
            });

        }

        private void getDirectories(String drive) {
            try {
                sendRequest("driveDirectories");
                sendRequest(drive);

                Log.d(TAG, "Getting directories");
                InputStream inputStream = socket.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String data;

                Bundle bundle = new Bundle();
                while (!(data = bufferedReader.readLine()).equals("EndOfStream")) {
                    Log.d(TAG, "Directory is " + data);
                    bundle.putString("name", data);
                    onProgressUpdate(bundle);
                    bundle = new Bundle();
                }
                Log.d(TAG, "Socket Data Ended");

                //socket.close();
                //Log.d(TAG, "Socket Closed");
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Exception in getDirectories()>> " + e);
            }

        }

        boolean sendRequest(String request) {
            try {

                Log.d(TAG, "Sending Request to Server i.e >>  " + request);
                PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                printWriter.println(request);
                printWriter.flush();

                //Log.d(TAG,"printWriter closed");
                //printWriter.close();

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Exception in sendRequest() " + e);
                return false;
            }
        }

    }

    class SubDirectoryProcessor extends AsyncTask<Socket, String, Void> {
        View v;
        String path;

        public SubDirectoryProcessor(View v,String path) {
            this.v = v;
            this.path=path;
        }

        @Override
        protected Void doInBackground(Socket... sockets) {
            getDirectory(path);

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            if(values[0].equals("InaccessibleFile"))
            {
                sendToast("File Inaccessible");
                Log.d(TAG, "File Inaccessible" );
            }
            else if (values[0].equals("UnAuthorizedAccess"))
            {
                sendToast("UnAuthorizedAccess");
                Log.d(TAG, "Access Denied" );
            }
            else
            {
                View myLayout = getLayoutInflater().inflate(R.layout.directory, null, false);

                CardView.LayoutParams layoutParams = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(5, 10, 5, 10);
                myLayout.setLayoutParams(layoutParams);

                TextView directoryName = myLayout.findViewById(R.id.directory_name);
                directoryName.setText(values[0]);

                myLayout.setOnClickListener((View v)->{
                    SubDirectoryProcessor subDirectoryProcessor=new SubDirectoryProcessor(v,values[0]);
                    subDirectoryProcessor.execute(socket);
                });

                runOnUiThread(() -> {
                    LinearLayout layout = v.findViewById(R.id.show_sub_directory);
                    layout.addView(myLayout);
                });

            }
        }
        boolean sendRequest(String request) {
            try {

                Log.d(TAG, "Sending Request to Server i.e >>  " + request);
                PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                printWriter.println(request);
                printWriter.flush();

                //Log.d(TAG,"printWriter closed");
                //printWriter.close();

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Exception in sendRequest() " + e);
                return false;
            }
        }
        private void getDirectory(String path)
        {
            sendRequest("subDirectories");
            sendRequest(path);

            try {
                InputStream inputStream = socket.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String data;

                while (!(data = bufferedReader.readLine()).equals("EndOfStream")) {
                    Log.d(TAG, "SubDirectory is " + data);
                    onProgressUpdate(data);
                }
                Log.d(TAG, "Socket data ended " + data);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        private void sendToast(String message){
            runOnUiThread(() -> {
                Toast.makeText(drive_navigation.this, message, Toast.LENGTH_SHORT).show();
            });
        }


    }
}