package com.ramascript.allenconnect.features.user;

public class followingModel {
    private long followingAt;
    private String followingTo;

    public followingModel() {
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