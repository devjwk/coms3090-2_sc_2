package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.android.volley.Request;
import com.android.volley.AuthFailureError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class AdminModeratorRequestsActivity extends AppCompatActivity {

    private LinearLayout pendingRequestsContainer;
    private Button btnBackAdminRequests;
    private Button btnRefreshModeratorRequests;
    private int adminUserId;
    private final List<PendingModeratorRequest> pendingRequests = new ArrayList<>();
    private static final String ROOT_URL = "http://coms-3090-015.class.las.iastate.edu:8080";
    private static final String REQUESTER_PARAM = "requesterID";

    private static class PendingModeratorRequest {
        Long userId;
        String email;
        String displayName;
        String status;

        PendingModeratorRequest(Long userId, String email, String displayName, String status) {
            this.userId = userId;
            this.email = email;
            this.displayName = displayName;
            this.status = status;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_moderator_requests);

        adminUserId = getIntent().getIntExtra("ADMIN_USER_ID", -1);
        if (adminUserId <= 0) {
            Toast.makeText(
                    AdminModeratorRequestsActivity.this,
                    "Admin login required",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        pendingRequestsContainer = findViewById(R.id.pendingRequestsContainer);
        btnBackAdminRequests = findViewById(R.id.btnBackAdminRequests);
        btnRefreshModeratorRequests = findViewById(R.id.btnRefreshModeratorRequests);
        fetchPendingRequests();// Fetch pending moderator requests


        btnRefreshModeratorRequests.setOnClickListener(v -> {
            fetchPendingRequests();
            Toast.makeText(
                    AdminModeratorRequestsActivity.this,
                    "Moderator requests refreshed",
                    Toast.LENGTH_SHORT
            ).show();
        });

        btnBackAdminRequests.setOnClickListener(v -> finish());
    }

    private void renderPendingRequests() {
        pendingRequestsContainer.removeAllViews();

        if (pendingRequests.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No pending moderator requests.");
            emptyText.setTextColor(android.graphics.Color.parseColor("#CCCCCC"));
            emptyText.setTextSize(16);
            emptyText.setPadding(8, 24, 8, 24);

            pendingRequestsContainer.addView(emptyText);
            return;
        }

        for (PendingModeratorRequest request : new ArrayList<>(pendingRequests)) {
            pendingRequestsContainer.addView(createRequestCard(request));
        }
    }

    private LinearLayout createRequestCard(PendingModeratorRequest request) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(android.graphics.Color.parseColor("#1E1E30"));
        card.setPadding(28, 24, 28, 24);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 24);
        card.setLayoutParams(cardParams);

        TextView displayName = new TextView(this);
        displayName.setText(request.displayName);
        displayName.setTextColor(android.graphics.Color.WHITE);
        displayName.setTextSize(18);
        displayName.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(displayName);

        TextView email = new TextView(this);
        String emailText = request.email == null || request.email.trim().isEmpty()
                ? "Email: loading..."
                : "Email: " + request.email;
        email.setText(emailText);
        email.setTextColor(android.graphics.Color.parseColor("#CCCCCC"));
        email.setTextSize(14);
        email.setPadding(0, 8, 0, 0);
        card.addView(email);

        TextView status = new TextView(this);
        status.setText("Status: " + request.status);
        status.setTextColor(android.graphics.Color.parseColor("#AAAAAA"));
        status.setTextSize(14);
        status.setPadding(0, 8, 0, 0);
        card.addView(status);

        TextView isuCheck = new TextView(this);
        isuCheck.setText(getIsuEmailMessage(request.email));
        isuCheck.setTextColor(getIsuEmailColor(request.email));
        isuCheck.setTextSize(14);
        isuCheck.setPadding(0, 8, 0, 0);
        card.addView(isuCheck);

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setPadding(0, 18, 0, 0);

        Button approveButton = new Button(this);
        approveButton.setText("Approve");
        approveButton.setTextColor(android.graphics.Color.WHITE);
        approveButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#7B6FFF")
                )
        );

        Button rejectButton = new Button(this);
        rejectButton.setText("Reject");

        rejectButton.setTextColor(android.graphics.Color.WHITE);
        rejectButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#444444")
                )
        );

        LinearLayout.LayoutParams approveParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        approveParams.setMargins(0, 0, 12, 0);

        LinearLayout.LayoutParams rejectParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );

        buttonRow.addView(approveButton, approveParams);
        buttonRow.addView(rejectButton, rejectParams);

        card.addView(buttonRow);

        approveButton.setOnClickListener(v -> approveRequest(request));
        rejectButton.setOnClickListener(v -> rejectRequest(request));

        return card;
    }
    private void approveRequest(PendingModeratorRequest request) {
        updateUserStatus(request, "APPROVED", true);
    }

    private void handleSuccess(PendingModeratorRequest request) {
        pendingRequests.remove(request);
        renderPendingRequests();
        Toast.makeText(this, "approve success!", Toast.LENGTH_SHORT).show();
    }

    private void rejectRequest(PendingModeratorRequest request) {
        updateUserStatus(request, "DECLINED", false);
    }

    private void updateUserStatus(PendingModeratorRequest request, String newStatus, boolean approving) {
        if (request == null || request.userId == null || request.userId <= 0) {
            Toast.makeText(this, "Invalid user selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Brendan said the backend now verifies the requester with requesterID.
        // The body stays minimal because this endpoint only updates status.
        String url = ROOT_URL + "/users/status_edit/" + request.userId
                + "?" + REQUESTER_PARAM + "=" + adminUserId;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("status", newStatus);
        } catch (JSONException e) {
            Toast.makeText(this, "Failed to build status request", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("STATUS_UPDATE_URL", url);
        android.util.Log.d("STATUS_UPDATE_BODY", jsonBody.toString());

        StringRequest putRequest = new StringRequest(Request.Method.PUT, url,
                response -> {
                    android.util.Log.d("STATUS_UPDATE_OK", response == null ? "" : response);
                    handleStatusUpdateSuccess(request, approving);
                },
                error -> {
                    logVolleyError(approving ? "APPROVE_ERROR" : "REJECT_ERROR", error);
                    retryUserStatusWithLowercaseRequester(request, newStatus, approving);
                }
        ) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return jsonBody.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        putRequest.setShouldCache(false);
        VolleySingleton.getInstance(this).addToRequestQueue(putRequest);
    }

    private void retryUserStatusWithLowercaseRequester(PendingModeratorRequest request,
                                                       String newStatus,
                                                       boolean approving) {
        // Safety fallback in case the deployed backend accepts requesterId instead of requesterID.
        String url = ROOT_URL + "/users/status_edit/" + request.userId
                + "?requesterId=" + adminUserId;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("status", newStatus);
        } catch (JSONException e) {
            Toast.makeText(this, "Failed to build retry request", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("STATUS_RETRY_URL", url);
        android.util.Log.d("STATUS_RETRY_BODY", jsonBody.toString());

        StringRequest retryRequest = new StringRequest(Request.Method.PUT, url,
                response -> {
                    android.util.Log.d("STATUS_RETRY_OK", response == null ? "" : response);
                    handleStatusUpdateSuccess(request, approving);
                },
                error -> {
                    logVolleyError(approving ? "APPROVE_RETRY_ERROR" : "REJECT_RETRY_ERROR", error);
                    Toast.makeText(
                            this,
                            (approving ? "approve" : "reject") + " failed: backend rejected status update",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        ) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return jsonBody.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        retryRequest.setShouldCache(false);
        VolleySingleton.getInstance(this).addToRequestQueue(retryRequest);
    }

    private void handleStatusUpdateSuccess(PendingModeratorRequest request, boolean approving) {
        pendingRequests.remove(request);
        renderPendingRequests();
        Toast.makeText(
                this,
                approving ? "approve success!" : "reject success!",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void logVolleyError(String tag, com.android.volley.VolleyError error) {
        if (error == null) {
            android.util.Log.e(tag, "Volley error is null");
            return;
        }

        if (error.networkResponse != null) {
            android.util.Log.e(tag, "Status: " + error.networkResponse.statusCode);
            if (error.networkResponse.data != null) {
                android.util.Log.e(tag, "Body: " + new String(error.networkResponse.data));
            }
        } else {
            android.util.Log.e(tag, "No network response: " + error.toString());
        }
    }


    private boolean isValidIsuEmail(String email) {
        return email != null && email.toLowerCase().endsWith("@iastate.edu");
    }

    private String getIsuEmailMessage(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email loading...";
        }

        if (isValidIsuEmail(email)) {
            return "ISU email verified";
        }

        return "Warning: Non-ISU email";
    }

    private int getIsuEmailColor(String email) {
        if (email == null || email.trim().isEmpty()) {
            return android.graphics.Color.parseColor("#AAAAAA");
        }

        if (isValidIsuEmail(email)) {
            return android.graphics.Color.parseColor("#7BFFB2");
        }

        return android.graphics.Color.parseColor("#FFB86B");
    }

    private void fetchPendingRequests() {
        String primaryUrl = ROOT_URL + "/users/status/NEED_APPROVAL"
                + "?" + REQUESTER_PARAM + "=" + adminUserId;

        requestPendingUsersFromUrl(primaryUrl, true);
    }

    private void requestPendingUsersFromUrl(String url, boolean allowFallback) {
        android.util.Log.d("FETCH_PENDING_URL", url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    android.util.Log.d("FETCH_PENDING_RAW", response);
                    loadPendingRequestsFromResponse(response);
                },
                error -> {
                    String statusCode = error.networkResponse != null
                            ? String.valueOf(error.networkResponse.statusCode)
                            : "no response";

                    android.util.Log.e("FETCH_PENDING_ERROR", "URL: " + url);
                    android.util.Log.e("FETCH_PENDING_ERROR", "Status: " + statusCode);

                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        android.util.Log.e("FETCH_PENDING_ERROR",
                                "Body: " + new String(error.networkResponse.data));
                    }

                    if (allowFallback) {
                        // Fallback to the compact all-users endpoint if the status endpoint fails.
                        String fallbackUrl = ROOT_URL + "/users/all"
                                + "?" + REQUESTER_PARAM + "=" + adminUserId;
                        requestPendingUsersFromUrl(fallbackUrl, false);
                        return;
                    }

                    Toast.makeText(this, "Error fetching pending requests", Toast.LENGTH_SHORT).show();
                    pendingRequests.clear();
                    renderPendingRequests();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void loadPendingRequestsFromResponse(String response) {
        pendingRequests.clear();

        try {
            JSONArray users = extractUsersArray(response);

            for (int i = 0; i < users.length(); i++) {
                JSONObject rawUser = users.optJSONObject(i);
                if (rawUser == null) {
                    continue;
                }

                JSONObject user = unwrapUserObject(rawUser);

                String status = getStringAny(user, rawUser,
                        "status", "userStatus", "UserStatus", "user_status");

                if (!"NEED_APPROVAL".equalsIgnoreCase(status)) {
                    continue;
                }

                long userId = getLongAny(user, rawUser,
                        "userId", "userID", "user_id", "id");

                String email = getStringAny(user, rawUser,
                        "email", "Email", "userEmail", "user_email");

                String displayName = getStringAny(user, rawUser,
                        "displayName", "display_name", "name", "username", "userName", "user_name");

                if (displayName.isEmpty()) {
                    displayName = email.isEmpty() ? "Unknown User" : email;
                }

                android.util.Log.d("FETCH_PENDING_USER",
                        "id=" + userId + ", email=" + email + ", name=" + displayName + ", status=" + status);

                PendingModeratorRequest pendingRequest = new PendingModeratorRequest(
                        userId,
                        email,
                        displayName,
                        status
                );
                pendingRequests.add(pendingRequest);

                if (userId > 0 && email.isEmpty()) {
                    fetchUserEmailForPendingRequest(pendingRequest);
                }
            }

        } catch (Exception e) {
            android.util.Log.e("FETCH_PENDING_ERROR", "Parse error: " + e.getMessage());
            android.util.Log.e("FETCH_PENDING_ERROR", "Raw body: " + response);
            Toast.makeText(this, "Error parsing pending users", Toast.LENGTH_SHORT).show();
        }

        renderPendingRequests();
    }

    private void fetchUserEmailForPendingRequest(PendingModeratorRequest pendingRequest) {
        String url = ROOT_URL + "/users/" + pendingRequest.userId;
        android.util.Log.d("FETCH_USER_DETAIL_URL", url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    android.util.Log.d("FETCH_USER_DETAIL_RAW", response);
                    try {
                        JSONObject raw = new JSONObject(response);
                        JSONObject user = unwrapUserObject(raw);

                        String email = getStringAny(user, raw,
                                "email", "Email", "userEmail", "user_email");
                        String displayName = getStringAny(user, raw,
                                "displayName", "display_name", "name", "username", "userName", "user_name");

                        if (!email.isEmpty()) {
                            pendingRequest.email = email;
                        }
                        if (!displayName.isEmpty()) {
                            pendingRequest.displayName = displayName;
                        }

                        renderPendingRequests();
                    } catch (Exception e) {
                        android.util.Log.e("FETCH_USER_DETAIL_ERROR", "Parse error: " + e.getMessage());
                    }
                },
                error -> {
                    String statusCode = error.networkResponse != null
                            ? String.valueOf(error.networkResponse.statusCode)
                            : "no response";
                    android.util.Log.e("FETCH_USER_DETAIL_ERROR",
                            "Could not load user " + pendingRequest.userId + " status=" + statusCode);
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
    private JSONArray extractUsersArray(String response) throws JSONException {
        if (response == null || response.trim().isEmpty()) {
            return new JSONArray();
        }

        String trimmed = response.trim();

        if (trimmed.startsWith("[")) {
            return new JSONArray(trimmed);
        }

        JSONObject object = new JSONObject(trimmed);

        String[] possibleArrayKeys = new String[]{
                "users", "data", "results", "items", "content"
        };

        for (String key : possibleArrayKeys) {
            JSONArray arr = object.optJSONArray(key);
            if (arr != null) {
                return arr;
            }
        }

        JSONArray single = new JSONArray();
        single.put(object);
        return single;
    }

    private JSONObject unwrapUserObject(JSONObject object) {
        if (object == null) {
            return new JSONObject();
        }

        JSONObject nestedUser = object.optJSONObject("user");
        if (nestedUser != null) {
            return nestedUser;
        }

        JSONObject nestedAccount = object.optJSONObject("account");
        if (nestedAccount != null) {
            return nestedAccount;
        }

        JSONObject nestedUserInfo = object.optJSONObject("userInfo");
        if (nestedUserInfo != null) {
            return nestedUserInfo;
        }

        JSONObject nestedProfile = object.optJSONObject("profile");
        if (nestedProfile != null) {
            return nestedProfile;
        }

        return object;
    }

    private String getStringAny(JSONObject primary, JSONObject fallback, String... keys) {
        for (String key : keys) {
            String value = primary != null ? primary.optString(key, "").trim() : "";
            if (!value.isEmpty() && !"null".equalsIgnoreCase(value)) {
                return value;
            }
        }

        for (String key : keys) {
            String value = fallback != null ? fallback.optString(key, "").trim() : "";
            if (!value.isEmpty() && !"null".equalsIgnoreCase(value)) {
                return value;
            }
        }

        return "";
    }

    private long getLongAny(JSONObject primary, JSONObject fallback, String... keys) {
        for (String key : keys) {
            long value = primary != null ? primary.optLong(key, -1) : -1;
            if (value > 0) {
                return value;
            }
        }

        for (String key : keys) {
            long value = fallback != null ? fallback.optLong(key, -1) : -1;
            if (value > 0) {
                return value;
            }
        }

        return -1;
    }
    }
