package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {

    private TextView tvWelcomeName, tvAvatarInitial, tvProfileInitial;
    private TextView tvProfileName, tvProfileEmail, tvProfileBio;
    private TextView tvHobby1, tvHobby2;
    private TextView btnEditProfile, btnSeeAllGroups;
    private LinearLayout navProfile, navGroups, navMembers;

    private int userId;
    private String userJson;

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

        navMembers.setOnClickListener(v -> {
        });
    }
}