package com.ramascript.allenconnect.features.comment;

public class commentModel {
    private String commentBody;
    private Long commentedAt;
    private String commentedBy;

    public commentModel() {
    }

    public String getCommentBody() {
        return commentBody;
    }

    public void setCommentBody(String commentBody) {
        this.commentBody = commentBody;
    }

    public Long getCommentedAt() {
        return commentedAt;
    }

    public void setCommentedAt(Long commentedAt) {
        this.commentedAt = commentedAt;
    }

    public String getCommentedBy() {
        return commentedBy;
    }

    public void setCommentedBy(String commentedBy) {
        this.commentedBy = commentedBy;
    }
}
