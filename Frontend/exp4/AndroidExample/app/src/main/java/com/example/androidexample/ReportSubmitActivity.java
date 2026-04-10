package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class ReportSubmitActivity extends AppCompatActivity {

    private EditText etReportedUserId, etReportDescription;
    private Button btnSubmitReport, btnBackReport;

    private int userId;
    private String userJson;

    private static final String REPORT_URL = "http://coms-3090-015.class.las.iastate.edu:8080/reports";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_submit);

        userId = getIntent().getIntExtra("USER_ID", 1);
        userJson = getIntent().getStringExtra("USER_JSON");

        etReportedUserId = findViewById(R.id.etReportedUserId);
        etReportDescription = findViewById(R.id.etReportDescription);
        btnSubmitReport = findViewById(R.id.btnSubmitReport);
        btnBackReport = findViewById(R.id.btnBackReport);

        btnSubmitReport.setOnClickListener(v -> submitReport());
        btnBackReport.setOnClickListener(v -> finish());
    }

    private void submitReport() {
        String reportedUserIdStr = etReportedUserId.getText().toString().trim();
        String description = etReportDescription.getText().toString().trim();

        if (reportedUserIdStr.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int reportedUserId;
        try {
            reportedUserId = Integer.parseInt(reportedUserIdStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Reported User ID must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject();

        try {
            JSONObject reporterObj = new JSONObject();
            reporterObj.put("userId", userId);

            JSONObject reportedObj = new JSONObject();
            reportedObj.put("userId", reportedUserId);

            body.put("reporterId", reporterObj);
            body.put("reportedId", reportedObj);
            body.put("description", description);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to build report request", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.POST,
                REPORT_URL,
                response -> {
                    Log.d("REPORT_SUBMIT", "response: " + response);

                    if (response.contains("Report created")) {
                        Toast.makeText(this, "Report submitted", Toast.LENGTH_SHORT).show();
                        etReportedUserId.setText("");
                        etReportDescription.setText("");
                        finish();
                    } else {
                        Toast.makeText(this, "Unexpected response: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("REPORT_SUBMIT", "error: " + error.toString());

                    if (error.networkResponse != null) {
                        Log.e("REPORT_SUBMIT", "status: " + error.networkResponse.statusCode);

                        try {
                            String errorBody = new String(error.networkResponse.data);
                            Log.e("REPORT_SUBMIT", "body: " + errorBody);
                            Toast.makeText(this, errorBody, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(this, "Failed to submit report", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return body.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
}