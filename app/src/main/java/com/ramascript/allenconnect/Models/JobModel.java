package com.ramascript.allenconnect.Models;

public class JobModel {

    private String comapanyName, jobTitle;
    private String companyLogo;

    public JobModel() {
    }

    public JobModel(String comapanyName, String jobTitle, String companyLogo) {
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

    public String getCompanyLogo() {
        return companyLogo;
    }

    public void setCompanyLogo(String companyLogo) {
        this.companyLogo = companyLogo;
    }
}
