package com.HuimangTech.iShare.ui.downloads;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.HuimangTech.iShare.databinding.FragmentDownloadsBinding;
import com.HuimangTech.iShare.ui.downloads.DB.File;
import com.HuimangTech.iShare.ui.downloads.DB.FileHistoryDatabase;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DownloadsFragment extends Fragment implements RecyclerAdapter.OnFileListener {

    public List<File> files = new ArrayList<>();
    private FragmentDownloadsBinding binding;
    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDownloadsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Maybe.empty().subscribeOn(Schedulers.io()).subscribe(s -> s.toString(),
                Throwable::printStackTrace,
                () -> loadFileHistory()
        );

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        setAdapter();
    }

    void loadFileHistory() {
        FileHistoryDatabase db = Room.databaseBuilder(getActivity().getApplicationContext(),
                FileHistoryDatabase.class, "File").build();
        // long ID = db.FileDao().insert(new com.fyp.iShare.ui.downloads.DB.File("fileName", (long) 156446));
        files = db.FileDao().getAll();
    }

    void setAdapter() {
        recyclerView = binding.rvDownloads;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecyclerAdapter(files, this);
        //adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onFileClick(int position) {

    }
}