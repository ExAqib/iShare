package com.HuimangTech.iShare.ui.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.HuimangTech.iShare.R;
import com.HuimangTech.iShare.databinding.FragmentSignupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupFragment extends Fragment {

    String TAG = "tag";
    private LoginViewModel loginViewModel;
    private FragmentSignupBinding binding;
    private FirebaseAuth fbAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentSignupBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText userEmailEditText = binding.email;
        final EditText userNameEditText = binding.name;
        final EditText passwordEditText = binding.password;
        final Button resetButton = binding.signup;
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
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
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
                loginViewModel.loginDataChanged(userEmailEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        userEmailEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(userEmailEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        resetButton.setOnClickListener(v -> {
            String name = userNameEditText.getText().toString().trim();
            String mail = userEmailEditText.getText().toString().trim().toLowerCase();
            String password = passwordEditText.getText().toString().trim();

            if (!name.equals("") && !mail.equals("") && !password.equals("")) {
                loadingProgressBar.setVisibility(View.VISIBLE);

                fbAuth = FirebaseAuth.getInstance();

                fbAuth.createUserWithEmailAndPassword(mail, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser FBuser = fbAuth.getCurrentUser();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                        FBuser.updateProfile(profileUpdates);

                        // TODO: 8/20/2022 remove password from realtime database
                        User user = new User(name, mail, password);
                        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                        DatabaseReference databaseReference = firebaseDatabase.getReference("Clients");

                        FirebaseDatabase.getInstance().getReference("Clients")
                                .child(fbAuth.getCurrentUser().getUid())
                                .setValue(user).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        // TODO: 8/20/2022 set proper toasts
                                        Toast.makeText(requireContext(), "Account Created Auth", Toast.LENGTH_LONG).show();
                                        requireActivity().finish();
                                        loadingProgressBar.setVisibility(View.INVISIBLE);
                                    } else {
                                        Toast.makeText(requireContext(), "Account Created Failed", Toast.LENGTH_LONG).show();
                                        loadingProgressBar.setVisibility(View.INVISIBLE);
                                    }
                                });


                    /*databaseReference.orderByChild("email").equalTo(mail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {

                                userEmailEditText.setError("This email already exists");
                                Toast.makeText(requireContext(), "Email Already Registered", Toast.LENGTH_SHORT).show();
                                loadingProgressBar.setVisibility(View.INVISIBLE);

                            } else {
                                DatabaseReference ref = firebaseDatabase.getReference();

                                User user = new User(name, mail, password);
                                ref.child("Clients").push().setValue(user);

                                loginViewModel.login(userEmailEditText.getText().toString(),
                                        passwordEditText.getText().toString());

                                Toast.makeText(requireContext(), "Account Created", Toast.LENGTH_SHORT).show();

                                requireActivity().finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(requireContext(), error.toString(), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onDataChange: Error " + error);
                        }
                    });*/
                    }
                });

            }
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        if (getContext() != null && getContext().getApplicationContext() != null) {
            Toast.makeText(getContext().getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
            getActivity().finish();

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