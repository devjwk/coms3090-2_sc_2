package com.example.androidexample;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModeratorAccount {
    private final int moderatorId;
    private final String email;
    private final String displayName;
    private final String authProvider;
    private final String providerUserId;
    private final List<String> roles;
    private final Set<String> permissions;
    private final List<Integer> assignedGroups;

    public ModeratorAccount(int moderatorId,
                            String email,
                            String displayName,
                            String authProvider,
                            String providerUserId,
                            List<String> roles,
                            Set<String> permissions,
                            List<Integer> assignedGroups) {
        this.moderatorId = moderatorId;
        this.email = email;
        this.displayName = displayName;
        this.authProvider = authProvider;
        this.providerUserId = providerUserId;
        this.roles = roles != null ? roles : new ArrayList<>();
        this.permissions = permissions != null ? permissions : new HashSet<>();
        this.assignedGroups = assignedGroups != null ? assignedGroups : new ArrayList<>();
    }

    public int getModeratorId() {
        return moderatorId;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAuthProvider() {
        return authProvider;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public List<Integer> getAssignedGroups() {
        return assignedGroups;
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public static ModeratorAccount fromJson(JSONObject json) {
        int id = json.optInt("moderatorId", json.optInt("id", -1));
        String email = json.optString("email", "");
        String displayName = json.optString("displayName", "");
        String authProvider = json.optString("authProvider", "");
        String providerUserId = json.optString("providerUserId", "");

        List<String> roles = new ArrayList<>();
        JSONArray rolesArray = json.optJSONArray("roles");
        if (rolesArray != null) {
            for (int i = 0; i < rolesArray.length(); i++) {
                String role = rolesArray.optString(i, "");
                if (!role.isEmpty()) {
                    roles.add(role);
                }
            }
        }

        Set<String> permissions = new HashSet<>();
        JSONArray permissionsArray = json.optJSONArray("permissions");
        if (permissionsArray != null) {
            for (int i = 0; i < permissionsArray.length(); i++) {
                String permission = permissionsArray.optString(i, "");
                if (!permission.isEmpty()) {
                    permissions.add(permission);
                }
            }
        }

        List<Integer> assignedGroups = new ArrayList<>();
        JSONArray groupsArray = json.optJSONArray("assignedGroups");
        if (groupsArray == null) {
            groupsArray = json.optJSONArray("groupIds");
        }
        if (groupsArray != null) {
            for (int i = 0; i < groupsArray.length(); i++) {
                int groupId = groupsArray.optInt(i, -1);
                if (groupId > 0) {
                    assignedGroups.add(groupId);
                }
            }
        }

        return new ModeratorAccount(id, email, displayName, authProvider, providerUserId, roles, permissions, assignedGroups);
    }
}

