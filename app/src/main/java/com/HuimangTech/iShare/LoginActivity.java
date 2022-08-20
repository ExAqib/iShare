package com.HuimangTech.iShare;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.HuimangTech.iShare.ui.login.LoginFragment;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /// TODO: 8/20/2022 check for logging statues and show the appropriate fragment
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragment_container_view, LoginFragment.class, null)
                .commit();
    }
}