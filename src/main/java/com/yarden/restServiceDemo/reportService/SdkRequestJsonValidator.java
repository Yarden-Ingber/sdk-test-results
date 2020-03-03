package com.yarden.restServiceDemo.reportService;

import com.google.gson.JsonSyntaxException;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.pojos.SdkResultRequestJson;

public class SdkRequestJsonValidator {

    SdkResultRequestJson sdkResultRequestJson;

    public SdkRequestJsonValidator(SdkResultRequestJson sdkResultRequestJson){
        this.sdkResultRequestJson = sdkResultRequestJson;
    }

    public void validate(SheetData sheetData) throws JsonSyntaxException {
        if (sdkResultRequestJson.getSdk() == null) {
            throw new JsonSyntaxException("Missing sdk in the request json");
        }
        if (!sheetData.getSheetData().get(0).getAsJsonObject().keySet().contains(sdkResultRequestJson.getSdk())) {
            throw new JsonSyntaxException("No SDK named " + sdkResultRequestJson.getSdk() + " in the sheet");
        }
        checkGroupNameValid();
    }

    private void checkGroupNameValid(){
        boolean isFound = false;
        for (Enums.SdkGroupsSheetTabNames tab: Enums.SdkGroupsSheetTabNames.values()) {
            if (tab.value.equals(sdkResultRequestJson.getGroup())){
                isFound = true;
                break;
            }
        }
        if (!isFound) {
            throw new JsonSyntaxException("No tab named " + sdkResultRequestJson.getGroup() + " in the report");
        }
    }
}
