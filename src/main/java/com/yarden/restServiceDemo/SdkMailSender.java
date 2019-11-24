package com.yarden.restServiceDemo;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.lowagie.text.DocumentException;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.yarden.restServiceDemo.pojos.EmailNotificationJson;
import com.yarden.restServiceDemo.pojos.ReportMailData;

import java.io.IOException;

public class SdkMailSender {

    String sdk;
    String changeLog;
    String testCoverageGap;
    String version;

    public void send(String json) throws InterruptedException, DocumentException, IOException, MailjetSocketTimeoutException, MailjetException {
        EmailNotificationJson requestJson = new Gson().fromJson(json, EmailNotificationJson.class);
        if (requestJson.getSdk() == null || requestJson.getSdk().isEmpty()) {
            Logger.error("Failed sending mail report, Missing SDK in request json.");
            throw new JsonParseException("No SDK in request JSON");
        } else {
            sdk = requestJson.getSdk();
        }
        version = requestJson.getVersion().replaceAll("[^\\d.]", "");
        changeLog = requestJson.getChangeLog();
        testCoverageGap = requestJson.getTestCoverageGap();
        ReportMailData reportMailData = new ReportMailData()
                .setMailTextPart("SDK: \" + sdk + \"\\nVersion: \" + version + \"\\nChange Log:\\n\\n\" + changeLog")
                .setReportTitle("Test Report for SDK: " + sdk)
                .setVersion("SDK Version: " + version)
                .setChangeLog("Change log:<br/>" + changeLog.replace("\n", "<br/>"))
                .setCoverageGap("Test coverage gap:<br/><br/>" + testCoverageGap.replace("\n", "<br/>"))
                .setShouldAddTestResults(true)
                .setShouldAddCoverageGap(true)
                .setHighLevelReportTable(getHighLevelReportTable())
                .setDetailedMissingTestsTable(getDetailedMissingTestsTable())
                .setDetailedPassedTestsTable(getDetailedPassedTestsTable());
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
        HTMLTableBuilder tableBuilder = new HTMLTableBuilder(false, 2, 3);
        tableBuilder.addTableHeader("SDK", "Success Percentage", "Amount of Tests");
        tableBuilder.addRowValues(lastSdkResult.getAsJsonObject().get(Enums.HighLevelSheetColumnNames.Sdk.value).getAsString(),
                lastSdkResult.getAsJsonObject().get(Enums.HighLevelSheetColumnNames.SuccessPercentage.value).getAsString(),
                lastSdkResult.getAsJsonObject().get(Enums.HighLevelSheetColumnNames.AmountOfTests.value).getAsString());
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
                    tableBuilder.addRowValues(row.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString());
                }
            }
        }
        return tableBuilder;
    }

    private HTMLTableBuilder getDetailedPassedTestsTable() {
        JsonArray reportSheet = new SheetData(Enums.SheetTabsNames.Report.value).getSheetData();
        HTMLTableBuilder tableBuilder = new HTMLTableBuilder(false, 2, 2);
        tableBuilder.addTableHeader("Test Name", "Result");
        for (JsonElement row: reportSheet) {
            if (row.getAsJsonObject().get(sdk).getAsString().contains(Enums.TestResults.Passed.value)) {
                if (row.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString().equals(Enums.SheetColumnNames.IDRow.value)) {
                } else {
                    tableBuilder.addRowValues(row.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString(),"PASS");
                }
            }
        }
        return tableBuilder;
    }

}
