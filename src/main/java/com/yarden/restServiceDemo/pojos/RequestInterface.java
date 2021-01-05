package com.yarden.restServiceDemo.pojos;

import com.google.gson.JsonArray;

public interface RequestInterface {

    Boolean getSandbox();

    String getId();

    JsonArray getResults();

    String getGroup();

    void setResults(JsonArray results);
}
