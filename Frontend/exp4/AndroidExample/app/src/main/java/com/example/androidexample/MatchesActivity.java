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
 * Activity for managing matches (John's Part: GET and PUT).
 * Aligned with backend MatchController and MatchStatus Enum (PENDING, ACCEPTED, DECLINED, BLOCKED, UNMATCHED).
 */
public class MatchesActivity extends AppCompatActivity {

    private TextView tvMatchData;
    private EditText etMatchId;
    private Button btnRefreshMatches, btnAcceptMatch, btnRejectMatch, btnBlockMatch, btnUnmatchMatch, btnPendingMatch, btnBackToHome;

    private static final String BASE_URL = "http://coms-3090-015.class.las.iastate.edu:8080/matches";
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);

        // Initialize UI
        tvMatchData = findViewById(R.id.tvMatchData);
        etMatchId = findViewById(R.id.etMatchId);
        btnRefreshMatches = findViewById(R.id.btnRefreshMatches);
        
        btnAcceptMatch = findViewById(R.id.btnAcceptMatch);
        btnRejectMatch = findViewById(R.id.btnRejectMatch);
        btnBlockMatch = findViewById(R.id.btnBlockMatch);
        btnUnmatchMatch = findViewById(R.id.btnUnmatchMatch);
        btnPendingMatch = findViewById(R.id.btnPendingMatch);
        
        btnBackToHome = findViewById(R.id.btnBackToHome);

        currentUserId = getIntent().getIntExtra("USER_ID", 1);

        // GET: My Matches
        btnRefreshMatches.setOnClickListener(v -> fetchMyMatches());

        // PUT: Update Status (Full Enum Support)
        btnAcceptMatch.setOnClickListener(v -> updateMatchStatus("ACCEPTED"));
        btnRejectMatch.setOnClickListener(v -> updateMatchStatus("DECLINED"));
        btnBlockMatch.setOnClickListener(v -> updateMatchStatus("BLOCKED"));
        btnUnmatchMatch.setOnClickListener(v -> updateMatchStatus("UNMATCHED"));
        btnPendingMatch.setOnClickListener(v -> updateMatchStatus("PENDING"));

        btnBackToHome.setOnClickListener(v -> finish());

        // Initial fetch
        fetchMyMatches();
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
                            
                            long mId = match.optLong("matchId");
                            long u1 = match.optLong("user1Id");
                            long u2 = match.optLong("user2Id");
                            String status = match.optString("status");
                            String created = match.optString("createdAt");

                            sb.append("Match ID: ").append(mId)
                              .append("\nUser 1 ID: ").append(u1)
                              .append("\nUser 2 ID: ").append(u2)
                              .append("\nStatus: ").append(status)
                              .append("\nCreated: ").append(created)
                              .append("\n------------------\n");
                        }
                        tvMatchData.setText(sb.toString());
                    } catch (JSONException e) {
                        tvMatchData.setText("Error parsing matches.");
                    }
                },
                error -> tvMatchData.setText("Failed to load matches. (Server error or empty)"));

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
                error -> {
                    Log.e("MatchUpdate", "Error: " + error.toString());
                    Toast.makeText(this, "Update failed. Check ID or Status.", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
