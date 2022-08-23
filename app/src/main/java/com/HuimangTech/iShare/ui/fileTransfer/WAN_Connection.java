package com.HuimangTech.iShare.ui.fileTransfer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.HuimangTech.iShare.R;
import com.HuimangTech.iShare.SingletonSocket;
import com.HuimangTech.iShare.databinding.ActivityWanConnectionBinding;
import com.HuimangTech.iShare.ui.messages.chat;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class WAN_Connection extends AppCompatActivity {

    private static final String TAG = "Tag";
    PrintWriter printWriter;
    BufferedReader bufferedReader;
    Context ActivityContext;
    boolean doubleBackToExitPressedOnce = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityWanConnectionBinding binding = ActivityWanConnectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            printWriter = new PrintWriter(new OutputStreamWriter(SingletonSocket.getSocket().getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(SingletonSocket.getSocket().getInputStream()));

        } catch (Exception e) {
            Log.d(TAG, "onCreate: " + e);
        }

        displayData displayDataFragment = displayData.newInstance(null);

        FragmentManager manager = getSupportFragmentManager();
        SingletonSocket.setFragmentManger(manager);
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.myFrameLayoutWan, displayDataFragment)
                .commit();
        Log.d(TAG, "loadFragment: Transaction committed");

        LinearLayout mBottomToolView = findViewById(R.id.li_toolBar);
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mBottomToolView.getLayoutParams();
        layoutParams.setBehavior(new BottomToolBarBehavior());

        binding.btnClose.setOnClickListener(v -> {
            closeConnection();
            this.finish();
        });
        binding.btnMessage.setOnClickListener(v -> {
            Intent intent = new Intent(this, chat.class);
            startActivity(intent);
        });
        binding.btnPower.setOnClickListener(v -> {
            PowerControlDialog();

        });
    }


    public void closeConnection() {
        new Thread(() -> {
            printWriter.println("CLOSE_CONNECTION");
            printWriter.flush();
            SingletonSocket.CloseSocket();
        }).start();
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

    private void PowerControlDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheet);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_power_control, null);

        Button powerOff = dialogView.findViewById(R.id.btn_off);
        Button restart = dialogView.findViewById(R.id.btn_restart);
        Button sleep = dialogView.findViewById(R.id.btn_sleep);
        Button lock = dialogView.findViewById(R.id.btn_lock);

        powerOff.setOnClickListener(v1 -> {
            new Thread(() -> {
                printWriter.println("POWEROFF");
                printWriter.flush();
                dialog.dismiss();
            }).start();
        });

        restart.setOnClickListener(v1 -> {
            new Thread(() -> {
                printWriter.println("RESTART");
                printWriter.flush();
                dialog.dismiss();
            }).start();
        });

        sleep.setOnClickListener(v1 -> {
            new Thread(() -> {

                printWriter.println("SLEEP");
                printWriter.flush();
                dialog.dismiss();
            }).start();
        });

        lock.setOnClickListener(v1 -> {
            new Thread(() -> {

                printWriter.println("LOCK");
                printWriter.flush();
                dialog.dismiss();
            }).start();
        });


        ConstraintLayout footer = dialogView.findViewById(R.id.footer);
        TextView cancel = footer.findViewById(R.id.cancel);
        TextView setTimer = footer.findViewById(R.id.ok);

        cancel.setOnClickListener(v12 -> dialog.dismiss());

        setTimer.setOnClickListener(v1 -> {
            // TODO: 8/14/2022 extra Timer feature
        });

        dialog.setContentView(dialogView);
        dialog.setOnShowListener(dialog1 -> {
           /* BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog1;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
            BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);*/
        });
        dialog.show();
    }


}