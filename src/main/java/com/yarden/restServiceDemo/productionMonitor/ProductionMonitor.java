package com.yarden.restServiceDemo.productionMonitor;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.resource.Emailv31;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.Logger;
import com.yarden.restServiceDemo.reportService.SheetData;
import com.yarden.restServiceDemo.reportService.SheetTabIdentifier;
import com.yarden.restServiceDemo.splunkService.SplunkReporter;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

@Configuration
public class ProductionMonitor extends TimerTask {

    private static boolean isRunning = false;
    private static Timer timer;
    private static final String VERSION = "4";

    @EventListener(ApplicationReadyEvent.class)
    public static synchronized void start() {
        if (!isRunning) {
            timer = new Timer("ProductionMonitor");
            timer.scheduleAtFixedRate(new ProductionMonitor(), 30, 1000 * 60 * 10);
            isRunning = true;
            Logger.info("ProductionMonitor started");
        }
    }

    @Override
    public void run() {
        try {
            Logger.info("ProductionMonitor: Starting monitor");
            monitor();
            Logger.info("ProductionMonitor: Monitor ended");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void monitor() throws IOException, MailjetSocketTimeoutException, MailjetException {
        try {
            Logger.info("ProductionMonitor: Reporting Eyes endpoints");
            sendEyesEndpointsEvents();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        try {
            Logger.info("ProductionMonitor: Reporting VG status");
            sendVGEvent();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void sendEyesEndpointsEvents() throws IOException, MailjetSocketTimeoutException, MailjetException {
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        String endTime = new SimpleDateFormat("MM/dd/yyyy:HH:mm:ss").format(today);
        String startTime = new SimpleDateFormat("MM/dd/yyyy:HH:mm:ss").format(calendar.getTime());
        String query = "search starttime=\"" + startTime + "\" endtime=\"" + endTime + "\" data.Info.RequestType=GetUserInfo OR data.Info.RequestType=StartSession OR data.Info.RequestType=MatchExpectedOutputAsSession | rex field=data.Context.RequestUrl \"(?<domain>https://.*\\.applitools.com)\" | stats count by domain data.Site | where NOT LIKE(domain, \"https://test%\") | rename data.Site as site | table domain site";
        String theString = new SplunkReporter().search(query, "csv", 1000);
        new SplunkReporter().report(Enums.SplunkSourceTypes.ProductionMonitor, new JSONObject().put("eventType", "log").put("value", theString).put("domainsCount", StringUtils.countMatches(theString, "://")).toString());
        theString = theString.replace("\"", "").replace("domain,site\n", "");
        String[] domainsSitesList = theString.split("\n");
        StringBuilder failedEndpoints = new StringBuilder("");
        for (String domainSite : domainsSitesList) {
            String domain = domainSite.split(",")[0];
            String site = domainSite.split(",")[1];
            domain = domain + "/api/admin/userinfo";
            URL endpoint = new URL(domain);
            HttpURLConnection con = (HttpURLConnection) endpoint.openConnection();
            con.setConnectTimeout(1000 * 60 * 2);
            con.setRequestMethod("GET");
            JSONObject productionMonitorEventJson = new JSONObject();
            productionMonitorEventJson.put("version", VERSION);
            productionMonitorEventJson.put("site", site);
            productionMonitorEventJson.put("domain", domain);
            productionMonitorEventJson.put("eventType", "Endpoint");
            try {
                int responseStatusCode = 0;
                try {
                    responseStatusCode = con.getResponseCode();
                } catch (Throwable t) {
                    try {
                        responseStatusCode = con.getResponseCode();
                    } catch (Throwable t2) {
                        responseStatusCode = con.getResponseCode();
                    }
                }
                if (responseStatusCode == 200 || responseStatusCode == 403) {
                    productionMonitorEventJson.put("isUp", 1);
                } else {
                    Logger.error("ProductionMonitor: Status code for site " + site + " is: " + responseStatusCode);
                    productionMonitorEventJson.put("isUp", 0);
                    failedEndpoints.append(site).append(";");
                    productionMonitorEventJson.put("statusCode", responseStatusCode);
                }
            } catch (Throwable t) {
                Logger.error("ProductionMonitor: failed to get response from endpoint " + domain);
                t.printStackTrace();
                productionMonitorEventJson.put("isUp", 0);
                failedEndpoints.append(site).append(";");
            }
            new SplunkReporter().report(Enums.SplunkSourceTypes.ProductionMonitor, productionMonitorEventJson.toString());
        }
        if (!failedEndpoints.toString().isEmpty()) {
            sendMailNotification(failedEndpoints.toString());
        }
    }

    private void sendVGEvent(){
        SheetData vgStatusSheet = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.VisualGrid.value, Enums.VisualGridSheetTabsNames.Status.value));
        vgStatusSheet.getSheetData();
        for (String browser : vgStatusSheet.getColumnNames()) {
            JSONObject productionMonitorEventJson = new JSONObject();
            productionMonitorEventJson.put("version", VERSION);
            productionMonitorEventJson.put("eventType", "VG");
            productionMonitorEventJson.put("Browser", browser);
            if (vgStatusSheet.getSheetData().get(0).getAsJsonObject().get(browser).getAsString().equals(Enums.TestResults.Passed.value)) {
                productionMonitorEventJson.put("isUp", 1);
            } else {
                productionMonitorEventJson.put("isUp", 0);
            }
            new SplunkReporter().report(Enums.SplunkSourceTypes.ProductionMonitor, productionMonitorEventJson.toString());
        }
    }

    private void sendMailNotification(String endpoint) throws MailjetSocketTimeoutException, MailjetException {
        JSONArray recipient = new JSONArray().put(new JSONObject().put("Email", "eyesops@applitools.com").put("Name", "Production_monitor"));
        sendMailNotification(recipient, "Production monitor alert", "The GET request for endpoints: " + endpoint + " failed");
    }

    private void sendMailNotification(JSONArray recipient, String subject, String content) throws MailjetSocketTimeoutException, MailjetException {
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
                                .put(Emailv31.Message.TO, recipient)
                                .put(Emailv31.Message.SUBJECT, subject)
                                .put(Emailv31.Message.TEXTPART, content)
                                .put(Emailv31.Message.CUSTOMID, "SdkRelease")));
        response = client.post(request);
        Logger.info(Integer.toString(response.getStatus()));
        Logger.info(response.getData().toString());
    }

}
