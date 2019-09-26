package com.yarden.restServiceDemo;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.Collections;

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
        validateThereIsIdRowOnSheet(googleSheetTabName, requestJson);
    }

    public static void validateThereIsIdRowOnSheet(String googleSheetTabName, RequestJson requestJson){
        for (JsonElement sheetEntry : SheetData.getSheetData(googleSheetTabName)) {
            if (sheetEntry.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString().equals(Enums.SheetColumnNames.IDRow.value)) {
                return;
            }
        }
        JsonElement newEntry = new JsonParser().parse("{\"" + Enums.SheetColumnNames.TestName.value + "\":\"" + Enums.SheetColumnNames.IDRow.value + "\",\"" + requestJson.getSdk() + "\":\"" + requestJson.getId() + "\"}");
        SheetData.getSheetData(googleSheetTabName).add(newEntry);
    }
}
