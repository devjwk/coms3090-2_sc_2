package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.java_websocket.handshake.ServerHandshake;

public class ChatActivity extends AppCompatActivity implements WebSocketEventListener {

    private Button sendBtn, backBtn;
    private EditText msgEtx;
    private LinearLayout chatContainer;
    private ScrollView scrollView;
    private TextView tvChatWith;

    private String currentUsername;
    private String otherUsername;

    //private static final String WS_BASE = "ws://coms-3090-015.class.las.iastate.edu:8080/chat/";
    private static final String WS_BASE = "ws://10.0.2.2:8080/chat/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        currentUsername = getIntent().getStringExtra("USERNAME");
        otherUsername   = getIntent().getStringExtra("OTHER_USERNAME");

        if (currentUsername == null) currentUsername = "me";
        if (otherUsername == null)   otherUsername   = "User";

        sendBtn       = findViewById(R.id.sendBtn);
        backBtn       = findViewById(R.id.backBtn);
        msgEtx        = findViewById(R.id.msgEdt);
        chatContainer = findViewById(R.id.chatContainer);
        scrollView    = findViewById(R.id.scrollView);
        tvChatWith    = findViewById(R.id.tvChatWith);

        tvChatWith.setText(otherUsername);

        WebSocketClientManager.getInstance().setWebSocketEventListener(this);
        WebSocketClientManager.getInstance().connectWebSocket(WS_BASE + currentUsername);

        sendBtn.setOnClickListener(v -> {
            String message = msgEtx.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(this, "Message cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                WebSocketClientManager.getInstance().sendMessage(message);
                addMessageBubble(message, true);
                msgEtx.setText("");
            } catch (Exception e) {
                Log.d("ChatActivity", "Send error: " + e.getMessage());
            }
        });

        backBtn.setOnClickListener(v -> {
            WebSocketClientManager.getInstance().disconnectWebSocket();
            finish();
        });
    }

    private void addMessageBubble(String message, boolean isSent) {
        TextView bubble = new TextView(this);
        bubble.setText(message);
        bubble.setTextSize(15);
        bubble.setPadding(24, 16, 24, 16);
        bubble.setMaxWidth(900);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 8, 16, 8);

        if (isSent) {
            bubble.setBackgroundColor(0xFF7B6FFF);
            bubble.setTextColor(0xFFFFFFFF);
            params.gravity = Gravity.END;
        } else {
            bubble.setBackgroundColor(0xFF1E1E30);
            bubble.setTextColor(0xFFFFFFFF);
            params.gravity = Gravity.START;
        }

        bubble.setLayoutParams(params);
        chatContainer.addView(bubble);
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    @Override
    public void onWebSocketMessage(String message) {
        runOnUiThread(() -> addMessageBubble(message, false));
    }

    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {
        runOnUiThread(() -> addMessageBubble("Connected ✓", false));
    }

    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        runOnUiThread(() -> addMessageBubble("Disconnected", false));
    }

    @Override
    public void onWebSocketError(Exception ex) {
        runOnUiThread(() -> addMessageBubble("Error: " + ex.getMessage(), false));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WebSocketClientManager.getInstance().disconnectWebSocket();
    }
}