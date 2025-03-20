package com.ramascript.allenconnect.features.chat;

public class chatMsgModel {
    String uId, message;
    long timestamp;
    boolean read;

    public chatMsgModel() {
    }

    public chatMsgModel(String uId, String message, long timestamp) {
        this.uId = uId;
        this.message = message;
        this.timestamp = timestamp;
        this.read = false;
    }

    public chatMsgModel(String uId, String message, long timestamp, boolean read) {
        this.uId = uId;
        this.message = message;
        this.timestamp = timestamp;
        this.read = read;
    }

    public chatMsgModel(String uId, String message) {
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
