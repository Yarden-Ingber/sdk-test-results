package com.yarden.restServiceDemo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EmailNotificationJson {

    @SerializedName("sdk")
    @Expose
    private String sdk;
    @SerializedName("version")
    @Expose
    private String version;
    @SerializedName("changeLogUrl")
    @Expose
    private String changeLogUrl;

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

    public String getChangeLogUrl() {
        return changeLogUrl;
    }

    public void setChangeLogUrl(String changeLogUrl) {
        this.changeLogUrl = changeLogUrl;
    }

}