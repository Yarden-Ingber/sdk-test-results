package com.yarden.restServiceDemo.mailService;

import com.google.gson.Gson;
import com.lowagie.text.DocumentException;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.yarden.restServiceDemo.pojos.EmailNotificationJson;
import com.yarden.restServiceDemo.pojos.ReportMailData;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class NonTestTableMailSender {

    public void send(String json) throws InterruptedException, DocumentException, IOException, MailjetSocketTimeoutException, MailjetException {
        EmailNotificationJson requestJson = new Gson().fromJson(json, EmailNotificationJson.class);
        String reportTitle = requestJson.getReportTitle();
        String mailTextPart = requestJson.getMailTextPart();
        String version = requestJson.getVersion();
        String changeLog = requestJson.getChangeLog();
        String testCoverageGap = requestJson.getTestCoverageGap();
        ReportMailData reportMailData = new ReportMailData()
                .setMailTextPart(mailTextPart)
                .setReportTitle(reportTitle)
                .setVersion(version)
                .setChangeLog(changeLog)
                .setCoverageGap(testCoverageGap)
                .setRecipientsJsonArray(new JSONArray()
                        .put(new JSONObject()
                                .put("Email", "release.reports@applitools.com")
                                .put("Name", "Release_Report")));
        new MailSender().send(reportMailData);
    }

}
