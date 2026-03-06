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
 * Updated to use /gm/glist/{groupId} to correctly fetch userId and displayName.
 */
public class GroupMembershipActivity extends AppCompatActivity {

    private EditText etGroupId, etTargetUserId;
    private Button btnJoinGroup, btnCheckMembership, btnApprove, btnBan, btnToggleMod, btnBack;
    private TextView tvMembershipInfo;

    private static final String BASE_URL = "http://coms-3090-015.class.las.iastate.edu:8080/gm";

    private int myUserId;
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
                response -> Toast.makeText(this, "Join success!", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(this, "Join failed", Toast.LENGTH_SHORT).show());

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void checkMembership(int userIdToFind) {
        String gidStr = etGroupId.getText().toString().trim();
        if (gidStr.isEmpty()) {
            Toast.makeText(this, "Enter Group ID to check", Toast.LENGTH_SHORT).show();
            return;
        }
        long targetGid = Long.parseLong(gidStr);

        // API 변경: /gm/glist/{groupId} 를 호출하여 UserInfo(userId, displayName, ...) 목록을 가져옴
        String url = BASE_URL + "/glist/" + targetGid;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        boolean found = false;
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            
                            // 백엔드 UserInfo 레코드의 필드명: userId, displayName
                            long uid = obj.optLong("userId", -1);

                            if (uid == userIdToFind) {
                                String dName = obj.optString("displayName", "N/A");
                                String gName = obj.optString("groupName", "N/A");
                                long gid = obj.optLong("groupId", targetGid);

                                tvMembershipInfo.setText("RECORD FOUND!\n" +
                                        "User ID: " + uid + "\n" +
                                        "Display Name: " + dName + "\n" +
                                        "Group Name: " + gName + "\n" +
                                        "Group ID: " + gid);
                                
                                tvMembershipInfo.setTextColor(0xFF7B6FFF);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            tvMembershipInfo.setText("User " + userIdToFind + " is not in Group " + targetGid);
                            tvMembershipInfo.setTextColor(0xFFFF4B4B);
                        }
                    } catch (JSONException e) {
                        tvMembershipInfo.setText("Error parsing server data.");
                    }
                },
                error -> tvMembershipInfo.setText("Group not found or No members."));

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void updateStatus(String newStatus) {
        String membershipId = etTargetUserId.getText().toString().trim();
        if (membershipId.isEmpty()) {
            Toast.makeText(this, "Enter Target User ID (as record ID for demo)", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/memstat/" + membershipId;
        Map<String, String> params = new HashMap<>();
        params.put("status", newStatus);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, new JSONObject(params),
                response -> Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(this, "Update failed. Check ID.", Toast.LENGTH_SHORT).show());

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void toggleModerator() {
        String membershipId = etTargetUserId.getText().toString().trim();
        if (membershipId.isEmpty()) {
            Toast.makeText(this, "Enter Target User ID (as record ID for demo)", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/modstat/" + membershipId;
        Map<String, Boolean> params = new HashMap<>();
        params.put("is_moderator", !isCurrentMod); 

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, new JSONObject(params),
                response -> {
                    isCurrentMod = !isCurrentMod;
                    Toast.makeText(this, "Moderator status toggled!", Toast.LENGTH_SHORT).show();
                },
                error -> Toast.makeText(this, "Toggle failed", Toast.LENGTH_SHORT).show());

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
