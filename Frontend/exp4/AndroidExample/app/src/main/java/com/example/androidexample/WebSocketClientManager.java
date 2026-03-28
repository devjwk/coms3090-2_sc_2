package com.example.androidexample;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class WebSocketClientManager {

    private static WebSocketClientManager instance;
    private MyWebSocketClient webSocketClient;
    private WebSocketEventListener webSocketEventListener;

    private WebSocketClientManager() {}

    public static synchronized WebSocketClientManager getInstance() {
        if (instance == null) {
            instance = new WebSocketClientManager();
        }
        return instance;
    }

    public void setWebSocketEventListener(WebSocketEventListener listener) {
        this.webSocketEventListener = listener;
    }

    public void removeWebSocketEventListener() {
        this.webSocketEventListener = null;
    }

    public void connectWebSocket(String serverUrl) {
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

    public void sendMessage(String message) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(message);
        }
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
}