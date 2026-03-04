package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    // UI Components
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
        etBio = findViewById(R.id.etBio);etMajor = findViewById(R.id.etMajor);
        etAge = findViewById(R.id.etAge);
        etInterests = findViewById(R.id.etInterests);
        btnSignup = findViewById(R.id.btnSignup);
        btnBackToMain = findViewById(R.id.btnBackToMain);

        btnSignup.setOnClickListener(v -> performSignup());

        btnBackToMain.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void performSignup() {
        final Map<String, String> params = new HashMap<>();
        params.put("email", etEmail.getText().toString());
        params.put("passwordHash", etPassword.getText().toString());
        params.put("displayName", etDisplayName.getText().toString());
        params.put("bio", etBio.getText().toString());
        params.put("major", etMajor.getText().toString());
        params.put("age", etAge.getText().toString());
        params.put("interests", etInterests.getText().toString());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_SIGNUP,
                response -> {
                    // 1. Log the raw string response from the server.
                    Log.d("Volley Raw Response", "Server says: " + response);

                    try {
                        // 2. Try to parse it as a JSONObject.
                        JSONObject jsonResponse = new JSONObject(response);
                        Log.d("Volley Signup Rsp", jsonResponse.toString());
                        Toast.makeText(getApplicationContext(), "Signup Successful!", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        Log.e("Volley Signup Error", "JSON Parsing error: " + e.getMessage());
                        Toast.makeText(getApplicationContext(), "Signup Failed! (Bad response format)", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("Volley Signup Error", "Error: " + error.toString());
                    Toast.makeText(getApplicationContext(), "Signup Failed! Check logs.", Toast.LENGTH_SHORT).show();
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

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }
}
