
package com.example.androidexample;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationFormatter {



        public static String formatNotification(String type, String message, String timestamp) {

            switch (type) {
                case "MATCH_CREATED":
                    return "🔥 Match! " + message;

                case "GROUP_JOIN":
                    return "👥 " + message;

                case "GROUP_LEAVE":
                    return "👋 " + message;

                default:
                    return message;
            }
        }
}