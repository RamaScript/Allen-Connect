package com.ramascript.allenconnect.Models;

public class UserModel {
    private String CRN;
    private String email;
    private String phoneNo;
    private String password;
    private String name;
    private String course;
    private String year;
    private String ID;
    private String profilePhoto;
    private String lastMsg;
    private String userType;
    private int followersCount;

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public UserModel(String profilePhoto, String name, String lastMsg) {
        this.profilePhoto = profilePhoto;
        this.name = name;
        this.lastMsg = lastMsg;
    }

    public String getLastMsg() {
        return lastMsg;
    }

    public void setLastMsg(String lastMsg) {
        this.lastMsg = lastMsg;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public UserModel() {
    }

    public UserModel(String CRN, String email, String phoneNo, String password, String name, String course, String year, String profilePhoto, String userType) {
        this.CRN = CRN;
        this.email = email;
        this.phoneNo = phoneNo;
        this.password = password;
        this.name = name;
        this.course = course;
        this.year = year;
        this.profilePhoto = profilePhoto;
        this.userType = userType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getCRN() {
        return CRN;
    }

    public void setCRN(String CRN) {
        this.CRN = CRN;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
