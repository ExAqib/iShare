package com.fyp.awacam;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.fyp.awacam.databinding.ActivityWanConnectionBinding;

public class WAN_Connection extends AppCompatActivity {

    private static final String TAG = "Tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityWanConnectionBinding binding = ActivityWanConnectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

         displayData displayDataFragment =   displayData.newInstance(null);

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction=manager.beginTransaction();

        transaction.replace(R.id.myFrameLayoutWan,displayDataFragment);
        transaction.commit();
        Log.d(TAG, "loadFragment: Transaction committed");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.send_file,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int ID=item.getItemId();
        if(ID==R.id.send_file_menu){
            new Thread(()->SingletonSocket.sendRequest("$RECEIVE_FILE$")).start();
            startActivity(new Intent(WAN_Connection.this,Internal_Storage.class));
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }
}