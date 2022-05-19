package com.fyp.awacam;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
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
import java.io.IOException;
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

    PrintWriter printWriter;
    BufferedReader bufferedReader;
    InputStream inputStream;

    public Networking(Activity activity, Context APPLICATION_CONTEXT, Context context, Socket s, LinearLayout linearLayout) {
        this.context = context;
        this.socket = s;
        this.driveNavigationActivity = activity;
        this.APPLICATION_CONTEXT = APPLICATION_CONTEXT;
        this.linearLayout = linearLayout;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    void start() {
        try {
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

            myLayout.setOnClickListener(v -> {
                Log.d(TAG, "onClick: ");
                DirectoryProcessor directoryProcessor = new DirectoryProcessor();
                directoryProcessor.execute(new Parameter(v, values[0].getString("name")));
            });
        }

        private void setDriveNames() {
            try {
                sendRequest("driveNames");
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
            }
            else if (values[0].equals("InaccessibleFile")) {
                sendToast("File Inaccessible");
                Log.d(TAG, "File Inaccessible ");
            }
            else if (values[0].equals("UnAuthorizedAccess")) {
                sendToast("Access Denied");
                Log.d(TAG, "Access Denied");
            }
            else {
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

    }

    class ReceiveFile extends AsyncTask<String, String, Void> {
        String FilePath;
        ProgressDialog progressDialog;
        int fileSize = 0;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        boolean CancelDownload = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);

            progressDialog.setTitle("Downloading");
            progressDialog.setMessage("Your File is being downloaded in background. You can use any other app.");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Cancel", (dialog, which) -> {
                Log.d(TAG, "Download Cancelled");
                CancelDownload = true;
            });
            progressDialog.setProgress(0);

            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... strings) {
            sendRequest("downloadFile");
            FilePath = strings[0];
            sendRequest(FilePath);

            try {
                Log.d(TAG, "Receiving File");

                //To receive the file name

                String fileName = bufferedReader.readLine();
                // Log.d(TAG, "Received File Name is "+fileName);

                //For File size
                String FileSize = bufferedReader.readLine();
                fileSize = Integer.parseInt(FileSize);
                progressDialog.setMax(fileSize / 1000000);

                startDownloading(fileName);


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
            Log.d(TAG, "onPostExecute: called for receive file");
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

        private void startDownloading(String fileName) {
            try {
                Log.d(TAG, "Received File size is " + (float) fileSize / 100000 + "MB (" + fileSize + "byes)");

                DataInputStream dIn = new DataInputStream(inputStream);
                FileOutputStream fos = new FileOutputStream(getFilePath(fileName));

                byte[] data;
                int bufferSize = 2048*4;
                int totalBytesRead = 0;
                int bytesReadPerCycle;
                boolean killDownloadingThread = false;

                while (totalBytesRead < fileSize && !CancelDownload) {

                    //checking if the buffer size exceeds, the size of file or remaining data of file
                    if (bufferSize > (fileSize - totalBytesRead)) {
                        Log.d(TAG, "Adjusting buffer size from " + bufferSize);
                        bufferSize = fileSize - totalBytesRead;
                        Log.d(TAG, "to " + bufferSize);
                    }
                    data = new byte[bufferSize];

                    if (!dataAvailable(dIn)) {
                        killDownloadingThread = true;
                        sendToast("Unknown Error");
                        break;
                    }
                    else {
                        Log.d(TAG, "Trying to Read");
                        bytesReadPerCycle = dIn.read(data, 0, bufferSize);
                        Log.d(TAG, "Read " + totalBytesRead + " of " + fileSize + " bytes");

                        fos.write(data, 0, bytesReadPerCycle);
                        totalBytesRead += bytesReadPerCycle;
                        Log.d(TAG, "Total data read "+totalBytesRead/1000000+" MB's ("+totalBytesRead+" bytes)");

                        publishProgress(String.valueOf(totalBytesRead));
                    }

                }

                if (totalBytesRead == fileSize) {
                    Log.d(TAG, " Download Completed Successfully ");
                    sendRequest("DONE");
                }
                else if (CancelDownload) {
                    Log.d(TAG, " Download cancelled ");

                    sendRequest("CANCEL");
//                    progressDialog.setTitle("Canceling Download");
//                    progressDialog.setMessage("Please Wait...");

                    //Getting some data that has arrived after cancelling.

                    Log.d(TAG, "Sleeping thread for 5 second");
                    Thread.sleep(5000);

                    String cancelledData=bufferedReader.readLine();
                    Log.d(TAG, "Cancelled DAta is "+cancelledData);

                    fos.write(cancelledData.getBytes());
                    fos.flush();

                    while(!cancelledData.equals("DATA_ENDED"))
                    {
                        Log.d(TAG, "Cancelled Data is ");
                        cancelledData=bufferedReader.readLine();

                        fos.write(cancelledData.getBytes());
                        fos.flush();

                        Log.d(TAG, cancelledData);
                    }

                    Log.d(TAG, "Last  Data after Cancellation is  "+cancelledData);

                    int availableBytes = dIn.available();
                    Log.d(TAG, "startDownloading: Checking for Available data after cancel operation called "+ availableBytes);

                    while (availableBytes > 0) {
                        Log.d(TAG, "startDownloading: Received "+ availableBytes+" bytes");

                        data = new byte[availableBytes];
                        bytesReadPerCycle = dIn.read(data, 0, availableBytes);
                        fos.write(data, 0, bytesReadPerCycle);
                        availableBytes = dIn.available();
                    }
                    Log.d(TAG, "startDownloading: Socket data Ended");
                    sendToast("Cancelled");
                }
                else if (killDownloadingThread) {
                    sendRequest("KillThread");
                    Log.d(TAG, "Sleeping thread for 2 second");
                    Thread.sleep(2000);

                    String cancelledData=bufferedReader.readLine();
                    Log.d(TAG, "Cancelled DAta is "+cancelledData);

                    fos.write(cancelledData.getBytes());
                    fos.flush();

                    while(!cancelledData.equals("DATA_ENDED"))
                    {
                        Log.d(TAG, "Cancelled Data is ");
                        cancelledData=bufferedReader.readLine();


                        fos.write(cancelledData.getBytes());
                        fos.flush();
                        Log.d(TAG, cancelledData);
                    }

                    Log.d(TAG, "Last  Data after Cancellation is  "+cancelledData);

                    Log.d(TAG, "Checking for any available data");
                    int availableBytes = dIn.available();
                    while (availableBytes > 0) {
                        Log.d(TAG, "Found "+availableBytes+" bytes");

                        data = new byte[availableBytes];
                        bytesReadPerCycle = dIn.read(data, 0, availableBytes);
                        fos.write(data, 0, bytesReadPerCycle);
                        availableBytes = dIn.available();
                    }
                    Log.d(TAG, "Data Ended");
                }

                Log.d(TAG, "doInBackground for Receive File is returning  ");

            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, " Exception>> " + e);
            }
        }

        private boolean dataAvailable(DataInputStream dIn) {
            int sleepThread = 0;
            try {
                while (dIn.available() <= 0) {

                    //If the data has not Arrived yet, wait for it by sleeping the thread

                    Log.d(TAG, "Sleeping thread for 3 sec ");
                    Thread.sleep(3000);
                    sleepThread++;

                    if (sleepThread == 3) {
                        // Data has not arrived even after waiting
                        Log.d(TAG, " Unknown Error (killing Downloading Thread)");
                        return false;
                    }
                }

            } catch (IOException | InterruptedException e) {
                Log.d(TAG, "dataAvailable: Exception" + e);
                e.printStackTrace();
                return false;
            }
            return true;
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
//           PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
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
