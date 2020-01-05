package com.yarden.restServiceDemo.mailService;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.lowagie.text.DocumentException;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.Logger;
import com.yarden.restServiceDemo.awsS3Service.AwsS3Provider;
import com.yarden.restServiceDemo.reportService.SheetData;
import com.yarden.restServiceDemo.pojos.EmailNotificationJson;
import com.yarden.restServiceDemo.pojos.ReportMailData;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class SdkMailSender {

    private String sdk;
    private String changeLog;
    private String testCoverageGap;
    private String version;
    private EmailNotificationJson requestJson;

    public void send(String json) throws InterruptedException, DocumentException, IOException, MailjetSocketTimeoutException, MailjetException {
        requestJson = new Gson().fromJson(json, EmailNotificationJson.class);
        if (requestJson.getSdk() == null || requestJson.getSdk().isEmpty()) {
            Logger.error("Failed sending mail report, Missing SDK in request json.");
            throw new JsonParseException("No SDK in request JSON");
        } else {
            sdk = requestJson.getSdk();
        }
        version = getVersion();
        changeLog = requestJson.getChangeLog();
        testCoverageGap = requestJson.getTestCoverageGap();
        String newVersionInstructions = getNewVersionInstructions();
        ReportMailData reportMailData = new ReportMailData()
                .setMailTextPart("A new SDK is about to be released.\n\nSDK: " + sdk + "\nVersion:\n* " + version.replaceAll(";", "\n* ") + "\n\n" + newVersionInstructions)
                .setReportTitle("Test report for SDK: " + sdk)
                .setVersion(version)
                .setChangeLog(changeLog)
                .setCoverageGap(testCoverageGap)
                .setHighLevelReportTable(getHighLevelReportTable())
                .setDetailedMissingTestsTable(getDetailedMissingTestsTable())
                .setDetailedPassedTestsTable(getDetailedPassedTestsTable())
                .setHtmlReportS3BucketName(Enums.EnvVariables.AwsS3SdkReportsBucketName.value)
                .setRecipientsJsonArray(new JSONArray()
                    .put(new JSONObject()
                            .put("Email", Enums.EnvVariables.MailReportRecipient.value)
                            .put("Name", "Release_Report")));
        new MailSender().send(reportMailData);
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
            JsonArray reportSheet = new SheetData(sdkGroup.value).getSheetData();
            if(reportSheet.get(0).getAsJsonObject().get(sdk) != null) {
                for (JsonElement row: reportSheet) {
                    if (row.getAsJsonObject().get(sdk).getAsString().isEmpty()) {
                        if (row.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString().equals(Enums.SheetColumnNames.IDRow.value)) {
                        } else {
                            tableBuilder.addRowValues(false, row.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString());
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
            JsonArray reportSheet = new SheetData(sdkGroup.value).getSheetData();
            if(reportSheet.get(0).getAsJsonObject().get(sdk) != null) {
                for (JsonElement row: reportSheet) {
                    if (row.getAsJsonObject().get(sdk).getAsString().contains(Enums.TestResults.Passed.value)) {
                        if (row.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString().equals(Enums.SheetColumnNames.IDRow.value)) {
                        } else {
                            tableBuilder.addRowValues(false, row.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString(),"PASS",
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
            JsonArray reportSheet = new SheetData(sdkGroup.value).getSheetData();
            for (JsonElement sheetEntry: reportSheet){
                int passedValueInteger = getPermutationResultCountForSingleTestEntry(sheetEntry, Enums.SheetColumnNames.Pass);
                int failedValueInteger = getPermutationResultCountForSingleTestEntry(sheetEntry, Enums.SheetColumnNames.Fail);
                totalAmount += passedValueInteger + failedValueInteger;
            }
        }
        return totalAmount;
    }

    private int getPermutationResultCountForSingleTestEntry(JsonElement sheetEntry, Enums.SheetColumnNames permutationResult){
        JsonElement passedValue = sheetEntry.getAsJsonObject().get(requestJson.getSdk() + permutationResult.value);
        return (passedValue == null || passedValue.getAsString().isEmpty()) ?
                0 :
                sheetEntry.getAsJsonObject().get(requestJson.getSdk() + permutationResult.value).getAsInt();
    }

    private String getNewVersionInstructions(){
        String text = "Instructions and dependencies: ";
        if (sdk.equals("dotnet")) {
            return text + "https://www.nuget.org/packages/Eyes.Selenium/";
        }
        if (sdk.equals("java")) {
            return text + "https://mvnrepository.com/artifact/com.applitools/eyes-common-java3/" + version;
        }
        return "";
    }

    private String getSumOfPermutationsForTest(JsonElement row){
        try {
            return Integer.toString(row.getAsJsonObject().get(requestJson.getSdk() + Enums.SheetColumnNames.Pass.value).getAsInt());
        } catch (Exception e) {
            return "0";
        }
    }

}
