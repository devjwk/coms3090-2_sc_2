package com.example.androidexample;

public class Report {
    private int reportId;
    private int reporterId;
    private int reportedId;
    private String description;
    private String status;
    private String createdAt;

    public Report(int reportId, int reporterId, int reportedId, String description, String status, String createdAt) {
        this.reportId = reportId;
        this.reporterId = reporterId;
        this.reportedId = reportedId;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getReportId() {
        return reportId;
    }

    public int getReporterId() {
        return reporterId;
    }

    public int getReportedId() {
        return reportedId;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}