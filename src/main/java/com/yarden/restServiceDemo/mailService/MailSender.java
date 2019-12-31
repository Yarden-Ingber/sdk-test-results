package com.yarden.restServiceDemo.mailService;
import com.lowagie.text.DocumentException;
import com.mailjet.client.*;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.resource.Emailv31;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.HtmlReportGenerator;
import com.yarden.restServiceDemo.pojos.ReportMailData;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;

public class MailSender {

    ReportMailData reportMailData;

    public void send(ReportMailData reportMailData) throws InterruptedException, DocumentException, IOException, MailjetSocketTimeoutException, MailjetException {
        this.reportMailData = reportMailData;
        MailjetClient client;
        MailjetRequest request;
        MailjetResponse response;
        client = new MailjetClient(Enums.EnvVariables.MailjetApiKeyPublic.value, Enums.EnvVariables.MailjetApiKeyPrivate.value, new ClientOptions("v3.1"));
        request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, new JSONObject()
                                        .put("Email", "yarden.ingber@applitools.com")
                                        .put("Name", "Yarden Ingber"))
                                .put(Emailv31.Message.TO, reportMailData.getRecipientsJsonArray())
                                .put(Emailv31.Message.SUBJECT, "SDK Release")
                                .put(Emailv31.Message.TEXTPART,
                                        reportMailData.getMailTextPart() + "\n\nHTML Report:\n" + new HtmlReportGenerator(reportMailData).getHtmlReportUrlInAwsS3(reportMailData.getHtmlReportS3BucketName()))
                                .put(Emailv31.Message.CUSTOMID, "SdkRelease")));
        response = client.post(request);
        System.out.println(response.getStatus());
        System.out.println(response.getData());
    }

    private String getPdfReportAsBase64() throws IOException, DocumentException {
        final String htmlReportFileName = "test_report.html";
        final String pdfReportFileName = "test_report.pdf";
        PrintWriter writer = new PrintWriter(htmlReportFileName, "UTF-8");
        try {
//            writer.println(getHtmlReportAsPlainSting());
        } finally {
            writer.close();
        }
        String inputFile = htmlReportFileName;
        String url = new File(inputFile).toURI().toURL().toString();
        String outputFile = pdfReportFileName;
        OutputStream os = new FileOutputStream(outputFile);
        try {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocument(url);
            renderer.layout();
            renderer.createPDF(os);
        } finally {
            os.close();
        }
        String result = Base64.encode(IOUtils.toByteArray(new FileInputStream(pdfReportFileName)));
        try {
            new File(htmlReportFileName).delete();
            new File(pdfReportFileName).delete();
        } catch (Throwable t) {t.printStackTrace();}
        return result;
    }
}
