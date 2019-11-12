package com.yarden.restServiceDemo.pojos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EmailNotificationJson {

    @SerializedName("sdk")
    @Expose
    private String sdk;
    @SerializedName("version")
    @Expose
    private String version;
    @SerializedName("changeLog")
    @Expose
    private String changeLog;

    @SerializedName("testCoverageGap")
    @Expose
    private String testCoverageGap;

    public String getSdk() {
        return sdk;
    }

    public void setSdk(String sdk) {
        this.sdk = sdk;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getChangeLog() {
        return changeLog;
    }

    public void setChangeLogUrl(String changeLog) {
        this.changeLog = changeLog;
    }

    public String getTestCoverageGap() {
        return testCoverageGap;
    }

    public void setTestCoverageGap(String testCoverageGap) {
        this.testCoverageGap = testCoverageGap;
    }
}