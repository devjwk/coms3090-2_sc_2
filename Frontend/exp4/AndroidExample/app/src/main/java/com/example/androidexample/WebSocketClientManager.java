package com.example.androidexample;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class WebSocketClientManager {

    private static WebSocketClientManager instance;
    private MyWebSocketClient webSocketClient;
    private WebSocketEventListener webSocketEventListener;

    // Queue messages received when no listener is active (e.g. chat screen is closed)
    private final List<String> messageQueue = new ArrayList<>();

    // Store current user ID so we can filter out our own echoes from the queue
    private int currentUserId = -1;

    private WebSocketClientManager() {}

    public static synchronized WebSocketClientManager getInstance() {
        if (instance == null) {
            instance = new WebSocketClientManager();
        }
        return instance;
    }

    public void setWebSocketEventListener(WebSocketEventListener listener) {
        this.webSocketEventListener = listener;
        // Flush any queued messages to the new listener
        if (listener != null && !messageQueue.isEmpty()) {
            Log.d("WebSocket", "Flushing " + messageQueue.size() + " queued messages to listener");
            List<String> queued = new ArrayList<>(messageQueue);
            messageQueue.clear();
            for (String msg : queued) {
                listener.onWebSocketMessage(msg);
            }
        }
    }

    public void removeWebSocketEventListener() {
        this.webSocketEventListener = null;
    }

    public void clearMessageQueue() {
        if (!messageQueue.isEmpty()) {
            Log.d("WebSocket", "Clearing " + messageQueue.size() + " queued messages (history already loaded)");
            messageQueue.clear();
        }
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    public int getCurrentUserId() {
        return currentUserId;
    }

    public void connectWebSocket(String serverUrl) {
        // Don't create a new connection if already connected to the same URL
        if (webSocketClient != null && webSocketClient.isOpen()) {
            Log.d("WebSocket", "Already connected, skipping reconnect");
            return;
        }
        // Close stale client before reconnecting
        if (webSocketClient != null) {
            try { webSocketClient.close(); } catch (Exception ignored) {}
        }
        try {
            URI serverUri = URI.create(serverUrl);
            webSocketClient = new MyWebSocketClient(serverUri);
            webSocketClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
            if (webSocketEventListener != null) {
                webSocketEventListener.onWebSocketError(e);
            }
        }
    }

    public boolean sendMessage(String message) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(message);
            Log.d("WebSocket", "Sent: " + message);
            return true;
        }
        Log.e("WebSocket", "Cannot send — not connected");
        return false;
    }

    public boolean isConnected() {
        return webSocketClient != null && webSocketClient.isOpen();
    }

    public void disconnectWebSocket() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }

    private class MyWebSocketClient extends WebSocketClient {

        public MyWebSocketClient(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            Log.d("WebSocket", "Connected");
            if (webSocketEventListener != null) {
                webSocketEventListener.onWebSocketOpen(handshakedata);
            }
        }

        @Override
        public void onMessage(String message) {
            Log.d("WebSocket", "Received: " + message);
            if (webSocketEventListener != null) {
                webSocketEventListener.onWebSocketMessage(message);
            } else {
                // No listener active — only queue messages from OTHER users
                if (shouldQueueMessage(message)) {
                    Log.d("WebSocket", "No listener, queuing message: " + message);
                    messageQueue.add(message);
                } else {
                    Log.d("WebSocket", "Skipping own echo/system message from queue: " + message);
                }
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Log.d("WebSocket", "Closed");
            if (webSocketEventListener != null) {
                webSocketEventListener.onWebSocketClose(code, reason, remote);
            }
        }

        @Override
        public void onError(Exception ex) {
            Log.d("WebSocket", "Error: " + ex.getMessage());
            if (webSocketEventListener != null) {
                webSocketEventListener.onWebSocketError(ex);
            }
        }
    }

    /**
     * Determines if a message should be queued (i.e., it's from another user, not an echo or system msg).
     */
    private boolean shouldQueueMessage(String message) {
        // Try to parse as JSON and check senderId
        try {
            JSONObject json = new JSONObject(message);
            int senderId = json.optInt("senderUserId",
                    json.optInt("senderId", json.optInt("sender_id", -1)));

            // If senderId matches our user, it's an echo — don't queue
            if (currentUserId > 0 && senderId == currentUserId) {
                return false;
            }

            // If it has content and is from someone else, queue it
            String content = json.optString("content", json.optString("message", ""));
            return !content.isEmpty();
        } catch (Exception e) {
            // Plain text message — filter system messages
            String lower = message.toLowerCase();
            if (lower.contains("has joined") || lower.contains("has left")
                    || lower.contains("welcome") || lower.contains("entered")
                    || lower.contains("connected") || lower.contains("disconnected")) {
                return false;
            }
            // Queue other plain text messages
            return true;
        }
    }
}