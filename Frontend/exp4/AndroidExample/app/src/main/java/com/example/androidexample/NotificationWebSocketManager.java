package com.example.androidexample;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class NotificationWebSocketManager {

    private static NotificationWebSocketManager instance;
    private NotificationSocketClient webSocketClient;
    private NotificationWebSocketListener listener;

    private NotificationWebSocketManager() {}

    public static synchronized NotificationWebSocketManager getInstance() {
        if (instance == null) {
            instance = new NotificationWebSocketManager();
        }
        return instance;
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