package com.fyp.iShare.ui.messages;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.fyp.iShare.R;

import java.util.List;


public class MessageListAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 2;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 1;

    private final Context mContext;
    private final List<UserMessage> mMessageList;

    public MessageListAdapter(Context context, List<UserMessage> messageList) {
        mContext = context;
        mMessageList = messageList;
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        UserMessage message = mMessageList.get(position);

        if (message.getSender().getNickname().equals("7odaifa")) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    // Inflates the appropriate layout according to the ViewType.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.sent_message_item, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.received_message_item, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        UserMessage message = mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, dateText;

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
            dateText = itemView.findViewById(R.id.text_date_me);

        }

        void bind(UserMessage message) {
            messageText.setText(message.getMessage());

            // Format the stored timestamp into a readable String using method.
            //timeText.setText(message.getCreatedAt());
            timeText.setText("88");

            dateText.setVisibility(View.GONE);

        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText, dateText;
        ImageView profileImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
            nameText = itemView.findViewById(R.id.text_message_name);
            dateText = itemView.findViewById(R.id.text_date_other);
            profileImage = itemView.findViewById(R.id.image_message_profile);
        }

        void bind(UserMessage message) {
            messageText.setText(message.getMessage());

            // Format the stored timestamp into a readable String using method.
            //timeText.setText(message.getCreatedAt());
            timeText.setText("1125");

            nameText.setText(message.getSender().getNickname());

            dateText.setVisibility(View.GONE);

            // Insert the profile image from the URL into the ImageView.
            //Utils.displayRoundImageFromUrl(mContext, message.getSender().getProfileUrl(), profileImage);
        }
    }
}