package com.yarden.restServiceDemo.slackService;

import com.google.gson.Gson;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.pojos.SlackReportNotificationJson;
import com.yarden.restServiceDemo.pojos.SlackReportData;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class NonTestTableSlackReportSender {

    public void send(String json) throws IOException {
        SlackReportNotificationJson requestJson = new Gson().fromJson(json, SlackReportNotificationJson.class);
        String reportTitle = requestJson.getReportTitle();
        String mailTextPart = requestJson.getMailTextPart();
        String version = requestJson.getVersion();
        String changeLog = requestJson.getChangeLog();
        String testCoverageGap = requestJson.getTestCoverageGap();
        SlackReportData slackReportData = new SlackReportData()
                .setReportTextPart(mailTextPart)
                .setReportTitle(reportTitle)
                .setVersion(version)
                .setChangeLog(changeLog)
                .setCoverageGap(testCoverageGap)
                .setRecipientsJsonArray(new JSONArray()
                        .put(new JSONObject()
                                .put("Email", Enums.EnvVariables.MailReportRecipient.value)
                                .put("Name", "Release_Report")));
        new SlackReporter().report(slackReportData);
    }

}
