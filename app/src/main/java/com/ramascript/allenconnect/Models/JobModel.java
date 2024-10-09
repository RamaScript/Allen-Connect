package com.ramascript.allenconnect.Models;

public class JobModel {

    private String comapanyName, jobTitle;
    private int companyLogo;

    public JobModel() {
    }

    public JobModel(String comapanyName, String jobTitle, int companyLogo) {
        this.comapanyName = comapanyName;
        this.jobTitle = jobTitle;
        this.companyLogo = companyLogo;
    }

    public String getComapanyName() {
        return comapanyName;
    }

    public void setComapanyName(String comapanyName) {
        this.comapanyName = comapanyName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public int getCompanyLogo() {
        return companyLogo;
    }

    public void setCompanyLogo(int companyLogo) {
        this.companyLogo = companyLogo;
    }
}
