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
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class drive_navigation extends AppCompatActivity {

    private ActivityDriveNavigationBinding binding;
    static final String TAG = "tag";

    static final int PORT_NUM = 9999;
    static final String IP_ADDRESS = "192.168.0.113";
    //static final String IP_ADDRESS = "192.168.10.99";
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

                ReceiveFile receiveFile = new ReceiveFile();
                receiveFile.execute();

//                Connect connect = new Connect();
//                connect.execute();

//              ConnectClient client = new ConnectClient(socket, PORT_NUM, handler);
//              client.start();

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Exception in While(true)" + e);
            }
        });
        t1.start();
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

                binding.parentConstraint.addView(exceptionLayout);
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

            //Assigning ID just for testing. Will Remove at last
            myLayout.setId(ID);
            ID++;
            binding.parentConstraint.addView(myLayout);

            myLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: ");
                    DirectoryProcessor directoryProcessor = new DirectoryProcessor(v);
                    directoryProcessor.execute(values[0].getString("name"));
//                    SubDirectoryProcessor subDirectoryProcessor=new SubDirectoryProcessor(v,values[0].getString("name").trim());
//                    subDirectoryProcessor.execute(socket);
                }

            });
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

    }

    class DirectoryProcessor extends AsyncTask<String, String, Bundle> {
        View v;

        public DirectoryProcessor(View v) {
            this.v = v;
        }

        @Override
        protected Bundle doInBackground(String... strings) {

            Log.d(TAG, "doInBackground: ");
            //getDirectories(strings[0]);
            getDirectory(strings[0]);

            return null;
        }

       /* @Override
        protected void onProgressUpdate(Bundle... values) {

            // This ProgressUpdate was called by getDirectories method which provides him bundle
            //

            super.onProgressUpdate(values);
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
                    subDirectoryProcessor.execute();
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

        */

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            if (values[0].equals("InaccessibleFile")) {
                sendToast("File Inaccessible");
                Log.d(TAG, "File Inaccessible ");
            } else if (values[0].equals("UnAuthorizedAccess")) {
                sendToast("Access Denied");
                Log.d(TAG, "Access Denied");
            } else {
                View myLayout = getLayoutInflater().inflate(R.layout.directory, null, false);

                CardView.LayoutParams layoutParams = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(5, 10, 5, 10);
                myLayout.setLayoutParams(layoutParams);

                TextView directoryName = myLayout.findViewById(R.id.directory_name);
                directoryName.setText(values[0]);

                myLayout.setOnClickListener((View v) -> {
                    SubDirectoryProcessor subDirectoryProcessor = new SubDirectoryProcessor(v, values[0]);
                    subDirectoryProcessor.execute();
                });

                runOnUiThread(() -> {
                    LinearLayout layout = v.findViewById(R.id.show_directory);
                    layout.addView(myLayout);
                });

            }
        }

        private void getDirectory(String path) {
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

        void sendRequest(String request) {
            try {

                Log.d(TAG, "Sending Request to Server i.e >>  " + request);
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

        private void sendToast(String message) {
            runOnUiThread(() -> {
                Toast.makeText(drive_navigation.this, message, Toast.LENGTH_SHORT).show();
            });
        }
    }

    class SubDirectoryProcessor extends AsyncTask<Void, String, Void> {
        View v;
        String path;

        public SubDirectoryProcessor(View v, String path) {
            this.v = v;
            this.path = path;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getDirectory(path);
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            if (values[0].equals("File")) {
                sendToast("File");
            } else if (values[0].equals("InaccessibleFile")) {
                sendToast("File Inaccessible");
                Log.d(TAG, "File Inaccessible ");
            } else if (values[0].equals("UnAuthorizedAccess")) {
                sendToast("Access Denied");
                Log.d(TAG, "Access Denied");
            } else {
                View myLayout = getLayoutInflater().inflate(R.layout.directory, null, false);

                CardView.LayoutParams layoutParams = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(5, 10, 5, 10);
                myLayout.setLayoutParams(layoutParams);

                TextView directoryName = myLayout.findViewById(R.id.directory_name);
                directoryName.setText(values[0]);

                myLayout.setOnClickListener((View v) -> {
                    SubDirectoryProcessor subDirectoryProcessor = new SubDirectoryProcessor(v, values[0]);
                    subDirectoryProcessor.execute();
                });

                runOnUiThread(() -> {
                    LinearLayout layout = v.findViewById(R.id.show_sub_directory);
                    layout.addView(myLayout);
                });

            }
        }

        void sendRequest(String request) {
            try {

                Log.d(TAG, "Sending Request to Server i.e >>  " + request);
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

        private void getDirectory(String path) {
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

        private void sendToast(String message) {
            runOnUiThread(() -> {
                Toast.makeText(drive_navigation.this, message, Toast.LENGTH_SHORT).show();
            });
        }
    }

    class ReceiveFile extends AsyncTask<Void, String, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            DataInputStream dIn = null;
            try {
                dIn = new DataInputStream(socket.getInputStream());
                int length = dIn.readInt(); // read length of incoming message
                Log.d(TAG, "doInBackground: length is " + length);
                if (length > 0) {
                    byte[] message = new byte[length];
                    dIn.readFully(message, 0, message.length); // read the message
                    Log.d(TAG, "doInBackground: data is  " + Arrays.toString(message));

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

}