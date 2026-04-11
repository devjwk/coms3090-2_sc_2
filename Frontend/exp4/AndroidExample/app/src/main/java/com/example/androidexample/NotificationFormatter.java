package com.example.androidexample;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationFormatter {

    public static String getNotificationTypeLabel(String type) {
        switch (type) {
            case "MATCH_CREATED":
                return "❤️ [Match]";
            case "GROUP_JOIN":
                return "👥 [Group]";
            case "GROUP_LEAVE":
                return "👋 [Group]";
            case "GROUP_MESSAGE":
                return "💬 [Message]";
            default:
                return "🔔 [General]";
        }
    }

    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String formatNotification(String type, String message, String timestamp) {
        String time = (timestamp == null || timestamp.isEmpty()) ? getCurrentTime() : timestamp;
        return "[" + time + "] " + getNotificationTypeLabel(type) + " " + message;
    }
}