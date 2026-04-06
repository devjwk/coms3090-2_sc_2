package com.example.androidexample;

import java.util.ArrayList;
import java.util.List;
public class RecommendGroup {
    private int groupId;
    private String groupName;
    private String description;
    private String category;
    private int memberCount;
    private List<String> matchedKeywords;
    private int matchScore;

    public RecommendGroup(int groupId, String groupName, String description, String category, int memberCount, List<String> matchedKeywords, int matchScore) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.description = description;
        this.category = category;
        this.memberCount = memberCount;
        this.matchedKeywords = matchedKeywords != null ? matchedKeywords : new ArrayList<>();
        this.matchScore = matchScore;
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

    public String getCategory() {
        return category;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public List<String> getMatchedKeywords() {
        return matchedKeywords;
    }

    public int getMatchScore() {
        return matchScore;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public void setMatchedKeywords(List<String> matchedKeywords) {
        this.matchedKeywords = matchedKeywords!= null ? matchedKeywords : new ArrayList<>();;
    }

    public void setMatchScore(int matchScore) {
        this.matchScore = matchScore;
    }
}
