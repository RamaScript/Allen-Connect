package com.ramascript.allenconnect.Job;

import java.util.List;

public class JobModel {

    // Variables to store form values
    private String jobID;
    private String jobPostedBy;
    private long jobPostedAt;
    private String jobTitle;
    private String companyName;
    private String jobType;
    private String experienceRequired;
    private List<String> coursesEligible;
    private List<String> skillsRequired;
    private String salary;
    private String jobDescription;
    private String contactEmail;
    private String contactPhone;
    private String applicationDeadline;
    private String posterImgPath;
    private String logoImgPath;

    public JobModel() {
    }

    // to show in Jobs Fragment
    public JobModel(String companyName , String jobTitle, String logoImgPath) {
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.logoImgPath = logoImgPath;
    }

    public JobModel(String jobTitle, String companyName, String jobType, String experienceRequired, List<String> coursesEligible, List<String> skillsRequired, String salary, String jobDescription, String contactEmail, String contactPhone, String applicationDeadline, String posterImgPath, String logoImgPath) {
        this.jobTitle = jobTitle;
        this.companyName = companyName;
        this.jobType = jobType;
        this.experienceRequired = experienceRequired;
        this.coursesEligible = coursesEligible;
        this.skillsRequired = skillsRequired;
        this.salary = salary;
        this.jobDescription = jobDescription;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.applicationDeadline = applicationDeadline;
        this.posterImgPath = posterImgPath;
        this.logoImgPath = logoImgPath;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public String getJobPostedBy() {
        return jobPostedBy;
    }

    public void setJobPostedBy(String jobPostedBy) {
        this.jobPostedBy = jobPostedBy;
    }

    public long getJobPostedAt() {
        return jobPostedAt;
    }

    public void setJobPostedAt(long jobPostedAt) {
        this.jobPostedAt = jobPostedAt;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getExperienceRequired() {
        return experienceRequired;
    }

    public void setExperienceRequired(String experienceRequired) {
        this.experienceRequired = experienceRequired;
    }

    public List<String> getCoursesEligible() {
        return coursesEligible;
    }

    public void setCoursesEligible(List<String> coursesEligible) {
        this.coursesEligible = coursesEligible;
    }

    public List<String> getSkillsRequired() {
        return skillsRequired;
    }

    public void setSkillsRequired(List<String> skillsRequired) {
        this.skillsRequired = skillsRequired;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getApplicationDeadline() {
        return applicationDeadline;
    }

    public void setApplicationDeadline(String applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }

    public String getPosterImgPath() {
        return posterImgPath;
    }

    public void setPosterImgPath(String posterImgPath) {
        this.posterImgPath = posterImgPath;
    }

    public String getLogoImgPath() {
        return logoImgPath;
    }

    public void setLogoImgPath(String logoImgPath) {
        this.logoImgPath = logoImgPath;
    }
}
