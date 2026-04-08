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

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

public class ChatActivity extends AppCompatActivity implements WebSocketEventListener {

    private static final String TAG = "ChatActivity";

    private Button sendBtn, backBtn;
    private EditText msgEtx;
    private LinearLayout chatContainer;
    private ScrollView scrollView;
    private TextView tvChatWith;

    private int currentUserId;
    private int otherUserId;
    private String otherUsername;
    private String lastSentMessage = null;

    private static final String WS_BASE = "ws://coms-3090-015.class.las.iastate.edu:8080/chat/";
    private static final String BASE_URL = "http://coms-3090-015.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        currentUserId = getIntent().getIntExtra("USER_ID", 1);
        otherUserId   = getIntent().getIntExtra("OTHER_USER_ID", -1);
        otherUsername  = getIntent().getStringExtra("OTHER_USERNAME");

        sendBtn       = findViewById(R.id.sendBtn);
        backBtn       = findViewById(R.id.backBtn);
        msgEtx        = findViewById(R.id.msgEdt);
        chatContainer = findViewById(R.id.chatContainer);
        scrollView    = findViewById(R.id.scrollView);
        tvChatWith    = findViewById(R.id.tvChatWith);

        backBtn.setOnClickListener(v -> {
            WebSocketClientManager.getInstance().disconnectWebSocket();
            finish();
        });

        if (otherUsername != null && !otherUsername.isEmpty() && !otherUsername.equals("Chat")) {
            tvChatWith.setText(otherUsername);
            connectWebSocket();
        } else if (otherUserId > 0) {
            tvChatWith.setText("Loading...");
            fetchOtherUserName(otherUserId);
        } else {
            Toast.makeText(this, "No chat partner specified", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void connectWebSocket() {
        WebSocketClientManager.getInstance().disconnectWebSocket();

        WebSocketClientManager.getInstance().setWebSocketEventListener(this);

        String wsUrl = WS_BASE + currentUserId;
        Log.d(TAG, "Connecting WebSocket: " + wsUrl);

        WebSocketClientManager.getInstance().connectWebSocket(wsUrl);

        sendBtn.setOnClickListener(v -> {
            String message = msgEtx.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(this, "Message cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }



            try {
                lastSentMessage = message;
                WebSocketClientManager.getInstance().sendMessage(message);
                addMessageBubble(message, true);
                msgEtx.setText("");
            } catch (Exception e) {
                Log.e(TAG, "Send error: " + e.getMessage());
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchOtherUserName(int userId) {
        String url = BASE_URL + "/users/" + userId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject user = new JSONObject(response);
                        String name = user.optString("name", "");
                        if (name.isEmpty()) name = user.optString("displayName", "");
                        if (name.isEmpty()) name = "User " + userId;
                        otherUsername = name;
                    } catch (Exception e) {
                        otherUsername = "User " + userId;
                    }
                    tvChatWith.setText(otherUsername);
                    connectWebSocket();
                },
                error -> {
                    Log.e(TAG, "Failed to fetch user name: " + error);
                    otherUsername = "User " + userId;
                    tvChatWith.setText(otherUsername);
                    connectWebSocket();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void addMessageBubble(String message, boolean isSent) {
        TextView bubble = new TextView(this);
        bubble.setText(message);
        bubble.setTextSize(15);
        bubble.setPadding(36, 20, 36, 20);
        bubble.setMaxWidth(900);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 8, 16, 8);

        if (isSent) {
            bubble.setBackgroundResource(R.drawable.bubble_sent);
            bubble.setTextColor(0xFFFFFFFF);
            params.gravity = Gravity.END;
        } else {
            bubble.setBackgroundResource(R.drawable.bubble_received);
            bubble.setTextColor(0xFFFFFFFF);
            params.gravity = Gravity.START;
        }

        bubble.setLayoutParams(params);
        chatContainer.addView(bubble);
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {
        Log.d(TAG, "WebSocket OPEN — HTTP status: " + handshakedata.getHttpStatus());
        runOnUiThread(() -> addMessageBubble("Connected ✓", false));
    }

    @Override
    public void onWebSocketMessage(String message) {
        runOnUiThread(() -> {
            Log.d(TAG, "WS message: " + message);

            if (lastSentMessage != null) {
                if (message.equals(lastSentMessage) || message.contains(lastSentMessage)) {
                    lastSentMessage = null;
                    return;
                }
            }

            String lower = message.toLowerCase();
            if (lower.contains("has joined") || lower.contains("has left")
                    || lower.contains("welcome") || lower.contains("entered")
                    || lower.contains("connected") || lower.contains("disconnected")) {
                Log.d(TAG, "Filtered system msg: " + message);
                return;
            }

            addMessageBubble(message, false);
        });
    }

    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        Log.d(TAG, "WebSocket CLOSED: code=" + code + " reason=" + reason + " remote=" + remote);
        runOnUiThread(() ->
            Toast.makeText(this, "Chat disconnected", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onWebSocketError(Exception ex) {
        Log.e(TAG, "WebSocket ERROR: " + (ex != null ? ex.getMessage() : "unknown"));
        runOnUiThread(() ->
            Toast.makeText(this, "Connection error: " + (ex != null ? ex.getMessage() : "unknown"),
                    Toast.LENGTH_LONG).show()
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WebSocketClientManager.getInstance().removeWebSocketEventListener();
        WebSocketClientManager.getInstance().disconnectWebSocket();
    }
}