package com.yarden.restServiceDemo;

import com.google.gson.JsonSyntaxException;
import com.yarden.restServiceDemo.pojos.RequestJson;

public class RequestJsonValidator {

    RequestJson requestJson;

    public RequestJsonValidator(RequestJson requestJson){
        this.requestJson = requestJson;
    }

    public void validate(SheetData sheetData) throws JsonSyntaxException {
        if (requestJson.getSdk() == null) {
            throw new JsonSyntaxException("Missing sdk in the request json");
        }
        if (!sheetData.getSheetData().get(0).getAsJsonObject().keySet().contains(requestJson.getSdk())) {
            throw new JsonSyntaxException("No SDK named " + requestJson.getSdk() + " in the sheet");
        }
    }
}
