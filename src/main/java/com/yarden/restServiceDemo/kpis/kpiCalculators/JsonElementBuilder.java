package com.yarden.restServiceDemo.kpis.kpiCalculators;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonElementBuilder {

    StringBuilder stringBuilder = new StringBuilder();

    public void addKeyValue(String key, String value){
        if (!stringBuilder.toString().isEmpty()) {
            stringBuilder.append(",");
        }
        stringBuilder.append("\"");
        stringBuilder.append(key);
        stringBuilder.append("\":\"");
        stringBuilder.append(value);
        stringBuilder.append("\"");
    }

    public JsonElement buildJsonElement(){
        return new JsonParser().parse("{" + stringBuilder.toString() + "}");
    }
}
