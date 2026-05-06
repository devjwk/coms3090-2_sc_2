package com.example.androidexample;

import org.json.JSONObject;

public class ModeratorManagedGroup {
    private final int groupId;
    private final String groupName;
    private final String description;

    public ModeratorManagedGroup(int groupId, String groupName, String description) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.description = description;
    }

    public int getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getDescription() {
        return description;
    }

    public static ModeratorManagedGroup fromJson(JSONObject obj) {
        int id = obj.optInt("groupId", obj.optInt("id", -1));
        String name = obj.optString("groupName", obj.optString("name", "Group " + id));
        String desc = obj.optString("description", "");
        return new ModeratorManagedGroup(id, name, desc);
    }
}

