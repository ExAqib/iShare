package com.HuimangTech.iShare.ui.login;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.HuimangTech.iShare.R;
import com.HuimangTech.iShare.databinding.FragmentAccountBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;
    ProgressBar loadingProgressBar;
    TextView name;
    TextView email;
    View itemName;
    View itemEmail;
    View itemPassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentAccountBinding.inflate(inflater, container, false);

        loadingProgressBar = binding.loading;
        loadingProgressBar.setVisibility(View.VISIBLE);

        binding.btnLogout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            requireActivity().finish();
        });

        setUserInfo();
        updateUserInfoListeners();

        return binding.getRoot();
    }

    void setUserInfo() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Clients");
        userID = user.getUid();

        itemName = binding.getRoot().findViewById(R.id.profile_name_item);
        itemEmail = binding.getRoot().findViewById(R.id.profile_email_item);
        itemPassword = binding.getRoot().findViewById(R.id.profile_pass_item);

        name = itemName.findViewById(R.id.tv_name);
        email = itemEmail.findViewById(R.id.tv_email);

        reference.child(userID).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("firebase", "Error getting data", task.getException());
            } else {
                User userProfile = task.getResult().getValue(User.class);
                if (userProfile != null) {
                    name.setText(userProfile.Name);
                    email.setText(userProfile.Email);
                    loadingProgressBar.setVisibility(View.GONE);
                } else
                    requireActivity().finish();
            }
        });

    }

    void updateUserInfoListeners() {
        itemName.setOnClickListener(view -> {
            Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.profile_name_update_item);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.show();

            dialog.findViewById(R.id.footer).findViewById(R.id.ok).setOnClickListener(view1 -> {
                EditText newName = dialog.findViewById(R.id.edt_name);

                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(String.valueOf(newName.getText()))
                        .build();

                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                reference.child(userID).child("Name").setValue(newName.getText().toString());
                                name.setText(newName.getText());
                                Toast.makeText(getContext(), "Display name is updated", Toast.LENGTH_SHORT).show();
                            }
                        });
                dialog.cancel();
            });

            dialog.findViewById(R.id.footer).findViewById(R.id.cancel).setOnClickListener(view1 -> {
                dialog.cancel();
            });
        });

        itemEmail.setOnClickListener(view -> {
            Toast.makeText(getContext(), "You Can't Change Your Email", Toast.LENGTH_SHORT).show();
        });

        itemPassword.setOnClickListener(view -> {
            Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.profile_password_update_item);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.show();

            dialog.findViewById(R.id.footer).findViewById(R.id.ok).setOnClickListener(view1 -> {
                EditText oldPassword = dialog.findViewById(R.id.edt_oldPassword);
                EditText newPassword = dialog.findViewById(R.id.edt_newPassword);
                EditText confirmPassword = dialog.findViewById(R.id.edt_confirmPassword);
                if (oldPassword.getText().toString().equals("")) {
                    oldPassword.setError("password is blank!");
                } else {
                    if (!newPassword.getText().toString().equals(confirmPassword.getText().toString())) {
                        newPassword.setError("password do not match");
                        confirmPassword.setError("password do not match");
                    } else {
                        reference.child(userID).get().addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                Log.e("firebase", "Error getting data", task.getException());
                            } else {
                                User userProfile = task.getResult().getValue(User.class);
                                if (userProfile != null) {
                                    if (oldPassword.getText().toString().equals(userProfile.Password)) {
                                        user.updatePassword(newPassword.getText().toString())
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        reference.child(userID).child("Password").setValue(newPassword.getText().toString());
                                                        Toast.makeText(getContext(), "password is successfully updated!", Toast.LENGTH_SHORT).show();
                                                        dialog.cancel();
                                                    }
                                                });
                                    } else {
                                        oldPassword.setError("Password Incorrect");
                                    }
                                } else {
                                    requireActivity().finish();
                                }
                            }
                        });
                    }
                }

            });

            dialog.findViewById(R.id.footer).findViewById(R.id.cancel).setOnClickListener(view1 -> {
                dialog.cancel();
            });
        });
    }


}