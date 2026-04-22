package com.example.androidexample;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModeratorSessionManager {
    private static final String PREF_NAME = "moderator_session";
    private static final String KEY_MODERATOR_ID = "moderator_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_AUTH_PROVIDER = "auth_provider";
    private static final String KEY_PROVIDER_USER_ID = "provider_user_id";
    private static final String KEY_ROLES = "roles";
    private static final String KEY_PERMISSIONS = "permissions";
    private static final String KEY_ASSIGNED_GROUP_IDS = "assigned_group_ids";

    private final SharedPreferences prefs;

    public ModeratorSessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(ModeratorAccount account) {
        Set<String> groupIdStrings = new HashSet<>();
        for (Integer id : account.getAssignedGroups()) {
            if (id != null) {
                groupIdStrings.add(String.valueOf(id));
            }
        }

        prefs.edit()
                .putInt(KEY_MODERATOR_ID, account.getModeratorId())
                .putString(KEY_EMAIL, account.getEmail())
                .putString(KEY_AUTH_PROVIDER, account.getAuthProvider())
                .putString(KEY_PROVIDER_USER_ID, account.getProviderUserId())
                .putStringSet(KEY_ROLES, new HashSet<>(account.getRoles()))
                .putStringSet(KEY_PERMISSIONS, new HashSet<>(account.getPermissions()))
                .putStringSet(KEY_ASSIGNED_GROUP_IDS, groupIdStrings)
                .apply();
    }

    public ModeratorAccount getSession() {
        int moderatorId = prefs.getInt(KEY_MODERATOR_ID, -1);
        if (moderatorId <= 0) {
            return null;
        }

        String email = prefs.getString(KEY_EMAIL, "");
        String authProvider = prefs.getString(KEY_AUTH_PROVIDER, "");
        String providerUserId = prefs.getString(KEY_PROVIDER_USER_ID, "");

        Set<String> roleSet = prefs.getStringSet(KEY_ROLES, new HashSet<>());
        Set<String> permissions = prefs.getStringSet(KEY_PERMISSIONS, new HashSet<>());
        Set<String> assignedGroupSet = prefs.getStringSet(KEY_ASSIGNED_GROUP_IDS, new HashSet<>());

        List<String> roles = new ArrayList<>(roleSet != null ? roleSet : new HashSet<>());
        Set<String> safePermissions = new HashSet<>(permissions != null ? permissions : new HashSet<>());
        List<Integer> assignedGroups = new ArrayList<>();

        if (assignedGroupSet != null) {
            for (String id : assignedGroupSet) {
                try {
                    assignedGroups.add(Integer.parseInt(id));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return new ModeratorAccount(moderatorId, email, authProvider, providerUserId, roles, safePermissions, assignedGroups);
    }

    public boolean isLoggedIn() {
        return prefs.getInt(KEY_MODERATOR_ID, -1) > 0;
    }

    public int getModeratorId() {
        return prefs.getInt(KEY_MODERATOR_ID, -1);
    }

    public Set<String> getPermissions() {
        Set<String> permissions = prefs.getStringSet(KEY_PERMISSIONS, new HashSet<>());
        return new HashSet<>(permissions != null ? permissions : new HashSet<>());
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}

