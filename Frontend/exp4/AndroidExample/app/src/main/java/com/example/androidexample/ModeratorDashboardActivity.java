package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ModeratorDashboardActivity extends AppCompatActivity {
    private ModeratorRepository repository;
    private ModeratorSessionManager sessionManager;

    private int moderatorId;

    private TextView tvModeratorProfile;
    private TextView tvModeratorPermissions;
    private EditText etGroupName;
    private EditText etGroupDescription;
    private EditText etGroupInterests;
    private Button btnCreateModeratorGroup;
    private LinearLayout layoutModeratorGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator_dashboard);

        repository = new ModeratorRepository();
        sessionManager = new ModeratorSessionManager(this);

        moderatorId = getIntent().getIntExtra("MODERATOR_ID", sessionManager.getModeratorId());
        if (moderatorId <= 0) {
            Toast.makeText(this, "No moderator session found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvModeratorProfile = findViewById(R.id.tvModeratorProfile);
        tvModeratorPermissions = findViewById(R.id.tvModeratorPermissions);
        etGroupName = findViewById(R.id.etCreateModeratorGroupName);
        etGroupDescription = findViewById(R.id.etCreateModeratorGroupDescription);
        etGroupInterests = findViewById(R.id.etCreateModeratorGroupInterests);
        btnCreateModeratorGroup = findViewById(R.id.btnCreateModeratorGroup);
        layoutModeratorGroups = findViewById(R.id.layoutModeratorGroups);

        Button btnRefresh = findViewById(R.id.btnRefreshModeratorDashboard);
        Button btnLogout = findViewById(R.id.btnModeratorLogout);

        btnRefresh.setOnClickListener(v -> loadDashboard());
        btnLogout.setOnClickListener(v -> {
            sessionManager.clear();
            finish();
        });

        btnCreateModeratorGroup.setOnClickListener(v -> createGroup());

        applyPermissionGates();
        loadDashboard();
    }

    private void applyPermissionGates() {
        Set<String> permissions = sessionManager.getPermissions();
        boolean canCreate = permissions.contains(ModeratorPermissions.CREATE_GROUP);
        btnCreateModeratorGroup.setEnabled(canCreate);
        btnCreateModeratorGroup.setAlpha(canCreate ? 1f : 0.5f);
    }

    private void loadDashboard() {
        loadProfile();
        loadGroups();
    }

    private void loadProfile() {
        repository.getModeratorProfile(this, moderatorId, new ModeratorRepository.ModeratorAccountCallback() {
            @Override
            public void onSuccess(ModeratorAccount account) {
                sessionManager.saveSession(account);
                tvModeratorProfile.setText(
                        "Moderator ID: " + account.getModeratorId() + "\n"
                                + "Email: " + account.getEmail() + "\n"
                                + "Auth Provider: " + account.getAuthProvider() + "\n"
                                + "Roles: " + TextUtils.join(", ", account.getRoles())
                );
                tvModeratorPermissions.setText("Permissions: " + TextUtils.join(", ", account.getPermissions()));
                applyPermissionGates();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ModeratorDashboardActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroups() {
        repository.getManagedGroups(this, moderatorId, new ModeratorRepository.GroupsCallback() {
            @Override
            public void onSuccess(List<ModeratorManagedGroup> groups) {
                renderGroupCards(groups);
            }

            @Override
            public void onError(String error) {
                layoutModeratorGroups.removeAllViews();
                TextView tv = new TextView(ModeratorDashboardActivity.this);
                tv.setText(error);
                tv.setTextColor(0xFFFFFFFF);
                layoutModeratorGroups.addView(tv);
            }
        });
    }

    private void renderGroupCards(List<ModeratorManagedGroup> groups) {
        layoutModeratorGroups.removeAllViews();
        if (groups.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No assigned groups yet.");
            empty.setTextColor(0xFFFFFFFF);
            layoutModeratorGroups.addView(empty);
            return;
        }

        for (ModeratorManagedGroup group : groups) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(12), dp(12), dp(12), dp(12));
            card.setBackgroundColor(0xFF1E1E30);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, dp(10));
            card.setLayoutParams(params);

            TextView tvName = new TextView(this);
            tvName.setText(group.getGroupName() + "  (#" + group.getGroupId() + ")");
            tvName.setTextColor(0xFFFFFFFF);
            tvName.setTextSize(16);

            TextView tvDesc = new TextView(this);
            tvDesc.setText(group.getDescription());
            tvDesc.setTextColor(0xFFB0B0C0);

            TextView tvOpen = new TextView(this);
            tvOpen.setText("Open Group Management");
            tvOpen.setTextColor(0xFF7B6FFF);
            tvOpen.setGravity(Gravity.END);

            card.addView(tvName);
            card.addView(tvDesc);
            card.addView(tvOpen);

            card.setOnClickListener(v -> {
                Intent intent = new Intent(ModeratorDashboardActivity.this, ModeratorGroupManagementActivity.class);
                intent.putExtra("MODERATOR_ID", moderatorId);
                intent.putExtra("GROUP_ID", group.getGroupId());
                intent.putExtra("GROUP_NAME", group.getGroupName());
                startActivity(intent);
            });

            layoutModeratorGroups.addView(card);
        }
    }

    private void createGroup() {
        String name = etGroupName.getText().toString().trim();
        String description = etGroupDescription.getText().toString().trim();
        String interestsRaw = etGroupInterests.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Enter group name", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> interests = new ArrayList<>();
        if (!interestsRaw.isEmpty()) {
            interests = Arrays.asList(interestsRaw.split(","));
        }

        repository.createGroup(this, moderatorId, name, description, interests, new ModeratorRepository.JsonObjectCallback() {
            @Override
            public void onSuccess(JSONObject object) {
                Toast.makeText(ModeratorDashboardActivity.this, "Group created", Toast.LENGTH_SHORT).show();
                etGroupName.setText("");
                etGroupDescription.setText("");
                etGroupInterests.setText("");
                loadGroups();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ModeratorDashboardActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}

