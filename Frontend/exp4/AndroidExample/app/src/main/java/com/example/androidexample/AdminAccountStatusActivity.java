package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AdminAccountStatusActivity extends AppCompatActivity {

    private static final String TAG = "AdminAccountStatus";
    private static final String BASE_URL = "http://coms-3090-015.class.las.iastate.edu:8080";

    private LinearLayout userStatusContainer;
    private Button btnRefreshAccountStatus;
    private Button btnBackAccountStatus;

    private int adminUserId;
    private final List<AdminUserStatusItem> users = new ArrayList<>();

    private static class AdminUserStatusItem {
        long userId;
        String email;
        String displayName;
        String status;
        boolean active;

        AdminUserStatusItem(long userId, String email, String displayName, String status, boolean active) {
            this.userId = userId;
            this.email = email;
            this.displayName = displayName;
            this.status = status;
            this.active = active;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_account_status);

        adminUserId = getIntent().getIntExtra("ADMIN_USER_ID", -1);

        userStatusContainer = findViewById(R.id.userStatusContainer);
        btnRefreshAccountStatus = findViewById(R.id.btnRefreshAccountStatus);
        btnBackAccountStatus = findViewById(R.id.btnBackAccountStatus);

        btnRefreshAccountStatus.setOnClickListener(v -> fetchUsers());
        btnBackAccountStatus.setOnClickListener(v -> finish());

        fetchUsers();
    }

    private void fetchUsers() {
        String url = BASE_URL + "/users/all";
        Log.d(TAG, "GET " + url);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    Log.d(TAG, "Users raw response: " + response);
                    users.clear();

                    try {
                        org.json.JSONArray userArray = extractUserArray(response);

                        for (int i = 0; i < userArray.length(); i++) {
                            JSONObject user = userArray.getJSONObject(i);

                            long id = getLongAny(user, "userID", "userId", "user_id", "id");
                            if (id <= 0) {
                                Log.w(TAG, "Skipping user with missing ID: " + user.toString());
                                continue;
                            }

                            String email = getStringAny(user,
                                    "email", "userEmail", "user_email", "mail");

                            String displayName = getStringAny(user,
                                    "displayName", "displayname", "name", "userName", "username");

                            if (displayName.isEmpty()) {
                                displayName = "User " + id;
                            }

                            users.add(new AdminUserStatusItem(
                                    id,
                                    email,
                                    displayName,
                                    user.optString("status", "UNKNOWN"),
                                    user.optBoolean("active", true)
                            ));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse users response", e);
                        Toast.makeText(this, "Failed to parse users", Toast.LENGTH_SHORT).show();
                    }

                    renderUsers();

                    for (AdminUserStatusItem user : new ArrayList<>(users)) {
                        if ((user.email == null || user.email.trim().isEmpty()) && user.userId > 0) {
                            fetchUserDetailsAndRefresh(user);
                        }
                    }
                },
                error -> {
                    int statusCode = error.networkResponse != null ? error.networkResponse.statusCode : -1;
                    String errorBody = "";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        errorBody = new String(error.networkResponse.data);
                    }
                    Log.e(TAG, "Failed to fetch users. HTTP " + statusCode + " body=" + errorBody);
                    Toast.makeText(this, "Failed to load users: " + statusCode, Toast.LENGTH_SHORT).show();
                    renderUsers();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private org.json.JSONArray extractUserArray(String response) throws JSONException {
        if (response == null || response.trim().isEmpty()) {
            return new org.json.JSONArray();
        }

        String trimmed = response.trim();

        if (trimmed.startsWith("[")) {
            return new org.json.JSONArray(trimmed);
        }

        JSONObject object = new JSONObject(trimmed);

        String[] possibleArrayKeys = new String[]{
                "users", "data", "results", "items", "content"
        };

        for (String key : possibleArrayKeys) {
            org.json.JSONArray array = object.optJSONArray(key);
            if (array != null) {
                return array;
            }
        }

        org.json.JSONArray singleUserArray = new org.json.JSONArray();
        singleUserArray.put(object);
        return singleUserArray;
    }

    private void renderUsers() {
        userStatusContainer.removeAllViews();

        if (users.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No users found.");
            emptyText.setTextColor(Color.parseColor("#CCCCCC"));
            emptyText.setTextSize(16);
            emptyText.setPadding(dp(8), dp(24), dp(8), dp(24));
            userStatusContainer.addView(emptyText);
            return;
        }

        for (AdminUserStatusItem user : new ArrayList<>(users)) {
            userStatusContainer.addView(createUserCard(user));
        }
    }

    private LinearLayout createUserCard(AdminUserStatusItem user) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.parseColor("#1E1E30"));
        card.setPadding(dp(16), dp(14), dp(16), dp(14));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cardParams);

        TextView nameText = new TextView(this);
        String displayName = user.displayName == null || user.displayName.trim().isEmpty()
                ? "User " + user.userId
                : user.displayName;
        nameText.setText(displayName + "  (#" + user.userId + ")");
        nameText.setTextColor(Color.WHITE);
        nameText.setTextSize(17);
        nameText.setTypeface(null, Typeface.BOLD);
        card.addView(nameText);

        TextView emailText = new TextView(this);
        String emailDisplay = user.email == null || user.email.trim().isEmpty()
                ? "Loading email..."
                : user.email;
        emailText.setText("Email: " + emailDisplay);
        emailText.setTextColor(Color.parseColor("#CCCCCC"));
        emailText.setTextSize(14);
        emailText.setPadding(0, dp(6), 0, 0);
        card.addView(emailText);

        TextView statusText = new TextView(this);
        statusText.setText("Status: " + user.status + " | Active: " + user.active);
        statusText.setTextColor(getStatusColor(user.status));
        statusText.setTextSize(14);
        statusText.setPadding(0, dp(6), 0, dp(10));
        card.addView(statusText);

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setGravity(Gravity.CENTER_VERTICAL);

        Button suspendButton = makeActionButton("Suspend", "#8A3A4A");
        Button disableButton = makeActionButton("Disable", "#D9534F");
        Button restoreButton = makeActionButton("Restore", "#7B6FFF");

        buttonRow.addView(suspendButton, makeButtonParams(true));
        buttonRow.addView(disableButton, makeButtonParams(true));
        buttonRow.addView(restoreButton, makeButtonParams(false));

        card.addView(buttonRow);

        suspendButton.setOnClickListener(v -> updateUserStatus(user, "SUSPENDED"));
        disableButton.setOnClickListener(v -> updateUserStatus(user, "DISABLED"));
        restoreButton.setOnClickListener(v -> updateUserStatus(user, "APPROVED"));

        return card;
    }

    private Button makeActionButton(String text, String colorHex) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextColor(Color.WHITE);
        button.setTextSize(13);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(colorHex)));
        return button;
    }

    private LinearLayout.LayoutParams makeButtonParams(boolean hasRightMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        if (hasRightMargin) {
            params.setMargins(0, 0, dp(8), 0);
        }
        return params;
    }

    private void updateUserStatus(AdminUserStatusItem user, String newStatus) {
        if (adminUserId <= 0) {
            Toast.makeText(this, "Admin ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/users/status_edit/" + user.userId + "?requesterID=" + adminUserId;
        Log.d(TAG, "PUT " + url + " status=" + newStatus);

        JSONObject body = new JSONObject();
        try {
            body.put("status", newStatus);
        } catch (JSONException e) {
            Toast.makeText(this, "Failed to build request", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.PUT,
                url,
                response -> handleStatusUpdateSuccess(user, newStatus),
                error -> {
                    int statusCode = error.networkResponse != null ? error.networkResponse.statusCode : -1;
                    String errorBody = "";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        errorBody = new String(error.networkResponse.data);
                    }
                    Log.e(TAG, "Update failed. HTTP " + statusCode + " body=" + errorBody);
                    Toast.makeText(this, "Status update failed: " + statusCode, Toast.LENGTH_SHORT).show();
                }
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

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void handleStatusUpdateSuccess(AdminUserStatusItem user, String newStatus) {
        user.status = newStatus;
        renderUsers();
        Toast.makeText(this, "User set to " + newStatus, Toast.LENGTH_SHORT).show();
    }

    private String getStringAny(JSONObject object, String... keys) {
        if (object == null || keys == null) {
            return "";
        }

        for (String key : keys) {
            String value = object.optString(key, "").trim();
            if (!value.isEmpty() && !"null".equalsIgnoreCase(value)) {
                return value;
            }
        }

        return "";
    }

    private long getLongAny(JSONObject object, String... keys) {
        if (object == null || keys == null) {
            return 0;
        }

        for (String key : keys) {
            if (!object.has(key) || object.isNull(key)) {
                continue;
            }

            Object raw = object.opt(key);
            if (raw instanceof Number) {
                long value = ((Number) raw).longValue();
                if (value > 0) {
                    return value;
                }
            }

            String valueText = String.valueOf(raw).trim();
            if (!valueText.isEmpty() && !"null".equalsIgnoreCase(valueText)) {
                try {
                    long value = Long.parseLong(valueText);
                    if (value > 0) {
                        return value;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return 0;
    }

    private void fetchUserDetailsAndRefresh(AdminUserStatusItem targetUser) {
        String url = BASE_URL + "/users/" + targetUser.userId;
        Log.d(TAG, "GET detail for missing email: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    String email = getStringAny(response,
                            "email", "userEmail", "user_email", "mail");

                    String displayName = getStringAny(response,
                            "displayName", "displayname", "name", "userName", "username");

                    String status = response.optString("status", targetUser.status);
                    boolean active = response.optBoolean("active", targetUser.active);

                    if (!email.isEmpty()) {
                        targetUser.email = email;
                    }
                    if (!displayName.isEmpty()) {
                        targetUser.displayName = displayName;
                    }
                    if (status != null && !status.trim().isEmpty()) {
                        targetUser.status = status;
                    }
                    targetUser.active = active;

                    renderUsers();
                },
                error -> {
                    int statusCode = error.networkResponse != null ? error.networkResponse.statusCode : -1;
                    Log.e(TAG, "Failed to fetch user detail for " + targetUser.userId + ". HTTP " + statusCode);
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private int getStatusColor(String status) {
        if (status == null) {
            return Color.parseColor("#AAAAAA");
        }
        switch (status.toUpperCase()) {
            case "APPROVED":
                return Color.parseColor("#7BFFB2");
            case "SUSPENDED":
            case "DISABLED":
            case "DECLINED":
                return Color.parseColor("#FFB86B");
            case "NEED_APPROVAL":
                return Color.parseColor("#9E97FF");
            default:
                return Color.parseColor("#AAAAAA");
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
