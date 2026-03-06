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
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity for managing group memberships.
 * Aligned with backend requirements: Join, List, Update, and Leave group.
 */
public class GroupMembershipActivity extends AppCompatActivity {

    private EditText etGroupId, etTargetUserId;
    private Button btnJoinGroup, btnCheckMembership, btnApprove, btnBan, btnToggleMod, btnLeaveGroup, btnBack;
    private TextView tvMembershipInfo;

    private static final String BASE_URL = "http://10.0.2.2:3002/gm";

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
        btnLeaveGroup = findViewById(R.id.btnLeaveGroup);
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
        btnLeaveGroup.setOnClickListener(v -> leaveGroup());
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

        // Updated to use /gm/glist/{groupId} which returns UserInfo records
        String url = BASE_URL + "/glist/" + targetGid;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        boolean found = false;
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            
                            // Robust parsing for UserInfo record fields: userId, displayName, groupName, groupId
                            long uid = obj.optLong("userId", obj.optLong("userid", -1));

                            if (uid == userIdToFind) {
                                String dName = obj.optString("displayName", obj.optString("displayname", "N/A"));
                                String gName = obj.optString("groupName", obj.optString("groupname", "N/A"));
                                long gid = obj.optLong("groupId", obj.optLong("groupid", targetGid));

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
            Toast.makeText(this, "Enter Membership ID in 'Target ID' field", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Enter Membership ID in 'Target ID' field", Toast.LENGTH_SHORT).show();
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

    private void leaveGroup() {
        String gidStr = etGroupId.getText().toString().trim();
        if (gidStr.isEmpty()) {
            Toast.makeText(this, "Enter Group ID to leave", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/leave/" + gidStr;

        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    Toast.makeText(this, "Successfully left group", Toast.LENGTH_SHORT).show();
                    tvMembershipInfo.setText("Group left successfully.");
                },
                error -> Toast.makeText(this, "Failed to leave group", Toast.LENGTH_SHORT).show());

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
