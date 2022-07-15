package com.fyp.awacam;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;


public class directories extends Fragment {

    private static final String TAG = "tag";
    LinearLayout linearlayout;
    static Parameters parameters;
    static PrintWriter printWriter;
    static BufferedReader bufferedReader;

    ArrayList<Bundle> PcData = new ArrayList<>();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public directories() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment directories.
     */
    // TODO: Rename and change types and number of parameters
    public static directories newInstance(String param1, String param2, Parameters p) {
        directories fragment = new directories();
        parameters = p;
        printWriter = p.getPrintWriter();
        bufferedReader = p.getBufferedReader();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called for directory fragment");

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called for directory fragment");

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_directories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called for directory fragment");
/*
        linearlayout = getView().findViewById(R.id.show_directory_linear_layout);

        DirectoryProcessor directoryProcessor = new DirectoryProcessor();
       // directoryProcessor.execute(new Parameter(v, values[0].getString("name")));
        directoryProcessor.execute(parameters);*/

    }

    @Override
    public void onStart() {
        super.onStart();

        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                if(PcData.isEmpty()) {
                    Log.d(TAG, "handleOnBackPressed: No previous data found ");
                    //requireActivity().onBackPressed();
                }
                else {
                    Log.d(TAG, "Restoring data ");
                    restoreData(true);
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        Log.d(TAG, "onViewStart called for directory fragment");

        linearlayout = getView().findViewById(R.id.show_directory_linear_layout);

        DirectoryProcessor directoryProcessor = new DirectoryProcessor();
        // directoryProcessor.execute(new Parameter(v, values[0].getString("name")));
        directoryProcessor.execute(parameters.getPath());


    }

    class DirectoryProcessor extends AsyncTask<String, String, Bundle> {
       // Parameters obj;
        String path;
        Bundle bundle=new Bundle();
        int size=0;
/*
        @Override
        protected Bundle doInBackground(Object... objects) {

            obj = (Parameters) objects[0];
            getDirectory(obj.getPath());

            return null;
        }*/

        @Override
        protected Bundle doInBackground(String... strings) {
            path=strings[0];
            getDirectory(path);
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            switch (values[0]) {
                case "File":
                    try {
                        requireActivity().runOnUiThread(() -> {

                            Log.d(TAG, "It is file!!");

                            AlertDialog.Builder confirmationMessage = new AlertDialog.Builder(requireContext());
                            confirmationMessage.setTitle("Download...");
                            confirmationMessage.setMessage("Are you Sure you want to download this file?");
                            confirmationMessage.setPositiveButton("Yes", (dialog, which) -> {
                                ReceiveFile receiveFile = new ReceiveFile();
                                receiveFile.execute(path);
                                restoreData(false);
                            });
                            confirmationMessage.setNegativeButton("No", (dialog, which) -> {
                               /* int i = requireActivity().getSupportFragmentManager()
                                        .getBackStackEntryCount();
                                Log.d(TAG, "Total Fragment " + i);

                                Log.d(TAG, "pop BackStack called ");
                                requireActivity().getSupportFragmentManager().popBackStack();*/
                            });

                            confirmationMessage.setCancelable(false);
                            confirmationMessage.show();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d(TAG, "Exception >> " + e);
                    }

                    break;
                case "CLIENT_LEFT":
                    sendToast("CLIENT HAS LEFT");
                    Log.d(TAG, "CLIENT HAS LEFT");
                    sendRequest("CLIENT_LEFT_ACKNOWLEDGEMENT_MOBILE");
                    requireActivity().runOnUiThread(() -> {
                        AlertDialog.Builder confirmationMessage = new AlertDialog.Builder(requireContext());
                        confirmationMessage.setTitle("CLIENT LEFT");
                        confirmationMessage.setMessage("YOUR PC HAS LEFT. MAKE SURE THE SOFTWARE IS RUNNING ON PC.");
                        confirmationMessage.setCancelable(false);
                        confirmationMessage.show();
                    });
                    break;
                case "InaccessibleFile":
                    sendToast("File Inaccessible");
                    Log.d(TAG, "File Inaccessible ");
                    break;
                case "UnAuthorizedAccess":
                    sendToast("Access Denied");
                    Log.d(TAG, "Access Denied");
                    break;
                default:
                    View myLayout = getLayoutInflater().inflate(R.layout.directory, null, false);

                    CardView.LayoutParams layoutParams = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(5, 10, 5, 10);
                    myLayout.setLayoutParams(layoutParams);

                    TextView directoryName = myLayout.findViewById(R.id.directory_name);
                    directoryName.setText(values[0]);

                    size++;
                    bundle.putInt("SIZE",size);
                    bundle.putString(String.valueOf(size), values[0]);
                    myLayout.setOnClickListener((View v) -> {

                   /* Wan_Networking.SubDirectoryProcessor subDirectoryProcessor = new Wan_Networking.SubDirectoryProcessor();
                    subDirectoryProcessor.execute(new Wan_Networking.Parameter(v, values[0]));*/

                     /*   Parameters parameters = new Parameters(values[0], bufferedReader, printWriter);
                        sub_directories.newInstance(null,null,parameters);*/

                        PcData.add(bundle);

                        requireActivity().runOnUiThread(() -> {

                            int totalChild = linearlayout.getChildCount();
                            Log.d(TAG, "Total Elements in linearlayout are " + totalChild);

                            if (totalChild > 0) {
                                linearlayout.removeAllViews();
                            }
                        });
                        DirectoryProcessor directoryProcessor = new DirectoryProcessor();
                        directoryProcessor.execute(values[0]);

                       /* requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(((ViewGroup) requireView().getParent()).getId(), directories.newInstance(null, null, parameters))
                                .addToBackStack(null)
                                .commit();*/

                    });

                    requireActivity().runOnUiThread(() -> {
                        linearlayout.addView(myLayout);
                    });

                    break;
            }
        }

        private void getDirectory(String path) {
            sendRequest("subDirectories");
            sendRequest(path);

            try {
                String data;
                boolean emptyDirectory=true;

                while (!(data = bufferedReader.readLine()).equals("EndOfStream")) {
                    emptyDirectory=false;
                    Log.d(TAG, "SubDirectory is " + data);
                    onProgressUpdate(data);
                }
                if(emptyDirectory){
                    Log.d(TAG, "Empty Folder");

                    sendToast("Folder is Empty");
                    restoreData(false);
                    return;
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
            progressDialog = new ProgressDialog(getContext());

            progressDialog.setTitle("Downloading");
            progressDialog.setMessage("Your File is being downloaded in background. You can use any other app.");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

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
                progressDialog.incrementProgressBy(1);
                progressDialog.setProgress(1);

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
//              Log.d(TAG, "Creating File ");
//            ContextWrapper contextWrapper = new ContextWrapper(APPLICATION_CONTEXT);
//            File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
//            File file = new File(musicDirectory, Name);

            int copies = 1;
            String PdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
            File file = new File(PdfPath, Name);
            if (file.exists()) {
                Log.d(TAG, "File Already exists:");
                while (file.exists()) {
                    file = new File(PdfPath, "(" + copies + ")" + Name);
                    copies++;
                }
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

                DataInputStream dIn = new DataInputStream(SingletonSocket.getSocket().getInputStream());
                FileOutputStream fos = new FileOutputStream(getFilePath(fileName));

                byte[] data;
                int bufferSize = 2048 * 4;
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

                    if (dataAvailable(dIn)) {
                        killDownloadingThread = true;
                        //sendToast("Unknown Error");
                        break;
                    } else {
                        Log.d(TAG, "Trying to Read");
                        bytesReadPerCycle = dIn.read(data, 0, bufferSize);
                        Log.d(TAG, "Read " + totalBytesRead + " of " + fileSize + " bytes");

                        fos.write(data, 0, bytesReadPerCycle);
                        totalBytesRead += bytesReadPerCycle;
                        Log.d(TAG, "Total data read " + totalBytesRead / 1000000 + " MB's (" + totalBytesRead + " bytes)");

                        publishProgress(String.valueOf(totalBytesRead));
                    }

                }

                if (totalBytesRead == fileSize) {
                    Log.d(TAG, " Download Completed Successfully ");
                    sendRequest("DONE");

                    getActivity().startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
                } else if (killDownloadingThread) {
                    sendRequest("KillThread");
                    Log.d(TAG, "Sleeping thread for 2 second");
                    Thread.sleep(2000);

                    String cancelledData = bufferedReader.readLine();
                    Log.d(TAG, "Cancelled DAta is " + cancelledData);

                    while (!cancelledData.equals("DATA_ENDED")) {

                        fos.write(cancelledData.getBytes());
                        fos.flush();
                        cancelledData = bufferedReader.readLine();
                        Log.d(TAG, "Cancelled Data is " + cancelledData);
                    }

                    Log.d(TAG, "Last  Data after Cancellation is  " + cancelledData);
                    sendToast("Download Completed");
                }

                Log.d(TAG, "doInBackground for Receive File is returning  ");

            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, " Exception>> " + e);
            }
        }


        private boolean dataAvailable(DataInputStream dIn) {
            return false;
           /* int sleepThread = 0;
            try {
                while (dIn.available() <= 0) {

                    //If the data has not Arrived yet, wait for it by sleeping the thread

                    Log.d(TAG, "Sleeping thread for 3 sec ");
                    Thread.sleep(3000);
                    sleepThread++;

                    if (sleepThread == 2) {
                        // Data has not arrived even after waiting
                        Log.d(TAG, " Unknown Error (killing Downloading Thread)");
                        return true;
                    }
                }

            } catch (IOException | InterruptedException e) {
                Log.d(TAG, "dataAvailable: Exception" + e);
                e.printStackTrace();
                return true;
            }
            return false;*/
        }


    }

    void sendRequest(String request) {
        try {
            Log.d(TAG, "Sending Request to Server i.e >>  " + request);
//           PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            printWriter.println(request);
            printWriter.flush();

        } catch (Exception e) {
            e.printStackTrace();
            sendToast("Request Timeout");
            Log.d(TAG, "Exception in sendRequest() " + e);
        }
    }

    private void sendToast(String message) {
        getActivity().runOnUiThread(() -> {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    private void restoreData(boolean remove) {

        Bundle b=PcData.get(PcData.size()-1);

        if(remove){
            PcData.remove(PcData.size()-1);

            requireActivity().runOnUiThread(() -> {
                linearlayout.removeAllViews();
            });

        }


        for (int i = 1; i <= b.getInt("SIZE"); i++) {

            final String data =b.getString(String.valueOf(i));

            View myLayout = inflateData(data);

            myLayout.setOnClickListener((View v) -> {

                PcData.add(b);

                requireActivity().runOnUiThread(() -> {

                    int totalChild = linearlayout.getChildCount();
                    Log.d(TAG, "Total children in linearlayout are " + totalChild);

                    if (totalChild > 0) {
                        linearlayout.removeAllViews();
                    }
                });

                DirectoryProcessor directoryProcessor = new DirectoryProcessor();
                directoryProcessor.execute(data);
            });

            requireActivity().runOnUiThread(() -> {
                linearlayout.addView(myLayout);
            });

        }

    }

    private View inflateData(String data ){
        View myLayout = getLayoutInflater().inflate(R.layout.directory, null, false);

        CardView.LayoutParams layoutParams = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(5, 10, 5, 10);
        myLayout.setLayoutParams(layoutParams);

        TextView directoryName = myLayout.findViewById(R.id.directory_name);
        directoryName.setText(data);
        return myLayout;
}

}