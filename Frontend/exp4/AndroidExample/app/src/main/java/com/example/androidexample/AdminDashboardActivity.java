package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class AdminDashboardActivity extends AppCompatActivity {

    private Button btnModeratorRequests;
    private Button btnReportedUsers;
    private Button btnSuspendedAccounts;
    private Button btnUsageAnalytics;
    private Button btnBackAdminDashboard;
    private int adminUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        adminUserId = getIntent().getIntExtra("USER_ID", -1);
        btnModeratorRequests = findViewById(R.id.btnModeratorRequests);
        btnReportedUsers = findViewById(R.id.btnReportedUsers);
        btnSuspendedAccounts = findViewById(R.id.btnSuspendedAccounts);
        btnUsageAnalytics = findViewById(R.id.btnUsageAnalytics);
        btnBackAdminDashboard = findViewById(R.id.btnBackAdminDashboard);

        btnModeratorRequests.setOnClickListener(v -> {
            Intent intent = new Intent(
                    AdminDashboardActivity.this,
                    AdminModeratorRequestsActivity.class
            );
            intent.putExtra("ADMIN_USER_ID", adminUserId);
            startActivity(intent);
        });

        btnReportedUsers.setOnClickListener(v -> {
            Intent intent = new Intent(
                    AdminDashboardActivity.this,
                    AdminReportActivity.class
            );
            intent.putExtra("ADMIN_USER_ID", adminUserId);
            startActivity(intent);
        });

        btnSuspendedAccounts.setOnClickListener(v -> {
            Intent intent = new Intent(
                    AdminDashboardActivity.this,
                    AdminAccountStatusActivity.class
            );
            intent.putExtra("ADMIN_USER_ID", adminUserId);
            startActivity(intent);
        });

        btnUsageAnalytics.setOnClickListener(v -> {
            Intent intent = new Intent(
                    AdminDashboardActivity.this,
                    AdminUsageAnalyticsActivity.class
            );
            intent.putExtra("ADMIN_USER_ID", adminUserId);
            startActivity(intent);
        });

        btnBackAdminDashboard.setOnClickListener(v -> finish());
    }
}