package com.ramascript.allenconnect.Models;

public class PostModel {
    private String postID;
    private String postImage;
    private String postCaption;
    private String postedBy;
    private long postedAt;

    public PostModel() {
    }

    public PostModel(String postID, String postImage, String postCaption, String postedBy, long postedAt) {
        this.postID = postID;
        this.postImage = postImage;
        this.postCaption = postCaption;
        this.postedBy = postedBy;
        this.postedAt = postedAt;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getPostCaption() {
        return postCaption;
    }

    public void setPostCaption(String postCaption) {
        this.postCaption = postCaption;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    public long getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(long postedAt) {
        this.postedAt = postedAt;
    }
}
