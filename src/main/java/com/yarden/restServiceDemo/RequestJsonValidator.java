package com.yarden.restServiceDemo;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class RequestJsonValidator {

    RequestJson requestJson;

    public RequestJsonValidator(RequestJson requestJson){
        this.requestJson = requestJson;
    }

    public void validate(SheetData sheetData) throws JsonParseException {
        if (requestJson.getSdk() == null) {
            throw new JsonParseException("Missing sdk in the request json");
        }
        if (!sheetData.getSheetData().get(0).getAsJsonObject().keySet().contains(requestJson.getSdk())) {
            throw new JsonParseException("No SDK named " + requestJson.getSdk() + " in the sheet");
        }
    }
}
