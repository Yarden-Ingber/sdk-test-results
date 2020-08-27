package com.yarden.restServiceDemo.repoMonitoringService.npmApiService.pojos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NpmPackageData {

    @SerializedName("latest")
    @Expose
    private String latest;
    @SerializedName("compat-10.7")
    @Expose
    private String compat107;

    public String getLatest() {
        return latest;
    }

    public void setLatest(String latest) {
        this.latest = latest;
    }

    public String getCompat107() {
        return compat107;
    }

    public void setCompat107(String compat107) {
        this.compat107 = compat107;
    }

}