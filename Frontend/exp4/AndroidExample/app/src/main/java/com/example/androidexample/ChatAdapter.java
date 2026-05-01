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

    public interface OnMessageContextActionListener {
        void onMessageContextAction(View anchorView, ChatMessage message, int position);
    }

    private final List<ChatMessage> messages;
    private final OnMessageContextActionListener contextActionListener;
    private final boolean showMessageMetadata;

    public ChatAdapter(List<ChatMessage> messages) {
        this(messages, null, false);
    }

    public ChatAdapter(List<ChatMessage> messages, OnMessageContextActionListener contextActionListener) {
        this(messages, contextActionListener, false);
    }

    public ChatAdapter(List<ChatMessage> messages,
                       OnMessageContextActionListener contextActionListener,
                       boolean showMessageMetadata) {
        this.messages = messages;
        this.contextActionListener = contextActionListener;
        this.showMessageMetadata = showMessageMetadata;
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

        // --- From Document 1: Bind context actions ---
        bindContextActions(holder, msg);

        // Sender name display (both versions agree)
        if (!msg.isSent() && msg.getSenderName() != null && !msg.getSenderName().isEmpty()) {
            holder.tvSenderName.setVisibility(View.VISIBLE);
            holder.tvSenderName.setText(msg.getSenderName());
        } else {
            holder.tvSenderName.setVisibility(View.GONE);
        }

        // Bubble alignment and styling (both versions agree)
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

        // --- From Document 1: Moderation styling ---
        if (msg.isModeratedRemoved()) {
            holder.tvMessage.setAlpha(0.7f);
            holder.tvMessage.setTextColor(0xFFC8C8D4);
            holder.tvMessage.setTypeface(null, android.graphics.Typeface.ITALIC);
        } else {
            holder.tvMessage.setAlpha(1f);
            holder.tvMessage.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        // --- From Document 1: Metadata/timestamp logic (supports both modes) ---
        String metadataText = buildMetadataText(msg);
        if (!metadataText.isEmpty()) {
            holder.tvTimestamp.setVisibility(View.VISIBLE);
            holder.tvTimestamp.setText(metadataText);
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

    // --- From Document 1: Builds timestamp or full metadata string ---
    private String buildMetadataText(ChatMessage msg) {
        String timestamp = msg.getTimestamp() == null ? "" : msg.getTimestamp().trim();
        if (!showMessageMetadata) {
            return timestamp;
        }

        StringBuilder builder = new StringBuilder();
        if (msg.getId() > 0) {
            builder.append("ID #").append(msg.getId());
        }
        if (!timestamp.isEmpty()) {
            if (builder.length() > 0) {
                builder.append(" • ");
            }
            builder.append(timestamp);
        }
        return builder.toString();
    }

    // --- From Document 1: Long-press / context-click handling ---
    private void bindContextActions(@NonNull ChatViewHolder holder, ChatMessage msg) {
        if (contextActionListener == null || msg.isModeratedRemoved()) {
            holder.tvMessage.setOnLongClickListener(null);
            holder.tvMessage.setOnContextClickListener(null);
            return;
        }

        holder.tvMessage.setOnLongClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) {
                return false;
            }
            contextActionListener.onMessageContextAction(v, msg, adapterPos);
            return true;
        });

        holder.tvMessage.setOnContextClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) {
                return false;
            }
            contextActionListener.onMessageContextAction(v, msg, adapterPos);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ViewHolder uses public visibility (from Document 1) for broader access
    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvSenderName;
        TextView tvMessage;
        TextView tvTimestamp;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}