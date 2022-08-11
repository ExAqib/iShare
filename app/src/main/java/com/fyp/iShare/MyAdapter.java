package com.fyp.iShare;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    Context context;
    File[] filesAndFolders;
    String TAG = "tag";

    public MyAdapter(Context context, File[] filesAndFolders) {
        this.context = context;
        this.filesAndFolders = filesAndFolders;
    }

    public static byte[] convertFileToByteArray(File f, DataOutputStream dataOutputStream) {
        byte[] byteArray = null;
        try {
            InputStream inputStream = new FileInputStream(f);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024 * 8];
            int bytesRead;


            while ((bytesRead = inputStream.read(b)) != -1) {
                bos.write(b, 0, bytesRead);
                //  dataOutputStream.write(b, 0, bytesRead);
            }

            byteArray = bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return byteArray;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        File selectedFile = filesAndFolders[position];
        holder.textView.setText(selectedFile.getName());

        if (selectedFile.isDirectory()) {
            holder.imageView.setImageResource(R.drawable.ic_baseline_folder_24);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_baseline_insert_drive_file_24);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View v) {
                if (selectedFile.isDirectory()) {

                    String path = selectedFile.getAbsolutePath();
                    Intent intent = new Intent(context, Internal_Storage.class);
                    intent.putExtra("path", path);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } else {

                    Thread t1 = new Thread(() -> sendFile(selectedFile));
                    t1.start();

                   /* try{
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(selectedFile.getAbsolutePath()), "video/*");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);

                    }
                    catch(Exception e)
                    {
                        Toast.makeText(context.getApplicationContext(), " "+e, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Exception"+e);
                    }*/

                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return filesAndFolders.length;
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

    private void sendFile(File selectedFile) {


        int bufferSize = 2048 * 4;

        SingletonSocket.sendRequest(selectedFile.getName());
        SingletonSocket.sendRequest(String.valueOf(bufferSize));
        SingletonSocket.sendRequest(String.valueOf(selectedFile.length()));


        byte[] file = new byte[(int) selectedFile.length()];
        FileInputStream fis = null;
        byte[] b = new byte[bufferSize];

        try {
            Log.d(TAG, "File path is " + selectedFile.getPath());
            fis = new FileInputStream(selectedFile.getPath());


            int readNum;

            //DataOutputStream dataOutputStream = new DataOutputStream(SingletonSocket.getSocket().getOutputStream());
            //convertFileToByteArray(selectedFile,dataOutputStream);
            sendByteArray(convertFileToByteArray(selectedFile, null));

           /* OutputStream dataOutputStream = new DataOutputStream(SingletonSocket.getSocket().getOutputStream());

            dataOutputStream.write( convertFileToByteArray(selectedFile,null));
            Log.d(TAG, "sleeping thread for 3 sec" );

            Thread.sleep(3000);
            dataOutputStream.flush();
*/


            // dataOutputStream.close();


           /* OutputStream OutputStream = new DataOutputStream(SingletonSocket.getSocket().getOutputStream());



            OutputStream.write( convertFileToByteArray(selectedFile));
            OutputStream.flush();
            OutputStream.close();*/

          /*     dataOutputStream.write( convertFileToByteArray(selectedFile));
            dataOutputStream.flush();
            dataOutputStream.close();
*/
            /*FileOutputStream fos = new FileOutputStream(getFilePath("M_Aqib.pdf"));
            fos.write(convertFileToByteArray(selectedFile));
            fos.flush();
*/
           /* while ((readNum = fis.read(file)) != -1) {
                 Log.d(TAG, "REad NUm is " + readNum);

                dataOutputStream.write(file, 0, readNum);
                dataOutputStream.flush();

                fos.write(file, 0, readNum);
                fos.flush();

            }
            Log.d(TAG, "REad NUm was " + readNum);

            fos.close();
            dataOutputStream.close();*/
            Log.d(TAG, "File and data stream closed");
        } catch (Exception e) {
            Log.d("tag", e.toString());
        }
    }

    private void sendByteArray(byte[] convertFileToByteArray) throws IOException, InterruptedException {
        int buffSize = 1024 * 2;
        int dataSent = 0;

        int halfData = convertFileToByteArray.length / 2;
        Log.d(TAG, " half data is  " + halfData);

        SingletonSocket.sendRequest(String.valueOf(halfData));


        OutputStream outputStream = SingletonSocket.getSocket().getOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

       /* for (int i = 0; i <convertFileToByteArray.length ; i++) {
            convertFileToByteArray[i]+=1;
        }
*/

        while (dataSent < convertFileToByteArray.length) {
            Thread.sleep(500);

            if (convertFileToByteArray.length - dataSent < buffSize) {
                buffSize = convertFileToByteArray.length - dataSent;
                Log.d(TAG, "buffer size adjusted to " + buffSize);


            }

            dataOutputStream.write(convertFileToByteArray, dataSent, buffSize);
            Log.d(TAG, "dos size after sending " + dataOutputStream.size());

            dataOutputStream.flush();
            dataSent += buffSize;
        }
        Log.d(TAG, "data Send is  " + dataSent);

        //  dataOutputStream.close();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView textView;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.tv_directory_name);
            imageView = itemView.findViewById(R.id.rcv_img);
        }
    }

}
