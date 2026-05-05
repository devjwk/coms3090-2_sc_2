package com.example.androidexample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {
    private LinearLayout notificationListContainer;
    private View tvEmptyNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        notificationListContainer = findViewById(R.id.notificationListContainer);
        tvEmptyNotifications = findViewById(R.id.tvEmptyNotifications);


        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        View btnClearAll = findViewById(R.id.btnClearAll);
        if (btnClearAll != null) {
            btnClearAll.setOnClickListener(v -> {
                NotificationWebSocketManager.clearNotificationHistory();
                refreshNotificationList();
            });
        }

        refreshNotificationList();
    }

    private void refreshNotificationList() {
        if (notificationListContainer == null) return;

        notificationListContainer.removeAllViews();
        List<NotificationItem> history = NotificationWebSocketManager.getNotificationHistory();

        if (history == null || history.isEmpty()) {
            if (tvEmptyNotifications != null) tvEmptyNotifications.setVisibility(View.VISIBLE);
        } else {
            if (tvEmptyNotifications != null) tvEmptyNotifications.setVisibility(View.GONE);
            for (NotificationItem item : history) {
                addNotificationView(item);
            }
        }
    }

    private void addNotificationView(NotificationItem item) {

        View view = LayoutInflater.from(this).inflate(R.layout.item_notification, null);
        TextView tvMessage = view.findViewById(R.id.tvNotificationMessage);
        TextView tvTime = view.findViewById(R.id.tvNotificationTime);

        try {

            org.json.JSONObject json = new org.json.JSONObject(item.getMessage());
            String type = json.optString("type", "GENERAL");
            String body = json.optString("message", "");
            String timestamp = json.optString("timestamp", "");

            if (tvMessage != null) {
                tvMessage.setText(NotificationFormatter.formatNotification(type, body, timestamp));
            }
        } catch (Exception e) {
            if (tvMessage != null) tvMessage.setText(item.getMessage());
        }

        if (tvTime != null) {
            tvTime.setText(item.getFormattedTime());
        }

        notificationListContainer.addView(view);
    }
}