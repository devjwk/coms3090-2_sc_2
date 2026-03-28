package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.java_websocket.handshake.ServerHandshake;

public class WebSocketConnectActivity extends AppCompatActivity implements WebSocketEventListener {

    private Button connectBtn;
    private EditText serverEtx, usernameEtx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_websocket_connect);

        connectBtn = findViewById(R.id.connectBtn);
        serverEtx = findViewById(R.id.serverEdt);
        usernameEtx = findViewById(R.id.unameEdt);

        WebSocketClientManager.getInstance().setWebSocketEventListener(this);

        connectBtn.setOnClickListener(view -> {
            String serverUrl = serverEtx.getText().toString().trim();
            String username = usernameEtx.getText().toString().trim();

            if (serverUrl.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "Enter server URL and username", Toast.LENGTH_SHORT).show();
                return;
            }

            String fullUrl = serverUrl + username;
            WebSocketClientManager.getInstance().connectWebSocket(fullUrl);

            Intent intent = new Intent(this, WebSocketNotificationActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });
    }

    @Override
    public void onWebSocketMessage(String message) {}

    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {}

    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {
        runOnUiThread(() ->
                Toast.makeText(this, "WebSocket Connected", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onWebSocketError(Exception ex) {
        runOnUiThread(() ->
                Toast.makeText(this, "Connection Error", Toast.LENGTH_SHORT).show()
        );
    }
}