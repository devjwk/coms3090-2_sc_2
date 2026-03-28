package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.java_websocket.handshake.ServerHandshake;

public class WebSocketNotificationActivity extends AppCompatActivity implements WebSocketEventListener {

    private Button sendBtn;
    private Button clearBtn;
    private EditText msgEtx;
    private TextView msgTv;
    private TextView statusTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_websocket_notification);

        sendBtn = findViewById(R.id.sendBtn);
        clearBtn = findViewById(R.id.clearBtn);
        msgEtx = findViewById(R.id.msgEdt);
        msgTv = findViewById(R.id.tx1);
        statusTv = findViewById(R.id.statusTv);

        WebSocketClientManager.getInstance().setWebSocketEventListener(this);

        sendBtn.setOnClickListener(v -> {
            try {
                String message = msgEtx.getText().toString().trim();
                if (!message.isEmpty()) {
                    WebSocketClientManager.getInstance().sendMessage(message);
                    msgEtx.setText("");
                }
            } catch (Exception e) {
                Log.d("ExceptionSendMessage", String.valueOf(e.getMessage()));
            }
        });

        clearBtn.setOnClickListener(v -> {
            msgTv.setText("");
            Toast.makeText(getApplicationContext(), "Notifications Cleared", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onWebSocketMessage(String message) {
        runOnUiThread(() -> {
            String currentText = msgTv.getText().toString();
            String formattedMessage = NotificationFormatter.formatNotification(message);

            msgTv.setText(currentText + "\n" + formattedMessage);

            Toast.makeText(
                    getApplicationContext(),
                    formattedMessage,
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        String closedBy = remote ? "server" : "local";
        runOnUiThread(() -> {
            statusTv.setText("Disconnected");
            String s = msgTv.getText().toString();
            msgTv.setText(s + "\n---\nconnection closed by " + closedBy + "\nreason: " + reason);
        });
    }

    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {
        runOnUiThread(() -> statusTv.setText("Connected"));
    }

    @Override
    public void onWebSocketError(Exception ex) {
        runOnUiThread(() -> statusTv.setText("Connection Error"));
    }
}