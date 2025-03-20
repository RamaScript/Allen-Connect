package com.ramascript.allenconnect.features.user;

import java.util.Map;

public class userModel {

    private String ID;
    private String profilePhoto;
    private String lastMsg;
    private String userType;
    private int followersCount;
    private int followingCount;

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

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
    private Boolean online; // Online status
    private Long lastSeen; // Last seen timestamp

    // Fields for user stats
    private Map<String, Object> Following; // Map of users being followed
    private Map<String, Object> Followers; // Map of followers
    private int postsCount; // Number of posts

    public userModel() {
    }

    // for chat purpose
    public userModel(String profilePhoto, String name, String lastMsg) {
        this.profilePhoto = profilePhoto;
        this.name = name;
        this.lastMsg = lastMsg;
    }

    // for student
    public userModel(String CRN, String email, String phoneNo, String password, String name, String course, String year,
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
    // public userModel(String CRN, String email, String phoneNo, String password,
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
    public userModel(String email, String phoneNo, String password, String name, String course, String passingYear,
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
    // public userModel(String email, String phoneNo, String password, String name,
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
    public userModel(String name, String phoneNo, String email, String password, String profilePhoto, String userType) {
        this.name = name;
        this.phoneNo = phoneNo;
        this.email = email;
        this.password = password;
        this.profilePhoto = profilePhoto;
        this.userType = userType;

    }

    // public userModel(String name, String phoneNo, String email,
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

    // Online status getter and setter
    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    // Last seen getter and setter
    public Long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Long lastSeen) {
        this.lastSeen = lastSeen;
    }

    /**
     * Returns a formatted string for the last seen time
     * 
     * @return formatted last seen string
     */
    public String getLastSeenText() {
        Boolean isUserOnline = getOnline();
        if (isUserOnline != null && isUserOnline) {
            return "Online Now";
        }

        if (lastSeen == null) {
            return "";
        }

        return "Last seen " + getFormattedLastSeen(lastSeen);
    }

    /**
     * Formats a timestamp for last seen display
     * 
     * @param timestamp the timestamp to format
     * @return formatted last seen time string
     */
    public static String getFormattedLastSeen(Long timestamp) {
        if (timestamp == null) {
            return "";
        }

        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - timestamp;

        // Convert to seconds
        long seconds = timeDiff / 1000;

        if (seconds < 60) {
            return "just now";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (seconds < 604800) { // Less than a week
            long days = seconds / 86400;
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else {
            // Format the date as "MM/dd/yyyy"
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yyyy",
                    java.util.Locale.getDefault());
            return "on " + sdf.format(new java.util.Date(timestamp));
        }
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

    // Getters and setters for user stats
    public Map<String, Object> getFollowing() {
        return Following;
    }

    public void setFollowing(Map<String, Object> following) {
        Following = following;
    }

    public Map<String, Object> getFollowers() {
        return Followers;
    }

    public void setFollowers(Map<String, Object> followers) {
        Followers = followers;
    }

    public int getPostsCount() {
        return postsCount;
    }

    public void setPostsCount(int postsCount) {
        this.postsCount = postsCount;
    }

}
