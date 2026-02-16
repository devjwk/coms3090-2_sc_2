package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button signupButton;

    private static final String name = "authprefs";
    private static final String keyUsername = "savedusername";
    private static final String keyPass = "savedpassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.login_username_edt);
        passwordEditText = findViewById(R.id.login_password_edt);
        loginButton = findViewById(R.id.login_login_btn);
        signupButton = findViewById(R.id.login_signup_btn);

        loginButton.setOnClickListener(v -> {
            String enteredUsername = usernameEditText.getText().toString().trim();
            String enteredPassword = passwordEditText.getText().toString();

            if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
                Toast.makeText(this, "Enter username and password.", Toast.LENGTH_LONG).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences(name, MODE_PRIVATE);
            String savedUsername = prefs.getString(keyUsername, null);
            String savedPassword = prefs.getString(keyPass, null);

            if (savedUsername == null || savedPassword == null) {
                Toast.makeText(this, "No account found. Please sign up first.", Toast.LENGTH_LONG).show();
                return;
            }

            if (enteredUsername.equals(savedUsername) && enteredPassword.equals(savedPassword)) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("USERNAME", enteredUsername);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid username or password.", Toast.LENGTH_LONG).show();
            }
        });

        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }
}
