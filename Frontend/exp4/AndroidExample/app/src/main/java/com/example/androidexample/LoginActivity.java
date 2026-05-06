package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import android.view.View;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;

    private Button btnLogin, btnSignup, btnDeleteUser, btnModerator,btnAdmin, btnModeratorLogin;

    private static final String LOGIN_URL = "http://coms-3090-015.class.las.iastate.edu:8080/login";
    //private static final String LOGIN_URL = "http://10.0.2.2:3002/login";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername    = findViewById(R.id.login_username_edt);
        etPassword    = findViewById(R.id.login_password_edt);
        btnLogin      = findViewById(R.id.login_login_btn);
        btnSignup     = findViewById(R.id.login_signup_btn);
        btnDeleteUser = findViewById(R.id.login_delete_user_btn);
        btnModerator  = findViewById(R.id.login_moderator_btn);
        btnModeratorLogin = findViewById(R.id.login_moderator_login_btn);
        btnAdmin = findViewById(R.id.login_admin_btn);
        btnAdmin.setVisibility(View.GONE);
        btnLogin.setOnClickListener(v -> loginUser());

        btnSignup.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)));

        btnDeleteUser.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, DeleteUserActivity.class)));

        btnModerator.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ModeratorActivity.class))
                );

        btnModeratorLogin.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ModeratorLoginActivity.class))
        );
    }

    private void loginUser() {
        String email    = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        String url;
        try {
            url = LOGIN_URL + "?email=" + URLEncoder.encode(email, "UTF-8")
                    + "&passwordHash=" + URLEncoder.encode(password, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(this, "Error building request", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    android.util.Log.d("LOGIN_RESPONSE", response.toString());
                    try {
                        JSONObject userObject = response.optJSONObject("user");
                        if (userObject == null) {
                            userObject = response;
                        }

                        boolean hasValidLoginResponse = "success".equalsIgnoreCase(response.optString("message", ""))
                                || response.has("user_id")
                                || response.has("userId")
                                || response.has("user");

                        if (!hasValidLoginResponse) {
                            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int uid = userObject.optInt("userId", userObject.optInt("user_id", -1));
                        if (uid <= 0) {
                            Toast.makeText(this, "Invalid user ID from server", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        boolean isAdmin = response.optBoolean("isAdmin", false);

                        String userRole = response.optString("UserRole",
                                response.optString("userRole",
                                        userObject.optString("role", "")));

                        Intent intent;
                        if (isAdmin || "ADMIN".equalsIgnoreCase(userRole)) {
                            intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                            intent.putExtra("ADMIN_USER_ID", uid);
                        } else {
                            intent = new Intent(LoginActivity.this, HomeActivity.class);
                        }

                        intent.putExtra("USER_ID", uid);
                        intent.putExtra("USER_JSON", userObject.toString());
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(this, "Invalid server response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
}