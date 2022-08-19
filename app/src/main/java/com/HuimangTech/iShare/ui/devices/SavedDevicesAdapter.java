package com.HuimangTech.iShare.ui.devices;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.HuimangTech.iShare.R;

import java.util.List;

public class SavedDevicesAdapter extends RecyclerView.Adapter<SavedDevicesAdapter.ViewHolder> {

    private final List<String> devices;
    private final OnDeviceListener onDeviceListener;

    public SavedDevicesAdapter(List<String> devices, OnDeviceListener onDeviceListener) {
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

    public interface OnDeviceListener {
        void onDeviceClick(int position, String deviceName, String RecyclerView);
    }

    // convenience method for getting data at click position
    /*int getId(int position) {
        return devices.get(position).getID();
    }*/


    // allows clicks events to be caught
    /*void setClickListener(OnDeviceListener onDeviceListener) {
        this.onDeviceListener = onDeviceListener;
    }*/

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public LinearLayout linearLayout;
        public TextView name;

        OnDeviceListener onDeviceListener;

        public ViewHolder(View itemView, OnDeviceListener onDeviceListener) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.li_deviceItems);
            this.name = itemView.findViewById(R.id.tv_deviceName);

            // allows clicks events to be caught
            this.onDeviceListener = onDeviceListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            TextView deviceName = view.findViewById(R.id.tv_deviceName);
            onDeviceListener.onDeviceClick(getAdapterPosition(), deviceName.getText().toString(), "SavedDevicesRecyclerView");

            /*if(RecycleView.equals(AvailableDevicesRecyclerView)){
                onDeviceListener.onDeviceClick(getAdapterPosition(),deviceName.getText().toString(),"SavedDevicesRecyclerView");

            }else if (RecycleView.equals(SavedDevicesRecyclerView)){
                onDeviceListener.onDeviceClick(getAdapterPosition(),deviceName.getText().toString(),"SavedDevicesRecyclerView");
            }*/

        }
    }
}
