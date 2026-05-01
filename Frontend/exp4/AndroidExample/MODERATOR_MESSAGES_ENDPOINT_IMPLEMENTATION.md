# Moderator Group Messages Endpoint Integration

## Overview
Implemented direct message loading for moderator group chat using the new backend GET endpoint instead of WebSocket-based real-time messaging. This provides a clean history-only view for moderators.

## Changes Made

### 1. ModeratorRepository.java
Added new method to fetch moderator group messages:
```java
public void getModeratorGroupMessages(android.content.Context context,
                                     int moderatorId,
                                     int groupId,
                                     JsonArrayCallback callback) {
    String url = BASE_URL + "/" + moderatorId + "/groups/" + groupId + "/messages";
    getArray(context, url, "Failed to load group messages", callback);
}
```

**Endpoint**: `@GetMapping("/{moderatorId}/groups/{groupId}/messages")`

### 2. GroupChatActivity.java

#### Added new method: `loadModeratorGroupMessages()`
This method:
- Validates moderatorId and groupId are valid (> 0)
- Calls `moderatorRepository.getModeratorGroupMessages()` 
- Parses the returned messages using existing `parseAndLoadMessages()` method
- Displays message history WITHOUT connecting to WebSocket
- Shows toast error messages if loading fails

```java
private void loadModeratorGroupMessages() {
    if (moderatorId <= 0 || groupId <= 0) {
        Log.w(TAG, "Invalid moderatorId (" + moderatorId + ") or groupId (" + groupId + ")");
        Toast.makeText(this, "Moderator data missing", Toast.LENGTH_SHORT).show();
        return;
    }
    Log.d(TAG, "Loading messages from moderator endpoint: moderatorId=" + moderatorId + " groupId=" + groupId);
    moderatorRepository.getModeratorGroupMessages(this, moderatorId, groupId, new ModeratorRepository.JsonArrayCallback() {
        @Override
        public void onSuccess(JSONArray array) {
            Log.d(TAG, "Moderator loaded " + array.length() + " messages");
            runOnUiThread(() -> {
                messageList.clear();
                parseAndLoadMessages(array.toString());
                // For moderator view, don't connect WebSocket - just show message history
                Log.d(TAG, "Moderator view: loading complete, not connecting WebSocket");
            });
        }

        @Override
        public void onError(String error) {
            Log.e(TAG, "Failed to load moderator messages: " + error);
            runOnUiThread(() -> {
                Toast.makeText(GroupChatActivity.this, "Failed to load messages: " + error, Toast.LENGTH_SHORT).show();
            });
        }
    });
}
```

#### Modified: `startConversationFlow()`
Now checks for moderator view and skips the conversation API flow:
```java
private void startConversationFlow() {
    // For moderator view, skip conversation API and load messages directly
    if (isModeratorView && moderatorId > 0) {
        Log.d(TAG, "Moderator view: Loading messages directly from group endpoint");
        loadModeratorGroupMessages();
        return;
    }
    
    if (conversationId > 0) {
        Log.d(TAG, "Step 2: Using cached conversationId = " + conversationId);
        loadChatHistoryThenConnect();
    } else {
        fetchGroupConversationId();
    }
}
```

#### Updated: Default currentUserId
Changed default from `1` to `-1` to prevent invalid user ID issues:
```java
currentUserId = getIntent().getIntExtra("USER_ID", -1);
```

## Message Flow for Moderators

1. **Login**: Moderator logs in via ModeratorLoginActivity
2. **Dashboard**: ModeratorDashboardActivity displays managed groups
3. **Click Group**: Moderator clicks on a group chat
4. **Intent**: Opens GroupChatActivity with:
   - `IS_MODERATOR=true`
   - `MODERATOR_ID=<moderatorId>`
   - `GROUP_ID=<groupId>`
   - `GROUP_NAME=<groupName>`
5. **Load Members**: `fetchGroupMembers()` loads member list from `/groups/{groupId}`
6. **Load Messages**: `startConversationFlow()` detects moderator view and calls `loadModeratorGroupMessages()`
7. **Direct Fetch**: Messages are loaded via new GET endpoint: `/{moderatorId}/groups/{groupId}/messages`
8. **Display**: Message history is displayed in chat RecyclerView
9. **No Real-time**: WebSocket is NOT connected (no real-time updates)

## Key Differences from Regular Chat

| Aspect | Regular User Chat | Moderator Chat |
|--------|------------------|-----------------|
| Message Source | Conversation API + WebSocket | Direct group messages API |
| Real-time Updates | Yes (WebSocket) | No (history only) |
| Message Sending | Enabled | Disabled (read-only) |
| Message Deletion | Via moderation API | Via moderation API |
| User Id Handling | Requires valid currentUserId | Uses moderatorId instead |
| Connection Type | WebSocket (persistent) | REST GET (one-time) |

## Benefits

1. **Simpler Flow**: No conversation creation/lookup needed for moderators
2. **Direct Access**: Group messages endpoint provides direct message history
3. **Read-Only UX**: Clear message history without real-time noise
4. **Efficient**: Single REST call instead of persistent WebSocket connection
5. **Moderation Focus**: Moderators can review message history and moderate

## Testing Checklist

- [ ] Moderator can log in
- [ ] Moderator dashboard loads managed groups
- [ ] Moderator can click on group and open group chat
- [ ] Messages load correctly from `/moderatorId/groups/groupId/messages`
- [ ] Message senders are displayed correctly
- [ ] Send button is disabled for moderators
- [ ] Manage/Delete buttons work for moderators
- [ ] No WebSocket connection errors in logs
- [ ] Regular user chat flow still works (WebSocket + conversation API)
