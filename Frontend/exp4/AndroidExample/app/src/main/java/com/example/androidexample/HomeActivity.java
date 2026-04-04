package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

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

        // Set navigation listeners
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, Login.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USER_JSON", userJson);
            startActivity(intent);
        });

        btnSeeAllGroups.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, GroupsActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, Login.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USER_JSON", userJson);
            startActivity(intent);
        });

        navGroups.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, GroupsActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

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
    }
}
