package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
    private LinearLayout layoutPendingActions;

    private EditText etApproveMemberId;
    private EditText etRemoveMemberId;
    private EditText etModerationMessageId;
    private EditText etEventTitle;
    private EditText etEventDescription;
    private EditText etEventLocation;
    private EditText etEventTime;
    private EditText etAnnouncementTitle;
    private EditText etAnnouncementContent;
    private EditText etAnnouncementId;
    private EditText etEventId;

    private Button btnApproveMember;
    private Button btnRemoveMember;
    private Button btnRemoveMessage;
    private Button btnRestoreMessage;
    private Button btnScheduleEvent;
    private Button btnCreateAnnouncement;
    private Button btnPinAnnouncement;
    private Button btnUnpinAnnouncement;
    private Button btnDeleteAnnouncement;
    private Button btnEditEvent;
    private Button btnDeleteEvent;

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
        layoutPendingActions = findViewById(R.id.layoutPendingActions);

        etApproveMemberId = findViewById(R.id.etApproveMemberId);
        etRemoveMemberId = findViewById(R.id.etRemoveMemberId);
        etModerationMessageId = findViewById(R.id.etModerationMessageId);
        etEventTitle = findViewById(R.id.etEventTitle);
        etEventDescription = findViewById(R.id.etEventDescription);
        etEventLocation = findViewById(R.id.etEventLocation);
        etEventTime = findViewById(R.id.etEventTime);
        etAnnouncementTitle = findViewById(R.id.etAnnouncementTitle);
        etAnnouncementContent = findViewById(R.id.etAnnouncementContent);
        etAnnouncementId = findViewById(R.id.etAnnouncementId);
        etEventId = findViewById(R.id.etEventId);

        btnApproveMember = findViewById(R.id.btnApproveMember);
        btnRemoveMember = findViewById(R.id.btnRemoveMember);
        btnRemoveMessage = findViewById(R.id.btnRemoveMessage);
        btnRestoreMessage = findViewById(R.id.btnRestoreMessage);
        btnScheduleEvent = findViewById(R.id.btnScheduleEvent);
        btnCreateAnnouncement = findViewById(R.id.btnCreateAnnouncement);
        btnPinAnnouncement = findViewById(R.id.btnPinAnnouncement);
        btnUnpinAnnouncement = findViewById(R.id.btnUnpinAnnouncement);
        btnDeleteAnnouncement = findViewById(R.id.btnDeleteAnnouncement);
        btnEditEvent = findViewById(R.id.btnEditEvent);
        btnDeleteEvent = findViewById(R.id.btnDeleteEvent);
        Button btnRefresh = findViewById(R.id.btnRefreshGroupManagement);
        Button btnBack = findViewById(R.id.btnBackGroupManagement);

        tvGroupHeader.setText("Manage: " + (groupName == null ? ("Group #" + groupId) : groupName));

        btnApproveMember.setOnClickListener(v -> approveMember());
        btnRemoveMember.setOnClickListener(v -> removeMember());
        btnRemoveMessage.setOnClickListener(v -> removeMessage());
        btnRestoreMessage.setOnClickListener(v -> restoreMessage());
        btnScheduleEvent.setOnClickListener(v -> scheduleEvent());
        btnCreateAnnouncement.setOnClickListener(v -> createAnnouncement());
        btnPinAnnouncement.setOnClickListener(v -> pinAnnouncement());
        btnUnpinAnnouncement.setOnClickListener(v -> unpinAnnouncement());
        btnDeleteAnnouncement.setOnClickListener(v -> deleteAnnouncement());
        btnEditEvent.setOnClickListener(v -> editEvent());
        btnDeleteEvent.setOnClickListener(v -> deleteEvent());

        btnRefresh.setOnClickListener(v -> loadAllData());
        btnBack.setOnClickListener(v -> finish());

        applyPermissionGates();
        loadAllData();
    }

    private void applyPermissionGates() {
        Set<String> permissions = sessionManager.getPermissions();

        // Fallback for backends that do not send permissions yet.
        boolean hasPermissionPayload = permissions != null && !permissions.isEmpty();

        boolean canManageMembers = !hasPermissionPayload || permissions.contains(ModeratorPermissions.APPROVE_REMOVE_MEMBERS);
        boolean canModerate = !hasPermissionPayload || permissions.contains(ModeratorPermissions.MODERATE_CONVERSATIONS);
        boolean canSchedule = !hasPermissionPayload || permissions.contains(ModeratorPermissions.SCHEDULE_EVENTS);
        boolean canPin = !hasPermissionPayload || permissions.contains(ModeratorPermissions.PIN_ANNOUNCEMENTS);

        setButtonState(btnApproveMember, canManageMembers);
        setButtonState(btnRemoveMember, canManageMembers);
        setButtonState(btnRemoveMessage, canModerate);
        setButtonState(btnRestoreMessage, canModerate);
        setButtonState(btnScheduleEvent, canSchedule);
        setButtonState(btnEditEvent, canSchedule);
        setButtonState(btnDeleteEvent, canSchedule);
        setButtonState(btnCreateAnnouncement, canPin);
        setButtonState(btnPinAnnouncement, canPin);
        setButtonState(btnUnpinAnnouncement, canPin);
        setButtonState(btnDeleteAnnouncement, canPin);
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
                tvMembers.setText(formatMemberNames(array));
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
                renderPendingActions(array);
            }

            @Override
            public void onError(String error) {
                tvPendingMembers.setText(error);
                renderPendingActions(null);
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

    private String formatMemberNames(JSONArray array) {
        if (array == null || array.length() == 0) {
            return "Members: none";
        }

        StringBuilder sb = new StringBuilder("Members:\n");
        for (int i = 0; i < array.length(); i++) {
            Object raw = array.opt(i);
            String name = extractMemberName(raw);
            if (name == null || name.trim().isEmpty()) {
                Integer userId = extractMemberId(raw);
                name = userId != null && userId > 0 ? "User #" + userId : String.valueOf(raw);
            }
            sb.append("- ").append(name).append("\n");
        }
        return sb.toString();
    }

    private String extractMemberName(Object raw) {
        if (!(raw instanceof JSONObject)) {
            return raw == null ? "" : String.valueOf(raw);
        }

        JSONObject obj = (JSONObject) raw;
        String[] keys = new String[]{"displayName", "displayname", "name", "userName", "username"};
        for (String key : keys) {
            String value = obj.optString(key, "").trim();
            if (!value.isEmpty()) {
                return value;
            }
        }

        JSONObject user = obj.optJSONObject("user");
        if (user != null) {
            for (String key : keys) {
                String value = user.optString(key, "").trim();
                if (!value.isEmpty()) {
                    return value;
                }
            }
        }

        Integer id = extractMemberId(raw);
        return id != null && id > 0 ? "User #" + id : "";
    }

    private Integer extractMemberId(Object raw) {
        if (!(raw instanceof JSONObject)) {
            return null;
        }

        JSONObject obj = (JSONObject) raw;
        int id = obj.optInt("userId", obj.optInt("userid", obj.optInt("id", obj.optInt("memberId", -1))));
        if (id > 0) {
            return id;
        }

        JSONObject user = obj.optJSONObject("user");
        if (user != null) {
            int nestedId = user.optInt("userId", user.optInt("id", -1));
            if (nestedId > 0) {
                return nestedId;
            }
        }

        return null;
    }

    private void renderPendingActions(JSONArray array) {
        layoutPendingActions.removeAllViews();
        if (array == null || array.length() == 0) {
            return;
        }

        for (int i = 0; i < array.length(); i++) {
            Object raw = array.opt(i);
            Integer pendingUserId = extractPendingUserId(raw);
            if (pendingUserId == null || pendingUserId <= 0) {
                continue;
            }

            Button approveButton = new Button(this);
            approveButton.setText("Approve User #" + pendingUserId);
            approveButton.setAllCaps(false);
            approveButton.setOnClickListener(v -> {
                etApproveMemberId.setText(String.valueOf(pendingUserId));
                approveMember();
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.bottomMargin = dp(8);
            approveButton.setLayoutParams(params);

            layoutPendingActions.addView(approveButton);
        }
    }

    private Integer extractPendingUserId(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number) {
            return ((Number) raw).intValue();
        }
        if (!(raw instanceof JSONObject)) {
            return null;
        }

        JSONObject obj = (JSONObject) raw;
        int id = obj.optInt("userId",
                obj.optInt("userid",
                        obj.optInt("id",
                                obj.optInt("memberId", -1))));
        if (id > 0) {
            return id;
        }

        JSONObject userObj = obj.optJSONObject("user");
        if (userObj != null) {
            int nested = userObj.optInt("userId", userObj.optInt("id", -1));
            if (nested > 0) {
                return nested;
            }
        }
        return null;
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
        String description = etEventDescription.getText().toString().trim();
        String location = etEventLocation.getText().toString().trim();
        String eventTime = etEventTime.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description)
                || TextUtils.isEmpty(location) || TextUtils.isEmpty(eventTime)) {
            Toast.makeText(this, "Enter title, description, location, and event time", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.scheduleEvent(this, moderatorId, groupId, title, description, location, eventTime, new ModeratorRepository.ActionCallback() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(ModeratorGroupManagementActivity.this, "Event scheduled", Toast.LENGTH_SHORT).show();
                etEventTitle.setText("");
                etEventDescription.setText("");
                etEventLocation.setText("");
                etEventTime.setText("");
                loadAllData();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ModeratorGroupManagementActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void editEvent() {
        Integer eventId = safeParseInt(etEventId.getText().toString().trim());
        String title = etEventTitle.getText().toString().trim();
        String description = etEventDescription.getText().toString().trim();
        String location = etEventLocation.getText().toString().trim();
        String eventTime = etEventTime.getText().toString().trim();

        if (eventId == null) {
            Toast.makeText(this, "Enter a valid event ID", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description)
                || TextUtils.isEmpty(location) || TextUtils.isEmpty(eventTime)) {
            Toast.makeText(this, "Enter title, description, location, and event time", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.editEvent(this, moderatorId, groupId, eventId, title, description, location, eventTime, new ModeratorRepository.ActionCallback() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(ModeratorGroupManagementActivity.this, "Event edited", Toast.LENGTH_SHORT).show();
                etEventId.setText("");
                etEventTitle.setText("");
                etEventDescription.setText("");
                etEventLocation.setText("");
                etEventTime.setText("");
                loadAllData();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ModeratorGroupManagementActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteEvent() {
        Integer eventId = safeParseInt(etEventId.getText().toString().trim());
        if (eventId == null) {
            Toast.makeText(this, "Enter a valid event ID", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.deleteEvent(this, moderatorId, groupId, eventId, new ModeratorRepository.ActionCallback() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(ModeratorGroupManagementActivity.this, "Event deleted", Toast.LENGTH_SHORT).show();
                etEventId.setText("");
                loadAllData();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ModeratorGroupManagementActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createAnnouncement() {
        String title = etAnnouncementTitle.getText().toString().trim();
        String content = etAnnouncementContent.getText().toString().trim();
        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Enter announcement title and content", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.createAnnouncement(this, moderatorId, groupId, title, content, new ModeratorRepository.ActionCallback() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(ModeratorGroupManagementActivity.this, "Announcement created", Toast.LENGTH_SHORT).show();
                etAnnouncementTitle.setText("");
                etAnnouncementContent.setText("");
                loadAllData();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ModeratorGroupManagementActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pinAnnouncement() {
        Integer announcementId = safeParseInt(etAnnouncementId.getText().toString().trim());
        if (announcementId == null) {
            Toast.makeText(this, "Enter a valid announcement ID", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.pinAnnouncement(this, moderatorId, groupId, announcementId, new ModeratorRepository.ActionCallback() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(ModeratorGroupManagementActivity.this, "Announcement pinned", Toast.LENGTH_SHORT).show();
                loadAllData();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ModeratorGroupManagementActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unpinAnnouncement() {
        Integer announcementId = safeParseInt(etAnnouncementId.getText().toString().trim());
        if (announcementId == null) {
            Toast.makeText(this, "Enter a valid announcement ID", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.unpinAnnouncement(this, moderatorId, groupId, announcementId, new ModeratorRepository.ActionCallback() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(ModeratorGroupManagementActivity.this, "Announcement unpinned", Toast.LENGTH_SHORT).show();
                loadAllData();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ModeratorGroupManagementActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteAnnouncement() {
        Integer announcementId = safeParseInt(etAnnouncementId.getText().toString().trim());
        if (announcementId == null) {
            Toast.makeText(this, "Enter a valid announcement ID", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.deleteAnnouncement(this, moderatorId, groupId, announcementId, new ModeratorRepository.ActionCallback() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(ModeratorGroupManagementActivity.this, "Announcement deleted", Toast.LENGTH_SHORT).show();
                etAnnouncementId.setText("");
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

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
