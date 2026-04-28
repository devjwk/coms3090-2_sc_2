package com.example.androidexample;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class NotificationItem {
    private String message;
    // Notification message
    private long timestamp;
    // Timestamp of the notification
    private boolean isRead;
    // Flag to indicate if the notification has been read

    public NotificationItem(String message){
        this.message = message;
        this.timestamp = System.currentTimeMillis();// Get the current timestamp
        this.isRead = false;// Initialize as unread
    }
    //--Getter and Setter Methods ---
    public String getMessage() {
        return message;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public boolean isRead() {
        return isRead;
    }
    //Methods for setting time and read status
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
