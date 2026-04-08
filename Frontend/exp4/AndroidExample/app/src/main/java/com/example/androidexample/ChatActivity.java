package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements WebSocketEventListener {

    private static final String TAG = "ChatActivity";

    private Button sendBtn, backBtn;
    private EditText msgEtx;
    private RecyclerView recyclerChat;
    private TextView tvChatWith;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList = new ArrayList<>();

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
        recyclerChat  = findViewById(R.id.recyclerChat);
        tvChatWith    = findViewById(R.id.tvChatWith);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerChat.setLayoutManager(layoutManager);
        chatAdapter = new ChatAdapter(messageList);
        recyclerChat.setAdapter(chatAdapter);

        backBtn.setOnClickListener(v -> {
            WebSocketClientManager.getInstance().disconnectWebSocket();
            finish();
        });

        if (otherUsername != null && !otherUsername.isEmpty() && !otherUsername.equals("Chat")) {
            tvChatWith.setText(otherUsername);
            loadChatHistoryThenConnect();
        } else if (otherUserId > 0) {
            tvChatWith.setText("Loading...");
            fetchOtherUserName(otherUserId);
        } else {
            Toast.makeText(this, "No chat partner specified", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private void loadChatHistoryThenConnect() {
        String url = BASE_URL + "/chat/history/" + currentUserId + "/" + otherUserId;
        Log.d(TAG, "Fetching chat history: " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONArray arr = new JSONArray(response);
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);

                            long id = obj.optLong("id", 0);
                            int senderId = obj.optInt("senderId", obj.optInt("sender_id", 0));
                            int receiverId = obj.optInt("receiverId", obj.optInt("receiver_id", 0));
                            String content = obj.optString("content", obj.optString("message", ""));
                            String timestamp = obj.optString("timestamp", obj.optString("sentAt", ""));

                            boolean isSent = (senderId == currentUserId);

                            ChatMessage msg = new ChatMessage(id, senderId, receiverId, content, timestamp, isSent);
                            messageList.add(msg);
                        }
                        chatAdapter.notifyDataSetChanged();
                        scrollToBottom();
                        Log.d(TAG, "Loaded " + arr.length() + " history messages");
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing chat history: " + e.getMessage());
                    }
                    connectWebSocket();
                },
                error -> {
                    Log.w(TAG, "Chat history not available (backend may not have it yet): " + error);
                    connectWebSocket();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
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
                appendMessage(message, true);
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
                    loadChatHistoryThenConnect();
                },
                error -> {
                    Log.e(TAG, "Failed to fetch user name: " + error);
                    otherUsername = "User " + userId;
                    tvChatWith.setText(otherUsername);
                    loadChatHistoryThenConnect();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }


    private void appendMessage(String content, boolean isSent) {
        ChatMessage msg = new ChatMessage(content, isSent);
        messageList.add(msg);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
    }

    private void scrollToBottom() {
        if (!messageList.isEmpty()) {
            recyclerChat.scrollToPosition(messageList.size() - 1);
        }
    }

    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {
        Log.d(TAG, "WebSocket OPEN — HTTP status: " + handshakedata.getHttpStatus());
        runOnUiThread(() -> appendMessage("Connected ✓", false));
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

            appendMessage(message, false);
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