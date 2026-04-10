package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GroupChatListActivity extends AppCompatActivity {

    private static final String TAG = "GroupChatList";
    private static final String BASE_URL = "http://coms-3090-015.class.las.iastate.edu:8080";

    private LinearLayout groupListContainer;
    private TextView tvEmptyState;
    private EditText etGroupChatName, etUserIds;
    private Button btnCreateGroupChat;
    private int currentUserId;

    private static class GroupInfo {
        int groupId;
        String groupName;
        String description;
        GroupInfo(int id, String name, String desc) {
            this.groupId = id;
            this.groupName = name;
            this.description = desc;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_list);

        currentUserId     = getIntent().getIntExtra("USER_ID", 1);
        groupListContainer = findViewById(R.id.groupListContainer);
        tvEmptyState       = findViewById(R.id.tvEmptyState);
        Button btnBack     = findViewById(R.id.btnBack);

        etGroupChatName    = findViewById(R.id.etGroupChatName);
        etUserIds          = findViewById(R.id.etUserIds);
        btnCreateGroupChat = findViewById(R.id.btnCreateGroupChat);

        btnBack.setOnClickListener(v -> finish());
        btnCreateGroupChat.setOnClickListener(v -> createGroupChat());

        fetchMyGroups();
    }

    private void createGroupChat() {
        String name = etGroupChatName.getText().toString().trim();
        String userIdsStr = etUserIds.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Enter a group chat name.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userIdsStr.isEmpty()) {
            Toast.makeText(this, "Enter at least one user ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONArray userIdsArray = new JSONArray();
        try {
            String[] parts = userIdsStr.split(",");
            for (String part : parts) {
                int uid = Integer.parseInt(part.trim());
                userIdsArray.put(uid);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid user IDs. Use comma-separated numbers.", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject();
        try {
            body.put("name", name);
            body.put("userIds", userIdsArray);
        } catch (JSONException e) {
            Log.e(TAG, "Error building JSON body", e);
            return;
        }

        String url = BASE_URL + "/conversations/group";
        Log.d(TAG, "POST " + url + " body=" + body);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d(TAG, "Create group chat response: " + response);
                    Toast.makeText(this, "Group chat created!", Toast.LENGTH_SHORT).show();
                    etGroupChatName.setText("");
                    etUserIds.setText("");
                    fetchMyGroups();
                },
                error -> {
                    Log.e(TAG, "Create group chat error: " + error);
                    String msg = "Failed to create group chat.";
                    if (error.networkResponse != null) {
                        msg += " Status: " + error.networkResponse.statusCode;
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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

    private void fetchMyGroups() {
        String url = BASE_URL + "/groups";
        Log.d(TAG, "GET " + url);

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
                            showEmpty("No groups available yet.\nCreate one above!");
                        } else {
                            tvEmptyState.setVisibility(TextView.GONE);
                            buildGroupCards(groups);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Parse error: " + e.getMessage());
                        showEmpty("Could not load groups.");
                    }
                },
                error -> {
                    Log.e(TAG, "Fetch error: " + error);
                    showEmpty("Could not load groups from server.");
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
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

            // Group avatar
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

            // Text column
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


            card.addView(textCol);

            TextView arrow = new TextView(this);
            arrow.setText("›");
            arrow.setTextColor(Color.parseColor("#7B6FFF"));
            arrow.setTextSize(22);
            card.addView(arrow);

            // Open group chat on tap
            final int gId = g.groupId;
            final String gName = g.groupName;
            card.setOnClickListener(v -> {
                Intent intent = new Intent(GroupChatListActivity.this, GroupChatActivity.class);
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
            tvEmptyState.setVisibility(TextView.VISIBLE);
            groupListContainer.removeAllViews();
        });
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
