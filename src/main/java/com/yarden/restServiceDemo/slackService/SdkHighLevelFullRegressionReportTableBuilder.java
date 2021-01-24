package com.yarden.restServiceDemo.slackService;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.awsS3Service.AwsS3Provider;
import com.yarden.restServiceDemo.pojos.SlackReportNotificationJson;
import com.yarden.restServiceDemo.reportService.SheetData;
import com.yarden.restServiceDemo.reportService.SheetTabIdentifier;

public class SdkHighLevelFullRegressionReportTableBuilder extends SdkHighLevelTableBuilderBaseClass{

    public final String currentPassedTestsCount;
    public final String currentFailedTestsCount;
    public final String currentMissingTestsCount;
    public final String previousPassedTestsCount;
    public final String previousFailedTestsCount;
    public final String previousMissingTestsCount;

    public SdkHighLevelFullRegressionReportTableBuilder(SlackReportNotificationJson requestJson){
        super(requestJson);
        currentPassedTestsCount = Integer.toString(getPassedTestCountForSdk((String testName) -> true));
        currentFailedTestsCount = Integer.toString(getFailedTestCountForSdk((String testName) -> true));
        currentMissingTestsCount = Integer.toString(getMissingTestsCountForSdk((String testName) -> true));
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

}
