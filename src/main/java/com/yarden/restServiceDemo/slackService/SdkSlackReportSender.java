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
import com.yarden.restServiceDemo.reportService.SheetData;
import com.yarden.restServiceDemo.pojos.SlackReportNotificationJson;
import com.yarden.restServiceDemo.pojos.SlackReportData;
import com.yarden.restServiceDemo.reportService.SheetTabIdentifier;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

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
        }
        new MailSender().send(slackReportData);
    }

    private String getVersion(){
        return requestJson.getVersion()
                .replace("RELEASE_CANDIDATE;", "")
                .replaceAll("RELEASE_CANDIDATE-", "")
                .replaceAll("@", " ");
    }

    private HTMLTableBuilder getHighLevelReportTable() {
        HTMLTableBuilder tableBuilder = new HTMLTableBuilder(false, 2, 4);
        tableBuilder.addTableHeader("SDK", "Success percentage", "Test count", "Previous release test count");
        String previousTestCountFileName = requestJson.getSdk() + "PreviousTestCount.txt";
        String previousTestCount = "";
        String currentTestCount = Integer.toString(getTotalTestCountForSdk());
        try {
            previousTestCount = AwsS3Provider.getStringFromFile(Enums.EnvVariables.AwsS3SdkReportsBucketName.value, previousTestCountFileName);
        } catch (Throwable t) { t.printStackTrace(); }
        AwsS3Provider.writeStringToFile(Enums.EnvVariables.AwsS3SdkReportsBucketName.value, previousTestCountFileName, currentTestCount);
        tableBuilder.addRowValues(true, requestJson.getSdk(), "100", currentTestCount, previousTestCount);
        return tableBuilder;
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
                                    getSumOfPermutationsForTest(row));
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
            return text + "https://mvnrepository.com/artifact/com.applitools/eyes-common-java3/" + version;
        } else if (sdk.equals("js_selenium_4")) {
            return text + "https://www.npmjs.com/package/@applitools/eyes-selenium";
        } else if (sdk.equals("js_wdio_5")) {
            return text + "https://www.npmjs.com/package/@applitools/eyes-webdriverio";
        }
        return "";
    }

    private String getSumOfPermutationsForTest(JsonElement row){
        try {
            return Integer.toString(row.getAsJsonObject().get(requestJson.getSdk() + Enums.SdkSheetColumnNames.Pass.value).getAsInt());
        } catch (Exception e) {
            return "0";
        }
    }

}
