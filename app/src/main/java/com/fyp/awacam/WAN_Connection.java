package com.fyp.awacam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
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


}