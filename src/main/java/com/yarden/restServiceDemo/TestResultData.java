package com.yarden.restServiceDemo;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TestResultData {

    @SerializedName("parameters")
    @Expose
    private JsonObject parameters;
    @SerializedName("passed")
    @Expose
    private Boolean passed;

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

}
