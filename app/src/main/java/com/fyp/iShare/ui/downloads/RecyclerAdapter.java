package com.fyp.iShare.ui.downloads;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

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
        holder.name.setText(files.get(position));
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
        public TextView status;

        OnFileListener onFileListener;

        public ViewHolder(View itemView, OnFileListener onFileListener) {
            super(itemView);
            relativeLayout = itemView.findViewById(R.id.rl_downloads);
            this.name = itemView.findViewById(R.id.tv_fileName);
            this.status = itemView.findViewById(R.id.tv_status);

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
