package com.yarden.restServiceDemo;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.lowagie.text.DocumentException;
import com.mailjet.client.*;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.resource.Emailv31;
import com.yarden.restServiceDemo.pojos.ReportMailData;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;

public class MailSender {

    StringBuilder htmlReportStringBuilder = new StringBuilder();
    ReportMailData reportMailData;

    public void send(ReportMailData reportMailData) throws InterruptedException, DocumentException, IOException, MailjetSocketTimeoutException, MailjetException {
        this.reportMailData = reportMailData;
        MailjetClient client;
        MailjetRequest request;
        MailjetResponse response;
        client = new MailjetClient(System.getenv("MJ_APIKEY_PUBLIC"), System.getenv("MJ_APIKEY_PRIVATE"), new ClientOptions("v3.1"));
        request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, new JSONObject()
                                        .put("Email", "yarden.ingber@applitools.com")
                                        .put("Name", "Yarden Ingber"))
                                .put(Emailv31.Message.TO, reportMailData.getRecipientsJsonArray())
                                .put(Emailv31.Message.SUBJECT, "SDK Release")
                                .put(Emailv31.Message.TEXTPART, reportMailData.getMailTextPart())
                                .put(Emailv31.Message.ATTACHMENTS, new JSONArray()
                                    .put(new JSONObject()
                                        .put("ContentType", "application/pdf")
                                            .put("Filename", "test_report.pdf")
                                            .put("Base64Content", getPdfReportAsBase64())))
                                .put(Emailv31.Message.CUSTOMID, "SdkRelease")));
        response = client.post(request);
        System.out.println(response.getStatus());
        System.out.println(response.getData());
    }

    private String getPdfReportAsBase64() throws IOException, InterruptedException, DocumentException {
        final String htmlReportFileName = "test_report.html";
        final String pdfReportFileName = "test_report.pdf";
        PrintWriter writer = new PrintWriter(htmlReportFileName, "UTF-8");
        writer.println(getHtmlReportAsPlainSting());
        writer.close();
        String inputFile = htmlReportFileName;
        String url = new File(inputFile).toURI().toURL().toString();
        String outputFile = pdfReportFileName;
        OutputStream os = new FileOutputStream(outputFile);
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocument(url);
        renderer.layout();
        renderer.createPDF(os);
        os.close();
        String result = Base64.encode(IOUtils.toByteArray(new FileInputStream(pdfReportFileName)));
        try {
            new File(htmlReportFileName).delete();
            new File(pdfReportFileName).delete();
        } catch (Throwable t) {t.printStackTrace();}
        return result;
    }

    private String getHtmlReportAsPlainSting(){
        htmlReportStringBuilder.append("<!DOCTYPE html><html><head>");
        htmlReportStringBuilder.append(getCSS());
        htmlReportStringBuilder.append("</head><body><div class=\"wrapper\">\n" +
                "    <div class=\"content\">\n" +
                "        <div class=\"header\">applitools</div>");
        htmlReportStringBuilder.append("<h2>" + reportMailData.getReportTitle() + "</h2>");
        htmlReportStringBuilder.append("<h2>Version: " + reportMailData.getVersion() + "</h2><br/>");
        if (reportMailData.getChangeLog() != null) {
            htmlReportStringBuilder.append("Change log:<br/>");
            htmlReportStringBuilder.append("<h2>" + reportMailData.getChangeLog() + "</h2><br/>");
            htmlReportStringBuilder.append("<br/>");
        }
        if (reportMailData.getHighLevelReportTable() != null) {
            htmlReportStringBuilder.append(reportMailData.getHighLevelReportTable());
        }
        if (reportMailData.getCoverageGap() != null) {
            htmlReportStringBuilder.append("<br/>Test coverage gap:<br/><br/>");
            htmlReportStringBuilder.append("<h2>" + reportMailData.getCoverageGap() + "</h2><br/>");
        }
        if (reportMailData.getDetailedMissingTestsTable() != null){
            htmlReportStringBuilder.append("<h2>Unexecuted Tests:</h2>");
            htmlReportStringBuilder.append(reportMailData.getDetailedMissingTestsTable());
            htmlReportStringBuilder.append("<br/>");
        }
        if (reportMailData.getDetailedPassedTestsTable() != null) {
            htmlReportStringBuilder.append("<h2>Passed Tests:</h2>");
            htmlReportStringBuilder.append(reportMailData.getDetailedPassedTestsTable());
        }
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
//                "        margin: 10px 10px 10px 10px;\n" +
                "        width: 700px;\n" +
                "        padding: 30px;\n" +
//                "        box-shadow: 0 10px 10px #c7ced0;\n" +
                "        border:1px solid #c7ced0;\n" +
                "    }\n" +
                "    .wrapper {\n" +
                "        background: #e4f0f4;\n" +
                "        padding: 10px;\n" +
//                "        margin: 10px 10px 10px 10px;\n" +
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
}
