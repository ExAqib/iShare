package com.HuimangTech.iShare;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import androidx.room.Room;

import com.HuimangTech.iShare.ui.downloads.DB.FileHistoryDatabase;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

public class DirectlyReceivePCfile extends AsyncTask<String, String, Void> {

    private static final String TAG = "tag";
    Context context;
    ProgressDialog progressDialog;
    int fileSize = 0;

    public DirectlyReceivePCfile(Context context) {
        this.context = context;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);

        progressDialog.setTitle("Receiving File...");
        progressDialog.setMessage("Your File is being received in background. Feel free to use any other app!");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        progressDialog.setProgress(0);
        progressDialog.show();
    }

    @Override
    protected Void doInBackground(String... strings) {
        try {
            Log.d(TAG, "Receiving File");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(SingletonSocket.getSocket().getInputStream()));
            String fileName = bufferedReader.readLine();

            String fileSize = bufferedReader.readLine();
            FileHistoryDatabase db = Room.databaseBuilder(context.getApplicationContext(),
                    FileHistoryDatabase.class, "File").build();

            db.FileDao().insert(new com.HuimangTech.iShare.ui.downloads.DB.File(fileName, Long.parseLong(fileSize)));


            this.fileSize = Integer.parseInt(fileSize);
            progressDialog.setMax(this.fileSize / 1000000);

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
        }

    }

    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);
        Log.d(TAG, "onPostExecute: called for receive file");
        progressDialog.dismiss();
    }

    private String getFilePath(String Name) {
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

            Log.d(TAG, "Received File size is " + (float) fileSize / 1000000 + "MB (" + fileSize + "byes)");

            DataInputStream dIn = new DataInputStream(SingletonSocket.getSocket().getInputStream());
            FileOutputStream fos = new FileOutputStream(getFilePath(fileName));

            int bufferSize = 2048 * 8;
            int totalBytesRead = 0;
            int bytesReadPerCycle;
            byte[] data=new byte[bufferSize];

            int chunk =0;
            while (totalBytesRead < fileSize) {
                chunk++;
             /*
              //checking if the buffer size exceeds, the size of file or remaining data of file
                if (bufferSize > (fileSize - totalBytesRead)) {
                    Log.d(TAG, "Adjusting buffer size from " + bufferSize);
                    bufferSize = fileSize - totalBytesRead;
                    Log.d(TAG, "to " + bufferSize);
                }
                */
                //data = new byte[bufferSize];

                Log.d(TAG, "Trying to Read chunk "+chunk);
                //bytesReadPerCycle = dIn.read(data, 0, bufferSize);
                bytesReadPerCycle = dIn.read(data);

                Log.d(TAG, "Read " + totalBytesRead + " of " + fileSize + " bytes");

                fos.write(data, 0, bytesReadPerCycle);
                totalBytesRead += bytesReadPerCycle;
                Log.d(TAG, "Total data read " + totalBytesRead / 1000000 + " MB's (" + totalBytesRead + " bytes)");

                publishProgress(String.valueOf(totalBytesRead));
            }

            if (totalBytesRead == fileSize) {
                Log.d(TAG, " Download Completed Successfully ");
                SingletonSocket.sendRequest("DONE");
                //sendToast("File saved in Downloads folder ");
                // context.requireActivity().startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
            }
            Log.d(TAG, "doInBackground for Receive File is returning  ");

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, " Exception>> " + e);
        }
    }

}
