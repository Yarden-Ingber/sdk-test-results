package com.yarden.restServiceDemo.slackService;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.HtmlReportGenerator;
import com.yarden.restServiceDemo.awsS3Service.AwsS3Provider;
import com.yarden.restServiceDemo.mailService.MailSender;
import com.yarden.restServiceDemo.pojos.SlackReportData;
import com.yarden.restServiceDemo.pojos.SlackReportNotificationJson;
import com.yarden.restServiceDemo.reportService.SheetData;
import com.yarden.restServiceDemo.reportService.SheetTabIdentifier;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class EyesSlackReporterSender {

    private SlackReportNotificationJson requestJson;
    private static final String EndedTestTasksCounterFile = "EndedTestTasksCounterFile.txt";
    private static final int NumOfTestTasks = Integer.parseInt("1");

    public void send(String json) throws IOException, MailjetSocketTimeoutException, MailjetException {
        requestJson = new Gson().fromJson(json, SlackReportNotificationJson.class);
        if (isAllTestsEnded()) {
            sendReport();
        }
    }

    private void sendReport() throws IOException, MailjetSocketTimeoutException, MailjetException{
        SlackReportData slackReportData = new SlackReportData()
                .setReportTextPart("A new version of Eyes is about to be released.")
                .setReportTitle("Test report for Eyes")
                .setMailSubject("Test report for Eyes")
                .setChangeLog(requestJson.getChangeLog())
                .setVersion(requestJson.getVersion())
                .setHighLevelReportTable(getHighLevelReportTable())
                .setDetailedPassedTestsTable(getDetailedPassedTestsTable())
                .setHtmlReportS3BucketName(Enums.EnvVariables.AwsS3EyesReportsBucketName.value);
        slackReportData.setHtmlReportUrl(new HtmlReportGenerator(slackReportData).getHtmlReportUrlInAwsS3(slackReportData.getHtmlReportS3BucketName()));
        setRecipientMail(slackReportData);
//        if (requestJson.getSpecificRecipient() == null || requestJson.getSpecificRecipient().isEmpty()){
//            new SlackReporter().report(slackReportData);
//        }
        new MailSender().send(slackReportData);
    }

    private static synchronized boolean isAllTestsEnded() throws IOException {
        String endedTestTasksCounterString = AwsS3Provider.getStringFromFile(Enums.EnvVariables.AwsS3EyesReportsBucketName.value, EndedTestTasksCounterFile);
        int count = Integer.parseInt(endedTestTasksCounterString) + 1;
        if (count >= NumOfTestTasks) {
            AwsS3Provider.writeStringToFile(Enums.EnvVariables.AwsS3EyesReportsBucketName.value, EndedTestTasksCounterFile, "0");
            return true;
        }
        AwsS3Provider.writeStringToFile(Enums.EnvVariables.AwsS3EyesReportsBucketName.value, EndedTestTasksCounterFile, String.valueOf(count));
        return false;
    }

    private HTMLTableBuilder getHighLevelReportTable() {
        HTMLTableBuilder tableBuilder = new HTMLTableBuilder(false, 2, 3);
        tableBuilder.addTableHeader("Success percentage", "Test count", "Previous release test count");
        String previousTestCountFileName = "EyesPreviousTestCount.txt";
        String previousTestCount = "";
        String currentTestCount = Integer.toString(getTotalTestCount());
        try {
            previousTestCount = AwsS3Provider.getStringFromFile(Enums.EnvVariables.AwsS3EyesReportsBucketName.value, previousTestCountFileName);
        } catch (Throwable t) { t.printStackTrace(); }
        AwsS3Provider.writeStringToFile(Enums.EnvVariables.AwsS3EyesReportsBucketName.value, previousTestCountFileName, currentTestCount);
        tableBuilder.addRowValues(true, "100", currentTestCount, previousTestCount);
        return tableBuilder;
    }

    private HTMLTableBuilder getDetailedPassedTestsTable() {
        HTMLTableBuilder tableBuilder = new HTMLTableBuilder(false, 2, 4);
        tableBuilder.addTableHeader("<div align=\"left\">Test name</div>", "Feature", "Feature sub-category", "Result");
        for (Enums.EyesSheetTabsNames eyesReportTab: Enums.EyesSheetTabsNames.values()) {
            if (eyesReportTab != Enums.EyesSheetTabsNames.Sandbox) {
                JsonArray reportSheet = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.Eyes.value, eyesReportTab.value)).getSheetData();
                for (JsonElement row: reportSheet) {
                    if (!row.getAsJsonObject().get(Enums.EyesSheetColumnNames.TestName.value).getAsString().equals(Enums.EyesSheetColumnNames.TimestampRow.value)
                            && !row.getAsJsonObject().get(Enums.EyesSheetColumnNames.TestName.value).getAsString().equals(Enums.EyesSheetColumnNames.IDRow.value)
                            && row.getAsJsonObject().get(Enums.EyesSheetColumnNames.Status.value).getAsString().equals(Enums.TestResults.Passed.value)) {
                        tableBuilder.addRowValues(false, row.getAsJsonObject().get(Enums.EyesSheetColumnNames.TestName.value).getAsString(),
                                row.getAsJsonObject().get(Enums.EyesSheetColumnNames.Feature.value).getAsString(),
                                row.getAsJsonObject().get(Enums.EyesSheetColumnNames.FeatureSubCategory.value).getAsString(),
                                "PASS");
                    }
                }
            }
        }
        return tableBuilder;
    }

    private int getTotalTestCount(){
        int totalAmount = 0;
        for (Enums.EyesSheetTabsNames eyesReportTab: Enums.EyesSheetTabsNames.values()) {
            if (eyesReportTab != Enums.EyesSheetTabsNames.Sandbox) {
                JsonArray reportSheet = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.Eyes.value, eyesReportTab.value)).getSheetData();
                for (JsonElement sheetEntry: reportSheet){
                    if (!sheetEntry.getAsJsonObject().get(Enums.EyesSheetColumnNames.TestName.value).getAsString().equals(Enums.EyesSheetColumnNames.TimestampRow.value)
                    && !sheetEntry.getAsJsonObject().get(Enums.EyesSheetColumnNames.TestName.value).getAsString().equals(Enums.EyesSheetColumnNames.IDRow.value)
                    && sheetEntry.getAsJsonObject().get(Enums.EyesSheetColumnNames.Status.value).getAsString().equals(Enums.TestResults.Passed.value)){
                        totalAmount++;
                    }
                }
            }
        }
        return totalAmount;
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
}
