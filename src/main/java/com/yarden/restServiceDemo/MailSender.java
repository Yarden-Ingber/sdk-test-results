package com.yarden.restServiceDemo;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.lowagie.text.DocumentException;
import com.mailjet.client.*;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.resource.Emailv31;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;

public class MailSender {

    StringBuilder htmlReportStringBuilder = new StringBuilder();
    String sdk;
    String changeLog;
    String version;

    public void send(EmailNotificationJson requestJson) throws InterruptedException, DocumentException, IOException, MailjetSocketTimeoutException, MailjetException {
        MailjetClient client;
        MailjetRequest request;
        MailjetResponse response;
        sdk = requestJson.getSdk();
        version = requestJson.getVersion().replaceAll("[^\\d.]", "");
        changeLog = requestJson.getChangeLog();
        client = new MailjetClient("d163f65725fda1781f7728f93ced7e67", "01156141f47b51d311a4b71409b1704a", new ClientOptions("v3.1"));
        request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, new JSONObject()
                                        .put("Email", "yarden.ingber@applitools.com")
                                        .put("Name", "Yarden Ingber"))
                                .put(Emailv31.Message.TO, getRecipientsJsonArray())
                                .put(Emailv31.Message.SUBJECT, "SDK Release")
                                .put(Emailv31.Message.TEXTPART, "SDK: " + sdk + "\nVersion: " + version + "\nChange Log:\n\n" + changeLog)
                                .put(Emailv31.Message.ATTACHMENTS, new JSONArray()
                                    .put(new JSONObject()
                                        .put("ContentType", "application/pdf")
                                            .put("Filename", "test_report.pdf")
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
                        .put("Name", "Yarden Ingber"));
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

    private String getHtmlReportAsBase64() throws IOException, InterruptedException, DocumentException {
        PrintWriter writer = new PrintWriter("test_report.html", "UTF-8");
        writer.println(getHtmlReportAsPlainSting());
        writer.close();
        String inputFile = "test_report.html";
        String url = new File(inputFile).toURI().toURL().toString();
        String outputFile = "test_report.pdf";
        OutputStream os = new FileOutputStream(outputFile);
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocument(url);
        renderer.layout();
        renderer.createPDF(os);
        os.close();
        return Base64.encode(IOUtils.toByteArray(new FileInputStream("test_report.pdf")));
    }

    private String getHtmlReportAsPlainSting(){
        htmlReportStringBuilder.append("<!DOCTYPE html><html><head>");
        htmlReportStringBuilder.append(getCSS());
        htmlReportStringBuilder.append("</head><body><div class=\"wrapper\">\n" +
                "    <div class=\"content\">\n" +
                "        <div class=\"header\">applitools</div>");
        htmlReportStringBuilder.append("\n" +
                "<h2>Test Report for SDK: " + sdk + "</h2>" +
                "<h2>SDK Version: " + version + "</h2><br/>" +
                "<h2>Change log:<br/>" + changeLog.replace("\n", "<br/>") + "</h2><br/>");
        htmlReportStringBuilder.append(getHighLevelReportTable());
        htmlReportStringBuilder.append("<br/>");
        htmlReportStringBuilder.append("<h2>Missing Tests:</h2>");
        htmlReportStringBuilder.append(getDetailedMissingTestsTable());
        htmlReportStringBuilder.append("<br/>");
        htmlReportStringBuilder.append("<h2>Passed Tests:</h2>");
        htmlReportStringBuilder.append(getDetailedPassedTestsTable());
        htmlReportStringBuilder.append("</div></div></body></html>");
        return htmlReportStringBuilder.toString();
    }

    private String getCSS(){
        return "<style type=\"text/css\">\n" +
                "    h1, h2, h3 {\n" +
                "        font-size: 1em;\n" +
                "    }\n" +
                "    .content {\n" +
                "        background:#ffffff;\n" +
                "        margin: 40px auto;\n" +
                "        width: 700px;\n" +
                "        padding: 30px;\n" +
                "        box-shadow: 0 10px 10px #c7ced0;\n" +
                "        border:1px solid #c7ced0;\n" +
                "    }\n" +
                "    .wrapper {\n" +
                "        background: #e4f0f4;\n" +
                "        padding: 1px;\n" +
                "        font-family: sans-serif;\n" +
                "    }\n" +
                "    .header {\n" +
                "        margin: -30px -30px 0;\n" +
                "        padding: 17px 30px;\n" +
                "        color: white;\n" +
                "        background: #3ab8ac;\n" +
                "        font-size: 24px;\n" +
                "        font-weight: bold;\n" +
                "    }\n" +
                "    body {\n" +
                "        margin: 0;\n" +
                "    }\n" +
                "    table {\n" +
                "        width: 400px;\n" +
                "    }\n" +
                "    tr {\n" +
                "        background: #f8f8f8;\n" +
                "    }\n" +
                "    td, th {\n" +
                "        padding: 3px 12px;\n" +
                "    }\n" +
                "    .fail {\n" +
                "       background: #b74938;\n" +
                "       color: white;\n" +
                "       font-family: sans-serif;\n" +
                "    }\n" +
                "    .pass {\n" +
                "       background: #34a87d;\n" +
                "       color: white;\n" +
                "       font-family: sans-serif;\n" +
                "    }\n" +
                "</style>";
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
        HTMLTableBuilder tableBuilder = new HTMLTableBuilder(false, 2, 3);
        tableBuilder.addTableHeader("SDK", "Success Percentage", "Amount of Tests");
        tableBuilder.addRowValues(lastSdkResult.getAsJsonObject().get(Enums.HighLevelSheetColumnNames.Sdk.value).getAsString(),
                lastSdkResult.getAsJsonObject().get(Enums.HighLevelSheetColumnNames.SuccessPercentage.value).getAsString(),
                lastSdkResult.getAsJsonObject().get(Enums.HighLevelSheetColumnNames.AmountOfTests.value).getAsString());
        return tableBuilder.toString();
    }

    private String getDetailedMissingTestsTable() {
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
        return tableBuilder.toString();
    }

    private String getDetailedPassedTestsTable() {
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
        return tableBuilder.toString();
    }
}
