//package com.example.androidexample;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.os.Bundle;
//import android.widget.Button;
//
//import java.util.ArrayList;
//import java.util.List;
//import com.example.androidexample.ReportRepository;
//
//public class ModeratorActivity extends AppCompatActivity {
//
//    private Button btnShowMembers, btnShowReports, btnBackModerator;
//    private RecyclerView recyclerModerator;
//
//    private List<ModeratorMember> memberList = new ArrayList<>();
//    private ModeratorMemberAdapter memberAdapter;
//
//    private ReportAdapter reportAdapter;
//
//    private List<Report> reportList = new ArrayList<>();
//    private static final String REPORT_URL = "http://coms-3090-015.class.las.iastate.edu:8080/reports/all";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_moderator);
//
//        btnShowMembers = findViewById(R.id.btnShowMembers);
//        btnShowReports = findViewById(R.id.btnShowReports);
//        btnBackModerator = findViewById(R.id.btnBackModerator);
//        recyclerModerator = findViewById(R.id.recyclerModerator);
//
//        recyclerModerator.setLayoutManager(new LinearLayoutManager(this));
//
//        // dummy members
//        memberList.add(new ModeratorMember(1, "John"));
//        memberList.add(new ModeratorMember(2, "Sarah"));
//        memberList.add(new ModeratorMember(3, "Mike"));
//
//        memberAdapter = new ModeratorMemberAdapter(memberList);
//        reportAdapter = new ReportAdapter(reportList);
//
//        recyclerModerator.setAdapter(memberAdapter);
//
//        btnShowMembers.setOnClickListener(v -> {
//            recyclerModerator.setAdapter(memberAdapter);
//            btnShowMembers.setBackgroundTintList(getColorStateList(android.R.color.holo_blue_light));
//            btnShowReports.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
//        });
//
//        btnShowReports.setOnClickListener(v -> {
//            fetchReportsFromBackend();
//            recyclerModerator.setAdapter(reportAdapter);
//            btnShowReports.setBackgroundTintList(getColorStateList(android.R.color.holo_blue_light));
//            btnShowMembers.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
//        });
//
//        btnBackModerator.setOnClickListener(v -> finish());
//        fetchReportsFromBackend();
//    }
//    private void fetchReportsFromBackend() {
//        com.android.volley.toolbox.JsonArrayRequest request =
//                new com.android.volley.toolbox.JsonArrayRequest(
//                        com.android.volley.Request.Method.GET,
//                        REPORT_URL,
//                        null,
//                        response -> {
//                            reportList.clear();
//
//                            try {
//                                for (int i = 0; i < response.length(); i++) {
//                                    org.json.JSONObject obj = response.getJSONObject(i);
//
//                                    int reportId = obj.optInt("reportId");
//
//                                    org.json.JSONObject reporterObj = obj.optJSONObject("reporterId");
//                                    org.json.JSONObject reportedObj = obj.optJSONObject("reportedId");
//
//                                    int reporterId = reporterObj != null ? reporterObj.optInt("userId") : 0;
//                                    int reportedId = reportedObj != null ? reportedObj.optInt("userId") : 0;
//
//                                    String description = obj.optString("description", "");
//                                    String status = obj.optString("status", "");
//                                    String createdAt = obj.optString("createdAt", "");
//
//                                    reportList.add(new Report(
//                                            reportId,
//                                            reporterId,
//                                            reportedId,
//                                            description,
//                                            status,
//                                            createdAt
//                                    ));
//                                }
//
//                                reportAdapter.notifyDataSetChanged();
//
//                            } catch (org.json.JSONException e) {
//                                e.printStackTrace();
//                                android.widget.Toast.makeText(this, "Failed to parse reports", android.widget.Toast.LENGTH_SHORT).show();
//                            }
//                        },
//                        error -> {
//                            android.util.Log.e("REPORT_FETCH", error.toString());
//
//                            if (error.networkResponse != null) {
//                                android.util.Log.e("REPORT_FETCH", "status: " + error.networkResponse.statusCode);
//                            }
//
//                            android.widget.Toast.makeText(this, "Failed to load reports", android.widget.Toast.LENGTH_SHORT).show();
//                        }
//                );
//
//        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
//    }
//}