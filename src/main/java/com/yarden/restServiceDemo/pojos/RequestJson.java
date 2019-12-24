package com.yarden.restServiceDemo.pojos;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RequestJson {

    @SerializedName("sdk")
    @Expose
    private String sdk;
    @SerializedName("group")
    @Expose
    private String group;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("sandbox")
    @Expose
    private Boolean sandbox;
    @SerializedName("results")
    @Expose
    private JsonArray results;

    public String getSdk() {
        return sdk;
    }

    public void setSdk(String sdk) {
        this.sdk = sdk;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getSandbox() {
        return sandbox;
    }

    public void setSandbox(Boolean sandbox) {
        this.sandbox = sandbox;
    }

    public JsonArray getResults() {
        return results;
    }

    public void setResults(JsonArray results) {
        this.results = results;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}