package com.HuimangTech.iShare.ui.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.HuimangTech.iShare.LinkedDevices;
import com.HuimangTech.iShare.LoginDetails;
import com.HuimangTech.iShare.R;
import com.HuimangTech.iShare.databinding.FragmentLoginBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginFragment extends Fragment {

    private static final String TAG = "tag";
    private LoginViewModel loginViewModel;
    private FragmentLoginBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);

        binding.signup.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_view, SignupFragment.class, null)
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit());

        binding.forgetPassword.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_view, ForgotPasswordFragment.class, null)
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit());


        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = binding.email;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;

        loginViewModel.getLoginFormState().observe(getViewLifecycleOwner(), loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            loginButton.setEnabled(loginFormState.isDataValid());
            if (loginFormState.getUsernameError() != null) {
                usernameEditText.setError(getString(loginFormState.getUsernameError()));
            }
            if (loginFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(loginFormState.getPasswordError()));
            }
        });

        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
            if (loginResult == null) {
                return;
            }
            loadingProgressBar.setVisibility(View.GONE);
            if (loginResult.getError() != null) {
                showLoginFailed(loginResult.getError());
            }
            if (loginResult.getSuccess() != null) {
                updateUiWithUser(loginResult.getSuccess());
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
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };

        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
            return false;
        });

        loginButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            String enteredMail = usernameEditText.getText().toString().trim();
            String enteredPassword = passwordEditText.getText().toString().trim();

            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = firebaseDatabase.getReference("Clients");
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    boolean AccountNotFound = true;

                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                        String mail = dataSnapshot1.child("email").getValue(String.class);
                        String password = dataSnapshot1.child("password").getValue(String.class);
                        String key = dataSnapshot1.getKey();


                        if (enteredMail.equals(mail)) {
                            AccountNotFound = false;
                            if (enteredPassword.equals(password)) {
                                //ToDo:Success Login
                                Toast.makeText(requireActivity(), "Login Success", Toast.LENGTH_SHORT).show();

                                //Start (Get all logged devices)
                                LoginDetails.LoggedIn = true;
                                LoginDetails.userEmail = mail;
                                LoginDetails.userPassword = password;
                                LoginDetails.userKey = key;

                                DatabaseReference databaseReference = firebaseDatabase.getReference("Clients/" + key + "/devices");
                                databaseReference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {

                                            String deviceID = dataSnapshot2.getKey();
                                            String deviceName = dataSnapshot2.child("Name").getValue(String.class);
                                            LinkedDevices.AddDevice(deviceName, deviceID);

                                            Log.d(TAG, "device ID " + deviceID + " device name " + deviceName);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.d(TAG, " Cancelled:" + databaseError);
                                    }

                                });

                                //End

                                loadingProgressBar.setVisibility(View.INVISIBLE);

                                loginViewModel.login(usernameEditText.getText().toString(),
                                        passwordEditText.getText().toString());
                            } else {
                                passwordEditText.setError("Incorrect Password");
                                loadingProgressBar.setVisibility(View.INVISIBLE);
                            }
                            break;
                        }
                    }

                    if (AccountNotFound) {
                        Toast.makeText(requireContext(), "Account Not Found", Toast.LENGTH_SHORT).show();
                        loadingProgressBar.setVisibility(View.INVISIBLE);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, " Cancelled:" + databaseError);
                }

            });

        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        if (getContext() != null && getContext().getApplicationContext() != null) {
            Toast.makeText(getContext().getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
            requireActivity().finish();
        }
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        if (getContext() != null && getContext().getApplicationContext() != null) {
            Toast.makeText(
                    getContext().getApplicationContext(),
                    errorString,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}