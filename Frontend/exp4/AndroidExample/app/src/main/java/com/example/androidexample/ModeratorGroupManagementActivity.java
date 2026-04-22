package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Set;

public class ModeratorGroupManagementActivity extends AppCompatActivity {
    private ModeratorRepository repository;
    private ModeratorSessionManager sessionManager;

    private int moderatorId;
    private int groupId;

    private TextView tvGroupHeader;
    private TextView tvGroupDetails;
    private TextView tvMembers;
    private TextView tvPendingMembers;
    private TextView tvEvents;
    private TextView tvAnnouncements;
    private TextView tvMessages;

    private EditText etApproveMemberId;
    private EditText etRemoveMemberId;
    private EditText etModerationMessageId;
    private EditText etEventTitle;
    private EditText etEventWhen;
    private EditText etAnnouncement;

    private Button btnApproveMember;
    private Button btnRemoveMember;
    private Button btnRemoveMessage;
    private Button btnRestoreMessage;
    private Button btnScheduleEvent;
    private Button btnPinAnnouncement;

    private int conversationId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator_group_management);

        repository = new ModeratorRepository();
        sessionManager = new ModeratorSessionManager(this);

        moderatorId = getIntent().getIntExtra("MODERATOR_ID", sessionManager.getModeratorId());
        groupId = getIntent().getIntExtra("GROUP_ID", -1);
        String groupName = getIntent().getStringExtra("GROUP_NAME");

        if (moderatorId <= 0 || groupId <= 0) {
            Toast.makeText(this, "Invalid moderator/group context", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvGroupHeader = findViewById(R.id.tvModGroupHeader);
        tvGroupDetails = findViewById(R.id.tvModGroupDetails);
        tvMembers = findViewById(R.id.tvModMembers);
        tvPendingMembers = findViewById(R.id.tvModPendingMembers);
        tvEvents = findViewById(R.id.tvModEvents);
        tvAnnouncements = findViewById(R.id.tvModAnnouncements);
        tvMessages = findViewById(R.id.tvModMessages);

        etApproveMemberId = findViewById(R.id.etApproveMemberId);
        etRemoveMemberId = findViewById(R.id.etRemoveMemberId);
        etModerationMessageId = findViewById(R.id.etModerationMessageId);
        etEventTitle = findViewById(R.id.etEventTitle);
        etEventWhen = findViewById(R.id.etEventWhen);
        etAnnouncement = findViewById(R.id.etAnnouncement);

        btnApproveMember = findViewById(R.id.btnApproveMember);
        btnRemoveMember = findViewById(R.id.btnRemoveMember);
        btnRemoveMessage = findViewById(R.id.btnRemoveMessage);
        btnRestoreMessage = findViewById(R.id.btnRestoreMessage);
        btnScheduleEvent = findViewById(R.id.btnScheduleEvent);
        btnPinAnnouncement = findViewById(R.id.btnPinAnnouncement);
        Button btnRefresh = findViewById(R.id.btnRefreshGroupManagement);
        Button btnBack = findViewById(R.id.btnBackGroupManagement);

        tvGroupHeader.setText("Manage: " + (groupName == null ? ("Group #" + groupId) : groupName));

        btnApproveMember.setOnClickListener(v -> approveMember());
        btnRemoveMember.setOnClickListener(v -> removeMember());
        btnRemoveMessage.setOnClickListener(v -> removeMessage());
        btnRestoreMessage.setOnClickListener(v -> restoreMessage());
        btnScheduleEvent.setOnClickListener(v -> scheduleEvent());
        btnPinAnnouncement.setOnClickListener(v -> pinAnnouncement());

        btnRefresh.setOnClickListener(v -> loadAllData());
        btnBack.setOnClickListener(v -> finish());

        applyPermissionGates();
        loadAllData();
    }

    private void applyPermissionGates() {
        Set<String> permissions = sessionManager.getPermissions();

        boolean canManageMembers = permissions.contains(ModeratorPermissions.APPROVE_REMOVE_MEMBERS);
        boolean canModerate = permissions.contains(ModeratorPermissions.MODERATE_CONVERSATIONS);
        boolean canSchedule = permissions.contains(ModeratorPermissions.SCHEDULE_EVENTS);
        boolean canPin = permissions.contains(ModeratorPermissions.PIN_ANNOUNCEMENTS);

        setButtonState(btnApproveMember, canManageMembers);
        setButtonState(btnRemoveMember, canManageMembers);
        setButtonState(btnRemoveMessage, canModerate);
        setButtonState(btnRestoreMessage, canModerate);
        setButtonState(btnScheduleEvent, canSchedule);
        setButtonState(btnPinAnnouncement, canPin);
    }

    private void setButtonState(Button button, boolean enabled) {
        button.setEnabled(enabled);
        button.setAlpha(enabled ? 1f : 0.5f);
    }

    private void loadAllData() {
        repository.getGroupDetails(this, moderatorId, groupId, new ModeratorRepository.JsonObjectCallback() {
            @Override
            public void onSuccess(JSONObject object) {
                tvGroupDetails.setText(object.toString());
                conversationId = object.optInt("conversationId",
                        object.optInt("conversation_id", object.optInt("id", groupId)));
                loadMessages();
            }

            @Override
            public void onError(String error) {
                tvGroupDetails.setText(error);
                conversationId = groupId;
                loadMessages();
            }
        });

        repository.getGroupMembers(this, moderatorId, groupId, new ModeratorRepository.JsonArrayCallback() {
            @Override
            public void onSuccess(JSONArray array) {
                tvMembers.setText(prettyArray("Members", array));
            }

            @Override
            public void onError(String error) {
                tvMembers.setText(error);
            }
        });

        repository.getPendingMembers(this, moderatorId, groupId, new ModeratorRepository.JsonArrayCallback() {
            @Override
            public void onSuccess(JSONArray array) {
                tvPendingMembers.setText(prettyArray("Pending Members", array));
            }

            @Override
            public void onError(String error) {
                tvPendingMembers.setText(error);
            }
        });

        repository.getGroupEvents(this, moderatorId, groupId, new ModeratorRepository.JsonArrayCallback() {
            @Override
            public void onSuccess(JSONArray array) {
                tvEvents.setText(prettyArray("Events", array));
            }

            @Override
            public void onError(String error) {
                tvEvents.setText(error);
            }
        });

        repository.getGroupAnnouncements(this, moderatorId, groupId, new ModeratorRepository.JsonArrayCallback() {
            @Override
            public void onSuccess(JSONArray array) {
                tvAnnouncements.setText(prettyArray("Announcements", array));
            }

            @Override
            public void onError(String error) {
                tvAnnouncements.setText(error);
            }
        });
    }

    private void loadMessages() {
        int targetConversationId = conversationId > 0 ? conversationId : groupId;
        repository.getConversationMessages(this, targetConversationId, new ModeratorRepository.JsonArrayCallback() {
            @Override
            public void onSuccess(JSONArray array) {
                tvMessages.setText(prettyArray("Messages", array));
            }

            @Override
            public void onError(String error) {
                tvMessages.setText(error);
            }
        });
    }

    private String prettyArray(String title, JSONArray array) {
        if (array == null || array.length() == 0) {
            return title + ": none";
        }
        StringBuilder sb = new StringBuilder(title).append(":\n");
        for (int i = 0; i < array.length(); i++) {
            sb.append("- ").append(array.opt(i)).append("\n");
        }
        return sb.toString();
    }

    private void approveMember() {
        Integer userId = safeParseInt(etApproveMemberId.getText().toString().trim());
        if (userId == null) {
            Toast.makeText(this, "Enter a valid member ID", Toast.LENGTH_SHORT).show();
            return;
        }
        repository.approveMember(this, moderatorId, groupId, userId, new ModeratorRepository.ActionCallback() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(ModeratorGroupManagementActivity.this, "Member approved", Toast.LENGTH_SHORT).show();
                etApproveMemberId.setText("");
                loadAllData();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ModeratorGroupManagementActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeMember() {
        Integer userId = safeParseInt(etRemoveMemberId.getText().toString().trim());
        if (userId == null) {
            Toast.makeText(this, "Enter a valid member ID", Toast.LENGTH_SHORT).show();
            return;
        }
        repository.removeMember(this, moderatorId, groupId, userId, new ModeratorRepository.ActionCallback() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(ModeratorGroupManagementActivity.this, "Member removed", Toast.LENGTH_SHORT).show();
                etRemoveMemberId.setText("");
                loadAllData();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ModeratorGroupManagementActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeMessage() {
        Integer messageId = safeParseInt(etModerationMessageId.getText().toString().trim());
        if (messageId == null) {
            Toast.makeText(this, "Enter a valid message ID", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.removeMessage(this, moderatorId, messageId, new ModeratorRepository.ActionCallback() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(ModeratorGroupManagementActivity.this, "Message removed", Toast.LENGTH_SHORT).show();
                etModerationMessageId.setText("");
                loadMessages();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ModeratorGroupManagementActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void restoreMessage() {
        Integer messageId = safeParseInt(etModerationMessageId.getText().toString().trim());
        if (messageId == null) {
            Toast.makeText(this, "Enter a valid message ID", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.restoreMessage(this, moderatorId, messageId, new ModeratorRepository.ActionCallback() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(ModeratorGroupManagementActivity.this, "Message restored", Toast.LENGTH_SHORT).show();
                etModerationMessageId.setText("");
                loadMessages();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ModeratorGroupManagementActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void scheduleEvent() {
        String title = etEventTitle.getText().toString().trim();
        String when = etEventWhen.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(when)) {
            Toast.makeText(this, "Enter event title and schedule", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.scheduleEvent(this, moderatorId, groupId, title, when, new ModeratorRepository.ActionCallback() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(ModeratorGroupManagementActivity.this, "Event scheduled", Toast.LENGTH_SHORT).show();
                etEventTitle.setText("");
                etEventWhen.setText("");
                loadAllData();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ModeratorGroupManagementActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pinAnnouncement() {
        String message = etAnnouncement.getText().toString().trim();
        if (message.isEmpty()) {
            Toast.makeText(this, "Enter announcement text", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.pinAnnouncement(this, moderatorId, groupId, message, new ModeratorRepository.ActionCallback() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(ModeratorGroupManagementActivity.this, "Announcement pinned", Toast.LENGTH_SHORT).show();
                etAnnouncement.setText("");
                loadAllData();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ModeratorGroupManagementActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Integer safeParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }
}

