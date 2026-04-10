package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;


import android.view.View;

import org.java_websocket.handshake.ServerHandshake;

import android.widget.Button;


/**
 * Main dashboard activity shown to the user after a successful login.
 * This screen displays the user's profile information, including their name,
 * email, bio, and hobbies. It also serves as a navigation hub to access
 * groups, group memberships, and matches.
 */
public class HomeActivity extends AppCompatActivity {

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

<<<<<<< Frontend/exp4/AndroidExample/app/src/main/java/com/example/androidexample/HomeActivity.java
    private LinearLayout layoutNotificationBanner;
    private TextView tvNotificationBanner;
=======
    private Button btnReport;

>>>>>>> Frontend/exp4/AndroidExample/app/src/main/java/com/example/androidexample/HomeActivity.java
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

        WebSocketClientManager.getInstance().setWebSocketEventListener(new WebSocketEventListener(){
            @Override
            public void onWebSocketOpen(ServerHandshake handshakedata) {
                android.util.Log.d("HOME_WS","WebSocket Connected");
            }
            @Override
            public void onWebSocketMessage(String message) {
                android.util.Log.d("HOME_WS", "Received: "+message);
                runOnUiThread(() -> handleNotificationMessage(message));}
            @Override
            public void onWebSocketClose(int code, String reason, boolean remote) {
                android.util.Log.d("HOME_WS","WebSocket Closed"+reason);
            }
            @Override
            public void onWebSocketError(Exception ex) {
                android.util.Log.d("HOME_WS","WebSocket Error"+ex.getMessage());
            }
        });
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
        connectNotificationSocket();

        btnReport.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ReportSubmitActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USER_JSON", userJson);
            startActivity(intent);

        });


    }
    private void openGroupRecommendActivity() {
        Intent intent = new Intent(HomeActivity.this, GroupRecommendActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("USER_JSON", userJson);
        startActivity(intent);
    }

    private void handleNotificationMessage(String message) {
        try {
            org.json.JSONObject json = new org.json.JSONObject(message);

            String type = json.optString("type", "GENERAL");
            String body = json.optString("message", "New notification");
            String timestamp = json.optString("timestamp", "");

            String formattedMessage =
                    NotificationFormatter.formatNotification(type, body, timestamp);

            showTopBanner(formattedMessage);

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void showTopBanner(String message) {
        tvNotificationBanner.setText(message);
        layoutNotificationBanner.setVisibility(View.VISIBLE);

        layoutNotificationBanner.removeCallbacks(hideBannerRunnable);
        layoutNotificationBanner.postDelayed(hideBannerRunnable, 3000);
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
        WebSocketClientManager.getInstance().removeWebSocketEventListener();
    }

    private void connectNotificationSocket() {
        String wsUrl = "ws://coms-3090-015.class.las.iastate.edu:8080/uver/notify/" + userId;
        WebSocketClientManager.getInstance().connectWebSocket(wsUrl);
    }

}
