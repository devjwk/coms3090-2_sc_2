package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button connectBtn;
    private EditText serverEtx, usernameEtx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectBtn = findViewById(R.id.connectBtn);
        serverEtx = findViewById(R.id.serverEdt);
        usernameEtx = findViewById(R.id.unameEdt);

        connectBtn.setOnClickListener(view -> {
            String baseUrl = serverEtx.getText().toString().trim();
            String username = usernameEtx.getText().toString().trim();

            if (baseUrl.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "Enter server URL and username", Toast.LENGTH_SHORT).show();
                return;
            }

            String serverUrl;
            if (baseUrl.endsWith("/")) {
                serverUrl = baseUrl + username;
            } else {
                serverUrl = baseUrl + "/" + username;
            }

            WebSocketManager.getInstance().connectWebSocket(serverUrl);

            Intent intent = new Intent(this, ChatActivity.class);
            startActivity(intent);
        });
    }
}