package com.example.androidexample;

import org.java_websocket.handshake.ServerHandshake;

public interface WebSocketEventListener {
    void onWebSocketOpen(ServerHandshake handshakedata);
    void onWebSocketMessage(String message);
    void onWebSocketClose(int code, String reason, boolean remote);
    void onWebSocketError(Exception ex);
}