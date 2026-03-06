package com.example.androidexample;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity for managing group memberships.
 * Connected to the production backend GMController.
 */
public class GroupMembershipActivity extends AppCompatActivity {

    private EditText etGroupId, etTargetUserId;
    private Button btnJoinGroup, btnCheckMembership, btnApprove, btnBan, btnToggleMod, btnBack;
    private TextView tvMembershipInfo;

    // Production BASE_URL for GMController
    private static final String BASE_URL = "http://coms-3090-015.class.las.iastate.edu:8080/gm";

    private int myUserId;
    private long currentMembershipId = -1;
    private boolean isCurrentMod = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_membership);

        etGroupId = findViewById(R.id.etGroupId);
        etTargetUserId = findViewById(R.id.etTargetUserId);
        btnJoinGroup = findViewById(R.id.btnJoinGroup);
        btnCheckMembership = findViewById(R.id.btnCheckMembership);
        btnApprove = findViewById(R.id.btnApprove);
        btnBan = findViewById(R.id.btnBan);
        btnToggleMod = findViewById(R.id.btnToggleMod);
        btnBack = findViewById(R.id.btnBack);
        tvMembershipInfo = findViewById(R.id.tvMembershipInfo);

        myUserId = getIntent().getIntExtra("USER_ID", 1);

        btnJoinGroup.setOnClickListener(v -> joinGroup());

        btnCheckMembership.setOnClickListener(v -> {
            String targetIdStr = etTargetUserId.getText().toString().trim();
            int idToQuery = targetIdStr.isEmpty() ? myUserId : Integer.parseInt(targetIdStr);
            checkMembership(idToQuery);
        });

        btnApprove.setOnClickListener(v -> updateStatus("active"));
        btnBan.setOnClickListener(v -> updateStatus("banned"));
        btnToggleMod.setOnClickListener(v -> toggleModerator());
        btnBack.setOnClickListener(v -> finish());
    }

    private void joinGroup() {
        String gidStr = etGroupId.getText().toString().trim();
        if (gidStr.isEmpty()) {
            Toast.makeText(this, "Enter Group ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("user_id", (long) myUserId);
        params.put("group_id", Long.parseLong(gidStr));
        params.put("is_moderator", false);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, BASE_URL + "/join", new JSONObject(params),
                response -> Toast.makeText(this, "Join Request Success!", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(this, "Join Request Failed", Toast.LENGTH_SHORT).show());

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void checkMembership(int userId) {
        String gidStr = etGroupId.getText().toString().trim();
        if (gidStr.isEmpty()) {
            Toast.makeText(this, "Enter Group ID", Toast.LENGTH_SHORT).show();
            return;
        }
        long targetGid = Long.parseLong(gidStr);

        String url = BASE_URL + "/ulist/" + userId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        boolean found = false;
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            // Assuming backend nests Group object as "groupId"
                            JSONObject groupObj = obj.optJSONObject("groupId");
                            long gid = (groupObj != null) ? groupObj.optLong("groupId") : obj.optLong("group_id");

                            if (gid == targetGid) {
                                currentMembershipId = obj.optLong("id");
                                String status = obj.optString("status", "unknown");
                                isCurrentMod = obj.optBoolean("is_moderator", false);

                                tvMembershipInfo.setText("Membership ID: " + currentMembershipId +
                                        "\nStatus: " + status.toUpperCase() +
                                        "\nModerator: " + (isCurrentMod ? "YES" : "NO"));
                                tvMembershipInfo.setTextColor(status.equalsIgnoreCase("banned") ? 0xFFFF4B4B : 0xFF7B6FFF);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            currentMembershipId = -1;
                            tvMembershipInfo.setText("No membership record found.");
                        }
                    } catch (JSONException e) {
                        tvMembershipInfo.setText("Error parsing data.");
                    }
                },
                error -> tvMembershipInfo.setText("User has no group memberships."));

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void updateStatus(String newStatus) {
        if (currentMembershipId == -1) {
            Toast.makeText(this, "Check membership first", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/memstat/" + currentMembershipId;
        Map<String, String> params = new HashMap<>();
        params.put("status", newStatus);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, new JSONObject(params),
                response -> {
                    Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    // Optional: refresh UI
                },
                error -> Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show());

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void toggleModerator() {
        if (currentMembershipId == -1) {
            Toast.makeText(this, "Check membership first", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/modstat/" + currentMembershipId;
        Map<String, Boolean> params = new HashMap<>();
        params.put("is_moderator", !isCurrentMod);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, new JSONObject(params),
                response -> {
                    isCurrentMod = !isCurrentMod;
                    Toast.makeText(this, "Moderator status toggled", Toast.LENGTH_SHORT).show();
                    tvMembershipInfo.setText(tvMembershipInfo.getText().toString().replaceAll("Moderator: .*", "Moderator: " + (isCurrentMod ? "YES" : "NO")));
                },
                error -> Toast.makeText(this, "Toggle failed", Toast.LENGTH_SHORT).show());

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
