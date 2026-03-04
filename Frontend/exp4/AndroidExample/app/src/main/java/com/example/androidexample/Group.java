package com.example.androidexample;

public class Group {

    private int groupId;
    private String name;
    private String description;
    private int createdBy;
    private String createdAt;

    public Group(int groupId, String name, String description, int createdBy, String createdAt) {
        this.groupId     = groupId;
        this.name        = name;
        this.description = description;
        this.createdBy   = createdBy;
        this.createdAt   = createdAt;
    }

    public int    getGroupId()     { return groupId; }
    public String getName()        { return name; }
    public String getDescription() { return description; }
    public int    getCreatedBy()   { return createdBy; }
    public String getCreatedAt()   { return createdAt; }

    public void setName(String name)               { this.name = name; }
    public void setDescription(String description) { this.description = description; }
}