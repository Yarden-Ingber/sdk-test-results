package com.yarden.restServiceDemo;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.lowagie.text.DocumentException;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.yarden.restServiceDemo.pojos.EmailNotificationJson;
import com.yarden.restServiceDemo.pojos.ReportMailData;

import java.io.IOException;

public class NonTestTableMailSender {

    public void send(String json) throws InterruptedException, DocumentException, IOException, MailjetSocketTimeoutException, MailjetException {
        EmailNotificationJson requestJson = new Gson().fromJson(json, EmailNotificationJson.class);
        String reportTitle = requestJson.getReportTitle();
        String mailTextPart = requestJson.getMailTextPart();
        String version = requestJson.getVersion().replaceAll("[^\\d.]", "");
        String changeLog = requestJson.getChangeLog();
        String testCoverageGap = requestJson.getTestCoverageGap();
        ReportMailData reportMailData = new ReportMailData()
                .setMailTextPart(mailTextPart)
                .setReportTitle(reportTitle)
                .setVersion("Version: " + version)
                .setChangeLog("Change log:<br/>" + changeLog.replace("\n", "<br/>"))
                .setCoverageGap("Test coverage gap:<br/><br/>" + testCoverageGap.replace("\n", "<br/>"))
                .setShouldAddCoverageGap(true);
        new MailSender().send(reportMailData);
    }

}
