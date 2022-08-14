package com.fyp.iShare;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.fyp.iShare.databinding.ActivityWanConnectionBinding;
import com.fyp.iShare.ui.messages.chat;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class WAN_Connection extends AppCompatActivity {

    private static final String TAG = "Tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityWanConnectionBinding binding = ActivityWanConnectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        displayData displayDataFragment = displayData.newInstance(null);

        FragmentManager manager = getSupportFragmentManager();
        SingletonSocket.setFragmentManger(manager);
        FragmentTransaction transaction = manager.beginTransaction();

        transaction.replace(R.id.myFrameLayoutWan, displayDataFragment);
        transaction.commit();
        Log.d(TAG, "loadFragment: Transaction committed");

        binding.btnClose.setOnClickListener(v -> {
            this.finish();
        });
        binding.btnMessage.setOnClickListener(v -> {
            Intent intent = new Intent(this, chat.class);
            startActivity(intent);
        });
        binding.btnPower.setOnClickListener(v -> {
            BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheet);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_power_control, null);

            ConstraintLayout footer = dialogView.findViewById(R.id.footer);
            TextView cancel = footer.findViewById(R.id.cancel);
            TextView ok = footer.findViewById(R.id.ok);

            cancel.setOnClickListener(v12 -> dialog.dismiss());

            ok.setOnClickListener(v1 -> {

            });

            dialog.setContentView(dialogView);
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
                    //FrameLayout bottomSheet = bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
                    //BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
                    //bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            });
            dialog.show();

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.send_file, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int ID = item.getItemId();
        if (ID == R.id.send_file_menu) {
            new Thread(() -> SingletonSocket.sendRequest("$RECEIVE_FILE$")).start();
            startActivity(new Intent(WAN_Connection.this, Internal_Storage.class));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}