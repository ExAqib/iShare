package com.fyp.iShare.ui.downloads;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fyp.iShare.databinding.FragmentDownloadsBinding;

public class DownloadsFragment extends Fragment {

    private FragmentDownloadsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DownloadsViewModel downloadsViewModel =
                new ViewModelProvider(this).get(DownloadsViewModel.class);

        binding = FragmentDownloadsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDownloads;
        downloadsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}