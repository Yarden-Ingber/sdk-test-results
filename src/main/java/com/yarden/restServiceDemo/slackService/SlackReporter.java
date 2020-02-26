package com.yarden.restServiceDemo.slackService;

import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.HtmlReportGenerator;
import com.yarden.restServiceDemo.pojos.SlackReportData;
import com.yarden.restServiceDemo.pojos.SdkReportMessage;

import java.io.IOException;

public class SlackReporter {

    public void report(SlackReportData slackReportData) throws IOException {
        SdkReportMessage message = new SdkReportMessage(slackReportData.getReportTextPart() + "\n\nHTML Report:\n" + slackReportData.getHtmlReportUrl());
        SlackRetrofitClient.getAPIService().sendReport(Enums.EnvVariables.SlackSdkReleaseChannelEndpoint.value, message).execute();
    }

}
