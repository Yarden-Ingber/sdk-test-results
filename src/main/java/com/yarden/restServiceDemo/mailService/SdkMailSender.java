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
        version = requestJson.getVersion();
        changeLog = requestJson.getChangeLog();
        testCoverageGap = requestJson.getTestCoverageGap();
        ReportMailData reportMailData = new ReportMailData()
                .setMailTextPart("SDK: " + sdk + "\nVersion: " + version + "\nChange Log:\n\n" + changeLog)
                .setReportTitle("Test Report for SDK: " + sdk)
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

    private HTMLTableBuilder getHighLevelReportTable() {
        JsonArray highLevelSheet = new SheetData(Enums.SheetTabsNames.HighLevel.value).getSheetData();
        JsonElement lastSdkResult = null;
        for (int i = highLevelSheet.size()-1; i >= 0; i--){
            lastSdkResult = highLevelSheet.get(i);
            if (lastSdkResult.getAsJsonObject().get(Enums.HighLevelSheetColumnNames.Sdk.value).getAsString().equals(sdk)) {
                break;
            }
        }
        HTMLTableBuilder tableBuilder = new HTMLTableBuilder(false, 2, 4);
        tableBuilder.addTableHeader("SDK", "Success percentage", "Test count", "Previous release test count");
        String previousTestCountFileName = requestJson.getSdk() + "previousTestCount.txt";
        String previousTestCount = "";
        String currentTestCount = lastSdkResult.getAsJsonObject().get(Enums.HighLevelSheetColumnNames.AmountOfTests.value).getAsString();
        try {
            previousTestCount = AwsS3Provider.getStringFromFile(Enums.EnvVariables.AwsS3SdkReportsBucketName.value, previousTestCountFileName);
        } catch (Throwable t) { t.printStackTrace(); }
        AwsS3Provider.writeStringToFile(Enums.EnvVariables.AwsS3SdkReportsBucketName.value, previousTestCountFileName, currentTestCount);
        tableBuilder.addRowValues(true, requestJson.getSdk(),
                lastSdkResult.getAsJsonObject().get(Enums.HighLevelSheetColumnNames.SuccessPercentage.value).getAsString(),
                currentTestCount, previousTestCount);
        return tableBuilder;
    }

    private HTMLTableBuilder getDetailedMissingTestsTable() {
        JsonArray reportSheet = new SheetData(Enums.SheetTabsNames.Report.value).getSheetData();
        HTMLTableBuilder tableBuilder = new HTMLTableBuilder(false, 2, 1);
        tableBuilder.addTableHeader("Test Name");
        for (JsonElement row: reportSheet) {
            if (row.getAsJsonObject().get(sdk).getAsString().isEmpty()) {
                if (row.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString().equals(Enums.SheetColumnNames.IDRow.value)) {
                } else {
                    tableBuilder.addRowValues(false, row.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString());
                }
            }
        }
        return tableBuilder;
    }

    private HTMLTableBuilder getDetailedPassedTestsTable() {
        JsonArray reportSheet = new SheetData(Enums.SheetTabsNames.Report.value).getSheetData();
        HTMLTableBuilder tableBuilder = new HTMLTableBuilder(false, 2, 3);
        tableBuilder.addTableHeader("Test Name", "Result", "Permutation Count");
        for (JsonElement row: reportSheet) {
            if (row.getAsJsonObject().get(sdk).getAsString().contains(Enums.TestResults.Passed.value)) {
                if (row.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString().equals(Enums.SheetColumnNames.IDRow.value)) {
                } else {
                    tableBuilder.addRowValues(false, row.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString(),"PASS",
                            getSumOfPermutationsForTest(row));
                }
            }
        }
        return tableBuilder;
    }

    private String getSumOfPermutationsForTest(JsonElement row){
        return Integer.toString(row.getAsJsonObject().get(requestJson.getSdk() + Enums.SheetColumnNames.Pass.value).getAsInt());
    }

}
