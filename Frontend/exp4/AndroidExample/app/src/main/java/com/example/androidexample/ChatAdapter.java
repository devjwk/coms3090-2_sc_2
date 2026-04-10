package com.example.androidexample;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isSent() ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        holder.tvMessage.setText(msg.getContent());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        if (msg.isSent()) {
            params.gravity = Gravity.END;
            holder.tvMessage.setBackgroundResource(R.drawable.bubble_sent);
            holder.tvMessage.setTextColor(0xFFFFFFFF);
        } else {
            params.gravity = Gravity.START;
            holder.tvMessage.setBackgroundResource(R.drawable.bubble_received);
            holder.tvMessage.setTextColor(0xFFFFFFFF);
        }

        holder.tvMessage.setLayoutParams(params);

        if (msg.getTimestamp() != null && !msg.getTimestamp().isEmpty()) {
            holder.tvTimestamp.setVisibility(View.VISIBLE);
            holder.tvTimestamp.setText(msg.getTimestamp());
            LinearLayout.LayoutParams tsParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            tsParams.gravity = msg.isSent() ? Gravity.END : Gravity.START;
            holder.tvTimestamp.setLayoutParams(tsParams);
        } else {
            holder.tvTimestamp.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        TextView tvTimestamp;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}

