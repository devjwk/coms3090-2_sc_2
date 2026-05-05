package com.example.androidexample;
// SignupActivity.java
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

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etDisplayName, etBio, etMajor, etAge, etInterests;
    private Button btnSignup, btnBackToMain;

    private static final String URL_SIGNUP = "http://coms-3090-015.class.las.iastate.edu:8080/users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize UI components
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etDisplayName = findViewById(R.id.etDisplayName);
        etBio = findViewById(R.id.etBio);
        etMajor = findViewById(R.id.etMajor);
        etAge = findViewById(R.id.etAge);
        etInterests = findViewById(R.id.etInterests);
        btnSignup = findViewById(R.id.btnSignup);
        btnBackToMain = findViewById(R.id.btnBackToMain);

        btnSignup.setOnClickListener(v -> performSignup());
        btnBackToMain.setOnClickListener(v -> finish());
    }

    private void performSignup() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and Password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        final Map<String, Object> params = new HashMap<>();
        params.put("email", email);
        params.put("passwordHash", password); // Matches backend User entity field
        params.put("displayName", etDisplayName.getText().toString().trim());
        params.put("bio", etBio.getText().toString().trim());
        params.put("major", etMajor.getText().toString().trim());
        params.put("active",true);// backend: @Column(name = "active", nullable = false)
        params.put("status","NEED_APPROVAL");// backend: @Column(name = "status", nullable = false)")
        try {
            String ageStr = etAge.getText().toString().trim();
            params.put("age", ageStr.isEmpty() ? null : Integer.parseInt(ageStr));
        } catch (NumberFormatException e) {
            params.put("age", null);
        }

        String interestsText = etInterests.getText().toString().trim();
        org.json.JSONArray interestsArray = new org.json.JSONArray();

        if (!interestsText.isEmpty()) {
            String[] interests = interestsText.split(",");
            for (String interest : interests) {
                String trimmed = interest.trim();
                if (!trimmed.isEmpty()) {
                    interestsArray.put(trimmed);
                }
            }
        }

        params.put("interests", interestsArray);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_SIGNUP,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String message = jsonResponse.optString("message", "");
                        
                        if ("success".equalsIgnoreCase(message)) {
                            Toast.makeText(getApplicationContext(), "Signup Successful!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Signup Failed: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        // If response is just "success" string instead of JSON
                        if (response.contains("success")) {
                            Toast.makeText(getApplicationContext(), "Signup Successful!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Log.e("Signup Error", "JSON Parsing error", e);
                            Toast.makeText(getApplicationContext(), "Server error. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                error -> {
                    Log.e("Signup Error", "Volley Error: " + error.toString());
                    Toast.makeText(getApplicationContext(), "Signup Failed. Check connection.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return new JSONObject(params).toString().getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
}
