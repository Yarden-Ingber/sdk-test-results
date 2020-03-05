package com.yarden.restServiceDemo.pojos;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TestResultData {

    @SerializedName("test_name")
    @Expose
    private String testName;
    @SerializedName("parameters")
    @Expose
    private JsonObject parameters;
    @SerializedName("passed")
    @Expose
    private Boolean passed;
    @SerializedName("result_url")
    @Expose
    private String resultUrl;

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public JsonObject getParameters() {
        return parameters;
    }

    public void setParameters(JsonObject parameters) {
        this.parameters = parameters;
    }

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }

    public String getResultUrl() {
        if (resultUrl == null) {
            return "";
        } else {
            return this.resultUrl;
        }
    }

    public void setResultUrl(String resultUrl) {
        this.resultUrl = resultUrl;
    }
}