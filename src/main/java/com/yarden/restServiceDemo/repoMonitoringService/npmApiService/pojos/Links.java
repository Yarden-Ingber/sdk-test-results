
package com.yarden.restServiceDemo.repoMonitoringService.npmApiService.pojos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Links {

    @SerializedName("npm")
    @Expose
    private String npm;
    @SerializedName("homepage")
    @Expose
    private String homepage;
    @SerializedName("repository")
    @Expose
    private String repository;
    @SerializedName("bugs")
    @Expose
    private String bugs;

    public String getNpm() {
        return npm;
    }

    public void setNpm(String npm) {
        this.npm = npm;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getBugs() {
        return bugs;
    }

    public void setBugs(String bugs) {
        this.bugs = bugs;
    }

}
