package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ModeratorLoginActivity extends AppCompatActivity {
    private EditText etModeratorEmail;
    private EditText etModeratorPassword;
    private ModeratorRepository repository;
    private ModeratorSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator_login);

        repository = new ModeratorRepository();
        sessionManager = new ModeratorSessionManager(this);

        etModeratorEmail = findViewById(R.id.etModeratorEmail);
        etModeratorPassword = findViewById(R.id.etModeratorPassword);
        Button btnModeratorLogin = findViewById(R.id.btnModeratorLogin);
        Button btnBack = findViewById(R.id.btnModeratorLoginBack);
        Button btnSignup = findViewById(R.id.btnModeratorSignup);

        btnModeratorLogin.setOnClickListener(v -> loginModerator());
        btnBack.setOnClickListener(v -> finish());
        btnSignup.setOnClickListener(v -> startActivity(new Intent(ModeratorLoginActivity.this, ModeratorSignupActivity.class)));
    }

    private void loginModerator() {
        String email = etModeratorEmail.getText().toString().trim();
        String passwordHash = etModeratorPassword.getText().toString().trim();

        if (email.isEmpty() || passwordHash.isEmpty()) {
            Toast.makeText(this, "Enter moderator credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.loginModerator(this, email, passwordHash, new ModeratorRepository.ModeratorAccountCallback() {
            @Override
            public void onSuccess(ModeratorAccount account) {
                sessionManager.saveSession(account);
                Intent intent = new Intent(ModeratorLoginActivity.this, ModeratorDashboardActivity.class);
                intent.putExtra("MODERATOR_ID", account.getModeratorId());
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ModeratorLoginActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

