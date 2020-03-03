package com.yarden.restServiceDemo.reportService;

import com.google.gson.JsonSyntaxException;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.pojos.EyesResultRequestJson;

public class EyesRequestJsonValidator {

    EyesResultRequestJson eyesResultRequestJson;

    public EyesRequestJsonValidator(EyesResultRequestJson eyesResultRequestJson){
        this.eyesResultRequestJson = eyesResultRequestJson;
    }

    public void validate() throws JsonSyntaxException {
        checkGroupNameValid();
    }

    private void checkGroupNameValid(){
        boolean isFound = false;
        for (Enums.EyesSheetTabsNames tab: Enums.EyesSheetTabsNames.values()) {
            if (tab.value.equals(eyesResultRequestJson.getGroup())){
                isFound = true;
                break;
            }
        }
        if (!isFound) {
            throw new JsonSyntaxException("No tab named " + eyesResultRequestJson.getGroup() + " in the report");
        }
    }

}
