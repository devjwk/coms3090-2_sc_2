package com.example.androidexample;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class NotificationWebSocketManager {

    private static NotificationWebSocketManager instance;
    private NotificationSocketClient webSocketClient;
    private NotificationWebSocketListener listener;
    private static List<NotificationItem> notificationHistory = new ArrayList<>();

    private NotificationWebSocketManager() {}

    public static synchronized NotificationWebSocketManager getInstance() {
        if (instance == null) {
            instance = new NotificationWebSocketManager();
        }
        return instance;
    }
    public static void addNotificationToHistory(String message) {
        //1.New notification item
        NotificationItem newItem = new NotificationItem(message);
        //2.new notification located at the top of the list
        notificationHistory.add(0, newItem);
        //3.Make sure the list doesn't exceed 20 items
        if (notificationHistory.size() > 20) {
            notificationHistory.remove(notificationHistory.size() - 1);
        }
        Log.d("NotifyWS", "History updated. Current size: "+ notificationHistory.size());
    }

    public static List<NotificationItem> getNotificationHistory() {
        return notificationHistory;
    }

    public static void clearNotificationHistory() {
        notificationHistory.clear();
    }
    public void setNotificationWebSocketListener(NotificationWebSocketListener listener) {
        this.listener = listener;
    }

    public void removeNotificationWebSocketListener() {
        this.listener = null;
    }

    public boolean isConnected() {
        return webSocketClient != null && webSocketClient.isOpen();
    }

    public void connectWebSocket(String serverUrl) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            Log.d("NotifyWS", "Already connected, skipping reconnect");
            return;
        }

        if (webSocketClient != null) {
            try {
                webSocketClient.close();
            } catch (Exception ignored) {}
        }

        try {
            URI serverUri = URI.create(serverUrl);
            webSocketClient = new NotificationSocketClient(serverUri);
            webSocketClient.connect();
        } catch (Exception e) {
            Log.e("NotifyWS", "Connect error: " + e.getMessage());
            if (listener != null) {
                listener.onNotificationError(e);
            }
        }
    }

    public void disconnectWebSocket() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }

    private class NotificationSocketClient extends WebSocketClient {

        public NotificationSocketClient(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            Log.d("NotifyWS", "Connected");
            if (listener != null) {
                listener.onNotificationOpen(handshakedata);
            }
        }

        @Override
        public void onMessage(String message) {
            Log.d("NotifyWS", "Received: " + message);
            // Handle the notification message
            addNotificationToHistory(message);
            if (listener != null) {
                listener.onNotificationMessage(message);
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Log.d("NotifyWS", "Closed: " + reason);
            if (listener != null) {
                listener.onNotificationClose(code, reason, remote);
            }
        }

        @Override
        public void onError(Exception ex) {
            Log.e("NotifyWS", "Error: " + ex.getMessage());
            if (listener != null) {
                listener.onNotificationError(ex);
            }
        }
    }
}