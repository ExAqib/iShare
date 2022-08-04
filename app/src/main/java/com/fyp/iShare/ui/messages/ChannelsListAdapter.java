package com.fyp.iShare.ui.messages;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.fyp.iShare.R;

import java.util.List;

public class ChannelsListAdapter extends RecyclerView.Adapter<ChannelsListAdapter.ViewHolder> {

    private List<User> channels;
    private ChannelsListAdapter.OnChannelListener onChannelListener;

    public ChannelsListAdapter(List<User> channels, ChannelsListAdapter.OnChannelListener onChannelListener) {
        this.channels = channels;
        this.onChannelListener = onChannelListener;

    }

    @Override
    public ChannelsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.channels_item, parent, false);
        ChannelsListAdapter.ViewHolder viewHolder = new ChannelsListAdapter.ViewHolder(listItem, onChannelListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ChannelsListAdapter.ViewHolder holder, int position) {
        holder.name.setText(channels.get(position).nickname);

        /*holder.itemView.setOnClickListener{
            onChannelListener.onWebhookClick(files.get(position).getWid());
        }*/

    }


    @Override
    public int getItemCount() {
        return channels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public RelativeLayout relativeLayout;
        public TextView name;
        public TextView lastMessage;
        // public TextView domain;
        // public TextView requestMethod;
        // public TextView contentType;

        ChannelsListAdapter.OnChannelListener onChannelListener;

        public ViewHolder(View itemView, ChannelsListAdapter.OnChannelListener onChannelListener) {
            super(itemView);
            relativeLayout= (RelativeLayout) itemView.findViewById(R.id.rl_downloads);
            this.name = itemView.findViewById(R.id.tv_userName);
            this.lastMessage = itemView.findViewById(R.id.tv_lastMessage);

            // allows clicks events to be caught
            this.onChannelListener = onChannelListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onChannelListener.onChannelClick(getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    /*int getId(int position) {
        return files.get(position).getID();
    }*/


    // allows clicks events to be caught
    /*void setClickListener(OnChannelListener onChannelListener) {
        this.onChannelListener = onChannelListener;
    }*/

    public interface OnChannelListener {
        void onChannelClick(int position);
    }
}
