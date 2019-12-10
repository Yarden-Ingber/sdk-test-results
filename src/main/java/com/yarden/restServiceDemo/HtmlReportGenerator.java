package com.yarden.restServiceDemo;

import com.lowagie.text.DocumentException;
import com.mailjet.client.Base64;
import com.yarden.restServiceDemo.awsS3Service.AwsS3Provider;
import com.yarden.restServiceDemo.pojos.ReportMailData;
import org.apache.commons.io.IOUtils;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;

public class HtmlReportGenerator {

    StringBuilder htmlReportStringBuilder = new StringBuilder();
    ReportMailData reportMailData;
    private final String htmlReportFileName = "test_report.html";
    private final String pdfReportFileName = "test_report.pdf";
    private final String sdkHtmlReportFileName = "sdk_report";

    public HtmlReportGenerator(ReportMailData reportMailData){
        this.reportMailData = reportMailData;
    }

    public String getHtmlReportUrlInAwsS3() throws FileNotFoundException, UnsupportedEncodingException {
        generateHtmlReportFile();
        String fileUrl = AwsS3Provider.uploadFileToS3(sdkHtmlReportFileName + "_" + Logger.getTimaStamp(), htmlReportFileName);
        try {
            new File(htmlReportFileName).delete();
        } catch (Throwable t) {t.printStackTrace();}
        return fileUrl;
    }

    public String getPdfReportAsBase64() throws IOException, DocumentException {
        generateHtmlReportFile();
        convertHtmlToPdfFile();
        String result = Base64.encode(IOUtils.toByteArray(new FileInputStream(pdfReportFileName)));
        try {
            new File(htmlReportFileName).delete();
            new File(pdfReportFileName).delete();
        } catch (Throwable t) {t.printStackTrace();}
        return result;
    }

    private void generateHtmlReportFile() throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(htmlReportFileName, "UTF-8");
        writer.println(getHtmlReportAsPlainSting());
        writer.close();
    }

    private void convertHtmlToPdfFile() throws IOException, DocumentException{
        String inputFile = htmlReportFileName;
        String url = new File(inputFile).toURI().toURL().toString();
        String outputFile = pdfReportFileName;
        OutputStream os = new FileOutputStream(outputFile);
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocument(url);
        renderer.layout();
        renderer.createPDF(os);
        os.close();
    }

    private String getHtmlReportAsPlainSting(){
        htmlReportStringBuilder.append("<!DOCTYPE html><html><head>");
        htmlReportStringBuilder.append(getCSS());
        htmlReportStringBuilder.append("</head><body><div class=\"wrapper\">\n" +
                "    <div class=\"content\">\n" +
                "        <div class=\"header\">applitools</div>");
        htmlReportStringBuilder.append("<h2>" + reportMailData.getReportTitle() + "</h2>");
        htmlReportStringBuilder.append("<h2>Version: " + reportMailData.getVersion() + "</h2><br/>");
        if (reportMailData.getChangeLog() != null && !reportMailData.getChangeLog().isEmpty()) {
            htmlReportStringBuilder.append("<h2>Change log:</h2>");
            htmlReportStringBuilder.append(reportMailData.getChangeLog() + "<br/>");
            htmlReportStringBuilder.append("<br/>");
        }
        if (reportMailData.getHighLevelReportTable() != null) {
            htmlReportStringBuilder.append(reportMailData.getHighLevelReportTable());
        }
        if (reportMailData.getCoverageGap() != null && !reportMailData.getCoverageGap().isEmpty()) {
            htmlReportStringBuilder.append("<br/><h2>Test coverage gap:</h2>");
            htmlReportStringBuilder.append(reportMailData.getCoverageGap() + "<br/><br/>");
        }
        if (reportMailData.getDetailedMissingTestsTable() != null){
            htmlReportStringBuilder.append("<details><summary><b>Unexecuted Tests:</b></summary>");
            htmlReportStringBuilder.append(reportMailData.getDetailedMissingTestsTable());
            htmlReportStringBuilder.append("</details><br/>");
        }
        if (reportMailData.getDetailedPassedTestsTable() != null) {
            htmlReportStringBuilder.append("<details><summary><b>Passed Tests:</b></summary>");
            htmlReportStringBuilder.append(reportMailData.getDetailedPassedTestsTable());
            htmlReportStringBuilder.append("</details>");
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
                "        margin: 40px auto;\n" +
                "        width: 65%;\n" +
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
                "        width: 70%;\n" +
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
