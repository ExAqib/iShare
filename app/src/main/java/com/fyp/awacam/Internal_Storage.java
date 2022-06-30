package com.fyp.awacam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.fyp.awacam.databinding.ActivityInternalStorageBinding;

import java.io.File;

public class Internal_Storage extends AppCompatActivity {

    ActivityInternalStorageBinding binding;
    static final String TAG = "tag";
    int RequestCode=1122;

    String[] Permissions = new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE};


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
        }

        File root = new File(path);
        File[] filesAndFolders = root.listFiles();

        if(filesAndFolders==null || filesAndFolders.length==0)
        {
            binding.noFilesTv.setVisibility(View.GONE);
            return;
        }
        binding.noFilesTv.setVisibility(View.INVISIBLE);

        binding.directoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.directoriesRecyclerView.setAdapter(new MyAdapter(getApplicationContext(),filesAndFolders));

    }

    private void grantPermissions() {
        Log.d(TAG, "Granting Permission");
        ActivityCompat.requestPermissions(this, Permissions, RequestCode);
    }

    private boolean permissionsGranted() {
        for (String permission : Permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permissions not granted i.e " + permission);
                return false;
            }
        }
        return true;
    }

}