package com.yarden.restServiceDemo.slackService;

import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.Logger;
import com.yarden.restServiceDemo.awsS3Service.AwsS3Provider;
import com.yarden.restServiceDemo.pojos.SlackReportNotificationJson;
import org.apache.http.impl.execchain.RequestAbortedException;

public class SdkReleaseEventHighLevelReportTableBuilder extends SdkHighLevelTableBuilderBaseClass{

    private String currentTotalTestCount;
    private String currentSpecificTestCount;
    private String currentGenericTestCount;
    private String previousSpecificTestCount;
    private String previousGenericTestCount;
    private String previousTotalTestCount;

    public SdkReleaseEventHighLevelReportTableBuilder(SlackReportNotificationJson requestJson) throws RequestAbortedException {
        super(requestJson);
        currentSpecificTestCount = Integer.toString(getPassedTestCountForSdk((String testName) -> !testName.contains(Enums.Strings.Generic.value)));
        currentGenericTestCount = Integer.toString(getPassedTestCountForSdk((String testName) -> testName.contains(Enums.Strings.Generic.value)));
        currentTotalTestCount = String.valueOf(Integer.parseInt(currentGenericTestCount) + Integer.parseInt(currentSpecificTestCount));
        if (currentTotalTestCount == "0"){
            throw new RequestAbortedException("No test results in sheet for sdk: " + requestJson.getSdk());
        }
        String previousSpecificTestCountFileName = requestJson.getSdk() + "PreviousSpecificTestCount.txt";
        String previousGenericTestCountFileName = requestJson.getSdk() + "PreviousGenericTestCount.txt";
        String previousTotalTestCountFileName = requestJson.getSdk() + "PreviousTotalTestCount.txt";
        previousSpecificTestCount = getTestCountFromFileNameInS3(previousSpecificTestCountFileName);
        previousGenericTestCount = getTestCountFromFileNameInS3(previousGenericTestCountFileName);
        previousTotalTestCount = getTestCountFromFileNameInS3(previousTotalTestCountFileName);
        AwsS3Provider.writeStringToFile(Enums.EnvVariables.AwsS3SdkReportsBucketName.value, previousSpecificTestCountFileName, currentSpecificTestCount);
        AwsS3Provider.writeStringToFile(Enums.EnvVariables.AwsS3SdkReportsBucketName.value, previousGenericTestCountFileName, currentGenericTestCount);
        AwsS3Provider.writeStringToFile(Enums.EnvVariables.AwsS3SdkReportsBucketName.value, previousTotalTestCountFileName, currentTotalTestCount);
    }

    public HTMLTableBuilder getHighLevelReportTable() throws RequestAbortedException {
        if (getFailedTestCountForSdk() > 0) {
            throw new RequestAbortedException("There are failed tests in the excel sheet");
        }
        HTMLTableBuilder tableBuilder = new HTMLTableBuilder(false, 3, 4);
        tableBuilder.addTableHeader("Test run", "Total test count", "Specific test count", "Generic test count");
        tableBuilder.addRowValues(true, "Current", currentTotalTestCount, currentSpecificTestCount, currentGenericTestCount);
        tableBuilder.addRowValues(true, "Previous", previousTotalTestCount, previousSpecificTestCount, previousGenericTestCount);
        return tableBuilder;
    }

    private String getTestCountFromFileNameInS3(String fileName){
        try {
            return AwsS3Provider.getStringFromFile(Enums.EnvVariables.AwsS3SdkReportsBucketName.value, fileName);
        } catch (Throwable t) {
            Logger.warn("No file named: " + fileName + " in S3 bucket: " + Enums.EnvVariables.AwsS3SdkReportsBucketName.value);
            t.printStackTrace();
        }
        return "";
    }

}
