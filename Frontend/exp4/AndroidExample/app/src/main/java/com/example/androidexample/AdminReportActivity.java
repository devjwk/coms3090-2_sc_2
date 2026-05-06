package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
// import android.widget.ScrollView;   // Removed unused import
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AdminReportActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://coms-3090-015.class.las.iastate.edu:8080";
    private static final String TAG = "AdminReportActivity";

    private LinearLayout reportsContainer;
    private TextView tvSummary;
    private Button btnRefreshReports;
    private Button btnBackReports;

    private final List<AdminReportItem> reports = new ArrayList<>();

    private static class AdminReportItem {
        int reportId;
        int reporterId;
        int reportedId;
        String reporterName;
        String reportedName;
        String description;
        String status;
        String createdAt;

        AdminReportItem(int reportId,
                        int reporterId,
                        int reportedId,
                        String reporterName,
                        String reportedName,
                        String description,
                        String status,
                        String createdAt) {
            this.reportId = reportId;
            this.reporterId = reporterId;
            this.reportedId = reportedId;
            this.reporterName = reporterName;
            this.reportedName = reportedName;
            this.description = description;
            this.status = status;
            this.createdAt = createdAt;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildLayout();
        fetchReports();
    }

    private void buildLayout() {
        setContentView(R.layout.activity_admin_report);

        reportsContainer = findViewById(R.id.reportsContainer);
        tvSummary = findViewById(R.id.tvReportSummary);
        btnRefreshReports = findViewById(R.id.btnRefreshReports);
        btnBackReports = findViewById(R.id.btnBackReports);

        btnRefreshReports.setOnClickListener(v -> fetchReports());
        btnBackReports.setOnClickListener(v -> finish());
    }

    private void fetchReports() {
        String url = BASE_URL + "/reports/all";
        Log.d(TAG, "FETCH_REPORTS_URL: " + url);
        tvSummary.setText("Loading reports...");

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    reports.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            reports.add(parseReport(obj));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Failed to parse reports", e);
                        Toast.makeText(this, "Failed to parse reports", Toast.LENGTH_SHORT).show();
                    }
                    renderReports();
                },
                error -> {
                    Log.e(TAG, "Failed to fetch reports: " + error);
                    tvSummary.setText("Failed to load reports.");
                    reportsContainer.removeAllViews();
                    Toast.makeText(this, "Failed to load reports", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private AdminReportItem parseReport(JSONObject obj) {
        int reportId = obj.optInt("reportId", obj.optInt("id", -1));

        JSONObject reporterObj = obj.optJSONObject("reporterId");
        JSONObject reportedObj = obj.optJSONObject("reportedId");

        int reporterId = extractUserId(reporterObj, obj.optInt("reporterId", -1));
        int reportedId = extractUserId(reportedObj, obj.optInt("reportedId", -1));

        String reporterName = extractDisplayName(reporterObj, "Reporter #" + reporterId);
        String reportedName = extractDisplayName(reportedObj, "Reported User #" + reportedId);

        String description = obj.optString("description", "");
        String status = obj.optString("status", "IN_REVIEW");
        String createdAt = obj.optString("createdAt", "");

        Log.d(TAG, "REPORT id=" + reportId + ", reporter=" + reporterId + ", reported=" + reportedId + ", status=" + status);

        return new AdminReportItem(reportId, reporterId, reportedId, reporterName, reportedName, description, status, createdAt);
    }

    private int extractUserId(JSONObject userObj, int fallback) {
        if (userObj == null) {
            return fallback;
        }
        return userObj.optInt("userId", userObj.optInt("id", fallback));
    }

    private String extractDisplayName(JSONObject userObj, String fallback) {
        if (userObj == null) {
            return fallback;
        }
        String name = userObj.optString("displayName", userObj.optString("name", ""));
        return name == null || name.trim().isEmpty() ? fallback : name;
    }

    private void renderReports() {
        reportsContainer.removeAllViews();

        int inReviewCount = 0;
        int approvedCount = 0;
        int declinedCount = 0;

        for (AdminReportItem report : reports) {
            if ("APPROVED".equalsIgnoreCase(report.status)) {
                approvedCount++;
            } else if ("DECLINED".equalsIgnoreCase(report.status)) {
                declinedCount++;
            } else {
                inReviewCount++;
            }
        }

        tvSummary.setText(
                "Total: " + reports.size()
                        + "   |   In Review: " + inReviewCount
                        + "   |   Approved: " + approvedCount
                        + "   |   Declined: " + declinedCount
        );

        if (reports.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No reports found.");
            empty.setTextColor(color("#CCCCCC"));
            empty.setTextSize(16);
            empty.setPadding(dp(8), dp(24), dp(8), dp(24));
            reportsContainer.addView(empty);
            return;
        }

        for (AdminReportItem report : new ArrayList<>(reports)) {
            reportsContainer.addView(createReportCard(report));
        }
    }

    private View createReportCard(AdminReportItem report) {
        LinearLayout card = new LinearLayout(this);
        // Report cards are still created dynamically because the number of reports comes from the backend.
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(color("#1E1E30"));
        card.setPadding(dp(14), dp(14), dp(14), dp(14));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cardParams);

        TextView header = new TextView(this);
        header.setText("Report #" + report.reportId + "   •   " + safeStatus(report.status));
        header.setTextColor(getStatusColor(report.status));
        header.setTextSize(17);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(header);

        TextView users = new TextView(this);
        users.setText("Reported: " + report.reportedName + " (#" + report.reportedId + ")\n"
                + "Reporter: " + report.reporterName + " (#" + report.reporterId + ")");
        users.setTextColor(color("#CCCCCC"));
        users.setTextSize(14);
        users.setPadding(0, dp(8), 0, 0);
        card.addView(users);

        TextView description = new TextView(this);
        description.setText(report.description == null || report.description.trim().isEmpty()
                ? "Description: none"
                : "Description: " + report.description);
        description.setTextColor(color("#FFFFFF"));
        description.setTextSize(14);
        description.setPadding(0, dp(10), 0, 0);
        card.addView(description);

        TextView created = new TextView(this);
        created.setText(report.createdAt == null || report.createdAt.trim().isEmpty()
                ? "Created: unknown"
                : "Created: " + report.createdAt);
        created.setTextColor(color("#AAAAAA"));
        created.setTextSize(12);
        created.setPadding(0, dp(8), 0, dp(8));
        card.addView(created);

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setGravity(Gravity.CENTER);
        buttonRow.setPadding(0, dp(8), 0, 0);

        Button reviewButton = makeStatusButton("In Review", "#3C3C50");
        Button approveButton = makeStatusButton("Approve", "#4CAF50");
        Button declineButton = makeStatusButton("Decline", "#D9534F");

        buttonRow.addView(reviewButton, weightedButtonParams(0, dp(6)));
        buttonRow.addView(approveButton, weightedButtonParams(dp(6), dp(6)));
        buttonRow.addView(declineButton, weightedButtonParams(dp(6), 0));
        card.addView(buttonRow);

        reviewButton.setOnClickListener(v -> updateReportStatus(report, "IN_REVIEW"));
        approveButton.setOnClickListener(v -> updateReportStatus(report, "APPROVED"));
        declineButton.setOnClickListener(v -> updateReportStatus(report, "DECLINED"));

        return card;
    }

    private Button makeStatusButton(String text, String bgColor) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextColor(color("#FFFFFF"));
        button.setTextSize(12);
        button.setBackgroundTintList(ColorStateList.valueOf(color(bgColor)));
        return button;
    }

    private LinearLayout.LayoutParams weightedButtonParams(int leftMargin, int rightMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        params.setMargins(leftMargin, 0, rightMargin, 0);
        return params;
    }

    private void updateReportStatus(AdminReportItem report, String newStatus) {
        if (report.reportId <= 0) {
            Toast.makeText(this, "Invalid report ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/reports/" + report.reportId;
        JSONObject body = new JSONObject();
        try {
            body.put("status", newStatus);
        } catch (JSONException e) {
            Toast.makeText(this, "Failed to build request", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "REPORT_STATUS_URL: " + url);
        Log.d(TAG, "REPORT_STATUS_BODY: " + body);

        StringRequest request = new StringRequest(
                Request.Method.PUT,
                url,
                response -> {
                    Log.d(TAG, "REPORT_STATUS_OK: " + response);
                    report.status = newStatus;
                    renderReports();
                    Toast.makeText(this, "Report marked " + newStatus, Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Log.e(TAG, "Failed to update report status: " + error);
                    String message = "Failed to update report";
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        message += " (HTTP " + statusCode + ")";
                        if (error.networkResponse.data != null) {
                            String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            Log.e(TAG, "REPORT_STATUS_ERROR_BODY: " + responseBody);
                            if (!responseBody.trim().isEmpty()) {
                                message += ": " + responseBody;
                            }
                        }
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public byte[] getBody() {
                return body.toString().getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private String safeStatus(String status) {
        return status == null || status.trim().isEmpty() ? "IN_REVIEW" : status;
    }

    private int getStatusColor(String status) {
        if ("APPROVED".equalsIgnoreCase(status)) {
            return color("#7BFFB2");
        }
        if ("DECLINED".equalsIgnoreCase(status)) {
            return color("#FF6B6B");
        }
        return color("#FFB86B");
    }

    private int color(String hex) {
        return android.graphics.Color.parseColor(hex);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}