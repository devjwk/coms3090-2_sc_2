package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class MatchActivity extends AppCompatActivity {

    private TextView msgResponse;

    private EditText etUser2Id;
    private Button btnCreateMatch;

    private EditText etDeleteMatchId;
    private Button btnUnmatch;

    private Button btnBack;

    private int currentUserId;

    private static final String BASE_URL = "http://10.0.2.2:3002/matches";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        currentUserId = getIntent().getIntExtra("USER_ID", 1);

        msgResponse     = findViewById(R.id.msgResponse);
        etUser2Id       = findViewById(R.id.etUser2Id);
        btnCreateMatch  = findViewById(R.id.btnCreateMatch);
        etDeleteMatchId = findViewById(R.id.etDeleteMatchId);
        btnUnmatch      = findViewById(R.id.btnUnmatch);
        btnBack         = findViewById(R.id.btnBack);

        btnCreateMatch.setOnClickListener(v -> createMatch());
        btnBack.setOnClickListener(v -> finish());
    }

    private void createMatch() {
        String user2IdStr = etUser2Id.getText().toString().trim();

        if (user2IdStr.isEmpty()) {
            Toast.makeText(this, "Enter a user ID to match with.", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject();
        try {
            body.put("user1Id", currentUserId);
            body.put("user2Id", Integer.parseInt(user2IdStr));
            body.put("status", "Pending");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                BASE_URL,
                body,
                response -> {
                    Log.d("Create Match", response.toString());
                    msgResponse.setText("Match created!\n\n" +
                            "Match ID: " + response.optInt("matchId") + "\n" +
                            "Status: Pending");
                    Toast.makeText(this, "Match created!", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Log.e("Create Match Error", error.toString());
                    Toast.makeText(this, "Failed to create match.", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }


}