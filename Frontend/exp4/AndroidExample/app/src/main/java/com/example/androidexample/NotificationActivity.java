package com.example.androidexample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private LinearLayout notificationListContainer;
    private TextView tvEmptyNotifications;
    private Button btnBack, btnClearAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // 1. view intialize
        notificationListContainer = findViewById(R.id.notificationListContainer);
        tvEmptyNotifications = findViewById(R.id.tvEmptyNotifications);
        btnBack = findViewById(R.id.btnBack);
        btnClearAll = findViewById(R.id.btnClearAll);

        // 2. back button
        btnBack.setOnClickListener(v -> finish());

        // 3. entire delete button
        btnClearAll.setOnClickListener(v -> {
            NotificationWebSocketManager.clearNotificationHistory();
            refreshNotificationList();
        });

        // 4. indicate list
        refreshNotificationList();
    }

    /**
     * getting stored notification history and add it to the screen
     */
    private void refreshNotificationList() {
        notificationListContainer.removeAllViews();
        List<NotificationItem> history = NotificationWebSocketManager.getNotificationHistory();

        if (history.isEmpty()) {
            tvEmptyNotifications.setVisibility(View.VISIBLE);
        } else {
            tvEmptyNotifications.setVisibility(View.GONE);
            for (NotificationItem item : history) {
                addNotificationView(item);
            }
        }
    }

    /**
     * make notification item(XML) and add it to the container
     */
    private void addNotificationView(NotificationItem item) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_notification, null);

        TextView tvMessage = view.findViewById(R.id.tvNotificationMessage);
        TextView tvTime = view.findViewById(R.id.tvNotificationTime);

        tvMessage.setText(item.getMessage());
        tvTime.setText(item.getFormattedTime());

        notificationListContainer.addView(view);
    }
}
