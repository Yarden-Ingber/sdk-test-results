package com.yarden.restServiceDemo.slackService;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.HtmlReportGenerator;
import com.yarden.restServiceDemo.Logger;
import com.yarden.restServiceDemo.awsS3Service.AwsS3Provider;
import com.yarden.restServiceDemo.mailService.MailSender;
import com.yarden.restServiceDemo.reportService.SdkVersionsReportService;
import com.yarden.restServiceDemo.reportService.SheetData;
import com.yarden.restServiceDemo.pojos.SlackReportNotificationJson;
import com.yarden.restServiceDemo.pojos.SlackReportData;
import com.yarden.restServiceDemo.reportService.SheetTabIdentifier;
import org.apache.http.impl.execchain.RequestAbortedException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class SdkSlackReportSender {

    private String sdk;
    private String changeLog;
    private String testCoverageGap;
    private String version;
    private SlackReportNotificationJson requestJson;

    public void send(String json) throws IOException, MailjetSocketTimeoutException, MailjetException {
        requestJson = new Gson().fromJson(json, SlackReportNotificationJson.class);
        if (requestJson.getSdk() == null || requestJson.getSdk().isEmpty()) {
            Logger.error("Failed sending report, Missing SDK in request json.");
            throw new JsonParseException("No SDK in request JSON");
        } else {
            sdk = requestJson.getSdk();
        }
        version = getVersion();
        changeLog = requestJson.getChangeLog();
        testCoverageGap = requestJson.getTestCoverageGap();
        String newVersionInstructions = getNewVersionInstructions();
        SlackReportData slackReportData = new SlackReportData()
                .setReportTextPart("A new SDK is about to be released.\n\nSDK: " + sdk + "\nVersion:\n* " + version.replaceAll(";", "\n* ") + "\n\n" + newVersionInstructions)
                .setReportTitle("Test report for SDK: " + sdk)
                .setMailSubject("Test report for SDK: " + sdk)
                .setVersion(version)
                .setChangeLog(changeLog)
                .setCoverageGap(testCoverageGap)
                .setHighLevelReportTable(getHighLevelReportTable())
                .setDetailedMissingTestsTable(getDetailedMissingTestsTable())
                .setDetailedPassedTestsTable(getDetailedPassedTestsTable())
                .setHtmlReportS3BucketName(Enums.EnvVariables.AwsS3SdkReportsBucketName.value);
        slackReportData.setHtmlReportUrl(new HtmlReportGenerator(slackReportData).getHtmlReportUrlInAwsS3(slackReportData.getHtmlReportS3BucketName()));
        setRecipientMail(slackReportData);
        if (requestJson.getSpecificRecipient() == null || requestJson.getSpecificRecipient().isEmpty()){
            new SlackReporter().report(slackReportData);
            new SdkVersionsReportService().updateVersion(json);
        }
        new MailSender().send(slackReportData);
    }

    public void sendFullRegression(String json) throws FileNotFoundException, UnsupportedEncodingException, MailjetSocketTimeoutException, MailjetException {
        requestJson = new Gson().fromJson(json, SlackReportNotificationJson.class);
        if (requestJson.getSdk() == null || requestJson.getSdk().isEmpty()) {
            Logger.error("Failed sending report, Missing SDK in request json.");
            throw new JsonParseException("No SDK in request JSON");
        } else {
            sdk = requestJson.getSdk();
        }
        SlackReportData slackReportData = new SlackReportData()
                .setReportTextPart("Full regression test report.\n\nSDK: " + sdk)
                .setReportTitle("Full regression test report for SDK: " + sdk)
                .setMailSubject("Full regression test report for SDK: " + sdk)
                .setHighLevelReportTable(getHighLevelFullRegressionReportTable())
                .setDetailedMissingTestsTable(getDetailedMissingTestsTable())
                .setDetailedPassedTestsTable(getDetailedPassedTestsTable())
                .setDetailedFailedTestsTable(getDetailedFailedTestsTable())
                .setHtmlReportS3BucketName(Enums.EnvVariables.AwsS3SdkReportsBucketName.value);
        slackReportData.setHtmlReportUrl(new HtmlReportGenerator(slackReportData).getHtmlReportUrlInAwsS3(slackReportData.getHtmlReportS3BucketName()));
        setRecipientMail(slackReportData);
        new MailSender().send(slackReportData);
    }

    private String getVersion(){
        return requestJson.getVersion()
                .replace("RELEASE_CANDIDATE;", "")
                .replaceAll("RELEASE_CANDIDATE-", "")
                .replaceAll("@", " ");
    }

    private HTMLTableBuilder getHighLevelReportTable() throws RequestAbortedException {
        if (getFailedTestCountForSdk() > 0) {
            throw new RequestAbortedException("There are failed tests in the excel sheet");
        }
        HTMLTableBuilder tableBuilder = new HTMLTableBuilder(false, 2, 6);
        tableBuilder.addTableHeader("SDK", "Success percentage", "Specific test count", "Previous release specific test count", "Generic test count", "Previous release generic tesst count");
        String previousSpecificTestCountFileName = requestJson.getSdk() + "PreviousSpecificTestCount.txt";
        String previousGenericTestCountFileName = requestJson.getSdk() + "PreviousGenericTestCount.txt";
        String previousSpecificTestCount = getTestCountFromFileNameInS3(previousSpecificTestCountFileName);
        String previousGenericTestCount = getTestCountFromFileNameInS3(previousGenericTestCountFileName);
        String currentSpecificTestCount = Integer.toString(getPassedTestCountForSdk((String testName) -> !testName.contains(Enums.Strings.Generic.value)));
        String currentGenericTestCount = Integer.toString(getPassedTestCountForSdk((String testName) -> testName.contains(Enums.Strings.Generic.value)));
        AwsS3Provider.writeStringToFile(Enums.EnvVariables.AwsS3SdkReportsBucketName.value, previousSpecificTestCountFileName, currentSpecificTestCount);
        AwsS3Provider.writeStringToFile(Enums.EnvVariables.AwsS3SdkReportsBucketName.value, previousGenericTestCountFileName, currentGenericTestCount);
        tableBuilder.addRowValues(true, requestJson.getSdk(), "100", currentSpecificTestCount, previousSpecificTestCount, currentGenericTestCount, previousGenericTestCount);
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

    private HTMLTableBuilder getHighLevelFullRegressionReportTable() {
        HTMLTableBuilder tableBuilder = new HTMLTableBuilder(false, 3, 4);
        tableBuilder.addTableHeader("Version", "Passed tests", "Failed tests", "Missing tests");
        String currentPassedTestsCount = Integer.toString(getPassedTestCountForSdk((String testName) -> true));
        String currentFailedTestsCount = Integer.toString(getFailedTestCountForSdk());
        String currentMissingTestsCount = Integer.toString(getMissingTestsCountForSdk());
        tableBuilder.addRowValues(true, "Current report", currentPassedTestsCount, currentFailedTestsCount, currentMissingTestsCount);
        tableBuilder.addRowValues(true, "Previous report", getPreviousTestCountInS3(TestCountType.PASSED, currentPassedTestsCount), getPreviousTestCountInS3(TestCountType.FAILED, currentFailedTestsCount), getPreviousTestCountInS3(TestCountType.MISSING, currentMissingTestsCount));
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

    private HTMLTableBuilder getDetailedMissingTestsTable() {
        HTMLTableBuilder tableBuilder = new HTMLTableBuilder(false, 2, 1);
        tableBuilder.addTableHeader("<div align=\"left\">Test name</div>");
        for (Enums.SdkGroupsSheetTabNames sdkGroup: Enums.SdkGroupsSheetTabNames.values()) {
            JsonArray reportSheet = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.SDK.value, sdkGroup.value)).getSheetData();
            if(reportSheet.get(0).getAsJsonObject().get(sdk) != null) {
                for (JsonElement row: reportSheet) {
                    if (row.getAsJsonObject().get(sdk).getAsString().isEmpty()) {
                        if (row.getAsJsonObject().get(Enums.SdkSheetColumnNames.TestName.value).getAsString().equals(Enums.SdkSheetColumnNames.IDRow.value)) {
                        } else {
                            tableBuilder.addRowValues(false, row.getAsJsonObject().get(Enums.SdkSheetColumnNames.TestName.value).getAsString());
                        }
                    }
                }
            }
        }
        return tableBuilder;
    }

    private HTMLTableBuilder getDetailedPassedTestsTable() {
        HTMLTableBuilder tableBuilder = new HTMLTableBuilder(false, 2, 3);
        tableBuilder.addTableHeader("<div align=\"left\">Test name</div>", "Result", "Permutations");
        for (Enums.SdkGroupsSheetTabNames sdkGroup: Enums.SdkGroupsSheetTabNames.values()) {
            JsonArray reportSheet = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.SDK.value, sdkGroup.value)).getSheetData();
            if(reportSheet.get(0).getAsJsonObject().get(sdk) != null) {
                for (JsonElement row: reportSheet) {
                    if (row.getAsJsonObject().get(sdk).getAsString().contains(Enums.TestResults.Passed.value)) {
                        if (row.getAsJsonObject().get(Enums.SdkSheetColumnNames.TestName.value).getAsString().equals(Enums.SdkSheetColumnNames.IDRow.value)) {
                        } else {
                            tableBuilder.addRowValues(false, row.getAsJsonObject().get(Enums.SdkSheetColumnNames.TestName.value).getAsString(),"PASS",
                                    getSumOfPassedPermutationsForTest(row));
                        }
                    }
                }
            }
        }
        return tableBuilder;
    }

    private HTMLTableBuilder getDetailedFailedTestsTable() {
        HTMLTableBuilder tableBuilder = new HTMLTableBuilder(false, 2, 3);
        tableBuilder.addTableHeader("<div align=\"left\">Test name</div>", "Result", "Permutations");
        for (Enums.SdkGroupsSheetTabNames sdkGroup: Enums.SdkGroupsSheetTabNames.values()) {
            JsonArray reportSheet = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.SDK.value, sdkGroup.value)).getSheetData();
            if(reportSheet.get(0).getAsJsonObject().get(sdk) != null) {
                for (JsonElement row: reportSheet) {
                    if (row.getAsJsonObject().get(sdk).getAsString().contains(Enums.TestResults.Failed.value)) {
                        if (row.getAsJsonObject().get(Enums.SdkSheetColumnNames.TestName.value).getAsString().equals(Enums.SdkSheetColumnNames.IDRow.value)) {
                        } else {
                            tableBuilder.addRowValues(false, row.getAsJsonObject().get(Enums.SdkSheetColumnNames.TestName.value).getAsString(),"Fail",
                                    getSumOfFailedPermutationsForTest(row));
                        }
                    }
                }
            }
        }
        return tableBuilder;
    }

    private int getTotalTestCountForSdk(){
        int totalAmount = 0;
        for (Enums.SdkGroupsSheetTabNames sdkGroup: Enums.SdkGroupsSheetTabNames.values()) {
            JsonArray reportSheet = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.SDK.value, sdkGroup.value)).getSheetData();
            for (JsonElement sheetEntry: reportSheet){
                int passedValueInteger = getPermutationResultCountForSingleTestEntry(sheetEntry, Enums.SdkSheetColumnNames.Pass);
                int failedValueInteger = getPermutationResultCountForSingleTestEntry(sheetEntry, Enums.SdkSheetColumnNames.Fail);
                totalAmount += passedValueInteger + failedValueInteger;
            }
        }
        return totalAmount;
    }

    @FunctionalInterface
    public interface AddingTestCountCondition {
        boolean shouldAddTest(String testName);
    }

    private int getPassedTestCountForSdk(AddingTestCountCondition addingTestCountCondition){
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

    private int getFailedTestCountForSdk(){
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

    private int getMissingTestsCountForSdk() {
        int totalAmount = 0;
        for (Enums.SdkGroupsSheetTabNames sdkGroup: Enums.SdkGroupsSheetTabNames.values()) {
            JsonArray reportSheet = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.SDK.value, sdkGroup.value)).getSheetData();
            if(reportSheet.get(0).getAsJsonObject().get(sdk) != null) {
                for (JsonElement row: reportSheet) {
                    if (row.getAsJsonObject().get(sdk).getAsString().isEmpty()) {
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

    private int getPermutationResultCountForSingleTestEntry(JsonElement sheetEntry, Enums.SdkSheetColumnNames permutationResult){
        JsonElement passedValue = sheetEntry.getAsJsonObject().get(requestJson.getSdk() + permutationResult.value);
        return (passedValue == null || passedValue.getAsString().isEmpty()) ?
                0 :
                sheetEntry.getAsJsonObject().get(requestJson.getSdk() + permutationResult.value).getAsInt();
    }

    private void setRecipientMail(SlackReportData slackReportData) {
        String recipientMail = "";
        if (requestJson.getSpecificRecipient() != null && !requestJson.getSpecificRecipient().isEmpty()) {
            recipientMail = requestJson.getSpecificRecipient();
        } else {
            recipientMail = Enums.EnvVariables.MailReportRecipient.value;
        }
        slackReportData.setRecipientsJsonArray(new JSONArray().put(new JSONObject().put("Email", recipientMail).put("Name", "Release_Report")));
    }

    private String getNewVersionInstructions(){
        String text = "Instructions and dependencies: ";
        if (sdk.equals("dotnet")) {
            return text + "https://www.nuget.org/packages/Eyes.Selenium/";
        } else if (sdk.equals("java")) {
            return text + "https://mvnrepository.com/artifact/com.applitools/eyes-selenium-java3/" + version;
        } else if (sdk.equals("js_selenium_4")) {
            return text + "https://www.npmjs.com/package/@applitools/eyes-selenium";
        } else if (sdk.equals("js_wdio_5")) {
            return text + "https://www.npmjs.com/package/@applitools/eyes-webdriverio";
        }
        return "";
    }

    private String getSumOfPassedPermutationsForTest(JsonElement row){
        try {
            return Integer.toString(row.getAsJsonObject().get(requestJson.getSdk() + Enums.SdkSheetColumnNames.Pass.value).getAsInt());
        } catch (Exception e) {
            return "0";
        }
    }

    private String getSumOfFailedPermutationsForTest(JsonElement row){
        try {
            return Integer.toString(row.getAsJsonObject().get(requestJson.getSdk() + Enums.SdkSheetColumnNames.Fail.value).getAsInt());
        } catch (Exception e) {
            return "0";
        }
    }

}
