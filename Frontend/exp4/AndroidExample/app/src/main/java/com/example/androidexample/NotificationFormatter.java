
package com.example.androidexample;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationFormatter {

    public static String getNotificationType(String message) {
        String lowerMessage = message.toLowerCase();

        if (lowerMessage.contains("match")) {
            return "❤️[Match]";
        } else if (lowerMessage.contains("group") || lowerMessage.contains("joined")) {
            return "👥[Group]";
        } else if (lowerMessage.contains("message")) {
            return "💬[Message]";
        } else {
            return "[General]";
        }
    }

    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String formatNotification(String message) {
        return "[" + getCurrentTime() + "] " + getNotificationType(message) + " " + message;
    }
}