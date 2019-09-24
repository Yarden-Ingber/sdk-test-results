package com.yarden.restServiceDemo;

import com.google.gson.JsonParseException;

public class RequestJsonValidator {

    RequestJson requestJson;

    public RequestJsonValidator(RequestJson requestJson){
        this.requestJson = requestJson;
    }

    public void validate(String googleSheetTabName) throws JsonParseException {
        if (requestJson.getSdk() == null) {
            throw new JsonParseException("Missing sdk in the request json");
        }
        if (!SheetData.getSheetData(googleSheetTabName).get(0).getAsJsonObject().keySet().contains(requestJson.getSdk())) {
            throw new JsonParseException("No SDK named " + requestJson.getSdk() + " in the sheet");
        }
    }
}
