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
 * This screen allows a user to join a group, check membership information,
 * update membership status, toggle moderator privileges, and leave a group.
 * It communicates with backend group membership endpoints using Volley requests.
 */
public class GroupMembershipActivity extends AppCompatActivity {

    /** Input field for entering the group ID */
    private EditText etGroupId;

    /** Input field for entering the target user or membership ID */
    private EditText etTargetUserId;

    /** Button used to join a group */
    private Button btnJoinGroup;

    /** Button used to check membership information */
    private Button btnCheckMembership;

    /** Button used to approve a membership request */
    private Button btnApprove;

    /** Button used to ban a member */
    private Button btnBan;

    /** Button used to toggle moderator status */
    private Button btnToggleMod;

    /** Button used to leave a group */
    private Button btnLeaveGroup;

    /** Button used to return to the previous screen */
    private Button btnBack;

    /** TextView used to display membership details and status messages */
    private TextView tvMembershipInfo;

    /** Base URL for all group membership related backend requests */
    private static final String BASE_URL = "http://coms-3090-015.class.las.iastate.edu:8080/gm";

    /** Logged-in user's ID passed through the activity intent */
    private int myUserId;

    /** Tracks whether the currently selected member is a moderator */
    private boolean isCurrentMod = false;

    /**
     * Initializes the activity, connects UI components, retrieves the logged-in user ID,
     * and sets button click listeners for all membership actions.
     *
     * @param savedInstanceState saved instance state bundle provided by Android
     */
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

    /**
     * Sends a request to the backend for the current user to join the specified group.
     * If the group ID field is empty, a warning message is shown instead.
     */
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

    /**
     * Checks whether a specific user belongs to the selected group by requesting
     * the backend membership list for that group. If a matching user is found,
     * membership information is displayed on the screen.
     *
     * @param userIdToFind the user ID to search for within the selected group
     */
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

    /**
     * Updates the membership status of the selected membership record.
     * The membership ID is read from the target ID input field and sent
     * to the backend with the requested status value.
     *
     * @param newStatus the new status to assign, such as "active" or "banned"
     */
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

    /**
     * Toggles the moderator status of the selected membership record.
     * The current moderator flag is flipped locally after a successful backend update.
     */
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

    /**
     * Sends a request for the current user to leave the specified group.
     * If successful, a confirmation message is displayed and the membership
     * information area is updated.
     */
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
