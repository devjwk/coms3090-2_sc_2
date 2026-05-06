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
    private Button btnAppSettings;
    private Button btnBackAdminDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        btnModeratorRequests = findViewById(R.id.btnModeratorRequests);
        btnReportedUsers = findViewById(R.id.btnReportedUsers);
        btnSuspendedAccounts = findViewById(R.id.btnSuspendedAccounts);
        btnUsageAnalytics = findViewById(R.id.btnUsageAnalytics);
        btnAppSettings = findViewById(R.id.btnAppSettings);
        btnBackAdminDashboard = findViewById(R.id.btnBackAdminDashboard);

        btnModeratorRequests.setOnClickListener(v -> {
            Intent intent = new Intent(
                    AdminDashboardActivity.this,
                    AdminModeratorRequestsActivity.class
            );
            startActivity(intent);
        });

        btnReportedUsers.setOnClickListener(v ->
                Toast.makeText(
                        AdminDashboardActivity.this,
                        "Reported users management coming soon",
                        Toast.LENGTH_SHORT
                ).show()
        );

        btnSuspendedAccounts.setOnClickListener(v ->
                Toast.makeText(
                        AdminDashboardActivity.this,
                        "Account suspension management coming soon",
                        Toast.LENGTH_SHORT
                ).show()
        );

        btnUsageAnalytics.setOnClickListener(v ->
                Toast.makeText(
                        AdminDashboardActivity.this,
                        "Usage analytics coming soon",
                        Toast.LENGTH_SHORT
                ).show()
        );

        btnAppSettings.setOnClickListener(v ->
                Toast.makeText(
                        AdminDashboardActivity.this,
                        "App-wide settings coming soon",
                        Toast.LENGTH_SHORT
                ).show()
        );

        btnBackAdminDashboard.setOnClickListener(v -> finish());
    }
}