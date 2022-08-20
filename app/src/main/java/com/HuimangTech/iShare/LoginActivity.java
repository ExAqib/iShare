package com.HuimangTech.iShare;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.HuimangTech.iShare.ui.login.AccountFragment;
import com.HuimangTech.iShare.ui.login.LoginFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /// TODO: 8/20/2022 check for logging statues and show the appropriate fragment

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container_view, AccountFragment.class, null)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container_view, LoginFragment.class, null)
                    .commit();
        }


    }
}