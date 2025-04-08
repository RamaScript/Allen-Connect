package com.ramascript.allenconnect.features.bot;

public class BotChatMessage {
    private String messageId;
    private String message;
    private boolean isUser;
    private String userId;
    private long timestamp;

    // Empty constructor for Firebase
    public BotChatMessage() {
    }

    public BotChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
        this.timestamp = System.currentTimeMillis();
    }

    public BotChatMessage(String messageId, String message, boolean isUser, String userId, long timestamp) {
        this.messageId = messageId;
        this.message = message;
        this.isUser = isUser;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}