package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupChatActivity extends AppCompatActivity implements WebSocketEventListener {

    private static final String TAG = "GroupChatActivity";
    private static final String PREFS_NAME = "GroupChatPrefs";

    private Button sendBtn, backBtn;
    private EditText msgEtx;
    private RecyclerView recyclerChat;
    private TextView tvGroupName, tvMemberCount;

    private ChatAdapter chatAdapter;
    private final List<ChatMessage> messageList = new ArrayList<>();

    // Maps senderId -> display name for group members
    private final Map<Integer, String> memberNames = new HashMap<>();
    private final List<Integer> memberUserIds = new ArrayList<>();

    private int currentUserId;
    private int groupId;
    private int conversationId = -1;
    private String groupName;
    private String lastSentMessage = null;

    private static final String WS_BASE = "ws://coms-3090-015.class.las.iastate.edu:8080/chat/";
    private static final String BASE_URL = "http://coms-3090-015.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentUserId = getIntent().getIntExtra("USER_ID", 1);
        groupId       = getIntent().getIntExtra("GROUP_ID", -1);
        groupName     = getIntent().getStringExtra("GROUP_NAME");

        sendBtn       = findViewById(R.id.sendBtn);
        backBtn       = findViewById(R.id.backBtn);
        msgEtx        = findViewById(R.id.msgEdt);
        recyclerChat  = findViewById(R.id.recyclerChat);
        tvGroupName   = findViewById(R.id.tvGroupName);
        tvMemberCount = findViewById(R.id.tvMemberCount);

        sendBtn.setEnabled(true); // Always enable send button immediately

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerChat.setLayoutManager(layoutManager);
        chatAdapter = new ChatAdapter(messageList);
        recyclerChat.setAdapter(chatAdapter);

        tvGroupName.setText(groupName != null ? groupName : "Group Chat");
        tvMemberCount.setVisibility(View.GONE);

        backBtn.setOnClickListener(v -> {
            WebSocketClientManager.getInstance().removeWebSocketEventListener();
            finish();
        });

        if (groupId > 0) {
            conversationId = getCachedConversationId(groupId);
            Log.d(TAG, "Cached conversationId for group " + groupId + " = " + conversationId);
            startConversationFlow();
        } else {
            Toast.makeText(this, "No group specified", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private int getCachedConversationId(int gId) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt("group_conv_" + gId, -1);
    }

    private void cacheConversationId(int gId, int convId) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putInt("group_conv_" + gId, convId).apply();
        Log.d(TAG, "Cached conversationId " + convId + " for group " + gId);
    }

    private void fetchGroupMembers() {
        String url = BASE_URL + "/groups/" + groupId;
        Log.d(TAG, "Step 1: GET group: " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d(TAG, "Group response: " + response);
                    try {
                        JSONObject group = new JSONObject(response);
                        parseMembersFromGroupJson(group);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing group response: " + e.getMessage());
                        fetchGroupMembersFallback();
                        return;
                    }
                    resolveRealNames(0);
                    startConversationFlow();
                },
                error -> {
                    Log.w(TAG, "GET /groups/" + groupId + " failed, falling back to /gm/glist: " + error);
                    fetchGroupMembersFallback();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void fetchGroupMembersFallback() {
        String url = BASE_URL + "/gm/glist/" + groupId;
        Log.d(TAG, "Fallback Step 1: GET members: " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d(TAG, "Members response (fallback): " + response);
                    try {
                        JSONArray arr = new JSONArray(response);
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            int uid = obj.optInt("userId", obj.optInt("userid", -1));
                            if (uid <= 0) continue;
                            if (!isMemberActive(obj)) {
                                Log.d(TAG, "Skipping inactive member: " + uid);
                                continue;
                            }
                            String name = obj.optString("displayName",
                                    obj.optString("displayname",
                                            obj.optString("name",
                                                    obj.optString("userName",
                                                            obj.optString("username", "")))));
                            if (name.isEmpty()) name = "User " + uid;
                            memberNames.put(uid, name);
                            if (!memberUserIds.contains(uid)) memberUserIds.add(uid);
                        }
                        tvMemberCount.setText(memberUserIds.size() + " members · Tap to view");
                        Log.d(TAG, "Loaded " + memberUserIds.size() + " members: " + memberUserIds);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing members (fallback): " + e.getMessage());
                    }
                    resolveRealNames(0);
                    startConversationFlow();
                },
                error -> {
                    Log.e(TAG, "Failed to fetch members (fallback): " + error);
                    tvMemberCount.setText("Group Chat");
                    startConversationFlow();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void parseMembersFromGroupJson(JSONObject group) {
        try {
            if (group.has("members")) {
                JSONArray members = group.optJSONArray("members");
                if (members != null) {
                    for (int i = 0; i < members.length(); i++) {
                        JSONObject m = members.getJSONObject(i);
                        int uid = m.optInt("userId", m.optInt("id", m.optInt("userid", -1)));
                        if (uid <= 0) continue;
                        if (!isMemberActive(m)) {
                            Log.d(TAG, "Skipping inactive member from /groups: " + uid);
                            continue;
                        }
                        String name = m.optString("displayName",
                                m.optString("displayname",
                                        m.optString("name", "")));
                        if (name.isEmpty()) name = "User " + uid;
                        memberNames.put(uid, name);
                        if (!memberUserIds.contains(uid)) memberUserIds.add(uid);
                    }
                }
            }
            if (memberUserIds.isEmpty() && group.has("userIds")) {
                JSONArray arr = group.optJSONArray("userIds");
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        int uid = arr.optInt(i, -1);
                        if (uid <= 0) continue;
                        memberUserIds.add(uid);
                        memberNames.put(uid, "User " + uid);
                    }
                }
            }
            if (memberUserIds.isEmpty()) {
                org.json.JSONArray names = group.names();
                if (names != null) {
                    for (int ni = 0; ni < names.length(); ni++) {
                        try {
                            String key = names.getString(ni);
                            Object val = group.get(key);
                            if (val instanceof JSONArray) {
                                JSONArray a = (JSONArray) val;
                                if (a.length() == 0) continue;
                                Object first = a.get(0);
                                if (first instanceof JSONObject) {
                                    JSONObject cand = (JSONObject) first;
                                    if (cand.has("userId") || cand.has("userid") || cand.has("id") || cand.has("username") || cand.has("displayName")) {
                                        Log.d(TAG, "Found candidate member array at key='" + key + "'");
                                        for (int i = 0; i < a.length(); i++) {
                                            JSONObject m = a.getJSONObject(i);
                                            int uid = m.optInt("userId", m.optInt("id", m.optInt("userid", -1)));
                                            if (uid <= 0) continue;
                                            if (!isMemberActive(m)) continue;
                                            String name = m.optString("displayName",
                                                    m.optString("displayname",
                                                            m.optString("name", "")));
                                            if (name.isEmpty()) name = "User " + uid;
                                            memberNames.put(uid, name);
                                            if (!memberUserIds.contains(uid)) memberUserIds.add(uid);
                                        }
                                        break;
                                    }
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
            runOnUiThread(() -> tvMemberCount.setText(memberUserIds.size() + " members · Tap to view"));
            Log.d(TAG, "Parsed members from /groups: " + memberUserIds + " names=" + memberNames);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing group members: " + e.getMessage());
        }
    }

    private void resolveRealNames(int index) {
        if (index >= memberUserIds.size()) {
            runOnUiThread(() -> tvMemberCount.setText(memberUserIds.size() + " members · Tap to view"));
            return;
        }
        int uid = memberUserIds.get(index);
        String url = BASE_URL + "/users/" + uid;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject user = new JSONObject(response);
                        String name = user.optString("name", "");
                        if (name.isEmpty()) name = user.optString("displayName", "");
                        if (!name.isEmpty()) memberNames.put(uid, name);
                        Log.d(TAG, "Resolved name for user " + uid + " -> " + memberNames.get(uid));
                    } catch (Exception e) { }
                    resolveRealNames(index + 1);
                },
                error -> resolveRealNames(index + 1));
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void fetchUserNameAndUpdate(int uid) {
        if (uid <= 0) return;
        if (memberNames.containsKey(uid)) return;
        String url = BASE_URL + "/users/" + uid;
        Log.d(TAG, "Fetching user name for " + uid + " -> " + url);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject user = new JSONObject(response);
                        String name = user.optString("name", "");
                        if (name.isEmpty()) name = user.optString("displayName", "");
                        if (!name.isEmpty()) {
                            memberNames.put(uid, name);
                            Log.d(TAG, "Fetched name for " + uid + " -> " + name);
                            final String finalName = name;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    boolean changed = false;
                                    for (ChatMessage m : messageList) {
                                        if (!m.isSent() && m.getSenderId() == uid) {
                                            String cur = m.getSenderName();
                                            if (cur == null || cur.isEmpty() || cur.equals("User " + uid)) {
                                                m.setSenderName(finalName);
                                                changed = true;
                                            }
                                        }
                                    }
                                    if (changed) chatAdapter.notifyDataSetChanged();
                                    tvMemberCount.setText(memberUserIds.size() + " members · Tap to view");
                                }
                            });
                        }
                    } catch (Exception ignored) {}
                }, error -> {
                    Log.d(TAG, "Failed to fetch user " + uid + " : " + error);
                });
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void startConversationFlow() {
        if (conversationId > 0) {
            Log.d(TAG, "Step 2: Using cached conversationId = " + conversationId);
            loadChatHistoryThenConnect();
        } else {
            fetchGroupConversationId();
        }
    }

    private void fetchGroupConversationId() {
        String url = BASE_URL + "/conversations/group";
        List<Integer> userIds = new ArrayList<>();
        if (!memberUserIds.contains(currentUserId)) {
            userIds.add(currentUserId);
        }
        userIds.addAll(memberUserIds);
        Collections.sort(userIds);
        JSONObject body = new JSONObject();
        try {
            body.put("groupId", groupId);
            body.put("name", groupName != null ? groupName : "Group " + groupId);
            JSONArray idsArray = new JSONArray();
            for (int uid : userIds) {
                idsArray.put(uid);
            }
            body.put("userIds", idsArray);
        } catch (Exception e) {
            Log.e(TAG, "Error building body", e);
            connectWebSocket();
            return;
        }
        Log.d(TAG, "Step 2: POST " + url + " body=" + body);
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d(TAG, "Conversation response: " + response);
                    try {
                        JSONObject json = new JSONObject(response);
                        conversationId = json.optInt("conversationId",
                                json.optInt("id",
                                        json.optInt("conversation_id", -1)));
                    } catch (Exception e) {
                        try {
                            conversationId = Integer.parseInt(response.trim());
                        } catch (NumberFormatException nfe) {
                            Log.e(TAG, "Could not parse conversationId from: " + response);
                        }
                    }
                    Log.d(TAG, "Got conversationId = " + conversationId);
                    if (conversationId > 0) {
                        cacheConversationId(groupId, conversationId);
                        fetchConversationDetails(conversationId);
                    }
                    loadChatHistoryThenConnect();
                },
                error -> {
                    Log.e(TAG, "Failed to create group conversation: " + error);
                    if (error.networkResponse != null) {
                        String errBody = new String(error.networkResponse.data);
                        Log.e(TAG, "Status: " + error.networkResponse.statusCode + " Body: " + errBody);
                        try {
                            JSONObject errJson = new JSONObject(errBody);
                            int cid = errJson.optInt("conversationId", errJson.optInt("id", -1));
                            if (cid > 0) {
                                conversationId = cid;
                                cacheConversationId(groupId, conversationId);
                                Log.d(TAG, "Got conversationId from error response: " + conversationId);
                                loadChatHistoryThenConnect();
                                return;
                            }
                        } catch (Exception ignored) {}
                    }
                    Log.w(TAG, "No conversationId obtained — messages cannot be sent.");
                    connectWebSocket();
                }) {
            @Override
            public byte[] getBody() { return body.toString().getBytes(); }
            @Override
            public String getBodyContentType() { return "application/json"; }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void loadChatHistoryThenConnect() {
        if (conversationId <= 0) {
            Log.w(TAG, "No valid conversationId (" + conversationId + "), skipping history");
            connectWebSocket();
            return;
        }
        String primaryUrl = BASE_URL + "/messages/conversation/" + conversationId + "/user/" + currentUserId;
        Log.d(TAG, "Step 3: GET (primary) " + primaryUrl);

        StringRequest request = new StringRequest(Request.Method.GET, primaryUrl,
                response -> {
                    Log.d(TAG, "Primary history (" + response.length() + " chars)");
                    int loaded = parseAndLoadMessages(response);
                    if (loaded == 0) {
                        Log.d(TAG, "Primary returned 0, trying fallback...");
                        loadChatHistoryFallback();
                    } else {
                        Log.d(TAG, "Loaded " + loaded + " messages from primary URL");
                        connectWebSocket();
                    }
                },
                error -> {
                    Log.e(TAG, "Primary history FAILED: " + error);
                    loadChatHistoryFallback();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void loadChatHistoryFallback() {
        String fallbackUrl = BASE_URL + "/messages/conversation/" + conversationId;
        Log.d(TAG, "GET (fallback) " + fallbackUrl);

        StringRequest request = new StringRequest(Request.Method.GET, fallbackUrl,
                response -> {
                    int loaded = parseAndLoadMessages(response);
                    Log.d(TAG, "Fallback loaded " + loaded + " messages");
                    connectWebSocket();
                },
                error -> {
                    Log.e(TAG, "Fallback also FAILED: " + error);
                    connectWebSocket();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private int parseAndLoadMessages(String response) {
        try {
            JSONArray arr = new JSONArray(response);
            if (arr.length() > 0) {
                Log.d(TAG, "FIRST MSG JSON: " + arr.getJSONObject(0).toString());
            }
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                long id = obj.optLong("messageId", obj.optLong("id", 0));
                String content = obj.optString("content", obj.optString("message", ""));
                String timestamp = obj.optString("sentAt", obj.optString("timestamp", ""));
                int senderId = extractUserId(obj, "sender", "senderUserId", "senderId", "sender_id", "fromId");
                int receiverId = extractUserId(obj, "receiver", "receiverUserId", "receiverId", "receiver_id", "toId");

                if (i < 3) {
                    Log.d(TAG, "MSG[" + i + "] senderId=" + senderId + " currentUserId=" + currentUserId
                            + " keys=" + obj.keys().toString() + " raw=" + obj.toString().substring(0, Math.min(200, obj.toString().length())));
                }

                boolean isSent = (senderId == currentUserId);
                ChatMessage msg = new ChatMessage(id, senderId, receiverId, content, timestamp, isSent);
                if (!isSent) {
                    String extracted = extractSenderName(obj, senderId);
                    if (extracted != null && !extracted.isEmpty()) {
                        msg.setSenderName(extracted);
                    } else if (memberNames.containsKey(senderId)) {
                        msg.setSenderName(memberNames.get(senderId));
                    } else {
                        msg.setSenderName("User " + senderId);
                        fetchUserNameAndUpdate(senderId);
                    }
                }
                messageList.add(msg);
            }
            chatAdapter.notifyDataSetChanged();
            scrollToBottom();
            return arr.length();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing history: " + e.getMessage());
            return 0;
        }
    }

    private int extractUserId(JSONObject obj, String objectKey, String... flatKeys) {
        for (String key : flatKeys) {
            int val = obj.optInt(key, -1);
            if (val > 0) return val;
        }
        if (obj.has(objectKey)) {
            int flatVal = obj.optInt(objectKey, -1);
            if (flatVal > 0) return flatVal;
        }
        if (obj.has(objectKey) && !obj.isNull(objectKey)) {
            try {
                JSONObject nested = obj.getJSONObject(objectKey);
                int val = nested.optInt("id",
                        nested.optInt("userId",
                                nested.optInt("user_id",
                                        nested.optInt("senderUserId", -1))));
                if (val > 0) return val;
            } catch (Exception ignored) {}
        }
        return 0;
    }

    private String extractSenderName(JSONObject obj, int senderId) {
        if (obj == null) return "";
        try {
            String[] flatNameKeys = new String[]{
                    "senderName", "sender_name", "senderDisplayName", "sender_display_name",
                    "displayName", "display_name", "name", "username", "userName"
            };
            for (String k : flatNameKeys) {
                if (obj.has(k)) {
                    String v = obj.optString(k, "").trim();
                    if (!v.isEmpty()) return v;
                }
            }
            if (obj.has("sender") && !obj.isNull("sender")) {
                try {
                    JSONObject s = obj.getJSONObject("sender");
                    for (String k : new String[]{"displayName", "display_name", "name", "username", "userName"}) {
                        if (s.has(k)) {
                            String v = s.optString(k, "").trim();
                            if (!v.isEmpty()) return v;
                        }
                    }
                } catch (Exception ignored) {}
            }
            if (obj.has("user") && !obj.isNull("user")) {
                try {
                    JSONObject s = obj.getJSONObject("user");
                    for (String k : new String[]{"displayName", "display_name", "name", "username", "userName"}) {
                        if (s.has(k)) {
                            String v = s.optString(k, "").trim();
                            if (!v.isEmpty()) return v;
                        }
                    }
                } catch (Exception ignored) {}
            }
            if (memberNames.containsKey(senderId)) return memberNames.get(senderId);
        } catch (Exception ignored) {}
        return "";
    }

    private boolean isMemberActive(JSONObject obj) {
        try {
            if (obj.has("status")) {
                String s = obj.optString("status", "").trim();
                if (!s.isEmpty()) {
                    String sl = s.toLowerCase();
                    if (sl.equals("active") || sl.equals("joined") || sl.equals("accepted") || sl.equals("member") || sl.equals("active_member")) return true;
                    if (sl.equals("inactive") || sl.equals("left") || sl.equals("banned") || sl.equals("removed")) return false;
                }
            }
            if (obj.has("active")) {
                if (obj.optBoolean("active", false)) return true;
                int v = obj.optInt("active", -1);
                if (v > 0) return true;
            }
            if (obj.has("isActive")) {
                if (obj.optBoolean("isActive", false)) return true;
            }
            if (obj.has("statusCode")) {
                int code = obj.optInt("statusCode", -1);
                if (code == 1) return true;
            }
            if (obj.has("membershipStatus")) {
                String s = obj.optString("membershipStatus", "").toLowerCase();
                if (s.contains("active") || s.contains("joined") || s.contains("accepted")) return true;
                if (s.contains("inactive") || s.contains("left") || s.contains("removed")) return false;
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    private void fetchConversationDetails(int convId) {
        String url = BASE_URL + "/conversations/" + convId;
        Log.d(TAG, "GET conversation details: " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d(TAG, "Conversation details response: " + response);
                    try {
                        JSONObject json = new JSONObject(response);
                        List<Integer> partIds = new ArrayList<>();
                        if (json.has("userIds")) {
                            JSONArray u = json.optJSONArray("userIds");
                            if (u != null) for (int i = 0; i < u.length(); i++) partIds.add(u.optInt(i, -1));
                        }
                        if (partIds.isEmpty() && json.has("participants")) {
                            JSONArray u = json.optJSONArray("participants");
                            if (u != null) for (int i = 0; i < u.length(); i++) partIds.add(u.optInt(i, -1));
                        }
                        if (partIds.isEmpty() && json.has("members")) {
                            JSONArray u = json.optJSONArray("members");
                            if (u != null) for (int i = 0; i < u.length(); i++) partIds.add(u.optInt(i, -1));
                        }
                        if (partIds.isEmpty() && json.has("users")) {
                            try {
                                JSONArray u = json.optJSONArray("users");
                                if (u != null) {
                                    for (int i = 0; i < u.length(); i++) {
                                        JSONObject o = u.optJSONObject(i);
                                        if (o != null) {
                                            int id = o.optInt("id", o.optInt("userId", -1));
                                            if (id > 0) partIds.add(id);
                                        }
                                    }
                                }
                            } catch (Exception ignored) {}
                        }
                        Log.d(TAG, "Conversation participants parsed: " + partIds);
                        boolean amMember = partIds.contains(currentUserId);
                        if (!amMember) {
                            Log.w(TAG, "Current user " + currentUserId + " is NOT in conversation " + convId + ". Retrying group conversation creation...");
                            // Retry group conversation creation to force-add user
                            fetchGroupConversationIdForceAdd();
                        } else {
                            Log.d(TAG, "Current user is a participant of conversation " + convId);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing conversation details: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "Failed to fetch conversation details: " + error);
                    if (error.networkResponse != null) {
                        try {
                            Log.e(TAG, "Status: " + error.networkResponse.statusCode + " Body: " + new String(error.networkResponse.data));
                        } catch (Exception ignored) {}
                    }
                });
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    // Force re-create group conversation with current user included
    private void fetchGroupConversationIdForceAdd() {
        String url = BASE_URL + "/conversations/group";
        List<Integer> userIds = new ArrayList<>();
        if (!memberUserIds.contains(currentUserId)) {
            userIds.add(currentUserId);
        }
        userIds.addAll(memberUserIds);
        Collections.sort(userIds);
        JSONObject body = new JSONObject();
        try {
            body.put("groupId", groupId);
            body.put("name", groupName != null ? groupName : "Group " + groupId);
            JSONArray idsArray = new JSONArray();
            for (int uid : userIds) {
                idsArray.put(uid);
            }
            body.put("userIds", idsArray);
        } catch (Exception e) {
            Log.e(TAG, "Error building body (force add)", e);
            connectWebSocket();
            return;
        }
        Log.d(TAG, "FORCE Step 2: POST " + url + " body=" + body);
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d(TAG, "FORCE Conversation response: " + response);
                    try {
                        JSONObject json = new JSONObject(response);
                        conversationId = json.optInt("conversationId",
                                json.optInt("id",
                                        json.optInt("conversation_id", -1)));
                    } catch (Exception e) {
                        try {
                            conversationId = Integer.parseInt(response.trim());
                        } catch (NumberFormatException nfe) {
                            Log.e(TAG, "Could not parse conversationId from: " + response);
                        }
                    }
                    Log.d(TAG, "FORCE Got conversationId = " + conversationId);
                    if (conversationId > 0) {
                        cacheConversationId(groupId, conversationId);
                        // After force-adding, check again
                        fetchConversationDetails(conversationId);
                    } else {
                        // Only show error if still not a participant
                        runOnUiThread(() -> {
                            Toast.makeText(this, "You are not a participant on the server for this conversation.", Toast.LENGTH_LONG).show();
                        });
                    }
                },
                error -> {
                    Log.e(TAG, "FORCE Failed to create group conversation: " + error);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "You are not a participant on the server for this conversation.", Toast.LENGTH_LONG).show();
                    });
                }) {
            @Override
            public byte[] getBody() { return body.toString().getBytes(); }
            @Override
            public String getBodyContentType() { return "application/json"; }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void connectWebSocket() {
        WebSocketClientManager.getInstance().clearMessageQueue();
        WebSocketClientManager.getInstance().setWebSocketEventListener(this);
        if (WebSocketClientManager.getInstance().isConnected()) {
            Log.d(TAG, "WebSocket already connected, reusing");
        } else {
            String wsUrl = WS_BASE + currentUserId;
            Log.d(TAG, "Connecting WebSocket: " + wsUrl);
            WebSocketClientManager.getInstance().connectWebSocket(wsUrl);
        }
        sendBtn.setOnClickListener(v -> {
            String message = msgEtx.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(this, "Message cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            //if (conversationId <= 0) {
              //  Toast.makeText(this, "Chat not ready — no conversationId", Toast.LENGTH_SHORT).show();
               // return;
            //}
            try {
                JSONObject jsonMsg = new JSONObject();
                jsonMsg.put("conversationId", conversationId);
                jsonMsg.put("content", message);
                Log.d(TAG, "Sending: " + jsonMsg);

                boolean sent = WebSocketClientManager.getInstance().sendMessage(jsonMsg.toString());
                if (sent) {
                    lastSentMessage = message;
                    appendMessage(message, true, null);
                    msgEtx.setText("");
                } else {
                    Toast.makeText(this, "Not connected. Reconnecting...", Toast.LENGTH_SHORT).show();
                    WebSocketClientManager.getInstance().connectWebSocket(WS_BASE + currentUserId);
                }
            } catch (Exception e) {
                Log.e(TAG, "Send error: " + e.getMessage());
            }
        });
    }

    private void appendMessage(String content, boolean isSent, String senderName) {
        ChatMessage msg = new ChatMessage(content, isSent);
        if (senderName != null) msg.setSenderName(senderName);
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

                    String name = extractSenderName(json, senderId);
                    if (name == null || name.isEmpty()) {
                        if (memberNames.containsKey(senderId)) {
                            name = memberNames.get(senderId);
                        } else {
                            name = "User " + senderId;
                            // fetch and update later
                            fetchUserNameAndUpdate(senderId);
                        }
                    }
                    appendMessage(content, false, name);
                    return;
                }
            } catch (Exception ignored) {}

            // Echo filter for plain text
            if (lastSentMessage != null) {
                if (message.equals(lastSentMessage) || message.contains(lastSentMessage)) {
                    lastSentMessage = null;
                    return;
                }
            }

            // Filter system messages
            String lower = message.toLowerCase();
            if (lower.contains("has joined") || lower.contains("has left")
                    || lower.contains("welcome") || lower.contains("entered")
                    || lower.contains("connected") || lower.contains("disconnected")) {
                return;
            }

            appendMessage(message, false, null);
        });
    }

    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        Log.d(TAG, "WebSocket CLOSED: " + code + " " + reason);
        runOnUiThread(() -> Toast.makeText(this, "Chat disconnected", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onWebSocketError(Exception ex) {
        Log.e(TAG, "WebSocket ERROR: " + (ex != null ? ex.getMessage() : "unknown"));
    }

    private void showMembersDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_group_members, null);
        TextView tvCount = dialogView.findViewById(R.id.tvDialogMemberCount);
        LinearLayout container = dialogView.findViewById(R.id.membersContainer);
        Button btnClose = dialogView.findViewById(R.id.btnCloseMembersDialog);

        tvCount.setText(memberNames.size() + " members");

        for (Map.Entry<Integer, String> entry : memberNames.entrySet()) {
            int uid = entry.getKey();
            String name = entry.getValue();

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(8), dp(10), dp(8), dp(10));

            FrameLayout avatarFrame = new FrameLayout(this);
            LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(dp(36), dp(36));
            ap.setMargins(0, 0, dp(12), 0);
            avatarFrame.setLayoutParams(ap);
            avatarFrame.setBackgroundColor(Color.parseColor("#7B6FFF"));

            TextView avatarText = new TextView(this);
            avatarText.setText(name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase());
            avatarText.setTextColor(Color.WHITE);
            avatarText.setTextSize(14);
            avatarText.setGravity(Gravity.CENTER);
            avatarText.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            avatarFrame.addView(avatarText);
            row.addView(avatarFrame);

            LinearLayout textCol = new LinearLayout(this);
            textCol.setOrientation(LinearLayout.VERTICAL);

            TextView tvName = new TextView(this);
            tvName.setText(name);
            tvName.setTextColor(Color.WHITE);
            tvName.setTextSize(15);
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);
            textCol.addView(tvName);

            TextView tvId = new TextView(this);
            boolean isYou = (uid == currentUserId);
            tvId.setText(isYou ? "You" : "ID: " + uid);
            tvId.setTextColor(isYou ? Color.parseColor("#7B6FFF") : Color.parseColor("#9B9BB4"));
            tvId.setTextSize(11);
            textCol.addView(tvId);
            row.addView(textCol);

            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(1)));
            divider.setBackgroundColor(Color.parseColor("#2A2A42"));

            container.addView(row);
            container.addView(divider);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView).setCancelable(true).create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WebSocketClientManager.getInstance().removeWebSocketEventListener();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
