package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import org.java_websocket.handshake.ServerHandshake;

public class ChatActivity extends AppCompatActivity implements WebSocketListener {

    private Button sendBtn;
    private EditText msgEtx;
    private TextView msgTv;
    private ScrollView scrollView;
    private TextView tvChatTitle;

    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        currentUsername = getIntent().getStringExtra("USERNAME");
        if (currentUsername == null) currentUsername = "User";

        sendBtn     = findViewById(R.id.sendBtn);
        msgEtx      = findViewById(R.id.msgEdt);
        msgTv       = findViewById(R.id.tx1);
        scrollView  = findViewById(R.id.scrollView);
        tvChatTitle = findViewById(R.id.tvChatTitle);

        tvChatTitle.setText("Chat — " + currentUsername);

        WebSocketManager.getInstance().setWebSocketListener(ChatActivity.this);

        sendBtn.setOnClickListener(v -> {
            try {
                String message = msgEtx.getText().toString().trim();
                if (message.isEmpty()) return;
                WebSocketManager.getInstance().sendMessage(message);
                msgEtx.setText("");
            } catch (Exception e) {
                Log.d("ChatActivity", "Send error: " + e.getMessage());
            }
        });
    }

    @Override
    public void onWebSocketMessage(String message) {
        runOnUiThread(() -> {
            String current = msgTv.getText().toString();
            msgTv.setText(current.isEmpty() ? message : current + "\n" + message);
            scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
        });
    }

    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        String closedBy = remote ? "server" : "local";
        runOnUiThread(() -> {
            String current = msgTv.getText().toString();
            msgTv.setText(current + "\n---\nDisconnected (" + closedBy + "): " + reason);
        });
    }

    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {
        runOnUiThread(() -> msgTv.setText("Connected to AntiSocial chat ✓\n"));
    }

    @Override
    public void onWebSocketError(Exception ex) {
        runOnUiThread(() -> {
            String current = msgTv.getText().toString();
            msgTv.setText(current + "\nError: " + ex.getMessage());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WebSocketManager.getInstance().disconnectWebSocket();
    }
}