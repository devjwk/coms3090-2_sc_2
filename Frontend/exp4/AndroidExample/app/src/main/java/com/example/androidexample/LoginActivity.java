package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin, btnSignup, btnDeleteUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.login_username_edt);
        etPassword = findViewById(R.id.login_password_edt);
        btnLogin   = findViewById(R.id.login_login_btn);
        btnSignup  = findViewById(R.id.login_signup_btn);
        btnDeleteUser = findViewById(R.id.login_delete_user_btn);

        btnLogin.setOnClickListener(v -> loginUser());

        btnSignup.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        btnDeleteUser.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, DeleteUserActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(LoginActivity.this, Login.class);
        intent.putExtra("USER_ID", 1);
        startActivity(intent);
        // finish(); // This line is removed to keep LoginActivity in the back stack
    }
}
