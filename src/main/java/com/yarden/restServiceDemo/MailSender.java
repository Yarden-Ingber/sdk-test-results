package com.yarden.restServiceDemo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mailjet.client.*;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class MailSender {

    StringBuilder htmlReportStringBuilder = new StringBuilder();
    String sdk;
    String version;

    public void send(EmailNotificationJson requestJson) throws MailjetSocketTimeoutException, MailjetException {
        MailjetClient client;
        MailjetRequest request;
        MailjetResponse response;
        sdk = requestJson.getSdk();
        version = requestJson.getVersion().replaceAll("[^\\d.]", "");
        client = new MailjetClient("d163f65725fda1781f7728f93ced7e67", "01156141f47b51d311a4b71409b1704a", new ClientOptions("v3.1"));
        request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, new JSONObject()
                                        .put("Email", "yarden.ingber@applitools.com")
                                        .put("Name", "Yarden Ingber"))
                                .put(Emailv31.Message.TO, getRecipientsJsonArray())
                                .put(Emailv31.Message.SUBJECT, "SDK Release")
                                .put(Emailv31.Message.TEXTPART, "SDK: " + sdk + "\nVersion: " + version)
                                .put(Emailv31.Message.ATTACHMENTS, new JSONArray()
                                    .put(new JSONObject()
                                        .put("ContentType", "text/plain")
                                            .put("Filename", "test_report.html")
                                            .put("Base64Content", getHtmlReportAsBase64())))
                                .put(Emailv31.Message.CUSTOMID, "SdkRelease")));
        response = client.post(request);
        System.out.println(response.getStatus());
        System.out.println(response.getData());
    }

    private JSONArray getRecipientsJsonArray(){
        return new JSONArray()
                .put(new JSONObject()
                        .put("Email", "yarden.ingber@applitools.com")
                        .put("Name", "Yarden"));
//                .put(new JSONObject()
//                        .put("Email", "adam.carmi@applitools.com")
//                        .put("Name", "Adam Carmi"))
//                .put(new JSONObject()
//                        .put("Email", "daniel.puterman@applitools.com")
//                        .put("Name", "Daniel Puterman"))
//                .put(new JSONObject()
//                        .put("Email", "amit.zur@applitools.com")
//                        .put("Name", "Amit Zur"))
//                .put(new JSONObject()
//                        .put("Email", "patrick.mccartney@applitools.com")
//                        .put("Name", "Patrick McCartney"))
//                .put(new JSONObject()
//                        .put("Email", "yarden.naveh@applitools.com")
//                        .put("Name", "Yarden Naveh"));
    }

    private String getHtmlReportAsBase64(){
        htmlReportStringBuilder.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"><html><head><META http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head><body><div>");
        htmlReportStringBuilder.append("<div>\n" +
                "\n" +
                "<h2>Test Report for SDK: " + sdk + "</h2>" +
                "<h2>SDK Version: " + version + "</h2><br>");
        htmlReportStringBuilder.append(getHighLevelReportTable());
        htmlReportStringBuilder.append("<br>");
        htmlReportStringBuilder.append(getDetailedReportTable());
        htmlReportStringBuilder.append("</div>\n" +
                "\n" +
                "</div></body></html>");
        return Base64.encode(htmlReportStringBuilder.toString().getBytes());
    }

    private String getHighLevelReportTable() {
        JsonArray highLevelSheet = new SheetData(Enums.SheetTabsNames.HighLevel.value).getSheetData();
        JsonElement lastSdkResult = null;
        for (int i = highLevelSheet.size()-1; i >= 0; i--){
            lastSdkResult = highLevelSheet.get(i);
            if (lastSdkResult.getAsJsonObject().get(Enums.HighLevelSheetColumnNames.Sdk.value).getAsString().equals(sdk)) {
                break;
            }
        }
        HTMLTableBuilder tableBuilder = new HTMLTableBuilder(true, 2, 3);
        tableBuilder.addTableHeader("SDK", "Success Percentage", "Amount of Tests");
        tableBuilder.addRowValues(lastSdkResult.getAsJsonObject().get(Enums.HighLevelSheetColumnNames.Sdk.value).getAsString(),
                lastSdkResult.getAsJsonObject().get(Enums.HighLevelSheetColumnNames.SuccessPercentage.value).getAsString(),
                lastSdkResult.getAsJsonObject().get(Enums.HighLevelSheetColumnNames.AmountOfTests.value).getAsString());
        return tableBuilder.toString();
    }

    private String getDetailedReportTable() {
        JsonArray reportSheet = new SheetData(Enums.SheetTabsNames.Report.value).getSheetData();
        HTMLTableBuilder tableBuilder = new HTMLTableBuilder(true, 2, 2);
        tableBuilder.addTableHeader("Test Name", "Result");
        for (JsonElement row: reportSheet) {
            if (row.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString().equals(Enums.SheetColumnNames.IDRow.value)) {
            } else if (row.getAsJsonObject().get(sdk).getAsString().isEmpty()) {
                tableBuilder.addRowValues(row.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString(), "");
            } else {
                tableBuilder.addRowValues(row.getAsJsonObject().get(Enums.SheetColumnNames.TestName.value).getAsString(),
                        row.getAsJsonObject().get(sdk).getAsString().contains(Enums.TestResults.Failed.value) ? "PASS" : "FAIL");
            }
        }
        return tableBuilder.toString();
    }
}
