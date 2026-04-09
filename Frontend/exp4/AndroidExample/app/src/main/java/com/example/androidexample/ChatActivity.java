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
    private int conversationId = -1;
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
            WebSocketClientManager.getInstance().removeWebSocketEventListener();
            finish();
        });

        if (otherUsername != null && !otherUsername.isEmpty() && !otherUsername.equals("Chat")) {
            tvChatWith.setText(otherUsername);
            fetchConversationId();
        } else if (otherUserId > 0) {
            tvChatWith.setText("Loading...");
            fetchOtherUserName(otherUserId);
        } else {
            Toast.makeText(this, "No chat partner specified", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private void fetchConversationId() {
        String url = BASE_URL + "/conversations/direct";
        Log.d(TAG, "POST " + url + " with user1Id=" + currentUserId + " user2Id=" + otherUserId);

        JSONObject body = new JSONObject();
        try {
            body.put("user1Id", currentUserId);
            body.put("user2Id", otherUserId);
        } catch (Exception e) {
            Log.e(TAG, "Error building request body", e);
            connectWebSocket();
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d(TAG, "Conversation response: " + response);
                    try {
                        JSONObject json = new JSONObject(response);
                        conversationId = json.optInt("conversationId",
                                json.optInt("id",
                                        json.optInt("conversation_id", -1)));
                        Log.d(TAG, "Got conversationId = " + conversationId);
                    } catch (Exception e) {
                        try {
                            conversationId = Integer.parseInt(response.trim());
                            Log.d(TAG, "Parsed conversationId from plain response = " + conversationId);
                        } catch (NumberFormatException nfe) {
                            Log.e(TAG, "Could not parse conversationId from: " + response);
                        }
                    }
                    loadChatHistoryThenConnect();
                },
                error -> {
                    Log.e(TAG, "Failed to get conversationId: " + error);
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Status: " + error.networkResponse.statusCode
                                + " Body: " + new String(error.networkResponse.data));
                    }
                    Toast.makeText(this, "Could not establish conversation", Toast.LENGTH_SHORT).show();
                    connectWebSocket();
                }) {
            @Override
            public byte[] getBody() {
                return body.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void loadChatHistoryThenConnect() {
        if (conversationId <= 0) {
            Log.w(TAG, "No valid conversationId (" + conversationId + "), skipping history load");
            Log.w(TAG, "This means POST /conversations/direct did not return a valid ID");
            connectWebSocket();
            return;
        }

        String primaryUrl = BASE_URL + "/messages/conversation/" + conversationId + "/user/" + currentUserId;
        Log.d(TAG, "GET (primary) " + primaryUrl);

        StringRequest request = new StringRequest(Request.Method.GET, primaryUrl,
                response -> {
                    Log.d(TAG, "Primary history response (" + response.length() + " chars): "
                            + (response.length() > 200 ? response.substring(0, 200) + "..." : response));
                    int loaded = parseAndLoadMessages(response);
                    if (loaded == 0) {
                        Log.d(TAG, "Primary URL returned 0 messages, trying fallback URL...");
                        loadChatHistoryFallback();
                    } else {
                        Log.d(TAG, "Loaded " + loaded + " history messages from primary URL");
                        connectWebSocket();
                    }
                },
                error -> {
                    Log.e(TAG, "Primary history URL FAILED: " + error);
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Primary error status: " + error.networkResponse.statusCode
                                + " body: " + new String(error.networkResponse.data));
                    }
                    Log.d(TAG, "Trying fallback history URL...");
                    loadChatHistoryFallback();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }


    private void loadChatHistoryFallback() {
        String fallbackUrl = BASE_URL + "/messages/conversation/" + conversationId;
        Log.d(TAG, "GET (fallback) " + fallbackUrl);

        StringRequest request = new StringRequest(Request.Method.GET, fallbackUrl,
                response -> {
                    Log.d(TAG, "Fallback history response (" + response.length() + " chars): "
                            + (response.length() > 200 ? response.substring(0, 200) + "..." : response));
                    int loaded = parseAndLoadMessages(response);
                    if (loaded == 0) {
                        Log.d(TAG, "Fallback also returned 0 messages — conversation is likely new (no messages yet)");
                    } else {
                        Log.d(TAG, "Loaded " + loaded + " history messages from fallback URL");
                    }
                    connectWebSocket();
                },
                error -> {
                    Log.e(TAG, "Fallback history URL also FAILED: " + error);
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Fallback error status: " + error.networkResponse.statusCode
                                + " body: " + new String(error.networkResponse.data));
                    }
                    Log.d(TAG, "Both history URLs failed — conversation may be new");
                    connectWebSocket();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }


    private int parseAndLoadMessages(String response) {
        try {
            JSONArray arr = new JSONArray(response);
            if (arr.length() > 0) {
                Log.d(TAG, "FIRST MSG JSON: " + arr.getJSONObject(0).toString());
                Log.d(TAG, "currentUserId = " + currentUserId);
            }
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                long id = obj.optLong("messageId", obj.optLong("id", 0));
                String content = obj.optString("content", obj.optString("message", ""));
                String timestamp = obj.optString("sentAt", obj.optString("timestamp", ""));

                int senderId = extractUserId(obj, "sender", "senderUserId", "senderId", "sender_id", "fromId");
                int receiverId = extractUserId(obj, "receiver", "receiverUserId", "receiverId", "receiver_id", "toId");

                boolean isSent = (senderId == currentUserId);
                Log.d(TAG, "Msg #" + i + " senderId=" + senderId + " currentUserId=" + currentUserId + " isSent=" + isSent);

                ChatMessage msg = new ChatMessage(id, senderId, receiverId, content, timestamp, isSent);
                messageList.add(msg);
            }
            chatAdapter.notifyDataSetChanged();
            scrollToBottom();
            return arr.length();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing chat history: " + e.getMessage());
            return 0;
        }
    }


    private int extractUserId(JSONObject obj, String objectKey, String... flatKeys) {
        for (String key : flatKeys) {
            int val = obj.optInt(key, -1);
            if (val > 0) return val;
        }
        if (obj.has(objectKey) && !obj.isNull(objectKey)) {
            try {
                JSONObject nested = obj.getJSONObject(objectKey);
                int val = nested.optInt("id", -1);
                if (val > 0) return val;
                val = nested.optInt("userId", -1);
                if (val > 0) return val;
                val = nested.optInt("user_id", -1);
                if (val > 0) return val;
            } catch (Exception ignored) {}
        }
        return 0;
    }

    private void connectWebSocket() {
        WebSocketClientManager.getInstance().setWebSocketEventListener(this);

        String wsUrl = WS_BASE + currentUserId;

        if (WebSocketClientManager.getInstance().isConnected()) {
            Log.d(TAG, "WebSocket already connected (from HomeActivity), reusing");
        } else {
            Log.d(TAG, "WebSocket not connected, connecting now: " + wsUrl);
            WebSocketClientManager.getInstance().connectWebSocket(wsUrl);
        }

        sendBtn.setOnClickListener(v -> {
            String message = msgEtx.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(this, "Message cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (conversationId <= 0) {
                Toast.makeText(this, "Chat not ready — no conversationId", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject jsonMsg = new JSONObject();
                jsonMsg.put("conversationId", conversationId);
                jsonMsg.put("content", message);

                Log.d(TAG, "Sending: " + jsonMsg);

                boolean sent = WebSocketClientManager.getInstance().sendMessage(jsonMsg.toString());
                if (sent) {
                    lastSentMessage = message;
                    appendMessage(message, true);
                    msgEtx.setText("");
                } else {
                    Toast.makeText(this, "Not connected. Reconnecting...", Toast.LENGTH_SHORT).show();
                    WebSocketClientManager.getInstance().connectWebSocket(WS_BASE + currentUserId);
                }
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
                    fetchConversationId();
                },
                error -> {
                    Log.e(TAG, "Failed to fetch user name: " + error);
                    otherUsername = "User " + userId;
                    tvChatWith.setText(otherUsername);
                    fetchConversationId();
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
        Log.d(TAG, "WebSocket OPEN");
        runOnUiThread(() -> appendMessage("Connected ✓", false));
    }

    @Override
    public void onWebSocketMessage(String message) {
        runOnUiThread(() -> {
            Log.d(TAG, "WS message: " + message);

            try {
                JSONObject json = new JSONObject(message);
                String content = json.optString("content", json.optString("message", ""));
                int senderId = json.optInt("senderUserId",
                        json.optInt("senderId", json.optInt("sender_id", -1)));

                if (!content.isEmpty()) {
                    boolean isSent = (senderId == currentUserId);

                    // Detect echo
                    if (!isSent && lastSentMessage != null && content.equals(lastSentMessage)) {
                        isSent = true;
                    }

                    if (isSent) {
                        lastSentMessage = null;
                        return;
                    }
                    appendMessage(content, false);
                    return;
                }
            } catch (Exception ignored) {
            }

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
        Log.d(TAG, "WebSocket CLOSED: code=" + code + " reason=" + reason);
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
    }
}