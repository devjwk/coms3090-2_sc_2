package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin, btnToSignup;

    private static final String LOGIN_URL = "http://coms-3090-015.class.las.iastate.edu:8080/login";
    private static final String EDIT_URL = "http://coms-3090-015.class.las.iastate.edu:8080/users/edit/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.login_username_edt);
        etPassword = findViewById(R.id.login_password_edt);
        btnLogin   = findViewById(R.id.login_login_btn);
        btnToSignup  = findViewById(R.id.login_signup_btn);

        btnLogin.setOnClickListener(v -> loginUser());

        btnToSignup.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        JSONObject jsonObject = new JSONObject(params);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                LOGIN_URL,
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int userId = response.getInt("id");
                            Intent intent = new Intent(Login.this, MainActivity.class);
                            intent.putExtra("id", userId);
                            startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
