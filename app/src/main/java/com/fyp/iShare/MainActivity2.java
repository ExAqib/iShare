package com.fyp.iShare;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.fyp.iShare.databinding.ActivityMain2Binding;
import com.fyp.iShare.ui.devices.DevicesFragment;
import com.fyp.iShare.ui.downloads.DownloadsFragment;
import com.fyp.iShare.ui.home.HomeFragment;
import com.fyp.iShare.ui.messages.MessagesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity2 extends AppCompatActivity {

    private ActivityMain2Binding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme();

        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        BottomNavigationView navView = findViewById(R.id.nav_view);

        final NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main2);
        final NavController navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(binding.navView, navController);


        navView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                item.setChecked(true);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment_activity_main2, HomeFragment.class, null)
                        .commit();
            } else if (item.getItemId() == R.id.navigation_devices) {
                // TODO: 8/15/2022 if logged in then go to devices 
                item.setChecked(true);
                if (true) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.nav_host_fragment_activity_main2, DevicesFragment.class, null)
                            .commit();
                } else {
                    //Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    //startActivity(intent);
                    //navView.setSelectedItemId(R.id.navigation_downloads);
                }


            } else if (item.getItemId() == R.id.navigation_messages) {
                item.setChecked(true);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment_activity_main2, MessagesFragment.class, null)
                        .commit();

            } else if (item.getItemId() == R.id.navigation_downloads) {
                item.setChecked(true);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment_activity_main2, DownloadsFragment.class, null)
                        .commit();

            }
            return false;
        });

    }


    public void setTheme() {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        String themeName = pref.getString("theme", "system");

        switch (themeName) {
            case "dark":
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "light":
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "system":
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }

    }

}