package com.ramascript.allenconnect.Chat;

public class ChatMsgModel {
    String uId, message;
    long timestamp;
    boolean read;

    public ChatMsgModel() {
    }

    public ChatMsgModel(String uId, String message, long timestamp) {
        this.uId = uId;
        this.message = message;
        this.timestamp = timestamp;
        this.read = false;
    }

    public ChatMsgModel(String uId, String message, long timestamp, boolean read) {
        this.uId = uId;
        this.message = message;
        this.timestamp = timestamp;
        this.read = read;
    }

    public ChatMsgModel(String uId, String message) {
        this.uId = uId;
        this.message = message;
        this.read = false;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
