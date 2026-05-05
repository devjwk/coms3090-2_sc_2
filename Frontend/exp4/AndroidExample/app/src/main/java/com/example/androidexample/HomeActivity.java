package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;


import android.view.View;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONArray;

import org.java_websocket.handshake.ServerHandshake;

import android.widget.Button;


/**
 * Main dashboard activity shown to the user after a successful login.
 * This screen displays the user's profile information, including their name,
 * email, bio, and hobbies. It also serves as a navigation hub to access
 * groups, group memberships, and matches.
 */
public class HomeActivity extends AppCompatActivity implements NotificationWebSocketListener {

    /** TextViews for displaying user greeting and initials */
    private TextView tvWelcomeName, tvAvatarInitial, tvProfileInitial;

    /** TextViews for displaying profile details: name, email, and bio */
    private TextView tvProfileName, tvProfileEmail, tvProfileBio;

    /** TextViews for displaying up to two hobbies of the user */
    private TextView tvHobby1, tvHobby2;

    /** Interactive text elements used as buttons for profile editing and viewing all groups */
    private TextView btnEditProfile, btnSeeAllGroups;

    /** Bottom navigation bar items represented as clickable layouts */
    private LinearLayout navProfile, navGroups, navMembers, navMatches;

    /** Unique identifier for the logged-in user */
    private int userId;

    /** Raw JSON string containing user details passed from the login screen */
    private String userJson;

    private androidx.cardview.widget.CardView cardGroup1, cardGroup2;

    private TextView tvGroup1Name, tvGroup1Desc, tvGroup2Name, tvGroup2Desc;

    private LinearLayout layoutNotificationBanner;
    private TextView tvNotificationBanner;
    private Button btnReport;

    /**
     * Initializes the activity, sets up the layout, retrieves user data from the intent,
     * populates profile views, and configures navigation listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        userId   = getIntent().getIntExtra("USER_ID", 1);
        userJson = getIntent().getStringExtra("USER_JSON");

        String wsUrl = "ws://coms-3090-015.class.las.iastate.edu:8080/chat/" + userId;
        Log.d("HomeActivity", "Connecting WebSocket early: " + wsUrl);
        WebSocketClientManager.getInstance().setCurrentUserId(userId);
        WebSocketClientManager.getInstance().connectWebSocket(wsUrl);

        tvWelcomeName  = findViewById(R.id.tvWelcomeName);
        tvAvatarInitial  = findViewById(R.id.tvAvatarInitial);
        tvProfileInitial = findViewById(R.id.tvProfileInitial);
        tvProfileName  = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileBio   = findViewById(R.id.tvProfileBio);
        tvHobby1       = findViewById(R.id.tvHobby1);
        tvHobby2       = findViewById(R.id.tvHobby2);
        btnEditProfile   = findViewById(R.id.btnEditProfile);
        btnSeeAllGroups  = findViewById(R.id.btnSeeAllGroups);
        navProfile     = findViewById(R.id.navProfile);
        navGroups      = findViewById(R.id.navGroups);
        navMembers     = findViewById(R.id.navMembers);
        navMatches     = findViewById(R.id.navMatches);
        cardGroup1     = findViewById(R.id.cardGroup1);
        cardGroup2     = findViewById(R.id.cardGroup2);
        tvGroup1Name   = findViewById(R.id.tvGroup1Name);
        tvGroup1Desc   = findViewById(R.id.tvGroup1Desc);
        tvGroup2Name   = findViewById(R.id.tvGroup2Name);
        tvGroup2Desc   = findViewById(R.id.tvGroup2Desc);

        layoutNotificationBanner = findViewById(R.id.layoutNotificationBanner);
        tvNotificationBanner = findViewById(R.id.tvNotificationBanner);


        btnReport      = findViewById(R.id.btnReport);



        // Parse and display user information if the JSON data is available
        if (userJson != null && !userJson.isEmpty()) {
            try {
                org.json.JSONObject jo = new org.json.JSONObject(userJson);
                String name  = jo.optString("name", jo.optString("displayName", "User"));
                String email = jo.optString("email", "");
                String bio   = jo.optString("bio", "");

                String initial = name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase();

                tvWelcomeName.setText(name + " 👋");
                tvAvatarInitial.setText(initial);
                tvProfileInitial.setText(initial);
                tvProfileName.setText(name);
                tvProfileEmail.setText(email);
                tvProfileBio.setText(bio);

                if (jo.has("hobbies") && !jo.isNull("hobbies")) {
                    org.json.JSONArray hobbies = jo.getJSONArray("hobbies");
                    if (hobbies.length() > 0) tvHobby1.setText(hobbies.optString(0));
                    if (hobbies.length() > 1) tvHobby2.setText(hobbies.optString(1));
                }

            } catch (org.json.JSONException e) {
                e.printStackTrace();
            }
        }
        tvGroup1Name.setText("Badminton Club");
        tvGroup1Desc.setText("Weekly badminton sessions");
        tvGroup2Name.setText("Coding Club");
        tvGroup2Desc.setText("Let's code together");


        // Set navigation listeners
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, Login.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USER_JSON", userJson);
            startActivity(intent);
        });

        navGroups.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, GroupsActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USER_JSON", userJson);
            startActivity(intent);
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SwipeActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });


        cardGroup1.setOnClickListener(v -> openGroupRecommendActivity());
        cardGroup2.setOnClickListener(v -> openGroupRecommendActivity());

        // 🤝 Navigation to group membership management
        navMembers.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, GroupMembershipActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        // 🔥 Navigation to matches management
        navMatches.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MatchesActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });
        LinearLayout navChat = findViewById(R.id.navChat);
        navChat.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ChatListActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        NotificationWebSocketManager.getInstance().setNotificationWebSocketListener(this);
        connectNotificationSocket();

//        //dummy test notification
//        layoutNotificationBanner.postDelayed(() -> {
//            String fakeMessage = "{"
//                    + "\"type\":\"GROUP_JOIN\","
//                    + "\"message\":\"John joined Coding Club\","
//                    + "\"timestamp\":\"\""
//                    + "}";
//
//            handleNotificationMessage(fakeMessage);
//        }, 2000);


        btnReport.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ReportSubmitActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USER_JSON", userJson);
            startActivity(intent);

        });
        View btnNotificationCenter = findViewById(R.id.btnNotificationCenter);

        if (btnNotificationCenter != null) {
            btnNotificationCenter.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, NotificationActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            });
        }


    }
    private void openGroupRecommendActivity() {
        Intent intent = new Intent(HomeActivity.this, GroupRecommendActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("USER_JSON", userJson);
        startActivity(intent);
    }

    private void handleNotificationMessage(String message) {
        try {
            Log.d("NOTIFY_DEBUG", "RAW DATA: " + message);

            org.json.JSONObject json = new org.json.JSONObject(message);

            String type = json.optString("type", "GENERAL");
            String body = json.optString("message", "");
            String timestampStr = json.optString("timestamp", "");

            String formattedMessage = NotificationFormatter.formatNotification(type, body, timestampStr);

            runOnUiThread(() -> showTopBanner(formattedMessage));

        } catch (Exception e) {
            Log.e("NOTIFY_DEBUG", "Parsing Failed: " + e.getMessage());

            String fallbackMessage = message == null || message.isEmpty()
                    ? "New notification"
                    : message;

            runOnUiThread(() -> showTopBanner(fallbackMessage));
        }
    }

    private void showTopBanner(String message) {
        tvNotificationBanner.setText(message);
        layoutNotificationBanner.setVisibility(View.VISIBLE);

        layoutNotificationBanner.removeCallbacks(hideBannerRunnable);
        layoutNotificationBanner.postDelayed(hideBannerRunnable, 3000);
    }

    // --- Matches / Conversation helpers ---
    private static final String BASE_URL = "http://coms-3090-015.class.las.iastate.edu:8080";

    /**
     * Ensures a direct conversation exists for every ACCEPTED match for this user.
     * If the backend already has a conversation this POST should be idempotent.
     */
    private void fetchAcceptedMatchesAndEnsureConversations() {
        String url = BASE_URL + "/matches/user/" + userId;
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            org.json.JSONObject match = response.getJSONObject(i);
                            String status = match.optString("status", "");
                            if (status == null) status = "";
                            if ("ACCEPTED".equalsIgnoreCase(status) || "MATCHED".equalsIgnoreCase(status)) {
                                int u1 = match.optInt("user1Id", -1);
                                int u2 = match.optInt("user2Id", -1);
                                if (u1 > 0 && u2 > 0) {
                                    int other = (u1 == userId) ? u2 : u1;
                                    if (other > 0) sendDirectConversationPost(userId, other);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("HomeActivity", "Error parsing matches response: " + e.getMessage());
                    }
                },
                error -> Log.e("HomeActivity", "Failed to fetch matches: " + error)
        );
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    /**
     * POST /conversations/direct with both user IDs to ensure the conversation exists
     * and both users are added as participants.
     */
    private void sendDirectConversationPost(int user1Id, int user2Id) {
        String url = BASE_URL + "/conversations/direct";
        org.json.JSONObject body = new org.json.JSONObject();
        try {
            body.put("user1Id", user1Id);
            body.put("user2Id", user2Id);
        } catch (Exception e) {
            Log.e("HomeActivity", "Error building request body", e);
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> Log.d("HomeActivity", "Ensured direct conversation: " + response),
                error -> Log.e("HomeActivity", "Failed to ensure direct conversation: " + error)
        ) {
            @Override
            public byte[] getBody() {
                return body.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };
        request.setShouldCache(false);
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private final Runnable hideBannerRunnable = new Runnable() {
        @Override
        public void run() {
            layoutNotificationBanner.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotificationWebSocketManager.getInstance().removeNotificationWebSocketListener();
    }

    private void connectNotificationSocket() {
        String wsUrl = "ws://coms-3090-015.class.las.iastate.edu:8080/uver/notify/" + userId;
        NotificationWebSocketManager.getInstance().connectWebSocket(wsUrl);
    }
    @Override
    public void onNotificationOpen(ServerHandshake handshakedata) {
        Log.d("HOME_WS", "Notification WebSocket Connected");
    }

    @Override
    public void onNotificationMessage(String message) {
        Log.d("HOME_WS", "Notification Received: " + message);
        runOnUiThread(() -> handleNotificationMessage(message));
    }

    @Override
    public void onNotificationClose(int code, String reason, boolean remote) {
        Log.d("HOME_WS", "Notification WebSocket Closed: " + reason);
    }

    @Override
    public void onNotificationError(Exception ex) {
        Log.e("HOME_WS", "Notification WebSocket Error: " + ex.getMessage());
    }
}
