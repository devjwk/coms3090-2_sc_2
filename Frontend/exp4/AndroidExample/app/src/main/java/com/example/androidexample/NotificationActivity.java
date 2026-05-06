package com.example.androidexample;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {
    private LinearLayout notificationListContainer;
    private View tvEmptyNotifications;

    private int userId = -1;
    private final List<Integer> backendNotificationIds = new ArrayList<>();

    private static final String BASE_URL = "http://coms-3090-015.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        userId = getIntent().getIntExtra("USER_ID", -1);

        notificationListContainer = findViewById(R.id.notificationListContainer);
        tvEmptyNotifications = findViewById(R.id.tvEmptyNotifications);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        View btnClearAll = findViewById(R.id.btnClearAll);
        if (btnClearAll != null) {
            btnClearAll.setOnClickListener(v -> {
                clearBackendNotifications();
                NotificationWebSocketManager.clearNotificationHistory();
                refreshNotificationList();
            });
        }

        refreshNotificationList();
    }

    private void refreshNotificationList() {
        if (notificationListContainer == null) return;

        notificationListContainer.removeAllViews();
        backendNotificationIds.clear();

        List<NotificationItem> history = NotificationWebSocketManager.getNotificationHistory();
        if (history != null) {
            for (NotificationItem item : history) {
                addLocalNotificationView(item);
            }
        }

        if (userId != -1) {
            fetchBackendNotifications();
        } else {
            updateEmptyState();
        }
    }

    private void addLocalNotificationView(NotificationItem item) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_notification, null);
        TextView tvMessage = view.findViewById(R.id.tvNotificationMessage);
        TextView tvTime = view.findViewById(R.id.tvNotificationTime);

        try {
            JSONObject json = new JSONObject(item.getMessage());
            String type = json.optString("type", "GENERAL");
            String body = json.optString("message", "");
            String timestamp = json.optString("timestamp", "");

            if (tvMessage != null) {
                tvMessage.setText(NotificationFormatter.formatNotification(type, body, timestamp));
            }
        } catch (Exception e) {
            if (tvMessage != null) {
                tvMessage.setText(item.getMessage());
            }
        }

        if (tvTime != null) {
            tvTime.setText(item.getFormattedTime());
        }

        notificationListContainer.addView(view);
    }

    private void fetchBackendNotifications() {
        String url = BASE_URL + "/notifications/" + userId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);

                            int notificationId = obj.optInt("id", -1);
                            if (notificationId != -1) {
                                backendNotificationIds.add(notificationId);
                            }

                            String type = obj.optString("type", "GENERAL");
                            String message = obj.optString("message", "");
                            String timestamp = obj.optString("timestamp", "");

                            addBackendNotificationView(type, message, timestamp);
                        }
                    } catch (JSONException e) {
                        Log.e("NOTIFICATION_FETCH", "Failed to parse backend notifications", e);
                        Toast.makeText(this, "Failed to parse notifications", Toast.LENGTH_SHORT).show();
                    }

                    updateEmptyState();
                },
                error -> {
                    Log.e("NOTIFICATION_FETCH", error.toString());
                    if (error.networkResponse != null) {
                        Log.e("NOTIFICATION_FETCH", "status: " + error.networkResponse.statusCode);
                    }
                    updateEmptyState();
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void addBackendNotificationView(String type, String message, String timestamp) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_notification, null);
        TextView tvMessage = view.findViewById(R.id.tvNotificationMessage);
        TextView tvTime = view.findViewById(R.id.tvNotificationTime);

        if (tvMessage != null) {
            tvMessage.setText(NotificationFormatter.formatNotification(type, message, timestamp));
        }

        if (tvTime != null) {
            if (timestamp == null || timestamp.isEmpty()) {
                tvTime.setText("Stored notification");
            } else {
                tvTime.setText(timestamp);
            }
        }

        notificationListContainer.addView(view);
    }

    private void clearBackendNotifications() {
        if (backendNotificationIds.isEmpty()) return;

        for (Integer notificationId : new ArrayList<>(backendNotificationIds)) {
            String url = BASE_URL + "/notifications/" + notificationId;

            StringRequest request = new StringRequest(
                    Request.Method.DELETE,
                    url,
                    response -> Log.d("NOTIFICATION_DELETE", "Deleted notification " + notificationId),
                    error -> {
                        Log.e("NOTIFICATION_DELETE", error.toString());
                        if (error.networkResponse != null) {
                            Log.e("NOTIFICATION_DELETE", "status: " + error.networkResponse.statusCode);
                        }
                    }
            );

            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
        }

        backendNotificationIds.clear();
    }

    private void updateEmptyState() {
        if (tvEmptyNotifications == null || notificationListContainer == null) return;

        if (notificationListContainer.getChildCount() == 0) {
            tvEmptyNotifications.setVisibility(View.VISIBLE);
        } else {
            tvEmptyNotifications.setVisibility(View.GONE);
        }
    }
}