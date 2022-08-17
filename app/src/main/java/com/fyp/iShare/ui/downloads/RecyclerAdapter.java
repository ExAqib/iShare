package com.fyp.iShare.ui.downloads;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.fyp.iShare.DownloadedFiles;
import com.fyp.iShare.R;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private final List<String> files;
    private final OnFileListener onFileListener;

    public RecyclerAdapter(List<String> files, OnFileListener onFileListener) {
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
        if(files.size()<=0){
            String no = "No Files Downloaded Yet";
            holder.name.setText(no);
        }else{
            holder.name.setText(files.get(position));
            holder.size.setText(DownloadedFiles.fileSize.get(position));

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
}
