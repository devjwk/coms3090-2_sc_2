package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignupActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText confirmEditText;
    private Button loginButton;
    private Button signupButton;

    private static final String name = "authprefs";
    private static final String keyUsername = "savedusername";
    private static final String keyPass = "savedpassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        usernameEditText = findViewById(R.id.signup_username_edt);
        passwordEditText = findViewById(R.id.signup_password_edt);
        confirmEditText = findViewById(R.id.signup_confirm_edt);
        loginButton = findViewById(R.id.signup_login_btn);
        signupButton = findViewById(R.id.signup_signup_btn);

        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        signupButton.setOnClickListener(v -> {
            String enteredUsername = usernameEditText.getText().toString().trim();
            String enteredPassword = passwordEditText.getText().toString();
            String confirm = confirmEditText.getText().toString();

            if (enteredUsername.isEmpty() || enteredPassword.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_LONG).show();
                return;
            }

            if (containsDigit(enteredUsername)) {
                Toast.makeText(this, "Username cannot contain numbers.", Toast.LENGTH_LONG).show();
                return;
            }

            if (enteredPassword.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_LONG).show();
                return;
            }

            if (!containsUppercase(enteredPassword)) {
                Toast.makeText(this, "Password must contain at least 1 uppercase letter.", Toast.LENGTH_LONG).show();
                return;
            }

            if (!containsDigit(enteredPassword)) {
                Toast.makeText(this, "Password must contain at least 1 digit.", Toast.LENGTH_LONG).show();
                return;
            }

            if (!enteredPassword.equals(confirm)) {
                Toast.makeText(this, "Passwords don't match.", Toast.LENGTH_LONG).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences(name, MODE_PRIVATE);
            prefs.edit()
                    .putString(keyUsername, enteredUsername)
                    .putString(keyPass, enteredPassword)
                    .apply();

            Toast.makeText(this, "Sign up successful. Please log in.", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean containsDigit(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isDigit(s.charAt(i))) return true;
        }
        return false;
    }

    private boolean containsUppercase(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isUpperCase(s.charAt(i))) return true;
        }
        return false;
    }
}
