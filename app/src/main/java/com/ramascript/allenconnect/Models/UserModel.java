package com.ramascript.allenconnect.Models;

public class UserModel {

    private String ID;
    private String profilePhoto;
    private String lastMsg;
    private String userType;
    private int followersCount;

    private String email;
    private String password;

    private String name;
    private String phoneNo;

    private String CRN;
    private String course;
    private String year;

    private String passingYear;
    private String company;
    private String jobRole;
    // private String fcmToken; // Stores FCM Token for push notifications

    private Long lastMessageTime; // Added field for message time
    private int unreadCount; // Added field for unread messages

    public UserModel() {
    }

    // for chat purpose
    public UserModel(String profilePhoto, String name, String lastMsg) {
        this.profilePhoto = profilePhoto;
        this.name = name;
        this.lastMsg = lastMsg;
    }

    // for student
    public UserModel(String CRN, String email, String phoneNo, String password, String name, String course, String year,
            String profilePhoto, String userType) {
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

    // Student Constructor with FCM Token (NEW)
    // public UserModel(String CRN, String email, String phoneNo, String password,
    // String name, String course, String year, String profilePhoto, String
    // userType, String fcmToken) {
    // this.CRN = CRN;
    // this.email = email;
    // this.phoneNo = phoneNo;
    // this.password = password;
    // this.name = name;
    // this.course = course;
    // this.year = year;
    // this.profilePhoto = profilePhoto;
    // this.userType = userType;
    // this.fcmToken = fcmToken; // NEW FIELD
    // }

    // for alumni
    public UserModel(String email, String phoneNo, String password, String name, String course, String passingYear,
            String company, String jobRole, String profilePhoto, String userType) {
        this.email = email;
        this.phoneNo = phoneNo;
        this.password = password;
        this.name = name;
        this.course = course;
        this.passingYear = passingYear;
        this.company = company;
        this.jobRole = jobRole;
        this.profilePhoto = profilePhoto;
        this.userType = userType;

    }

    // Alumni Constructor with FCM Token (NEW)
    // public UserModel(String email, String phoneNo, String password, String name,
    // String course, String passingYear, String company,
    // String jobRole, String profilePhoto, String userType, String fcmToken) {
    // this.email = email;
    // this.phoneNo = phoneNo;
    // this.password = password;
    // this.name = name;
    // this.course = course;
    // this.passingYear = passingYear;
    // this.company = company;
    // this.jobRole = jobRole;
    // this.profilePhoto = profilePhoto;
    // this.userType = userType;
    // this.fcmToken = fcmToken; // NEW FIELD
    // }

    // for professor
    public UserModel(String name, String phoneNo, String email, String password, String profilePhoto, String userType) {
        this.name = name;
        this.phoneNo = phoneNo;
        this.email = email;
        this.password = password;
        this.profilePhoto = profilePhoto;
        this.userType = userType;

    }

    // public UserModel(String name, String phoneNo, String email,
    // String password, String profilePhoto, String userType, String fcmToken) {
    // this.name = name;
    // this.phoneNo = phoneNo;
    // this.email = email;
    // this.password = password;
    // this.profilePhoto = profilePhoto;
    // this.userType = userType;
    // this.fcmToken = fcmToken; // NEW FIELD
    // }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public String getPassingYear() {
        return passingYear;
    }

    public void setPassingYear(String passingYear) {
        this.passingYear = passingYear;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

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

    // public String getFcmToken() {
    // return fcmToken;
    // }
    //
    // public void setFcmToken(String fcmToken) {
    // this.fcmToken = fcmToken;
    // }

    // New getters and setters for message time and unread count
    public Long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    // Helper method to format time
    public static String getTimeAgo(Long timestamp) {
        if (timestamp == null)
            return "";

        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - timestamp;

        // Convert to seconds
        long seconds = timeDiff / 1000;

        if (seconds < 60) {
            return "Just now";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + "m";
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + "h";
        } else {
            long days = seconds / 86400;
            return days + "d";
        }
    }

}
