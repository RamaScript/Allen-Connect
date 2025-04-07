package com.ramascript.allenconnect.features.report;

import java.io.Serializable;

public class ReportModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private String reportId;
    private String postId;
    private String reporterId;
    private String reporterName;
    private String reporterType;
    private String reason;
    private long timestamp;

    // Default constructor required for Firebase
    public ReportModel() {
    }

    public ReportModel(String reportId, String postId, String reporterId, String reporterName,
            String reporterType, String reason, long timestamp) {
        this.reportId = reportId;
        this.postId = postId;
        this.reporterId = reporterId;
        this.reporterName = reporterName;
        this.reporterType = reporterType;
        this.reason = reason;
        this.timestamp = timestamp;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReporterType() {
        return reporterType;
    }

    public void setReporterType(String reporterType) {
        this.reporterType = reporterType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}