package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ModeratorSignupActivity extends AppCompatActivity {
    private EditText etModeratorEmail;
    private EditText etModeratorPassword;
    private EditText etModeratorDisplayName;
    private ModeratorRepository repository;
    private ModeratorSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator_signup);

        repository = new ModeratorRepository();
        sessionManager = new ModeratorSessionManager(this);

        etModeratorEmail = findViewById(R.id.etModeratorSignupEmail);
        etModeratorPassword = findViewById(R.id.etModeratorSignupPassword);
        etModeratorDisplayName = findViewById(R.id.etModeratorSignupDisplayName);
        Button btnModeratorSignup = findViewById(R.id.btnModeratorSignup);
        Button btnBackToLogin = findViewById(R.id.btnModeratorSignupBack);

        btnModeratorSignup.setOnClickListener(v -> signupModerator());
        btnBackToLogin.setOnClickListener(v -> finish());
    }

    private void signupModerator() {
        String email = etModeratorEmail.getText().toString().trim();
        String password = etModeratorPassword.getText().toString().trim();
        String displayName = etModeratorDisplayName.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || displayName.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.signupModerator(this, email, password, displayName, new ModeratorRepository.ModeratorAccountCallback() {
            @Override
            public void onSuccess(ModeratorAccount account) {
                sessionManager.saveSession(account);
                Toast.makeText(ModeratorSignupActivity.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ModeratorSignupActivity.this, ModeratorDashboardActivity.class);
                intent.putExtra("MODERATOR_ID", account.getModeratorId());
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ModeratorSignupActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

