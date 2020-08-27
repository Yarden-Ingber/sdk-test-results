package com.yarden.restServiceDemo.slackService;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.pojos.SlackReportNotificationJson;
import com.yarden.restServiceDemo.reportService.SheetData;
import com.yarden.restServiceDemo.reportService.SheetTabIdentifier;

public class SdkHighLevelTableBuilderBaseClass {

    protected SlackReportNotificationJson requestJson;

    public SdkHighLevelTableBuilderBaseClass(SlackReportNotificationJson requestJson) {
        this.requestJson = requestJson;
    }

    protected int getPermutationResultCountForSingleTestEntry(JsonElement sheetEntry, Enums.SdkSheetColumnNames permutationResult){
        JsonElement passedValue = sheetEntry.getAsJsonObject().get(requestJson.getSdk() + permutationResult.value);
        return (passedValue == null || passedValue.getAsString().isEmpty()) ?
                0 :
                sheetEntry.getAsJsonObject().get(requestJson.getSdk() + permutationResult.value).getAsInt();
    }

    protected int getPassedTestCountForSdk(SdkSlackReportSender.AddingTestCountCondition addingTestCountCondition){
        int totalAmount = 0;
        for (Enums.SdkGroupsSheetTabNames sdkGroup: Enums.SdkGroupsSheetTabNames.values()) {
            JsonArray reportSheet = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.SDK.value, sdkGroup.value)).getSheetData();
            for (JsonElement sheetEntry: reportSheet){
                if (addingTestCountCondition.shouldAddTest(sheetEntry.getAsJsonObject().get(Enums.SdkSheetColumnNames.TestName.value).getAsString())) {
                    int passedValueInteger = getPermutationResultCountForSingleTestEntry(sheetEntry, Enums.SdkSheetColumnNames.Pass);
                    totalAmount += passedValueInteger;
                }
            }
        }
        return totalAmount;
    }

    protected int getFailedTestCountForSdk(){
        int totalAmount = 0;
        for (Enums.SdkGroupsSheetTabNames sdkGroup: Enums.SdkGroupsSheetTabNames.values()) {
            JsonArray reportSheet = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.SDK.value, sdkGroup.value)).getSheetData();
            for (JsonElement sheetEntry: reportSheet){
                int failedValueInteger = getPermutationResultCountForSingleTestEntry(sheetEntry, Enums.SdkSheetColumnNames.Fail);
                totalAmount += failedValueInteger;
            }
        }
        return totalAmount;
    }
}
