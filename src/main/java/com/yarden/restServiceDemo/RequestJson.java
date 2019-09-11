package com.yarden.restServiceDemo;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RequestJson {

    @SerializedName("sdk")
    @Expose
    private String sdk;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("sandbox")
    @Expose
    private Boolean sandbox;
    @SerializedName("results")
    @Expose
    private JsonObject results;

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

    public JsonObject getResults() {
        return results;
    }

    public void setResults(JsonObject results) {
        this.results = results;
    }

}