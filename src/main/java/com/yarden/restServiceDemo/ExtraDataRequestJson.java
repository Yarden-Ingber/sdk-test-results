package com.yarden.restServiceDemo;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ExtraDataRequestJson {

    @SerializedName("sdk")
    @Expose
    private String sdk;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("sandbox")
    @Expose
    private Boolean sandbox;
    @SerializedName("extra_data")
    @Expose
    private JsonArray extraData;

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

    public JsonArray getExtraData() {
        return extraData;
    }

    public void setExtraData(JsonArray results) {
        this.extraData = results;
    }

}