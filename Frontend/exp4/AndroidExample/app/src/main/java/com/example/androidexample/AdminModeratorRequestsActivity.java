package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class AdminModeratorRequestsActivity extends AppCompatActivity {

    private LinearLayout pendingRequestsContainer;
    private Button btnBackAdminRequests;
    private Button btnRefreshModeratorRequests;

    private final List<PendingModeratorRequest> pendingRequests = new ArrayList<>();

    private static class PendingModeratorRequest {
        int moderatorId;
        String email;
        String displayName;
        String status;

        PendingModeratorRequest(int moderatorId, String email, String displayName, String status) {
            this.moderatorId = moderatorId;
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

        loadMockPendingRequests();
        renderPendingRequests();

        btnRefreshModeratorRequests.setOnClickListener(v -> {
            loadMockPendingRequests();
            renderPendingRequests();
            Toast.makeText(
                    AdminModeratorRequestsActivity.this,
                    "Moderator requests refreshed",
                    Toast.LENGTH_SHORT
            ).show();
        });

        btnBackAdminRequests.setOnClickListener(v -> finish());
    }

    private void loadMockPendingRequests() {
        pendingRequests.clear();

        pendingRequests.add(new PendingModeratorRequest(
                1,
                "moderator1@iastate.edu",
                "John Moderator",
                "PENDING"
        ));

        pendingRequests.add(new PendingModeratorRequest(
                2,
                "moderator2@iastate.edu",
                "Sarah Moderator",
                "PENDING"
        ));

        pendingRequests.add(new PendingModeratorRequest(
                3,
                "fakeuser@gmail.com",
                "Non ISU User",
                "PENDING"
        ));
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
        if (!isValidIsuEmail(request.email)) {
            Toast.makeText(
                    this,
                    "Cannot approve non-ISU email: " + request.email,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        pendingRequests.remove(request);
        renderPendingRequests();

        Toast.makeText(
                this,
                "Approved " + request.email,
                Toast.LENGTH_SHORT
        ).show();
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
}