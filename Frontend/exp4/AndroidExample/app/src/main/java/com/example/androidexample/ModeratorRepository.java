package com.example.androidexample;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ModeratorRepository {
    private static final String BASE_URL = "http://coms-3090-015.class.las.iastate.edu:8080/moderators";
    private static final String ROOT_URL = "http://coms-3090-015.class.las.iastate.edu:8080";

    public interface ModeratorAccountCallback {
        void onSuccess(ModeratorAccount account);

        void onError(String error);
    }

    public interface GroupsCallback {
        void onSuccess(List<ModeratorManagedGroup> groups);

        void onError(String error);
    }

    public interface JsonObjectCallback {
        void onSuccess(JSONObject object);

        void onError(String error);
    }

    public interface JsonArrayCallback {
        void onSuccess(JSONArray array);

        void onError(String error);
    }

    public interface ActionCallback {
        void onSuccess(String response);

        void onError(String error);
    }

    public void loginModerator(android.content.Context context,
                               String email,
                               String passwordHash,
                               ModeratorAccountCallback callback) {
        String url = BASE_URL + "/login";
        JSONObject body = new JSONObject();
        try {
            body.put("email", email);
            body.put("passwordHash", passwordHash);
        } catch (JSONException e) {
            callback.onError("Failed to build request body");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> {
                    try {
                        JSONObject accountJson = response.optJSONObject("moderator");
                        if (accountJson == null) {
                            accountJson = response;
                        }
                        ModeratorAccount account = ModeratorAccount.fromJson(accountJson);
                        if (account.getModeratorId() <= 0) {
                            callback.onError("Invalid moderator ID in response");
                            return;
                        }
                        callback.onSuccess(account);
                    } catch (Exception e) {
                        callback.onError("Error parsing login response: " + e.getMessage());
                    }
                },
                error -> {
                    String errorMsg = "Login failed";
                    if (error != null && error.networkResponse != null) {
                        errorMsg += " (HTTP " + error.networkResponse.statusCode + ")";
                    }
                    callback.onError(errorMsg);
                });

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public void getModeratorProfile(android.content.Context context,
                                    int moderatorId,
                                    ModeratorAccountCallback callback) {
        String url = BASE_URL + "/" + moderatorId;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> callback.onSuccess(ModeratorAccount.fromJson(response)),
                error -> callback.onError("Failed to load moderator profile"));
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public void getManagedGroups(android.content.Context context,
                                 int moderatorId,
                                 GroupsCallback callback) {
        String url = BASE_URL + "/" + moderatorId + "/groups";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    List<ModeratorManagedGroup> groups = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject groupObj = response.optJSONObject(i);
                        if (groupObj != null) {
                            groups.add(ModeratorManagedGroup.fromJson(groupObj));
                        }
                    }
                    callback.onSuccess(groups);
                },
                error -> callback.onError("Failed to load groups"));
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public void createGroup(android.content.Context context,
                            int moderatorId,
                            String groupName,
                            String description,
                            List<String> interests,
                            JsonObjectCallback callback) {
        String url = BASE_URL + "/" + moderatorId + "/groups";
        JSONObject body = new JSONObject();
        try {
            body.put("groupName", groupName);
            body.put("description", description);
            JSONArray interestArray = new JSONArray();
            if (interests != null) {
                for (String interest : interests) {
                    if (interest != null && !interest.trim().isEmpty()) {
                        interestArray.put(interest.trim());
                    }
                }
            }
            body.put("interests", interestArray);
        } catch (JSONException e) {
            callback.onError("Failed to build group payload");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                callback::onSuccess,
                error -> callback.onError("Failed to create group"));
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public void editGroup(android.content.Context context,
                          int moderatorId,
                          int groupId,
                          String groupName,
                          String description,
                          List<String> interests,
                          JsonObjectCallback callback) {
        String url = BASE_URL + "/" + moderatorId + "/groups/" + groupId;
        JSONObject body = new JSONObject();
        try {
            body.put("groupName", groupName);
            body.put("description", description);
            JSONArray interestArray = new JSONArray();
            if (interests != null) {
                for (String interest : interests) {
                    if (interest != null && !interest.trim().isEmpty()) {
                        interestArray.put(interest.trim());
                    }
                }
            }
            body.put("interests", interestArray);
        } catch (JSONException e) {
            callback.onError("Failed to build group payload");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, body,
                callback::onSuccess,
                error -> callback.onError("Failed to update group"));
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public void deleteGroup(android.content.Context context,
                            int moderatorId,
                            int groupId,
                            ActionCallback callback) {
        String url = BASE_URL + "/" + moderatorId + "/groups/" + groupId;
        sendAction(context, Request.Method.DELETE, url, null, callback, "Failed to delete group");
    }

    public void getGroupDetails(android.content.Context context,
                                int moderatorId,
                                int groupId,
                                JsonObjectCallback callback) {
        String url = BASE_URL + "/" + moderatorId + "/groups/" + groupId;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                callback::onSuccess,
                error -> callback.onError("Failed to load group details"));
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public void getGroupMembers(android.content.Context context,
                                int moderatorId,
                                int groupId,
                                JsonArrayCallback callback) {
        String url = BASE_URL + "/" + moderatorId + "/groups/" + groupId + "/members";
        getArray(context, url, "Failed to load members", callback);
    }

    public void getPendingMembers(android.content.Context context,
                                  int moderatorId,
                                  int groupId,
                                  JsonArrayCallback callback) {
        String url = BASE_URL + "/" + moderatorId + "/groups/" + groupId + "/pending-members";
        getArray(context, url, "Failed to load pending members", callback);
    }

    public void getGroupEvents(android.content.Context context,
                               int moderatorId,
                               int groupId,
                               JsonArrayCallback callback) {
        String url = BASE_URL + "/" + moderatorId + "/groups/" + groupId + "/events";
        getArray(context, url, "Failed to load events", callback);
    }

    public void getGroupEventsForChat(android.content.Context context,
                                      int groupId,
                                      int moderatorId,
                                      JsonArrayCallback callback) {
        String publicUrl = ROOT_URL + "/groups/" + groupId + "/events";
        getArray(context, publicUrl, "Failed to load events", new JsonArrayCallback() {
            @Override
            public void onSuccess(JSONArray array) {
                if (array.length() > 0) {
                    callback.onSuccess(array);
                } else if (moderatorId > 0) {
                    // Public endpoint returned empty — fall through to moderator endpoint
                    String moderatorUrl = BASE_URL + "/" + moderatorId + "/groups/" + groupId + "/events";
                    getArray(context, moderatorUrl, "Failed to load events", callback);
                } else {
                    callback.onSuccess(array);
                }
            }

            @Override
            public void onError(String error) {
                if (moderatorId <= 0) {
                    callback.onError(error);
                    return;
                }
                String moderatorUrl = BASE_URL + "/" + moderatorId + "/groups/" + groupId + "/events";
                getArray(context, moderatorUrl, "Failed to load events", callback);
            }
        });
    }

    public void getGroupAnnouncements(android.content.Context context,
                                      int moderatorId,
                                      int groupId,
                                      JsonArrayCallback callback) {
        String url = BASE_URL + "/" + moderatorId + "/groups/" + groupId + "/announcements";
        getArray(context, url, "Failed to load announcements", callback);
    }

    public void getGroupAnnouncementsForChat(android.content.Context context,
                                             int groupId,
                                             int moderatorId,
                                             JsonArrayCallback callback) {
        String publicUrl = ROOT_URL + "/groups/" + groupId + "/announcements";
        getArray(context, publicUrl, "Failed to load announcements", new JsonArrayCallback() {
            @Override
            public void onSuccess(JSONArray array) {
                callback.onSuccess(array);
            }

            @Override
            public void onError(String error) {
                if (moderatorId <= 0) {
                    callback.onError(error);
                    return;
                }

                String moderatorUrl = BASE_URL + "/" + moderatorId + "/groups/" + groupId + "/announcements";
                getArray(context, moderatorUrl, "Failed to load announcements", callback);
            }
        });
    }

    public void getPinnedAnnouncementsForGroup(android.content.Context context,
                                               int groupId,
                                               JsonArrayCallback callback) {
        String url = ROOT_URL + "/groups/" + groupId + "/announcements/pinned";
        getArray(context, url, "Failed to load pinned announcements", callback);
    }

    public void getUnpinnedAnnouncementsForGroup(android.content.Context context,
                                                 int groupId,
                                                 JsonArrayCallback callback) {
        String url = ROOT_URL + "/groups/" + groupId + "/announcements/unpinned";
        getArray(context, url, "Failed to load unpinned announcements", callback);
    }

    public void getOrderedAnnouncementsForChat(android.content.Context context,
                                               int groupId,
                                               int moderatorId,
                                               JsonArrayCallback callback) {
        getPinnedAnnouncementsForGroup(context, groupId, new JsonArrayCallback() {
            @Override
            public void onSuccess(JSONArray pinnedArray) {
                getUnpinnedAnnouncementsForGroup(context, groupId, new JsonArrayCallback() {
                    @Override
                    public void onSuccess(JSONArray unpinnedArray) {
                        callback.onSuccess(mergeAnnouncements(pinnedArray, unpinnedArray));
                    }

                    @Override
                    public void onError(String error) {
                        // If unpinned endpoint fails, still return pinned items.
                        callback.onSuccess(mergeAnnouncements(pinnedArray, null));
                    }
                });
            }

            @Override
            public void onError(String error) {
                // Fallback to existing all-announcements endpoint path for resilience.
                getGroupAnnouncementsForChat(context, groupId, moderatorId, new JsonArrayCallback() {
                    @Override
                    public void onSuccess(JSONArray array) {
                        callback.onSuccess(sortAnnouncementsPinnedFirst(array));
                    }

                    @Override
                    public void onError(String fallbackError) {
                        callback.onError(fallbackError);
                    }
                });
            }
        });
    }

    public void approveMember(android.content.Context context,
                              int moderatorId,
                              int groupId,
                              int userId,
                              ActionCallback callback) {
        String url = BASE_URL + "/" + moderatorId + "/groups/" + groupId + "/members/" + userId + "/approve";
        sendAction(context, Request.Method.PUT, url, null, callback, "Failed to approve member");
    }

    public void removeMember(android.content.Context context,
                             int moderatorId,
                             int groupId,
                             int userId,
                             ActionCallback callback) {
        String url = BASE_URL + "/" + moderatorId + "/groups/" + groupId + "/members/" + userId;
        sendAction(context, Request.Method.DELETE, url, null, callback, "Failed to remove member");
    }

    public void getConversationMessages(android.content.Context context,
                                        int conversationId,
                                        JsonArrayCallback callback) {
        String url = ROOT_URL + "/messages/conversation/" + conversationId;
        getArray(context, url, "Failed to load messages", callback);
    }

    public void getModeratorGroupMessages(android.content.Context context,
                                         int moderatorId,
                                         int groupId,
                                         JsonArrayCallback callback) {
        String url = BASE_URL + "/" + moderatorId + "/groups/" + groupId + "/messages";
        getArray(context, url, "Failed to load group messages", callback);
    }

   public void removeMessage(android.content.Context context,
                              int moderatorId,
                              int messageId,
                              ActionCallback callback) {
        String url = BASE_URL + "/" + moderatorId + "/messages/" + messageId + "/remove";
        sendAction(context, Request.Method.PUT, url, null, callback, "Failed to remove message");
    }

    public void restoreMessage(android.content.Context context,
                               int moderatorId,
                               int messageId,
                               ActionCallback callback) {
        String url = BASE_URL + "/" + moderatorId + "/messages/" + messageId + "/restore";
        sendAction(context, Request.Method.PUT, url, null, callback, "Failed to restore message");
    }

    public void scheduleEvent(android.content.Context context,
                              int moderatorId,
                              int groupId,
                              String title,
                              String description,
                              String location,
                              String eventTime,
                              ActionCallback callback) {
        String url = BASE_URL + "/" + moderatorId + "/groups/" + groupId + "/events";
        JSONObject body = new JSONObject();
        try {
            body.put("title", title == null ? "" : title.trim());
            body.put("description", description == null ? "" : description.trim());
            body.put("location", location == null ? "" : location.trim());
            body.put("eventTime", eventTime == null ? "" : eventTime.trim());
        } catch (JSONException e) {
            callback.onError("Failed to build event payload");
            return;
        }

        android.util.Log.d("ScheduleEvent", "URL: " + url);
        android.util.Log.d("ScheduleEvent", "Body: " + body.toString());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> callback.onSuccess(response.toString()),
                error -> {
                    String msg = "Failed to schedule event";
                    if (error.networkResponse != null) {
                        msg += " (HTTP " + error.networkResponse.statusCode + ")";
                        try {
                            // This shows exactly what the backend rejected
                            String responseBody = new String(error.networkResponse.data,
                                    java.nio.charset.StandardCharsets.UTF_8);
                            android.util.Log.e("ScheduleEvent", "Error body: " + responseBody);
                            msg += ": " + responseBody;
                        } catch (Exception e) {
                            android.util.Log.e("ScheduleEvent", "Could not parse error body");
                        }
                    }
                    callback.onError(msg);
                });

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public void createAnnouncement(android.content.Context context,
                                   int moderatorId,
                                   int groupId,
                                   String title,
                                   String content,
                                   ActionCallback callback) {
        String url = BASE_URL + "/" + moderatorId + "/groups/" + groupId + "/announcements";
        JSONObject body = new JSONObject();
        try {
            body.put("title", title == null ? "" : title.trim());
            body.put("content", content == null ? "" : content.trim());
        } catch (JSONException e) {
            callback.onError("Failed to build announcement payload");
            return;
        }
        sendAction(context, Request.Method.POST, url, body, callback, "Failed to create announcement");
    }

    public void pinAnnouncement(android.content.Context context,
                                int moderatorId,
                                int groupId,
                                int announcementId,
                                ActionCallback callback) {
        String url = BASE_URL + "/" + moderatorId + "/groups/" + groupId + "/announcements/" + announcementId + "/pin";
        sendAction(context, Request.Method.PUT, url, null, callback, "Failed to pin announcement");
    }

    public void getEventDetails(android.content.Context context,
                                int moderatorId,
                                int groupId,
                                int eventId,
                                JsonObjectCallback callback) {
        String url = BASE_URL + "/" + moderatorId + "/groups/" + groupId + "/events/" + eventId;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                callback::onSuccess,
                error -> callback.onError("Failed to load event details"));
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public void editEvent(android.content.Context context,
                          int moderatorId,
                          int groupId,
                          int eventId,
                          String title,
                          String description,
                          String location,
                          String eventTime,
                          ActionCallback callback) {
        String url = BASE_URL + "/" + moderatorId + "/groups/" + groupId + "/events/" + eventId;
        JSONObject body = new JSONObject();
        try {
            body.put("title", title == null ? "" : title.trim());
            body.put("description", description == null ? "" : description.trim());
            body.put("location", location == null ? "" : location.trim());
            body.put("eventTime", eventTime == null ? "" : eventTime.trim());
        } catch (JSONException e) {
            callback.onError("Failed to build event payload");
            return;
        }
        sendAction(context, Request.Method.PUT, url, body, callback, "Failed to edit event");
    }

    public void deleteEvent(android.content.Context context,
                            int moderatorId,
                            int groupId,
                            int eventId,
                            ActionCallback callback) {
        String url = BASE_URL + "/" + moderatorId + "/groups/" + groupId + "/events/" + eventId;
        sendAction(context, Request.Method.DELETE, url, null, callback, "Failed to delete event");
    }

    public void unpinAnnouncement(android.content.Context context,
                                  int moderatorId,
                                  int groupId,
                                  int announcementId,
                                  ActionCallback callback) {
        String primaryUrl = BASE_URL + "/" + moderatorId + "/groups/" + groupId + "/announcements/" + announcementId + "/unpin";
        String typoFallbackUrl = BASE_URL + "/" + moderatorId + "/groups/" + groupId + "/announcements/" + announcementId + "/unppin";

        StringRequest request = new StringRequest(Request.Method.PUT, primaryUrl,
                callback::onSuccess,
                error -> {
                    int status = error != null && error.networkResponse != null ? error.networkResponse.statusCode : -1;
                    if (status == 404) {
                        // Backend typo compatibility: try /unppin when /unpin is unavailable.
                        sendAction(context, Request.Method.PUT, typoFallbackUrl, null, callback, "Failed to unpin announcement");
                        return;
                    }
                    callback.onError("Failed to unpin announcement");
                });
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public void deleteAnnouncement(android.content.Context context,
                                   int moderatorId,
                                   int groupId,
                                   int announcementId,
                                   ActionCallback callback) {
        String url = BASE_URL + "/" + moderatorId + "/groups/" + groupId + "/announcements/" + announcementId;
        sendAction(context, Request.Method.DELETE, url, null, callback, "Failed to delete announcement");
    }

    public void joinGroupRequest(android.content.Context context,
                                 int userId,
                                 int groupId,
                                 ActionCallback callback) {
        String url = ROOT_URL + "/gm/join";
        JSONObject body = new JSONObject();
        try {
            body.put("user_id", userId);
            body.put("group_id", groupId);
        } catch (JSONException e) {
            callback.onError("Failed to build join request body");
            return;
        }
        sendAction(context, Request.Method.POST, url, body, callback, "Failed to join group");
    }



    public void getAllGroups(android.content.Context context,
                             JsonArrayCallback callback) {
        String url = ROOT_URL + "/groups";
        getArray(context, url, "Failed to load all groups", callback);
    }

    public void getGroupById(android.content.Context context,
                             int groupId,
                             JsonObjectCallback callback) {
        String url = ROOT_URL + "/groups/" + groupId;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                callback::onSuccess,
                error -> callback.onError("Failed to load group"));
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public void signupModerator(android.content.Context context,
                                String email,
                                String passwordHash,
                                String displayName,
                                ModeratorAccountCallback callback) {
        String url = BASE_URL;
        JSONObject body = new JSONObject();
        try {
            body.put("displayName", displayName);
            body.put("email", email);
            body.put("passwordHash", passwordHash);
            body.put("active", true);
        } catch (JSONException e) {
            callback.onError("Failed to build request body");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> {
                    try {
                        JSONObject accountJson = response.optJSONObject("moderator");
                        if (accountJson == null) {
                            accountJson = response;
                        }
                        ModeratorAccount account = ModeratorAccount.fromJson(accountJson);
                        if (account.getModeratorId() <= 0) {
                            callback.onError("Invalid moderator ID in response");
                            return;
                        }
                        callback.onSuccess(account);
                    } catch (Exception e) {
                        callback.onError("Error parsing signup response: " + e.getMessage());
                    }
                },
                error -> {
                    String errorMsg = "Signup failed";
                    if (error != null && error.networkResponse != null) {
                        errorMsg += " (HTTP " + error.networkResponse.statusCode + ")";
                    }
                    callback.onError(errorMsg);
                });

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    private void getArray(android.content.Context context,
                          String url,
                          String errorMessage,
                          JsonArrayCallback callback) {
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONArray parsed = extractArrayFromResponse(response);
                        callback.onSuccess(parsed);
                    } catch (Exception e) {
                        callback.onError(errorMessage + ": invalid response format");
                    }
                },
                error -> callback.onError(errorMessage));
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    private JSONArray extractArrayFromResponse(String response) throws JSONException {
        if (response == null) {
            return new JSONArray();
        }

        String trimmed = response.trim();
        if (trimmed.startsWith("[")) {
            return new JSONArray(trimmed);
        }
        if (!trimmed.startsWith("{")) {
            return new JSONArray();
        }

        JSONObject object = new JSONObject(trimmed);
        String[] preferredKeys = new String[]{
                "members", "pendingMembers", "pending_members", "messages",
                "events", "announcements", "data", "results", "items", "content"
        };
        for (String key : preferredKeys) {
            JSONArray arr = object.optJSONArray(key);
            if (arr != null) {
                return arr;
            }
        }

        JSONArray names = object.names();
        if (names != null) {
            for (int i = 0; i < names.length(); i++) {
                String key = names.optString(i, "");
                if (key.isEmpty()) {
                    continue;
                }
                JSONArray arr = object.optJSONArray(key);
                if (arr != null) {
                    return arr;
                }
            }
        }

        return new JSONArray();
    }

    private void sendAction(android.content.Context context,
                            int method,
                            String url,
                            JSONObject body,
                            ActionCallback callback,
                            String errorMessage) {
        StringRequest request = new StringRequest(method, url,
                callback::onSuccess,
                error -> callback.onError(errorMessage)) {
            @Override
            public byte[] getBody() {
                if (body == null) {
                    return null;
                }
                return body.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    private JSONArray mergeAnnouncements(JSONArray pinned, JSONArray unpinned) {
        JSONArray merged = new JSONArray();
        java.util.HashSet<Integer> seenIds = new java.util.HashSet<>();
        appendUniqueAnnouncements(merged, pinned, seenIds);
        appendUniqueAnnouncements(merged, unpinned, seenIds);
        return merged;
    }

    private JSONArray sortAnnouncementsPinnedFirst(JSONArray source) {
        JSONArray pinned = new JSONArray();
        JSONArray unpinned = new JSONArray();
        if (source == null) {
            return new JSONArray();
        }
        for (int i = 0; i < source.length(); i++) {
            Object raw = source.opt(i);
            JSONObject obj = raw instanceof JSONObject ? (JSONObject) raw : null;
            if (obj != null && (obj.optBoolean("pinned", false) || obj.optBoolean("isPinned", false))) {
                pinned.put(obj);
            } else {
                unpinned.put(raw);
            }
        }
        return mergeAnnouncements(pinned, unpinned);
    }

    private void appendUniqueAnnouncements(JSONArray target,
                                           JSONArray source,
                                           java.util.HashSet<Integer> seenIds) {
        if (target == null || source == null) {
            return;
        }
        for (int i = 0; i < source.length(); i++) {
            Object raw = source.opt(i);
            JSONObject obj = raw instanceof JSONObject ? (JSONObject) raw : null;
            if (obj == null) {
                target.put(raw);
                continue;
            }
            int id = obj.optInt("announcementId", obj.optInt("id", -1));
            if (id > 0) {
                if (seenIds.contains(id)) {
                    continue;
                }
                seenIds.add(id);
            }
            target.put(obj);
        }
    }

}
