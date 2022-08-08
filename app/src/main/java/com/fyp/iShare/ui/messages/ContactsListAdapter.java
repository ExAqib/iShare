package com.fyp.iShare.ui.messages;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.fyp.iShare.MainActivity;
import com.fyp.iShare.R;

import java.util.List;

public class ContactsListAdapter extends RecyclerView.Adapter<ContactsListAdapter.ViewHolder> {

    private List<User> contacts;
    private OnContactListener onContactListener;

    public ContactsListAdapter(List<User> contacts, OnContactListener onContactListener) {
        this.contacts = contacts;
        this.onContactListener = onContactListener;
    }

    @Override
    public ContactsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.contact_item, parent, false);
      ViewHolder viewHolder = new ViewHolder(listItem, onContactListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.name.setText(contacts.get(position).nickname);
        /*holder.itemView.setOnClickListener{
            onContactListener.onWebhookClick(files.get(position).getWid());
        }*/
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public RelativeLayout relativeLayout;
        public TextView name;
        public TextView lastMessage;

        OnContactListener onContactListener;

        public ViewHolder(View itemView, OnContactListener onContactListener) {
            super(itemView);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.rl_contacts);
            this.name = itemView.findViewById(R.id.tv_userName);
            this.lastMessage = itemView.findViewById(R.id.tv_lastMessage);

            // allows clicks events to be caught
            this.onContactListener = onContactListener;
            itemView.setOnClickListener(this);

        }


        @Override
        public void onClick(View v) {
            onContactListener.onContactClick(getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    /*int getId(int position) {
        return files.get(position).getID();
    }*/


    // allows clicks events to be caught
    /*void setClickListener(OnContactListener onContactListener) {
        this.onContactListener = onContactListener;
    }*/

    public interface OnContactListener {
        void onContactClick(int position);
    }
}