package com.fyp.iShare;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.fyp.iShare.ui.login.LoginFragment;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragment_container_view, LoginFragment.class, null)
                .commit();
    }
}