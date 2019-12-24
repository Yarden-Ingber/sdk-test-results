package com.yarden.restServiceDemo.reportService;

import com.google.gson.JsonSyntaxException;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.pojos.RequestJson;

import java.util.Arrays;
import java.util.List;

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
        checkGroupNameValid();
    }

    private void checkGroupNameValid(){
        boolean isFound = false;
        for (Enums.SdkGroupsSheetTabNames tab: Enums.SdkGroupsSheetTabNames.values()) {
            if (tab.value.equals(requestJson.getGroup())){
                isFound = true;
                break;
            }
        }
        if (!isFound) {
            throw new JsonSyntaxException("No tab named " + requestJson.getGroup() + " in the report");
        }
    }
}
