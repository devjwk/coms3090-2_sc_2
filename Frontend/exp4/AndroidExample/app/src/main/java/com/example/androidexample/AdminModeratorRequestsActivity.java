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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AdminModeratorRequestsActivity extends AppCompatActivity {

    private LinearLayout pendingRequestsContainer;
    private Button btnBackAdminRequests;
    private Button btnRefreshModeratorRequests;

    private final List<PendingModeratorRequest> pendingRequests = new ArrayList<>();

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

        pendingRequestsContainer = findViewById(R.id.pendingRequestsContainer);
        btnBackAdminRequests = findViewById(R.id.btnBackAdminRequests);
        btnRefreshModeratorRequests = findViewById(R.id.btnRefreshModeratorRequests);
        fetchPendingRequests();// Fetch pending moderator requests


        btnRefreshModeratorRequests.setOnClickListener(v -> {
            fetchPendingRequests();
            renderPendingRequests();
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
        email.setText("Email: " + request.email);
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
        // 백엔드 경로: /users/edit/{id}
        String url = "http://coms-3090-015.class.las.iastate.edu:8080/users/edit/" + request.userId;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("userId", request.userId);
            jsonBody.put("status", "APPROVED");
            jsonBody.put("email", request.email);

        } catch (JSONException e) { e.printStackTrace(); }

        JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonBody,
                response -> {
                    handleSuccess(request);
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.statusCode == 200) {
                        handleSuccess(request);
                    } else {

                        android.util.Log.e("APPROVE_ERROR", "Status: " +
                                (error.networkResponse != null ? error.networkResponse.statusCode : "null"));
                        Toast.makeText(this, "approve failed: check data(400)", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(putRequest);
    }

    private void handleSuccess(PendingModeratorRequest request) {
        pendingRequests.remove(request);
        renderPendingRequests();
        Toast.makeText(this, "approve success!", Toast.LENGTH_SHORT).show();
    }

    private void rejectRequest(PendingModeratorRequest request) {
        pendingRequests.remove(request);
        renderPendingRequests();

        Toast.makeText(
                this,
                "Rejected " + request.email,
                Toast.LENGTH_SHORT
        ).show();
    }

    private boolean isValidIsuEmail(String email) {
        return email != null && email.toLowerCase().endsWith("@iastate.edu");
    }

    private String getIsuEmailMessage(String email) {
        if (isValidIsuEmail(email)) {
            return "ISU email verified";
        }

        return "Warning: Non-ISU email";
    }

    private int getIsuEmailColor(String email) {
        if (isValidIsuEmail(email)) {
            return android.graphics.Color.parseColor("#7BFFB2");
        }

        return android.graphics.Color.parseColor("#FFB86B");
    }

    private void fetchPendingRequests() {
        String url = "http://coms-3090-015.class.las.iastate.edu:8080/users/status/NEED_APPROVAL";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    pendingRequests.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject user = response.getJSONObject(i);
                            pendingRequests.add(new PendingModeratorRequest(
                                    user.getLong("userId"),
                                    user.getString("email"),
                                    user.getString("displayName"),
                                    user.getString("status")
                            ));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    renderPendingRequests();
                },
                error -> Toast.makeText(this, "Error fetching pending requests", Toast.LENGTH_SHORT).show());
        Volley.newRequestQueue(this).add(request);
    }
    }
