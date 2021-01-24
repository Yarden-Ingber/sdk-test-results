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

    protected int getPassedTestCountForSdk(SdkSlackReportSender.AddingTestCountCondition addingTestCountCondition){
        int totalAmount = 0;
        for (Enums.SdkGroupsSheetTabNames sdkGroup: Enums.SdkGroupsSheetTabNames.values()) {
            JsonArray reportSheet = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.SDK.value, sdkGroup.value)).getSheetData();
            if (reportSheet.get(0).getAsJsonObject().keySet().contains(requestJson.getSdk())) {
                for (JsonElement sheetEntry : reportSheet) {
                    if (addingTestCountCondition.shouldAddTest(sheetEntry.getAsJsonObject().get(Enums.SdkSheetColumnNames.TestName.value).getAsString())) {
                        if (sheetEntry.getAsJsonObject().get(requestJson.getSdk()).getAsString().equals(Enums.TestResults.Passed.value)) {
                            totalAmount++;
                        }
                    }
                }
            }
        }
        return totalAmount;
    }

    protected int getFailedTestCountForSdk(SdkSlackReportSender.AddingTestCountCondition addingTestCountCondition){
        int totalAmount = 0;
        for (Enums.SdkGroupsSheetTabNames sdkGroup: Enums.SdkGroupsSheetTabNames.values()) {
            JsonArray reportSheet = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.SDK.value, sdkGroup.value)).getSheetData();
            if (reportSheet.get(0).getAsJsonObject().keySet().contains(requestJson.getSdk())) {
                for (JsonElement sheetEntry: reportSheet){
                    if (addingTestCountCondition.shouldAddTest(sheetEntry.getAsJsonObject().get(Enums.SdkSheetColumnNames.TestName.value).getAsString())) {
                        if (sheetEntry.getAsJsonObject().get(requestJson.getSdk()).getAsString().equals(Enums.TestResults.Failed.value)) {
                            totalAmount++;
                        }
                    }
                }
            }
        }
        return totalAmount;
    }

    protected int getMissingTestsCountForSdk(SdkSlackReportSender.AddingTestCountCondition addingTestCountCondition) {
        int totalAmount = 0;
        for (Enums.SdkGroupsSheetTabNames sdkGroup: Enums.SdkGroupsSheetTabNames.values()) {
            JsonArray reportSheet = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.SDK.value, sdkGroup.value)).getSheetData();
            if(reportSheet.get(0).getAsJsonObject().get(requestJson.getSdk()) != null) {
                for (JsonElement row: reportSheet) {
                    if (row.getAsJsonObject().get(requestJson.getSdk()).getAsString().isEmpty()) {
                        if (addingTestCountCondition.shouldAddTest(row.getAsJsonObject().get(Enums.SdkSheetColumnNames.TestName.value).getAsString())) {
                            if (row.getAsJsonObject().get(Enums.SdkSheetColumnNames.TestName.value).getAsString().equals(Enums.SdkSheetColumnNames.IDRow.value)) {
                            } else {
                                totalAmount++;
                            }
                        }
                    }
                }
            }
        }
        return totalAmount;
    }
}
