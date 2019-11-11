package com.yarden.restServiceDemo.pojos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ExtraDataPojo {
    @SerializedName("test_name")
    @Expose
    private String testName;
    @SerializedName("data")
    @Expose
    private String data;

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
