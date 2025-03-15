package com.ramascript.allenconnect.Models;

public class FollowingModel {
    private long followingAt;
    private String followingTo;

    public FollowingModel() {
        // Required empty constructor for Firebase
    }

    public long getFollowingAt() {
        return followingAt;
    }

    public void setFollowingAt(long followingAt) {
        this.followingAt = followingAt;
    }

    public String getFollowingTo() {
        return followingTo;
    }

    public void setFollowingTo(String followingTo) {
        this.followingTo = followingTo;
    }
}