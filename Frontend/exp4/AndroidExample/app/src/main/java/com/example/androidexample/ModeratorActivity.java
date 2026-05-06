package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class ModeratorActivity extends AppCompatActivity {

    private Button btnShowMembers, btnShowReports, btnBackModerator;
    private RecyclerView recyclerModerator;

    private List<ModeratorMember> memberList = new ArrayList<>();
    private ModeratorMemberAdapter memberAdapter;

    private ReportAdapter reportAdapter;

    private List<Report> reportList = new ArrayList<>();
    private static final String REPORT_URL = "http://coms-3090-015.class.las.iastate.edu:8080/reports/all";
    private static final String USERS_URL = "http://coms-3090-015.class.las.iastate.edu:8080/users/all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator);

        btnShowMembers = findViewById(R.id.btnShowMembers);
        btnShowReports = findViewById(R.id.btnShowReports);
        btnBackModerator = findViewById(R.id.btnBackModerator);
        recyclerModerator = findViewById(R.id.recyclerModerator);

        recyclerModerator.setLayoutManager(new LinearLayoutManager(this));

        memberAdapter = new ModeratorMemberAdapter(memberList);
        reportAdapter = new ReportAdapter(reportList);

        recyclerModerator.setAdapter(memberAdapter);
        fetchUsersFromBackend();

        btnShowMembers.setOnClickListener(v -> {
            fetchUsersFromBackend();
            recyclerModerator.setAdapter(memberAdapter);
            btnShowMembers.setBackgroundTintList(getColorStateList(android.R.color.holo_blue_light));
            btnShowReports.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
        });

        btnShowReports.setOnClickListener(v -> {
            fetchReportsFromBackend();
            recyclerModerator.setAdapter(reportAdapter);
            btnShowReports.setBackgroundTintList(getColorStateList(android.R.color.holo_blue_light));
            btnShowMembers.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
        });

        btnBackModerator.setOnClickListener(v -> finish());
        fetchReportsFromBackend();
    }

    private void fetchUsersFromBackend() {
        com.android.volley.toolbox.JsonArrayRequest request =
                new com.android.volley.toolbox.JsonArrayRequest(
                        com.android.volley.Request.Method.GET,
                        USERS_URL,
                        null,
                        response -> {
                            memberList.clear();

                            try {
                                for (int i = 0; i < response.length(); i++) {
                                    org.json.JSONObject obj = response.getJSONObject(i);

                                    int userId = obj.optInt("userID",
                                            obj.optInt("userId",
                                                    obj.optInt("user_id", 0)));

                                    String displayName = obj.optString("displayName",
                                            obj.optString("name",
                                                    obj.optString("username", "User " + userId)));

                                    String status = obj.optString("status", "");
                                    String label = displayName;
                                    if (!status.isEmpty()) {
                                        label = displayName + " (" + status + ")";
                                    }

                                    memberList.add(new ModeratorMember(userId, label));
                                }

                                memberAdapter.notifyDataSetChanged();

                            } catch (org.json.JSONException e) {
                                e.printStackTrace();
                                android.widget.Toast.makeText(this, "Failed to parse users", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> {
                            android.util.Log.e("USER_FETCH", error.toString());

                            if (error.networkResponse != null) {
                                android.util.Log.e("USER_FETCH", "status: " + error.networkResponse.statusCode);
                            }

                            android.widget.Toast.makeText(this, "Failed to load users", android.widget.Toast.LENGTH_SHORT).show();
                        }
                );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
    private void fetchReportsFromBackend() {
        com.android.volley.toolbox.JsonArrayRequest request =
                new com.android.volley.toolbox.JsonArrayRequest(
                        com.android.volley.Request.Method.GET,
                        REPORT_URL,
                        null,
                        response -> {
                            reportList.clear();

                            try {
                                for (int i = 0; i < response.length(); i++) {
                                    org.json.JSONObject obj = response.getJSONObject(i);

                                    int reportId = obj.optInt("reportId");

                                    org.json.JSONObject reporterObj = obj.optJSONObject("reporterId");
                                    org.json.JSONObject reportedObj = obj.optJSONObject("reportedId");

                                    int reporterId = reporterObj != null
                                            ? reporterObj.optInt("userID",
                                                    reporterObj.optInt("userId",
                                                            reporterObj.optInt("user_id", 0)))
                                            : 0;
                                    int reportedId = reportedObj != null
                                            ? reportedObj.optInt("userID",
                                                    reportedObj.optInt("userId",
                                                            reportedObj.optInt("user_id", 0)))
                                            : 0;

                                    String description = obj.optString("description", "");
                                    String status = obj.optString("status", "");
                                    String createdAt = obj.optString("createdAt", "");

                                    reportList.add(new Report(
                                            reportId,
                                            reporterId,
                                            reportedId,
                                            description,
                                            status,
                                            createdAt
                                    ));
                                }

                                reportAdapter.notifyDataSetChanged();

                            } catch (org.json.JSONException e) {
                                e.printStackTrace();
                                android.widget.Toast.makeText(this, "Failed to parse reports", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> {
                            android.util.Log.e("REPORT_FETCH", error.toString());

                            if (error.networkResponse != null) {
                                android.util.Log.e("REPORT_FETCH", "status: " + error.networkResponse.statusCode);
                            }

                            android.widget.Toast.makeText(this, "Failed to load reports", android.widget.Toast.LENGTH_SHORT).show();
                        }
                );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
}