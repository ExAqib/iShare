package com.fyp.awacam;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fyp.awacam.databinding.ActivityDriveNavigationBinding;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.Arrays;

public class drive_navigation extends AppCompatActivity {

    private ActivityDriveNavigationBinding binding;
    static final String TAG = "tag";

    static final int PORT_NUM = 9999;
    static final String IP_ADDRESS = "192.168.0.113";

    Socket socket;
    static Handler handler;
    static int ID = 0;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDriveNavigationBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        progressBar = new ProgressBar(this);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(false);
        progressBar.setProgress(20);


        Log.d(TAG, "onCreate: Program started");

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
                connect.execute();

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

            getDirectory(strings[0]);

            return null;
        }

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
                    //Log.d(TAG, "SubDirectory is " + data);
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
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            Log.d(TAG, "SubDirectoryProcessor -> onPostExecute");

        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            if (values[0].equals("File")) {
                sendToast("File");
                runOnUiThread(() -> {

                    AlertDialog.Builder confirmationMessage = new AlertDialog.Builder(drive_navigation.this);
                    confirmationMessage.setTitle("Download File");
                    confirmationMessage.setMessage("Do you want to download?");
                    confirmationMessage.setPositiveButton("Yes", (dialog, which) -> {
                        ReceiveFile receiveFile = new ReceiveFile(path);
                        receiveFile.execute();

                    });
                    confirmationMessage.setNegativeButton("No", (dialog, which) -> {
                    });

                    confirmationMessage.setCancelable(false);
                    confirmationMessage.show();
                });

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
                    // Log.d(TAG, "SubDirectory is " + data);
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
        String FilePath;
        ProgressDialog progressDialog;
        int fileSize = 0;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        public ReceiveFile(String filePath) {
            FilePath = filePath;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(drive_navigation.this);

            progressDialog.setTitle("Downloading");
            progressDialog.setMessage("Your File is being downloaded in background. You can use any other app.");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            /*progressDialog.setButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });*/
            progressDialog.setProgress(0);

            progressDialog.show();
        }


        @Override
        protected Void doInBackground(Void... voids) {
            sendRequest("downloadFile");
            sendRequest(FilePath);

            DataInputStream dIn;
            try {
                Log.d(TAG, "Receiving File");

                //To receive the file name
                InputStream inputStream = socket.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String fileName = bufferedReader.readLine();
                // Log.d(TAG, "Received File Name is "+fileName);

                //For buffer size
                String FileSize = bufferedReader.readLine();
                fileSize = Integer.parseInt(FileSize);
                progressDialog.setMax((int) fileSize / 1000000);

                //Log.d(TAG, "Received File size is "+(float)fileSize/100000 +"MB ("+fileSize+"byes)");

                //For getting File data from server
                dIn = new DataInputStream(socket.getInputStream());

                //For saving in File
                FileOutputStream fos = new FileOutputStream(getFilePath(fileName));

                int bytesRead = 0;
                int bufferSize = 2048;

                byte[] data = new byte[bufferSize];
                int size;

                int counter=0;

                while (bytesRead < fileSize) {
                    if(bufferSize>(fileSize-bytesRead))
                    {
                        Log.d(TAG, "Adjusting buffer size from " +bufferSize);
                        bufferSize=fileSize-bytesRead;
                        Log.d(TAG, "to " +bufferSize);
                    }
                    data=new byte[bufferSize];

                    while (dIn.available() <=0) {
                        if(counter==3)
                        {
                            Log.d(TAG, "Breaking if else ");
                            counter=0;
                            break;
                        }
                        Log.d(TAG, "Sleeping thread for 2 sec ");
                        Thread.sleep(2000);
                        counter++;
                    }

                    Log.d(TAG, "bytes read " + bytesRead + " fileSize" + fileSize);

                    size = dIn.read(data, 0, bufferSize);
                    Log.d(TAG, "Readed");

                    fos.write(data, 0, size);

                    bytesRead += size;
                    //Log.d(TAG, "Total data read "+bytesRead/1000000+" MB's ("+bytesRead+" bytes)");

                    publishProgress(String.valueOf(bytesRead));
                }


                Log.d(TAG, "Sleeping thread for 5 second");
                Thread.sleep(5000);
                while (dIn.available() > 0) {
                    //bufferSize=dIn.available();
                    Log.d(TAG, "data is available");
                    size = dIn.read(data, 0, bufferSize);
                    fos.write(data, 0, size);
                }

                Log.d(TAG, "file Closed ");
                fos.flush();
                fos.close();
                Log.d(TAG, "doInBackground: File Received");

            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "doInBackground: Exception>> " + e);

            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            float bytesRead = Float.parseFloat(values[0]) / 1000000;

            try {
                progressDialog.incrementProgressBy((int) bytesRead / (fileSize / 1000000));
                progressDialog.setProgress((int) bytesRead);


            } catch (Exception e) {
                progressDialog.incrementProgressBy(progressDialog.getMax());
                progressDialog.setProgress(progressDialog.getMax());

                //Log.d(TAG, "Exception >> onProgressUpdate: Receive file : Download Dialog " + e);
            }
           // Log.d(TAG, decimalFormat.format(bytesRead) + "/" + fileSize / 1000000 + "MB Completed");

        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            progressDialog.dismiss();
        }


        private String getFilePath(String Name) {
            // Log.d(TAG, "Creating File ");

            ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
            File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
            File file = new File(musicDirectory, Name);
            if (file.exists()) {
                Log.d(TAG, "File Already exists:");
            } else {
                Log.d(TAG, "New file created ");
            }
            String path = file.getPath();
            Log.d(TAG, "File path is:" + path);

            return path;
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

    }

}