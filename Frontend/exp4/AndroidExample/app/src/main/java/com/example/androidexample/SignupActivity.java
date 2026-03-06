package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
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
        final Map<String, Object> params = new HashMap<>();
        params.put("email", etEmail.getText().toString().trim());
        params.put("passwordHash", etPassword.getText().toString().trim()); // Match backend field
        params.put("displayName", etDisplayName.getText().toString().trim());
        params.put("bio", etBio.getText().toString().trim());
        params.put("major", etMajor.getText().toString().trim());
        
        try {
            params.put("age", Integer.parseInt(etAge.getText().toString().trim()));
        } catch (NumberFormatException e) {
            params.put("age", 0);
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_SIGNUP,
                response -> {
                    Toast.makeText(getApplicationContext(), "Signup Successful!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    Log.e("Signup Error", error.toString());
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