package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
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

    private LinearLayout chatListContainer;
    private TextView tvEmptyState;
    private int currentUserId;

    private static class MatchedUser {
        int userId;
        String name;
        MatchedUser(int userId) { this.userId = userId; this.name = "User " + userId; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        currentUserId    = getIntent().getIntExtra("USER_ID", 1);
        chatListContainer = findViewById(R.id.chatListContainer);
        tvEmptyState      = findViewById(R.id.tvEmptyState);
        Button btnBack    = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        fetchAcceptedMatches();
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
                            showEmpty("No matches yet — swipe to find people!");
                        } else {
                            tvEmptyState.setVisibility(TextView.GONE);
                            fetchNamesAndBuildList(matched, 0);
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "Parse error: " + e.getMessage());
                        showEmpty("Could not load matches.");
                    }
                },
                error -> {
                    Log.e(TAG, "Fetch matches error: " + error);
                    showEmpty("No matches yet — swipe to find people!");
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void fetchNamesAndBuildList(List<MatchedUser> list, int index) {
        if (index >= list.size()) {
            runOnUiThread(() -> buildChatCards(list));
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
                    } catch (Exception e) {
                    }
                    fetchNamesAndBuildList(list, index + 1);
                },
                error -> {
                    fetchNamesAndBuildList(list, index + 1);
                }
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
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
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

    private void showEmpty(String msg) {
        runOnUiThread(() -> {
            tvEmptyState.setText(msg);
            tvEmptyState.setVisibility(TextView.VISIBLE);
            chatListContainer.removeAllViews();
        });
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}

