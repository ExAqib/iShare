package com.fyp.iShare;



import android.Manifest;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fyp.iShare.databinding.ActivityInternalStorageBinding;

import java.io.File;
import java.io.FilenameFilter;

public class Internal_Storage extends AppCompatActivity {

    ActivityInternalStorageBinding binding;
    static final String TAG = "tag";
    int RequestCode=1122;

    String[] Permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =ActivityInternalStorageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //RecyclerView
        //TextView noFilesTet

        if(!permissionsGranted())
        {
            grantPermissions();
        }
        String path;
        try{
            path = getIntent().getStringExtra("path");
        }
        catch(Exception e){
            path=null;
        }
        if(path==null || path.isEmpty()){
            path = Environment.getExternalStorageDirectory().getPath();
            //path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        }


        File root = new File(path);

        MyFileFilter filter = new MyFileFilter();


        File[] filesAndFolders = root.listFiles();
//        File[] filesAndFolders = root.listFiles(filter);


        if(filesAndFolders==null || filesAndFolders.length==0)
        {
            binding.noFilesTv.setVisibility(View.VISIBLE);
            return;
        }
        binding.noFilesTv.setVisibility(View.GONE);

        binding.directoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.directoriesRecyclerView.setAdapter(new MyAdapter(getApplicationContext(),filesAndFolders));

    }

    private void grantPermissions() {
        Log.d(TAG, "Granting Permission");
        ActivityCompat.requestPermissions(this, Permissions, RequestCode);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Log.d(TAG, "Opening settings for permission " );

                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                myActivityResultLauncher.launch(intent);
            } catch (Exception e) {
                Log.d(TAG, "grantPermissions: Exception"+e);
                Toast.makeText(Internal_Storage.this,""+ e, Toast.LENGTH_SHORT).show();
            }
        }

    }

    ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(Build.VERSION.SDK_INT>Build.VERSION_CODES.R){
                    if(!Environment.isExternalStorageManager()){
                        Toast.makeText(Internal_Storage.this, "Please Allow permission for storage access!", Toast.LENGTH_LONG).show();
                    }
                }

            }

    );


    private boolean permissionsGranted() {
        for (String permission : Permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permissions not granted i.e " + permission);
                return false;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Log.d(TAG, "Android version > 11 ");

            return Environment.isExternalStorageManager();
        }
        return true;
    }

    public class MyFileFilter implements FilenameFilter
    {

        @Override
        public boolean accept(File directory, String fileName) {
            return true;

           /* if (fileName.endsWith(".txt")) {
                return true;
            }
            return false;*/
        }
    }

}