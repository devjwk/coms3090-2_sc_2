package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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
import java.util.Set;

public class GroupChatActivity extends AppCompatActivity implements WebSocketEventListener {

    private static final String TAG = "GroupChatActivity";
    private static final String PREFS_NAME = "GroupChatPrefs";

    private Button sendBtn, backBtn, btnManageGroupChat, btnViewEvents;
    private EditText msgEtx;
    private RecyclerView recyclerChat;
    private TextView tvGroupName, tvMemberCount;
    private LinearLayout layoutPinnedAnnouncement;
    private TextView tvPinnedAnnouncement;

    private ChatAdapter chatAdapter;
    private final List<ChatMessage> messageList = new ArrayList<>();
    private ModeratorRepository moderatorRepository;

    // Maps senderId -> display name for group members
    private final Map<Integer, String> memberNames = new HashMap<>();
    private final List<Integer> memberUserIds = new ArrayList<>();

    private int currentUserId;
    private int groupId;
    private int moderatorId = -1;
    private boolean isModeratorView = false;
    private int conversationId = -1;
    private String groupName;
    private String lastSentMessage = null;
    private boolean canModerateMessages = false;

    private static final String WS_BASE = "ws://coms-3090-015.class.las.iastate.edu:8080/chat/";
    private static final String BASE_URL = "http://coms-3090-015.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentUserId = getIntent().getIntExtra("USER_ID", -1);
        groupId       = getIntent().getIntExtra("GROUP_ID", -1);
        groupName     = getIntent().getStringExtra("GROUP_NAME");
        moderatorId   = getIntent().getIntExtra("MODERATOR_ID", -1);
        isModeratorView = getIntent().getBooleanExtra("IS_MODERATOR", false);

        sendBtn       = findViewById(R.id.sendBtn);
        backBtn       = findViewById(R.id.backBtn);
        btnManageGroupChat = findViewById(R.id.btnManageGroupChat);
        btnViewEvents = findViewById(R.id.btnViewEvents);
        msgEtx        = findViewById(R.id.msgEdt);
        recyclerChat  = findViewById(R.id.recyclerChat);
        tvGroupName   = findViewById(R.id.tvGroupName);
        tvMemberCount = findViewById(R.id.tvMemberCount);
        layoutPinnedAnnouncement = findViewById(R.id.layoutPinnedAnnouncement);
        tvPinnedAnnouncement = findViewById(R.id.tvPinnedAnnouncement);

        moderatorRepository = new ModeratorRepository();

        // If USER_ID was not passed for a normal user flow, fall back to the WebSocket manager's
        // current identity instead of hardcoding a user id. That keeps sent/received alignment
        // tied to the real logged-in user.
        if (!isModeratorView && currentUserId <= 0) {
            int wsUserId = WebSocketClientManager.getInstance().getCurrentUserId();
            if (wsUserId > 0) {
                currentUserId = wsUserId;
                Log.d(TAG, "Recovered currentUserId from WebSocket manager: " + currentUserId);
            }
        }

        ModeratorSessionManager moderatorSessionManager = new ModeratorSessionManager(this);
        ModeratorAccount moderatorAccount = moderatorSessionManager.getSession();
        if (moderatorId <= 0 && moderatorAccount != null) {
            moderatorId = moderatorAccount.getModeratorId();
        }
        boolean hasAssignedGroupsPayload = moderatorAccount != null
                && moderatorAccount.getAssignedGroups() != null
                && !moderatorAccount.getAssignedGroups().isEmpty();
        boolean canManageThisGroup = !hasAssignedGroupsPayload
                || (moderatorAccount != null && moderatorAccount.getAssignedGroups().contains(groupId));
        // IMPORTANT: Only show the Manage button for an explicit moderator view. Do NOT show
        // it to regular users even if a stale moderator session exists on the device.
        boolean showManageButton = isModeratorView && moderatorId > 0 && canManageThisGroup;
        Set<String> moderatorPermissions = moderatorSessionManager.getPermissions();
        boolean hasPermissionPayload = moderatorPermissions != null && !moderatorPermissions.isEmpty();
        canModerateMessages = showManageButton
                && (!hasPermissionPayload || moderatorPermissions.contains(ModeratorPermissions.MODERATE_CONVERSATIONS));
        btnManageGroupChat.setVisibility(showManageButton ? View.VISIBLE : View.GONE);
        btnViewEvents.setVisibility(groupId > 0 ? View.VISIBLE : View.GONE);
        btnViewEvents.setOnClickListener(v -> loadAndShowGroupEvents());
        btnManageGroupChat.setOnClickListener(v -> {
            if (!showManageButton || moderatorId <= 0) {
                Toast.makeText(this, "Moderator session required", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent manageIntent = new Intent(GroupChatActivity.this, ModeratorGroupManagementActivity.class);
            manageIntent.putExtra("MODERATOR_ID", moderatorId);
            manageIntent.putExtra("GROUP_ID", groupId);
            manageIntent.putExtra("GROUP_NAME", groupName);
            startActivity(manageIntent);
        });

        sendBtn.setEnabled(true); // Always enable send button immediately

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerChat.setLayoutManager(layoutManager);
        chatAdapter = new ChatAdapter(
                messageList,
                canDeleteMessagesInThisChat() ? this::showMessageActionMenu : null,
                isModeratorView
        );
        recyclerChat.setAdapter(chatAdapter);

        tvGroupName.setText(groupName != null ? groupName : "Group Chat");
        tvMemberCount.setVisibility(View.VISIBLE);
        tvMemberCount.setOnClickListener(v -> showMembersDialog());

        backBtn.setOnClickListener(v -> {
            WebSocketClientManager.getInstance().removeWebSocketEventListener();
            finish();
        });

        if (groupId > 0) {
            loadPinnedAnnouncement();
            conversationId = getCachedConversationId(groupId);
            Log.d(TAG, "Cached conversationId for group " + groupId + " = " + conversationId);
            fetchGroupMembers();
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
        String url = BASE_URL + "/gm/glist/" + groupId;
        Log.d(TAG, "Step 1: GET members: " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d(TAG, "Members response: " + response);
                    try {
                        memberNames.clear();
                        memberUserIds.clear();
                        JSONArray arr = new JSONArray(response);
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            int uid = obj.optInt("userId", obj.optInt("userid", -1));
                            if (uid <= 0) {
                                continue;
                            }
                            String name = obj.optString("displayName",
                                    obj.optString("displayname",
                                            obj.optString("name",
                                                    obj.optString("userName",
                                                            obj.optString("username", "")))));
                            if (name.isEmpty()) {
                                continue;
                            }
                            memberNames.put(uid, name);
                            if (!memberUserIds.contains(uid)) {
                                memberUserIds.add(uid);
                            }
                        }
                        tvMemberCount.setText(memberUserIds.size() + " members · Tap to view");
                        Log.d(TAG, "Loaded members from /gm/glist: " + memberNames);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing members response: " + e.getMessage());
                    }
                    startConversationFlow();
                },
                error -> {
                    Log.e(TAG, "GET /gm/glist/" + groupId + " failed: " + error);
                    tvMemberCount.setText("Group Chat");
                    startConversationFlow();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void refreshGroupMembersForDialog() {
        if (groupId <= 0) {
            return;
        }
        String url = BASE_URL + "/gm/glist/" + groupId;
        Log.d(TAG, "Refreshing members list: " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        memberNames.clear();
                        memberUserIds.clear();
                        JSONArray arr = new JSONArray(response);
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            int uid = obj.optInt("userId", obj.optInt("userid", -1));
                            if (uid <= 0) {
                                continue;
                            }
                            String name = obj.optString("displayName",
                                    obj.optString("displayname",
                                            obj.optString("name",
                                                    obj.optString("userName",
                                                            obj.optString("username", "")))));
                            if (name.isEmpty()) {
                                continue;
                            }
                            memberNames.put(uid, name);
                            if (!memberUserIds.contains(uid)) {
                                memberUserIds.add(uid);
                            }
                        }
                        tvMemberCount.setText(memberUserIds.size() + " members · Tap to view");
                        Log.d(TAG, "Refreshed members: " + memberNames);
                    } catch (Exception e) {
                        Log.e(TAG, "Error refreshing members: " + e.getMessage());
                    }
                },
                error -> Log.e(TAG, "Refresh members failed: " + error));

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
        // For moderator view, skip conversation API and load messages directly
        if (isModeratorView && moderatorId > 0) {
            Log.d(TAG, "Moderator view: Loading messages directly from group endpoint");
            loadModeratorGroupMessages();
            return;
        }

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
                    int status = error.networkResponse != null ? error.networkResponse.statusCode : -1;
                    String errBody = error.networkResponse != null ? new String(error.networkResponse.data) : "";
                    Log.e(TAG, "Primary error status: " + status + " body: " + errBody);
                    if ((status == 403 || status == 401 || status == 404) && errBody.toLowerCase().contains("not in this conversation")) {
                        Log.w(TAG, "Membership warning from backend; proceeding despite message: " + errBody);
                        // Try fallback; fallback will also treat membership warnings as non-fatal
                        loadChatHistoryFallback();
                        return;
                    }
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
                    int status = error.networkResponse != null ? error.networkResponse.statusCode : -1;
                    String errBody = error.networkResponse != null ? new String(error.networkResponse.data) : "";
                    Log.e(TAG, "Fallback error status: " + status + " body: " + errBody);
                    if ((status == 403 || status == 401 || status == 404) && errBody.toLowerCase().contains("not in this conversation")) {
                        Log.w(TAG, "Membership warning from backend on fallback; connecting anyway: " + errBody);
                        connectWebSocket();
                        return;
                    }
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

                String senderName = extractSenderName(obj, senderId);
                boolean isSent = isOutgoingMessage(senderId, senderName);
                ChatMessage msg = new ChatMessage(id, senderId, receiverId, content, timestamp, isSent);
                if (isMessageRemoved(obj, content)) {
                    msg.markRemovedByModerator();
                }
                if (!isSent) {
                    if (senderName != null && !senderName.isEmpty()) {
                        msg.setSenderName(senderName);
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

    private boolean isMessageRemoved(JSONObject obj, String content) {
        if (obj == null) {
            return false;
        }
        if (obj.optBoolean("removed", false)
                || obj.optBoolean("isRemoved", false)
                || obj.optBoolean("deleted", false)
                || obj.optBoolean("hidden", false)) {
            return true;
        }
        if (obj.has("active") && !obj.optBoolean("active", true)) {
            return true;
        }
        String lowered = content == null ? "" : content.trim().toLowerCase();
        return lowered.equals("[deleted]")
                || lowered.equals("message removed by moderator")
                || lowered.equals("this message was deleted");
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

    private boolean isOutgoingMessage(int senderId, String senderName) {
        if (senderId > 0 && currentUserId > 0) {
            return senderId == currentUserId;
        }

        if (senderName == null || senderName.trim().isEmpty() || currentUserId <= 0) {
            return false;
        }

        String currentName = memberNames.get(currentUserId);
        if (currentName == null || currentName.trim().isEmpty()) {
            return false;
        }

        return senderName.trim().equalsIgnoreCase(currentName.trim());
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
                            // Backend no longer requires strict membership enforcement in many cases.
                            // Treat missing membership as a warning but do NOT block the user or auto force-add.
                            Log.w(TAG, "Current user " + currentUserId + " is NOT listed as participant of conversation " + convId + ". Proceeding without forcing membership. Participants: " + partIds);
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

    private void loadModeratorGroupMessages() {
        if (moderatorId <= 0 || groupId <= 0) {
            Log.w(TAG, "Invalid moderatorId (" + moderatorId + ") or groupId (" + groupId + ")");
            Toast.makeText(this, "Moderator data missing", Toast.LENGTH_SHORT).show();
            return;
        }
        // If we have a cached conversationId prefer the conversation messages endpoint
        if (conversationId > 0) {
            Log.d(TAG, "Loading messages from conversation endpoint: conversationId=" + conversationId);
            moderatorRepository.getConversationMessages(this, conversationId, new ModeratorRepository.JsonArrayCallback() {
                @Override
                public void onSuccess(JSONArray array) {
                    Log.d(TAG, "Conversation loaded " + array.length() + " messages");
                    runOnUiThread(() -> {
                        messageList.clear();
                        parseAndLoadMessages(array.toString());
                        Log.d(TAG, "Moderator view: conversation messages loaded, not connecting WebSocket");
                    });
                }

                @Override
                public void onError(String error) {
                    Log.w(TAG, "Conversation endpoint failed, falling back to moderator group messages: " + error);
                    // Try the moderator-specific group messages endpoint as a fallback
                    loadModeratorGroupMessagesFallback();
                }
            });
            return;
        }

        // No conversationId available - use moderator-specific group messages endpoint
        loadModeratorGroupMessagesFallback();
    }

    private void loadModeratorGroupMessagesFallback() {
        Log.d(TAG, "Loading messages from moderator endpoint: moderatorId=" + moderatorId + " groupId=" + groupId);
        moderatorRepository.getModeratorGroupMessages(this, moderatorId, groupId, new ModeratorRepository.JsonArrayCallback() {
            @Override
            public void onSuccess(JSONArray array) {
                Log.d(TAG, "Moderator loaded " + array.length() + " messages");
                runOnUiThread(() -> {
                    messageList.clear();
                    parseAndLoadMessages(array.toString());
                    // For moderator view, don't connect WebSocket - just show message history
                    Log.d(TAG, "Moderator view: loading complete, not connecting WebSocket");
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to load moderator messages: " + error);
                runOnUiThread(() -> {
                    Toast.makeText(GroupChatActivity.this, "Failed to load messages: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
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
                if (handleDeleteEvent(json)) {
                    return;
                }
                 String content = json.optString("content", json.optString("message", ""));
                 int senderId = json.optInt("senderUserId",
                         json.optInt("senderId", json.optInt("sender_id", -1)));

                if (!content.isEmpty()) {
                    String senderName = extractSenderName(json, senderId);
                    boolean isSent = isOutgoingMessage(senderId, senderName);

                    // Detect echo
                    if (!isSent && lastSentMessage != null && content.equals(lastSentMessage)) {
                        isSent = true;
                    }
                    if (isSent) {
                        lastSentMessage = null;
                        return;
                    }

                    String name = senderName;
                    if (name == null || name.isEmpty()) {
                        if (memberNames.containsKey(senderId)) {
                            name = memberNames.get(senderId);
                        } else {
                            name = "User " + senderId;
                            // fetch and update later
                            fetchUserNameAndUpdate(senderId);
                        }
                    }
                    long messageId = json.optLong("messageId", json.optLong("id", 0));
                    String timestamp = json.optString("sentAt", json.optString("timestamp", ""));
                    ChatMessage incoming = new ChatMessage(messageId, senderId, currentUserId, content, timestamp, false);
                    incoming.setSenderName(name);
                    messageList.add(incoming);
                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                    scrollToBottom();
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

        container.setVisibility(View.VISIBLE);
        tvCount.setText(memberNames.size() + " members");

        for (Map.Entry<Integer, String> entry : memberNames.entrySet()) {
            int userId = entry.getKey();
            String name = entry.getValue();

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(dp(8), dp(10), dp(8), dp(10));

            TextView tvName = new TextView(this);
            tvName.setText(name + " (User ID: " + userId + ")");
            tvName.setTextColor(Color.WHITE);
            tvName.setTextSize(15);
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);
            row.addView(tvName);

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
    protected void onResume() {
        super.onResume();
        if (groupId > 0) {
            loadPinnedAnnouncement();
            refreshGroupMembersForDialog();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WebSocketClientManager.getInstance().removeWebSocketEventListener();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void showMessageActionMenu(View anchor, ChatMessage message, int position) {
        if (!canDeleteMessagesInThisChat()) {
            return;
        }
        if (message == null || message.getId() <= 0) {
            Toast.makeText(this, "Message cannot be moderated yet", Toast.LENGTH_SHORT).show();
            return;
        }

        PopupMenu popupMenu = new PopupMenu(this, anchor);
        if (message.isModeratedRemoved()) {
            popupMenu.getMenu().add("Restore");
        } else {
            popupMenu.getMenu().add("Delete");
        }
        popupMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle() == null ? "" : item.getTitle().toString();
            if ("Delete".equalsIgnoreCase(title)) {
                confirmDeleteMessage(message, position);
                return true;
            }
            if ("Restore".equalsIgnoreCase(title)) {
                confirmRestoreMessage(message, position);
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void confirmRestoreMessage(ChatMessage message, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Restore message")
                .setMessage("This will restore the message for everyone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Restore", (dialog, which) -> restoreMessageForEveryone(message, position))
                .show();
    }

    private void restoreMessageForEveryone(ChatMessage message, int position) {
        if (moderatorId <= 0) {
            Toast.makeText(this, "Moderator session required", Toast.LENGTH_SHORT).show();
            return;
        }
        moderatorRepository.restoreMessage(this, moderatorId, (int) message.getId(), new ModeratorRepository.ActionCallback() {
            @Override
            public void onSuccess(String response) {
                if (position >= 0 && position < messageList.size()) {
                    messageList.get(position).setModeratedRemoved(false);
                    chatAdapter.notifyItemChanged(position);
                }
                Toast.makeText(GroupChatActivity.this, "Message restored", Toast.LENGTH_SHORT).show();
                reloadConversationMessages();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(GroupChatActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean canDeleteMessagesInThisChat() {
        return isModeratorView && moderatorId > 0 && canModerateMessages;
    }

    private void confirmDeleteMessage(ChatMessage message, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete message")
                .setMessage("This will remove the message for everyone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> deleteMessageForEveryone(message, position))
                .show();
    }

    private void deleteMessageForEveryone(ChatMessage message, int position) {
        if (moderatorId <= 0) {
            Toast.makeText(this, "Moderator session required", Toast.LENGTH_SHORT).show();
            return;
        }
        moderatorRepository.removeMessage(this, moderatorId, (int) message.getId(), new ModeratorRepository.ActionCallback() {
            @Override
            public void onSuccess(String response) {
                if (position >= 0 && position < messageList.size()) {
                    messageList.get(position).markRemovedByModerator();
                    chatAdapter.notifyItemChanged(position);
                }
                Toast.makeText(GroupChatActivity.this, "Message deleted", Toast.LENGTH_SHORT).show();
                reloadConversationMessages();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(GroupChatActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reloadConversationMessages() {
        if (conversationId <= 0) {
            return;
        }
        moderatorRepository.getConversationMessages(this, conversationId, new ModeratorRepository.JsonArrayCallback() {
            @Override
            public void onSuccess(JSONArray array) {
                messageList.clear();
                parseAndLoadMessages(array.toString());
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to refresh messages after moderation: " + error);
            }
        });
    }

    private boolean handleDeleteEvent(JSONObject json) {
        if (json == null) {
            return false;
        }
        String action = json.optString("action", json.optString("type", "")).toLowerCase();
        boolean looksLikeDelete = action.contains("delete") || action.contains("remove") || action.contains("hide");
        if (!looksLikeDelete) {
            return false;
        }

        long messageId = json.optLong("messageId", json.optLong("id", -1));
        if (messageId <= 0) {
            return false;
        }

        int index = -1;
        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i).getId() == messageId) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            reloadConversationMessages();
            return false;
        }

        messageList.get(index).markRemovedByModerator();
        chatAdapter.notifyItemChanged(index);
        return true;
    }

    private void loadAndShowGroupEvents() {
        if (groupId <= 0) {
            Toast.makeText(this, "Invalid group", Toast.LENGTH_SHORT).show();
            return;
        }

        moderatorRepository.getGroupEventsForChat(this, groupId, moderatorId, new ModeratorRepository.JsonArrayCallback() {
            @Override
            public void onSuccess(JSONArray array) {
                runOnUiThread(() -> showEventsDialog(array));
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(GroupChatActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showEventsDialog(JSONArray events) {
        String message = formatEventsForDialog(events);
        new AlertDialog.Builder(this)
                .setTitle("Group Events")
                .setMessage(message)
                .setPositiveButton("Close", null)
                .show();
    }

    private String formatEventsForDialog(JSONArray events) {
        if (events == null || events.length() == 0) {
            return "No events yet.";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < events.length(); i++) {
            Object raw = events.opt(i);
            String line = extractEventDisplayLine(raw);
            if (line.isEmpty()) {
                continue;
            }
            sb.append(i + 1).append(". ").append(line).append("\n");
        }

        if (sb.length() == 0) {
            return "No events yet.";
        }
        return sb.toString().trim();
    }

    private String extractEventDisplayLine(Object raw) {
        if (raw == null) return "";
        if (raw instanceof String) return ((String) raw).trim();
        if (!(raw instanceof JSONObject)) return String.valueOf(raw);

        JSONObject obj = (JSONObject) raw;
        String title = obj.optString("title",
                obj.optString("event",
                        obj.optString("name", ""))).trim();
        String id = obj.optString("eventId", obj.optString("id", "")).trim();
        String when = obj.optString("when",
                obj.optString("timestamp",
                        obj.optString("scheduledAt",
                                obj.optString("eventTime", "")))).trim();
        String location = obj.optString("location",
                obj.optString("eventLocation",
                        obj.optString("place", ""))).trim();
        String description = obj.optString("description",
                obj.optString("details",
                        obj.optString("content", ""))).trim();

        String titleWithId = title.isEmpty()
                ? ""
                : (id.isEmpty() ? title : title + " (ID: " + id + ")");

        StringBuilder line = new StringBuilder();
        if (!titleWithId.isEmpty()) {
            line.append(titleWithId);
        }
        if (!when.isEmpty()) {
            if (line.length() > 0) line.append("\n");
            line.append("Time: ").append(when);
        }
        if (!location.isEmpty()) {
            if (line.length() > 0) line.append("\n");
            line.append("Location: ").append(location);
        }
        if (!description.isEmpty()) {
            if (line.length() > 0) line.append("\n");
            line.append("Description: ").append(description);
        }

        if (line.length() == 0) return obj.toString();
        return line.toString();
    }

    private void loadPinnedAnnouncement() {
        moderatorRepository.getGroupAnnouncementsForChat(this, groupId, moderatorId, new ModeratorRepository.JsonArrayCallback() {
            @Override
            public void onSuccess(JSONArray array) {
                runOnUiThread(() -> renderPinnedAnnouncement(array));
            }

            @Override
            public void onError(String error) {
                Log.d(TAG, "Pinned announcement unavailable: " + error);
                runOnUiThread(() -> hidePinnedAnnouncement());
            }
        });
    }

    private void renderPinnedAnnouncement(JSONArray announcements) {
        JSONObject pinned = selectPinnedAnnouncement(announcements);
        if (pinned == null) {
            hidePinnedAnnouncement();
            return;
        }

        String displayText = formatPinnedAnnouncement(pinned);
        if (displayText.isEmpty()) {
            hidePinnedAnnouncement();
            return;
        }

        tvPinnedAnnouncement.setText(displayText);
        layoutPinnedAnnouncement.setVisibility(View.VISIBLE);
    }

    private void hidePinnedAnnouncement() {
        tvPinnedAnnouncement.setText("");
        layoutPinnedAnnouncement.setVisibility(View.GONE);
    }

    private JSONObject selectPinnedAnnouncement(JSONArray announcements) {
        if (announcements == null || announcements.length() == 0) {
            return null;
        }

        for (int i = 0; i < announcements.length(); i++) {
            JSONObject obj = announcements.optJSONObject(i);
            if (obj == null) {
                continue;
            }
            if (obj.optBoolean("pinned", false) || obj.optBoolean("isPinned", false)) {
                return obj;
            }
        }
        return null;
    }

    private String formatPinnedAnnouncement(JSONObject announcement) {
        if (announcement == null) {
            return "";
        }

        String title = announcement.optString("title", "").trim();
        String content = announcement.optString("content",
                announcement.optString("announcement", "")).trim();

        if (title.isEmpty() && content.isEmpty()) {
            return "";
        }
        if (title.isEmpty()) {
            return content;
        }
        if (content.isEmpty()) {
            return title;
        }
        return title + "\n" + content;
    }
}
