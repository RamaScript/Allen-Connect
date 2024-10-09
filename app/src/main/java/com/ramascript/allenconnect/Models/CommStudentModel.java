package com.ramascript.allenconnect.Models;

public class CommStudentModel {

    String name,course ,year;
    int profilePic;

    public CommStudentModel() {
    }

    public CommStudentModel(String name, String course, String year, int profilePic) {
        this.name = name;
        this.course = course;
        this.year = year;
        this.profilePic = profilePic;
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

    public int getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(int profilePic) {
        this.profilePic = profilePic;
    }
}
