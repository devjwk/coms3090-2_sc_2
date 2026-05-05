package com.example.androidexample;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationItem {
    private String message;
    private long timestamp;

    public NotificationItem(String message) {
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public String getMessage() { return message; }
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}