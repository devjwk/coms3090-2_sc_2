package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ChatListActivity extends AppCompatActivity {

    private static final String TAG = "ChatListActivity";
    private static final String BASE_URL = "http://coms-3090-015.class.las.iastate.edu:8080";

    private LinearLayout chatListContainer, groupListContainer;
    private ScrollView scrollDirect, scrollGroups;
    private TextView tvEmptyState;
    private Button tabDirect, tabGroups;
    private int currentUserId;

    private boolean isDirectTab = true;
    private boolean directLoaded = false;
    private boolean groupsLoaded = false;

    private static class MatchedUser {
        int userId;
        String name;
        MatchedUser(int userId) { this.userId = userId; this.name = "User " + userId; }
    }

    private static class GroupInfo {
        int groupId;
        String groupName;
        String description;
        List<String> memberNames = new ArrayList<>();
        List<Integer> memberIds = new ArrayList<>();
        GroupInfo(int id, String name, String desc) {
            this.groupId = id;
            this.groupName = name;
            this.description = desc;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        currentUserId      = getIntent().getIntExtra("USER_ID", 1);
        chatListContainer  = findViewById(R.id.chatListContainer);
        groupListContainer = findViewById(R.id.groupListContainer);
        scrollDirect       = findViewById(R.id.scrollDirect);
        scrollGroups       = findViewById(R.id.scrollGroups);
        tvEmptyState       = findViewById(R.id.tvEmptyState);
        tabDirect          = findViewById(R.id.tabDirect);
        tabGroups          = findViewById(R.id.tabGroups);
        Button btnBack     = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        tabDirect.setOnClickListener(v -> switchTab(true));
        tabGroups.setOnClickListener(v -> switchTab(false));

        fetchAcceptedMatches();
        fetchAllGroups();
    }

    private void switchTab(boolean direct) {
        isDirectTab = direct;

        if (direct) {
            tabDirect.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#7B6FFF")));
            tabDirect.setTextColor(Color.WHITE);
            tabGroups.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2A2A42")));
            tabGroups.setTextColor(Color.parseColor("#9B9BB4"));
            scrollDirect.setVisibility(View.VISIBLE);
            scrollGroups.setVisibility(View.GONE);

            if (!directLoaded) {
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("Loading matches...");
            } else if (chatListContainer.getChildCount() == 0) {
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("No matches yet — swipe to find people!");
            } else {
                tvEmptyState.setVisibility(View.GONE);
            }
        } else {
            tabGroups.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#7B6FFF")));
            tabGroups.setTextColor(Color.WHITE);
            tabDirect.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2A2A42")));
            tabDirect.setTextColor(Color.parseColor("#9B9BB4"));
            scrollDirect.setVisibility(View.GONE);
            scrollGroups.setVisibility(View.VISIBLE);

            if (!groupsLoaded) {
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("Loading groups...");
            } else if (groupListContainer.getChildCount() == 0) {
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("No groups available yet.");
            } else {
                tvEmptyState.setVisibility(View.GONE);
            }
        }
    }


    private void fetchAcceptedMatches() {
        String url = BASE_URL + "/matches/user/" + currentUserId;
        Log.d(TAG, "GET " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d(TAG, "Matches response: " + response);
                    try {
                        JSONArray arr = new JSONArray(response);
                        List<MatchedUser> matched = new ArrayList<>();

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject m = arr.getJSONObject(i);
                            String status = m.optString("status", "");

                            if ("ACCEPTED".equalsIgnoreCase(status)) {
                                int u1 = m.optInt("user1Id", -1);
                                int u2 = m.optInt("user2Id", -1);
                                int otherId = (u1 == currentUserId) ? u2 : u1;

                                if (otherId > 0) {
                                    boolean dup = false;
                                    for (MatchedUser mu : matched) {
                                        if (mu.userId == otherId) { dup = true; break; }
                                    }
                                    if (!dup) matched.add(new MatchedUser(otherId));
                                }
                            }
                        }

                        if (matched.isEmpty()) {
                            directLoaded = true;
                            if (isDirectTab) showEmpty("No matches yet — swipe to find people!");
                        } else {
                            fetchNamesAndBuildList(matched, 0);
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "Parse error: " + e.getMessage());
                        directLoaded = true;
                        if (isDirectTab) showEmpty("Could not load matches.");
                    }
                },
                error -> {
                    Log.e(TAG, "Fetch matches error: " + error);
                    directLoaded = true;
                    if (isDirectTab) showEmpty("No matches yet — swipe to find people!");
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void fetchNamesAndBuildList(List<MatchedUser> list, int index) {
        if (index >= list.size()) {
            directLoaded = true;
            runOnUiThread(() -> {
                buildChatCards(list);
                if (isDirectTab) tvEmptyState.setVisibility(View.GONE);
            });
            return;
        }

        MatchedUser mu = list.get(index);
        String url = BASE_URL + "/users/" + mu.userId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject user = new JSONObject(response);
                        String name = user.optString("name", "");
                        if (name.isEmpty()) name = user.optString("displayName", "");
                        if (name.isEmpty()) name = "User " + mu.userId;
                        mu.name = name;
                    } catch (Exception e) { }
                    fetchNamesAndBuildList(list, index + 1);
                },
                error -> fetchNamesAndBuildList(list, index + 1)
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void buildChatCards(List<MatchedUser> list) {
        chatListContainer.removeAllViews();

        for (MatchedUser mu : list) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(dp(16), dp(14), dp(16), dp(14));
            card.setBackgroundColor(Color.parseColor("#1E1E30"));

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(dp(12), dp(4), dp(12), dp(4));
            card.setLayoutParams(cardParams);

            FrameLayout avatar = new FrameLayout(this);
            LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(48), dp(48));
            avatarParams.setMargins(0, 0, dp(14), 0);
            avatar.setLayoutParams(avatarParams);
            avatar.setBackgroundColor(Color.parseColor("#7B6FFF"));

            TextView avatarText = new TextView(this);
            avatarText.setText(mu.name.isEmpty() ? "?" : String.valueOf(mu.name.charAt(0)).toUpperCase());
            avatarText.setTextColor(Color.WHITE);
            avatarText.setTextSize(20);
            avatarText.setGravity(Gravity.CENTER);
            avatarText.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
            avatar.addView(avatarText);
            card.addView(avatar);

            LinearLayout textCol = new LinearLayout(this);
            textCol.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            textCol.setLayoutParams(textParams);

            TextView tvName = new TextView(this);
            tvName.setText(mu.name);
            tvName.setTextColor(Color.WHITE);
            tvName.setTextSize(16);
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);
            textCol.addView(tvName);

            TextView tvSub = new TextView(this);
            tvSub.setText("Tap to chat");
            tvSub.setTextColor(Color.parseColor("#9B9BB4"));
            tvSub.setTextSize(12);
            textCol.addView(tvSub);

            card.addView(textCol);

            TextView arrow = new TextView(this);
            arrow.setText("›");
            arrow.setTextColor(Color.parseColor("#7B6FFF"));
            arrow.setTextSize(22);
            card.addView(arrow);

            final int otherUserId = mu.userId;
            final String otherName = mu.name;
            card.setOnClickListener(v -> {
                Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
                intent.putExtra("USER_ID", currentUserId);
                intent.putExtra("OTHER_USER_ID", otherUserId);
                intent.putExtra("OTHER_USERNAME", otherName);
                startActivity(intent);
            });

            chatListContainer.addView(card);
        }
    }


    private void fetchAllGroups() {
        String url = BASE_URL + "/groups/me/"+ currentUserId;
        Log.d(TAG, "GET groups: " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d(TAG, "Groups response: " + response);
                    try {
                        JSONArray arr = new JSONArray(response);
                        List<GroupInfo> groups = new ArrayList<>();

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            int gid = obj.optInt("groupId", obj.optInt("groupid",
                                    obj.optInt("group_id", -1)));
                            String name = obj.optString("groupName",
                                    obj.optString("groupname",
                                            obj.optString("group_name", "Group " + gid)));
                            String desc = obj.optString("description", "");
                            if (gid > 0) groups.add(new GroupInfo(gid, name, desc));
                        }

                        if (groups.isEmpty()) {
                            groupsLoaded = true;
                            if (!isDirectTab) showGroupsEmpty("No groups available yet.");
                        } else {
                            fetchGroupMembers(groups, 0);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Groups parse error: " + e.getMessage());
                        groupsLoaded = true;
                        if (!isDirectTab) showGroupsEmpty("Could not load groups.");
                    }
                },
                error -> {
                    Log.e(TAG, "Fetch groups error: " + error);
                    groupsLoaded = true;
                    if (!isDirectTab) showGroupsEmpty("Could not load groups from server.");
                });

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }


    private void fetchGroupMembers(List<GroupInfo> groups, int index) {
        if (index >= groups.size()) {
            resolveGroupMemberNames(groups, 0, 0);
            return;
        }

        GroupInfo g = groups.get(index);
        String url = BASE_URL + "/gm/glist/" + g.groupId;
        Log.d(TAG, "GET members for group " + g.groupId + ": " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONArray arr = new JSONArray(response);
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            int uid = obj.optInt("userId", obj.optInt("userid", -1));
                            String name = obj.optString("displayName",
                                    obj.optString("displayname",
                                            obj.optString("name",
                                                    obj.optString("userName",
                                                            obj.optString("username", "")))));

                            if (name.isEmpty() && obj.has("user")) {
                                JSONObject userObj = obj.optJSONObject("user");
                                if (userObj != null) {
                                    name = userObj.optString("name",
                                            userObj.optString("displayName", ""));
                                }
                            }

                            if (uid > 0) {
                                g.memberIds.add(uid);
                                if (name.isEmpty()) name = "User " + uid;
                                g.memberNames.add(name);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing members for group " + g.groupId + ": " + e.getMessage());
                    }
                    fetchGroupMembers(groups, index + 1);
                },
                error -> {
                    Log.e(TAG, "Failed to fetch members for group " + g.groupId);
                    fetchGroupMembers(groups, index + 1);
                });

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }


    private void resolveGroupMemberNames(List<GroupInfo> groups, int groupIdx, int memberIdx) {
        if (groupIdx >= groups.size()) {
            groupsLoaded = true;
            runOnUiThread(() -> {
                buildGroupCards(groups);
                if (!isDirectTab) tvEmptyState.setVisibility(View.GONE);
            });
            return;
        }

        GroupInfo g = groups.get(groupIdx);
        if (memberIdx >= g.memberIds.size()) {
            resolveGroupMemberNames(groups, groupIdx + 1, 0);
            return;
        }

        int uid = g.memberIds.get(memberIdx);
        String url = BASE_URL + "/users/" + uid;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject user = new JSONObject(response);
                        String name = user.optString("name", "");
                        if (name.isEmpty()) name = user.optString("displayName", "");
                        if (!name.isEmpty()) {
                            g.memberNames.set(memberIdx, name);
                        }
                    } catch (Exception e) { }
                    resolveGroupMemberNames(groups, groupIdx, memberIdx + 1);
                },
                error -> resolveGroupMemberNames(groups, groupIdx, memberIdx + 1));

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void buildGroupCards(List<GroupInfo> groups) {
        groupListContainer.removeAllViews();

        for (GroupInfo g : groups) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(dp(16), dp(14), dp(16), dp(14));
            card.setBackgroundColor(Color.parseColor("#1E1E30"));

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(dp(12), dp(4), dp(12), dp(4));
            card.setLayoutParams(cardParams);

            FrameLayout avatar = new FrameLayout(this);
            LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(48), dp(48));
            avatarParams.setMargins(0, 0, dp(14), 0);
            avatar.setLayoutParams(avatarParams);
            avatar.setBackgroundColor(Color.parseColor("#7B6FFF"));

            TextView avatarText = new TextView(this);
            avatarText.setText("👥");
            avatarText.setTextSize(20);
            avatarText.setGravity(Gravity.CENTER);
            avatarText.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
            avatar.addView(avatarText);
            card.addView(avatar);

            LinearLayout textCol = new LinearLayout(this);
            textCol.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            textCol.setLayoutParams(textParams);

            TextView tvName = new TextView(this);
            tvName.setText(g.groupName);
            tvName.setTextColor(Color.WHITE);
            tvName.setTextSize(16);
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);
            textCol.addView(tvName);

            if (g.description != null && !g.description.isEmpty()) {
                TextView tvDesc = new TextView(this);
                tvDesc.setText(g.description);
                tvDesc.setTextColor(Color.parseColor("#9B9BB4"));
                tvDesc.setTextSize(12);
                tvDesc.setMaxLines(1);
                textCol.addView(tvDesc);
            }


            card.addView(textCol);

            TextView arrow = new TextView(this);
            arrow.setText("›");
            arrow.setTextColor(Color.parseColor("#7B6FFF"));
            arrow.setTextSize(22);
            card.addView(arrow);

            final int gId = g.groupId;
            final String gName = g.groupName;
            card.setOnClickListener(v -> {
                Intent intent = new Intent(ChatListActivity.this, GroupChatActivity.class);
                intent.putExtra("USER_ID", currentUserId);
                intent.putExtra("GROUP_ID", gId);
                intent.putExtra("GROUP_NAME", gName);
                startActivity(intent);
            });

            groupListContainer.addView(card);
        }
    }


    private void showEmpty(String msg) {
        runOnUiThread(() -> {
            tvEmptyState.setText(msg);
            tvEmptyState.setVisibility(View.VISIBLE);
            chatListContainer.removeAllViews();
        });
    }

    private void showGroupsEmpty(String msg) {
        runOnUiThread(() -> {
            tvEmptyState.setText(msg);
            tvEmptyState.setVisibility(View.VISIBLE);
            groupListContainer.removeAllViews();
        });
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}

