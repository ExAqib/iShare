package com.fyp.iShare.ui.devices;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.iShare.R;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private List<String> devices;
    private OnDeviceListener onDeviceListener;

    public RecyclerAdapter(List<String> devices, OnDeviceListener onDeviceListener) {
        this.devices = devices;
        this.onDeviceListener = onDeviceListener;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.devices_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem, onDeviceListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.name.setText(devices.get(position));

        /*holder.itemView.setOnClickListener{
            onDeviceListener.onWebhookClick(devices.get(position).getWid());
        }*/

    }


    @Override
    public int getItemCount() {
        return devices.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public RelativeLayout relativeLayout;
        public TextView name;

        OnDeviceListener onDeviceListener;

        public ViewHolder(View itemView, OnDeviceListener onDeviceListener) {
            super(itemView);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.rl_deviceItems);
            this.name = itemView.findViewById(R.id.tv_deviceName);

            // allows clicks events to be caught
            this.onDeviceListener = onDeviceListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onDeviceListener.onDeviceClick(getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    /*int getId(int position) {
        return devices.get(position).getID();
    }*/


    // allows clicks events to be caught
    /*void setClickListener(OnDeviceListener onDeviceListener) {
        this.onDeviceListener = onDeviceListener;
    }*/

    public interface OnDeviceListener {
        void onDeviceClick(int position);
    }
}
