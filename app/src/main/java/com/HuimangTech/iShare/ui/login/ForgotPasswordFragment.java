package com.HuimangTech.iShare.ui.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.HuimangTech.iShare.databinding.FragmentForgotPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;


public class ForgotPasswordFragment extends Fragment {

    private LoginViewModel loginViewModel;
    private FragmentForgotPasswordBinding binding;
    private FirebaseAuth fbAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);
        fbAuth = FirebaseAuth.getInstance();

        binding.reset.setOnClickListener(view -> {
            resetPassword();
        });


        return binding.getRoot();

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText userEmailEditText = binding.email;
        final Button resetButton = binding.reset;
        final ProgressBar loadingProgressBar = binding.loading;


        loginViewModel.getLoginFormState().observe(getViewLifecycleOwner(), new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                resetButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    userEmailEditText.setError(getString(loginFormState.getUsernameError()));
                }
            }
        });


        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.resetDataChanged(userEmailEditText.getText().toString());
            }
        };
        userEmailEditText.addTextChangedListener(afterTextChangedListener);


    }

    private void resetPassword() {
        String mail = binding.email.getText().toString().trim();

        binding.loading.setVisibility(View.VISIBLE);

        fbAuth.sendPasswordResetEmail(mail).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "check your email to reset you password!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "something wrong happened, try again later!", Toast.LENGTH_LONG).show();
            }
            binding.loading.setVisibility(View.GONE);
        });

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}