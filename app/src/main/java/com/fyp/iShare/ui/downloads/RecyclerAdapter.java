package com.fyp.iShare.ui.downloads;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.fyp.iShare.R;
import com.fyp.iShare.ui.downloads.DB.File;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private final List<File> files;
    private final OnFileListener onFileListener;

    public RecyclerAdapter(List<File> files, OnFileListener onFileListener) {
        this.files = files;
        this.onFileListener = onFileListener;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.file_history_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem, onFileListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (files.size() <= 0) {
            String no = "No Files Downloaded Yet";
            holder.name.setText(no);
        } else {

            holder.name.setText(files.get(position).getName());

            long fileSize = files.get(position).getSize();
            holder.size.setText(humanReadableByte(fileSize));

            holder.type.setImageResource(extractFileType(files.get(position).getName()));
        }
        // TODO: 8/15/2022 bind other file info
    }


    @Override
    public int getItemCount() {
        return files.size();
    }

    public interface OnFileListener {
        void onFileClick(int position);
    }

    // convenience method for getting data at click position
    /*int getId(int position) {
        return files.get(position).getID();
    }*/


    // allows clicks events to be caught
    /*void setClickListener(OnFileListener onFileListener) {
        this.onFileListener = onFileListener;
    }*/

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public RelativeLayout relativeLayout;
        public TextView name;
        public TextView size;
        public TextView status;
        public ImageView type;

        OnFileListener onFileListener;

        public ViewHolder(View itemView, OnFileListener onFileListener) {
            super(itemView);
            relativeLayout = itemView.findViewById(R.id.rl_downloads);
            this.name = itemView.findViewById(R.id.tv_fileName);
            this.size = itemView.findViewById(R.id.tv_fileSize);
            this.status = itemView.findViewById(R.id.tv_status);
            this.type = itemView.findViewById(R.id.img_FileType);

            // allows clicks events to be caught
            this.onFileListener = onFileListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onFileListener.onFileClick(getAdapterPosition());
        }
    }

    private String humanReadableByte(long fileSize) {
        long absB = fileSize == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(fileSize);
        if (absB < 1024) {
            return fileSize + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(fileSize);
        return String.format("%.1f %cB", value / 1024.0, ci.current());
    }

    private int extractFileType(String name) {

        String[] fileArray = name.split("\\.");

        Log.d("tag", "extractFileType: " + fileArray[fileArray.length - 1]);

        switch (fileArray[fileArray.length - 1]) {
            case "txt": {
                return R.drawable.draft_48px;
            }
            case "mp3":
            case "m4a": {
                return R.drawable.music_note_48px;
            }
            case "png":
            case "jpg":
            case "jpeg": {
                return R.drawable.image_48px;
            }
            case "mp4": {
                return R.drawable.movie_48px;
            }
            case "rar": {
                return R.drawable.folder_zip_48px;
            }
            default:
                return R.drawable.draft_48px;
        }

    }

}
