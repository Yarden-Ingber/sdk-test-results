package com.yarden.restServiceDemo.slackService;

import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.Logger;
import com.yarden.restServiceDemo.awsS3Service.AwsS3Provider;
import com.yarden.restServiceDemo.pojos.SlackReportNotificationJson;
import org.apache.http.impl.execchain.RequestAbortedException;

public class SdkReleaseEventHighLevelReportTableBuilder extends SdkHighLevelTableBuilderBaseClass{

    public final String currentTotalTestCount;
    public final String currentSpecificTestCount;
    public final String currentGenericTestCount;
    public final String currentUnexecutedGenericTestCount;
    public final String previousSpecificTestCount;
    public final String previousGenericTestCount;
    public final String previousTotalTestCount;
    public final String previousUnexecutedGenericTestCount;

    public SdkReleaseEventHighLevelReportTableBuilder(SlackReportNotificationJson requestJson) throws RequestAbortedException {
        super(requestJson);
        if (getFailedTestCountForSdk((String testName) -> true) > 0) {
            throw new RequestAbortedException("There are failed tests in the excel sheet");
        }
        currentSpecificTestCount = Integer.toString(getPassedTestCountForSdk((String testName) -> !testName.contains(Enums.Strings.Generic.value)));
        currentGenericTestCount = Integer.toString(getPassedTestCountForSdk((String testName) -> testName.contains(Enums.Strings.Generic.value)));
        currentTotalTestCount = String.valueOf(Integer.parseInt(currentGenericTestCount) + Integer.parseInt(currentSpecificTestCount));
        currentUnexecutedGenericTestCount = Integer.toString(getMissingTestsCountForSdk((String testName) -> testName.contains(Enums.Strings.Generic.value)));
        if (currentTotalTestCount == "0"){
            throw new RequestAbortedException("No test results in sheet for sdk: " + requestJson.getSdk());
        }
        String previousSpecificTestCountFileName = requestJson.getSdk() + "PreviousSpecificTestCount.txt";
        String previousGenericTestCountFileName = requestJson.getSdk() + "PreviousGenericTestCount.txt";
        String previousTotalTestCountFileName = requestJson.getSdk() + "PreviousTotalTestCount.txt";
        String previousUnexecutedGenericTestCountFileName = requestJson.getSdk() + "PreviousUnexecutedGenericTestCount.txt";
        previousSpecificTestCount = getTestCountFromFileNameInS3(previousSpecificTestCountFileName);
        previousGenericTestCount = getTestCountFromFileNameInS3(previousGenericTestCountFileName);
        previousTotalTestCount = getTestCountFromFileNameInS3(previousTotalTestCountFileName);
        previousUnexecutedGenericTestCount = getTestCountFromFileNameInS3(previousUnexecutedGenericTestCountFileName);
        AwsS3Provider.writeStringToFile(Enums.EnvVariables.AwsS3SdkReportsBucketName.value, previousSpecificTestCountFileName, currentSpecificTestCount);
        AwsS3Provider.writeStringToFile(Enums.EnvVariables.AwsS3SdkReportsBucketName.value, previousGenericTestCountFileName, currentGenericTestCount);
        AwsS3Provider.writeStringToFile(Enums.EnvVariables.AwsS3SdkReportsBucketName.value, previousTotalTestCountFileName, currentTotalTestCount);
    }

    public HTMLTableBuilder getHighLevelReportTable() throws RequestAbortedException {
        HTMLTableBuilder tableBuilder = new HTMLTableBuilder(false, 3, 5);
        tableBuilder.addTableHeader("Test run", "Total test count", "Specific test count", "Generic test count", "Unexecuted generic tests");
        tableBuilder.addRowValues(true, "Current", currentTotalTestCount, currentSpecificTestCount, currentGenericTestCount, currentUnexecutedGenericTestCount);
        tableBuilder.addRowValues(true, "Previous", previousTotalTestCount, previousSpecificTestCount, previousGenericTestCount, previousUnexecutedGenericTestCount);
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
