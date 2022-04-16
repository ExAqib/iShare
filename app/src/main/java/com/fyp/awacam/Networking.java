package com.fyp.awacam;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DecimalFormat;

public class Networking {

    private final Context context;
    final String TAG = "tag";
    LayoutInflater layoutInflater;
    LinearLayout linearLayout;
    Socket socket;
    Activity driveNavigationActivity;
    Context APPLICATION_CONTEXT;

    public Networking(Activity activity, Context APPLICATION_CONTEXT, Context context, Socket s, LinearLayout linearLayout) {
        this.context = context;
        this.socket = s;
        this.driveNavigationActivity = activity;
        this.APPLICATION_CONTEXT = APPLICATION_CONTEXT;
        this.linearLayout = linearLayout;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    void start() {
        Connect connect = new Connect();
        connect.execute();
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

            // View myLayout = getLayoutInflater().inflate(R.layout.drive_info, null, false);
            View myLayout = layoutInflater.inflate(R.layout.drive_info, null, false);

            CardView.LayoutParams layoutParams = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(5, 10, 5, 10);
            myLayout.setLayoutParams(layoutParams);

            if (values[0].getString("name").trim().equals("Exception")) {

                View exceptionLayout = layoutInflater.inflate(R.layout.exception_file, null, false);
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

            myLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: ");
                    DirectoryProcessor directoryProcessor = new DirectoryProcessor();
                    directoryProcessor.execute(new Parameter(v, values[0].getString("name")));
                }

            });
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

    class DirectoryProcessor extends AsyncTask<Object, String, Bundle> {
        View view;
        Parameter obj;

        @Override
        protected Bundle doInBackground(Object... objects) {

            obj = (Parameter) objects[0];
            view = obj.getView();
            getDirectory(obj.getPath());

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
                View myLayout = layoutInflater.inflate(R.layout.directory, null, false);

                CardView.LayoutParams layoutParams = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(5, 10, 5, 10);
                myLayout.setLayoutParams(layoutParams);

                TextView directoryName = myLayout.findViewById(R.id.directory_name);
                directoryName.setText(values[0]);

                myLayout.setOnClickListener((View v) -> {
                    SubDirectoryProcessor subDirectoryProcessor = new SubDirectoryProcessor();
                    subDirectoryProcessor.execute(new Parameter(v, values[0]));
                });

                driveNavigationActivity.runOnUiThread(() -> {
                    LinearLayout layout = view.findViewById(R.id.show_directory);
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
    }

    class SubDirectoryProcessor extends AsyncTask<Object, String, Void> {
        View v;
        String path;
        Parameter parameter;

        @Override
        protected Void doInBackground(Object... objects) {
            parameter = (Parameter) objects[0];
            v = parameter.getView();
            path = parameter.getPath();
            getDirectory(parameter.getPath());
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
                driveNavigationActivity.runOnUiThread(() -> {

                    AlertDialog.Builder confirmationMessage = new AlertDialog.Builder(context);
                    confirmationMessage.setTitle("Download File");
                    confirmationMessage.setMessage("Do you want to download?");
                    confirmationMessage.setPositiveButton("Yes", (dialog, which) -> {
                        ReceiveFile receiveFile = new ReceiveFile();
                        receiveFile.execute(path);

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
                View myLayout = layoutInflater.inflate(R.layout.directory, null, false);

                CardView.LayoutParams layoutParams = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(5, 10, 5, 10);
                myLayout.setLayoutParams(layoutParams);

                TextView directoryName = myLayout.findViewById(R.id.directory_name);
                directoryName.setText(values[0]);

                myLayout.setOnClickListener((View v) -> {
                    SubDirectoryProcessor subDirectoryProcessor = new SubDirectoryProcessor();
                    subDirectoryProcessor.execute(new Parameter(v, values[0]));
                });

                driveNavigationActivity.runOnUiThread(() -> {
                    LinearLayout layout = v.findViewById(R.id.show_sub_directory);
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
                    // Log.d(TAG, "SubDirectory is " + data);
                    onProgressUpdate(data);
                }
                Log.d(TAG, "Socket data ended " + data);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    class ReceiveFile extends AsyncTask<String, String, Void> {
        String FilePath;
        ProgressDialog progressDialog;
        int fileSize = 0;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);

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
        protected Void doInBackground(String... strings) {
            sendRequest("downloadFile");
            FilePath = strings[0];
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

                int counter = 0;

                while (bytesRead < fileSize) {
                    if (bufferSize > (fileSize - bytesRead)) {
                        Log.d(TAG, "Adjusting buffer size from " + bufferSize);
                        bufferSize = fileSize - bytesRead;
                        Log.d(TAG, "to " + bufferSize);
                    }
                    data = new byte[bufferSize];

                    while (dIn.available() <= 0) {
                        if (counter == 3) {
                            Log.d(TAG, "Breaking if else ");
                            counter = 0;
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

                int availableBytes = dIn.available();
                while (availableBytes > 0) {
                    Log.d(TAG, "data is available");
                    size = dIn.read(data, 0, availableBytes);
                    fos.write(data, 0, size);
                    availableBytes = dIn.available();
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

            ContextWrapper contextWrapper = new ContextWrapper(APPLICATION_CONTEXT);
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
    }

    static class Parameter {
        View view;
        String path;

        public Parameter(View v, String path) {
            this.view = v;
            this.path = path;
        }

        public View getView() {
            return view;
        }

        public String getPath() {
            return path;
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
        driveNavigationActivity.runOnUiThread(() -> {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        });
    }

}
