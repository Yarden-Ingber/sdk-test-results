package com.yarden.restServiceDemo.slackService;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.awsS3Service.AwsS3Provider;
import com.yarden.restServiceDemo.pojos.SlackReportNotificationJson;
import com.yarden.restServiceDemo.reportService.SheetData;
import com.yarden.restServiceDemo.reportService.SheetTabIdentifier;

public class SdkHighLevelFullRegressionReportTableBuilder extends SdkHighLevelTableBuilderBaseClass{

    private String currentPassedTestsCount;
    private String currentFailedTestsCount;
    private String currentMissingTestsCount;
    private String previousPassedTestsCount;
    private String previousFailedTestsCount;
    private String previousMissingTestsCount;

    public SdkHighLevelFullRegressionReportTableBuilder(SlackReportNotificationJson requestJson){
        super(requestJson);
        currentPassedTestsCount = Integer.toString(getPassedTestCountForSdk((String testName) -> true));
        currentFailedTestsCount = Integer.toString(getFailedTestCountForSdk());
        currentMissingTestsCount = Integer.toString(getMissingTestsCountForSdk());
        previousPassedTestsCount = getPreviousTestCountInS3(TestCountType.PASSED, currentPassedTestsCount);
        previousFailedTestsCount = getPreviousTestCountInS3(TestCountType.FAILED, currentFailedTestsCount);
        previousMissingTestsCount = getPreviousTestCountInS3(TestCountType.MISSING, currentMissingTestsCount);
    }

    public HTMLTableBuilder getHighLevelReportTable() {
        HTMLTableBuilder tableBuilder = new HTMLTableBuilder(false, 3, 4);
        tableBuilder.addTableHeader("Version", "Passed tests", "Failed tests", "Missing tests");
        tableBuilder.addRowValues(true, "Current report", currentPassedTestsCount, currentFailedTestsCount, currentMissingTestsCount);
        tableBuilder.addRowValues(true, "Previous report", previousPassedTestsCount, previousFailedTestsCount, previousMissingTestsCount);
        return tableBuilder;
    }

    private String getPreviousTestCountInS3(TestCountType testCountType, String currentTestCount){
        String previousTestCountFileName = requestJson.getSdk() + "Previous"+ testCountType.name() + "FullRegressionTestCount.txt";
        String previousTestCount = "";
        try {
            previousTestCount = AwsS3Provider.getStringFromFile(Enums.EnvVariables.AwsS3SdkReportsBucketName.value, previousTestCountFileName);
        } catch (Throwable t) { t.printStackTrace(); }
        AwsS3Provider.writeStringToFile(Enums.EnvVariables.AwsS3SdkReportsBucketName.value, previousTestCountFileName, currentTestCount);
        return previousTestCount;
    }

    private enum TestCountType {
        PASSED, FAILED, MISSING;
    }

    private int getMissingTestsCountForSdk() {
        int totalAmount = 0;
        for (Enums.SdkGroupsSheetTabNames sdkGroup: Enums.SdkGroupsSheetTabNames.values()) {
            JsonArray reportSheet = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.SDK.value, sdkGroup.value)).getSheetData();
            if(reportSheet.get(0).getAsJsonObject().get(requestJson.getSdk()) != null) {
                for (JsonElement row: reportSheet) {
                    if (row.getAsJsonObject().get(requestJson.getSdk()).getAsString().isEmpty()) {
                        if (row.getAsJsonObject().get(Enums.SdkSheetColumnNames.TestName.value).getAsString().equals(Enums.SdkSheetColumnNames.IDRow.value)) {
                        } else {
                            totalAmount++;
                        }
                    }
                }
            }
        }
        return totalAmount;
    }

}
