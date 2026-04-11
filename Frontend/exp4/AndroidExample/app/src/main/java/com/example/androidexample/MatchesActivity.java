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
 * Integrated Activity for managing matches (Combined John's and Haiqa's work).
 * Supports full CRUD: CREATE (POST), READ (GET), UPDATE (PUT), DELETE (DEL).
 * Aligned with MatchStatus Enum (PENDING, ACCEPTED, DECLINED, BLOCKED, UNMATCHED).
 */
public class MatchesActivity extends AppCompatActivity {

    private TextView tvMatchData;
    private EditText etMatchId, etUser2Id;
    private Button btnRefreshMatches, btnCreateMatch, btnAcceptMatch, btnRejectMatch, btnBlockMatch, btnUnmatchMatch, btnPendingMatch, btnUnmatch, btnBackToHome;

    private static final String BASE_URL = "http://coms-3090-015.class.las.iastate.edu:8080/matches";
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);

        // Initialize UI
        tvMatchData = findViewById(R.id.tvMatchData);
        etMatchId = findViewById(R.id.etMatchId);
        etUser2Id = findViewById(R.id.etUser2Id);

        btnRefreshMatches = findViewById(R.id.btnRefreshMatches);
        btnCreateMatch = findViewById(R.id.btnCreateMatch);

        // 5 Status Buttons Initialization
        btnAcceptMatch = findViewById(R.id.btnAcceptMatch);
        btnRejectMatch = findViewById(R.id.btnRejectMatch);
        btnBlockMatch = findViewById(R.id.btnBlockMatch);
        btnUnmatchMatch = findViewById(R.id.btnUnmatchMatch);
        btnPendingMatch = findViewById(R.id.btnPendingMatch);

        btnUnmatch = findViewById(R.id.btnUnmatch);
        btnBackToHome = findViewById(R.id.btnBackToHome);

        currentUserId = getIntent().getIntExtra("USER_ID", 1);

        // POST: Create Match
        btnCreateMatch.setOnClickListener(v -> createMatch());

        // GET: My Matches
        btnRefreshMatches.setOnClickListener(v -> fetchMyMatches());

        // PUT: Update Status (Support all 5 statuses)
        btnAcceptMatch.setOnClickListener(v -> updateMatchStatus("ACCEPTED"));
        btnRejectMatch.setOnClickListener(v -> updateMatchStatus("DECLINED"));
        btnBlockMatch.setOnClickListener(v -> updateMatchStatus("BLOCKED"));
        btnUnmatchMatch.setOnClickListener(v -> updateMatchStatus("UNMATCHED"));
        btnPendingMatch.setOnClickListener(v -> updateMatchStatus("PENDING"));

        // DEL: Unmatch (Permanent delete)
        btnUnmatch.setOnClickListener(v -> unmatch());

        btnBackToHome.setOnClickListener(v -> finish());

        // Initial fetch
        fetchMyMatches();
    }

    private void createMatch() {
        String user2IdStr = etUser2Id.getText().toString().trim();
        if (user2IdStr.isEmpty()) {
            Toast.makeText(this, "Enter a user ID to match with.", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject();
        try {
            body.put("user1Id", (long) currentUserId);
            body.put("user2Id", Long.parseLong(user2IdStr));
            body.put("status", "PENDING");
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid User ID", Toast.LENGTH_SHORT).show();
            return;
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, BASE_URL, body,
                response -> {
                    Toast.makeText(this, "Match Request Sent!", Toast.LENGTH_SHORT).show();
                    fetchMyMatches();
                },
                error -> Toast.makeText(this, "Failed to create match.", Toast.LENGTH_SHORT).show());

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void fetchMyMatches() {
        tvMatchData.setText("Fetching matches...");
        String url = BASE_URL + "/user/" + currentUserId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() == 0) {
                            tvMatchData.setText("No matches found.");
                            return;
                        }
                        StringBuilder sb = new StringBuilder("Your Matches:\n\n");
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject match = response.getJSONObject(i);
                            sb.append("Match ID: ").append(match.optLong("matchId"))
                                    .append("\nUser 1 ID: ").append(match.optLong("user1Id"))
                                    .append("\nUser 2 ID: ").append(match.optLong("user2Id"))
                                    .append("\nStatus: ").append(match.optString("status"))
                                    .append("\nCreated: ").append(match.optString("createdAt"))
                                    .append("\n------------------\n");
                        }
                        tvMatchData.setText(sb.toString());
                    } catch (JSONException e) {
                        tvMatchData.setText("Error parsing matches.");
                    }
                },
                error -> tvMatchData.setText("No matches found for this user."));

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void updateMatchStatus(String newStatus) {
        String matchIdStr = etMatchId.getText().toString().trim();
        if (matchIdStr.isEmpty()) {
            Toast.makeText(this, "Enter Match ID first", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/edit/" + matchIdStr;
        JSONObject body = new JSONObject();
        try {
            body.put("status", newStatus);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, body,
                response -> {
                    Toast.makeText(this, "Match status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    fetchMyMatches();
                },
                error -> Toast.makeText(this, "Update failed. Check ID or Status.", Toast.LENGTH_SHORT).show());

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void unmatch() {
        String matchIdStr = etMatchId.getText().toString().trim();
        if (matchIdStr.isEmpty()) {
            Toast.makeText(this, "Enter Match ID to delete.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/" + matchIdStr;
        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    Toast.makeText(this, "Unmatched successfully!", Toast.LENGTH_SHORT).show();
                    fetchMyMatches();
                },
                error -> Toast.makeText(this, "Failed to unmatch. Check ID.", Toast.LENGTH_SHORT).show());

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
