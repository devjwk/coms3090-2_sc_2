package com.example.androidexample;

import org.java_websocket.handshake.ServerHandshake;

public interface NotificationWebSocketListener {
    void onNotificationOpen(ServerHandshake handshakedata);
    void onNotificationMessage(String message);
    void onNotificationClose(int code, String reason, boolean remote);
    void onNotificationError(Exception ex);
}