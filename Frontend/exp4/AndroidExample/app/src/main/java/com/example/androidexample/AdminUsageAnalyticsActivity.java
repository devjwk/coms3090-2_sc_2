package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AdminUsageAnalyticsActivity extends AppCompatActivity {

    private static final String TAG = "AdminUsageAnalytics";
    private static final String BASE_URL = "http://coms-3090-015.class.las.iastate.edu:8080";

    private TextView tvTotalUsers;
    private TextView tvPendingUsers;
    private TextView tvApprovedUsers;
    private TextView tvSuspendedUsers;
    private TextView tvDisabledUsers;
    private TextView tvDeclinedUsers;
    private TextView tvPendingReports;
    private TextView tvApprovedReports;
    private TextView tvDeclinedReports;
    private TextView tvRecentMatches;
    private TextView tvAnalyticsStatus;

    private Button btnRefreshAnalytics;
    private Button btnBackUsageAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_usage_analytics);

        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvPendingUsers = findViewById(R.id.tvPendingUsers);
        tvApprovedUsers = findViewById(R.id.tvApprovedUsers);
        tvSuspendedUsers = findViewById(R.id.tvSuspendedUsers);
        tvDisabledUsers = findViewById(R.id.tvDisabledUsers);
        tvDeclinedUsers = findViewById(R.id.tvDeclinedUsers);
        tvPendingReports = findViewById(R.id.tvPendingReports);
        tvApprovedReports = findViewById(R.id.tvApprovedReports);
        tvDeclinedReports = findViewById(R.id.tvDeclinedReports);
        tvRecentMatches = findViewById(R.id.tvRecentMatches);
        tvAnalyticsStatus = findViewById(R.id.tvAnalyticsStatus);

        btnRefreshAnalytics = findViewById(R.id.btnRefreshAnalytics);
        btnBackUsageAnalytics = findViewById(R.id.btnBackUsageAnalytics);

        btnRefreshAnalytics.setOnClickListener(v -> loadAnalytics());
        btnBackUsageAnalytics.setOnClickListener(v -> finish());

        loadAnalytics();
    }

    private void loadAnalytics() {
        tvAnalyticsStatus.setText("Loading analytics...");

        setMetric(tvTotalUsers, "Total Users", "...");
        setMetric(tvPendingUsers, "Pending Users", "...");
        setMetric(tvApprovedUsers, "Approved Users", "...");
        setMetric(tvSuspendedUsers, "Suspended Users", "...");
        setMetric(tvDisabledUsers, "Disabled Users", "...");
        setMetric(tvDeclinedUsers, "Declined Users", "...");
        setMetric(tvPendingReports, "Pending Reports", "...");
        setMetric(tvApprovedReports, "Approved Reports", "...");
        setMetric(tvDeclinedReports, "Declined Reports", "...");
        setMetric(tvRecentMatches, "Recent Matches", "...");

        fetchUserAnalytics();
        fetchReportAnalytics();
        fetchRecentMatches();
    }

    private void fetchUserAnalytics() {
        String url = BASE_URL + "/users/all";
        Log.d(TAG, "GET " + url);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    Log.d(TAG, "Users analytics response: " + response);
                    try {
                        JSONArray users = extractArray(response);

                        int total = users.length();
                        int pending = 0;
                        int approved = 0;
                        int suspended = 0;
                        int disabled = 0;
                        int declined = 0;

                        for (int i = 0; i < users.length(); i++) {
                            JSONObject user = users.optJSONObject(i);
                            if (user == null) {
                                continue;
                            }

                            String status = user.optString("status", "UNKNOWN").toUpperCase();

                            switch (status) {
                                case "NEED_APPROVAL":
                                    pending++;
                                    break;
                                case "APPROVED":
                                    approved++;
                                    break;
                                case "SUSPENDED":
                                    suspended++;
                                    break;
                                case "DISABLED":
                                    disabled++;
                                    break;
                                case "DECLINED":
                                    declined++;
                                    break;
                                default:
                                    break;
                            }
                        }

                        setMetric(tvTotalUsers, "Total Users", String.valueOf(total));
                        setMetric(tvPendingUsers, "Pending Users", String.valueOf(pending));
                        setMetric(tvApprovedUsers, "Approved Users", String.valueOf(approved));
                        setMetric(tvSuspendedUsers, "Suspended Users", String.valueOf(suspended));
                        setMetric(tvDisabledUsers, "Disabled Users", String.valueOf(disabled));
                        setMetric(tvDeclinedUsers, "Declined Users", String.valueOf(declined));
                        tvAnalyticsStatus.setText("Analytics loaded");
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse user analytics", e);
                        Toast.makeText(this, "Failed to parse user analytics", Toast.LENGTH_SHORT).show();
                        tvAnalyticsStatus.setText("User analytics failed");
                    }
                },
                error -> {
                    int statusCode = error.networkResponse != null ? error.networkResponse.statusCode : -1;
                    Log.e(TAG, "Failed to load users analytics. HTTP " + statusCode);
                    Toast.makeText(this, "Failed to load user analytics: " + statusCode, Toast.LENGTH_SHORT).show();
                    tvAnalyticsStatus.setText("User analytics failed");
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void fetchReportAnalytics() {
        String url = BASE_URL + "/reports/all";
        Log.d(TAG, "GET " + url);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    Log.d(TAG, "Reports analytics response: " + response);
                    try {
                        JSONArray reports = extractArray(response);

                        int pending = 0;
                        int approved = 0;
                        int declined = 0;

                        for (int i = 0; i < reports.length(); i++) {
                            JSONObject report = reports.optJSONObject(i);
                            if (report == null) {
                                continue;
                            }

                            String status = report.optString("status", "UNKNOWN").toUpperCase();

                            switch (status) {
                                case "IN_REVIEW":
                                    pending++;
                                    break;
                                case "APPROVED":
                                    approved++;
                                    break;
                                case "DECLINED":
                                    declined++;
                                    break;
                                default:
                                    break;
                            }
                        }

                        setMetric(tvPendingReports, "Pending Reports", String.valueOf(pending));
                        setMetric(tvApprovedReports, "Approved Reports", String.valueOf(approved));
                        setMetric(tvDeclinedReports, "Declined Reports", String.valueOf(declined));
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse report analytics", e);
                        Toast.makeText(this, "Failed to parse report analytics", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    int statusCode = error.networkResponse != null ? error.networkResponse.statusCode : -1;
                    Log.e(TAG, "Failed to load report analytics. HTTP " + statusCode);
                    Toast.makeText(this, "Failed to load report analytics: " + statusCode, Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void fetchRecentMatches() {
        String url = BASE_URL + "/matches/count/recent";
        Log.d(TAG, "GET " + url);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    Log.d(TAG, "Recent matches response: " + response);
                    String count = parseCountResponse(response);
                    setMetric(tvRecentMatches, "Recent Matches", count);
                },
                error -> {
                    int statusCode = error.networkResponse != null ? error.networkResponse.statusCode : -1;
                    Log.e(TAG, "Failed to load recent matches. HTTP " + statusCode);
                    setMetric(tvRecentMatches, "Recent Matches", "N/A");
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private JSONArray extractArray(String response) throws JSONException {
        if (response == null || response.trim().isEmpty()) {
            return new JSONArray();
        }

        String trimmed = response.trim();
        if (trimmed.startsWith("[")) {
            return new JSONArray(trimmed);
        }

        JSONObject object = new JSONObject(trimmed);
        String[] possibleKeys = new String[]{"users", "reports", "data", "results", "items", "content"};

        for (String key : possibleKeys) {
            JSONArray array = object.optJSONArray(key);
            if (array != null) {
                return array;
            }
        }

        JSONArray singleItem = new JSONArray();
        singleItem.put(object);
        return singleItem;
    }

    private String parseCountResponse(String response) {
        if (response == null) {
            return "N/A";
        }

        String trimmed = response.trim();
        if (trimmed.isEmpty()) {
            return "N/A";
        }

        try {
            JSONObject object = new JSONObject(trimmed);
            if (object.has("count")) {
                return String.valueOf(object.optLong("count"));
            }
            if (object.has("recentMatches")) {
                return String.valueOf(object.optLong("recentMatches"));
            }
            if (object.has("value")) {
                return String.valueOf(object.optLong("value"));
            }
        } catch (JSONException ignored) {
        }

        return trimmed.replace("\"", "");
    }

    private void setMetric(TextView textView, String label, String value) {
        if (textView != null) {
            textView.setText(label + ": " + value);
        }
    }
}
