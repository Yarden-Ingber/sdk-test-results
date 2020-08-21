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
        SlackReportData slackReportData = new SlackReportData()
                .setReportTextPart(requestJson.getMailTextPart())
                .setReportTitle(requestJson.getReportTitle())
                .setSdk(requestJson.getSdk())
                .setVersion(requestJson.getVersion())
                .setChangeLog(requestJson.getChangeLog())
                .setCoverageGap(requestJson.getTestCoverageGap())
                .setRecipientsJsonArray(new JSONArray()
                        .put(new JSONObject()
                                .put("Email", Enums.EnvVariables.MailReportRecipient.value)
                                .put("Name", "Release_Report")));
        new SlackReporter().report(slackReportData);
    }

}
