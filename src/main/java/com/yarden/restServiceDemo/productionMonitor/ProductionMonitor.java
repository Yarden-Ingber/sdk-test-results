package com.yarden.restServiceDemo.productionMonitor;

import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.Logger;
import com.yarden.restServiceDemo.kpis.WriteKpisToSplunkPeriodically;
import com.yarden.restServiceDemo.reportService.SheetData;
import com.yarden.restServiceDemo.reportService.SheetTabIdentifier;
import com.yarden.restServiceDemo.splunkService.SplunkReporter;
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

    @EventListener(ApplicationReadyEvent.class)
    public static synchronized void start() {
        if (!isRunning) {
            timer = new Timer("ProductionMonitor");
            timer.scheduleAtFixedRate(new WriteKpisToSplunkPeriodically(), 30, 1000 * 60 * 1);
            isRunning = true;
            Logger.info("ProductionMonitor started");
        }
    }

    public static synchronized void stop(){
        isRunning = false;
        timer.cancel();
    }

    @Override
    public void run() {
        try {
            monitor();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void monitor() throws IOException {
        StringBuilder failedVGBrowsers = new StringBuilder("");
        StringBuilder failedEndpoints = new StringBuilder("");
        JSONObject productionMonitorEventJson = new JSONObject();
        if (isVGUp(failedVGBrowsers)) {
            productionMonitorEventJson.put("isVGUp", 1);
        } else {
            productionMonitorEventJson.put("isVGUp", 0);
            productionMonitorEventJson.put("failedBrowsers", failedVGBrowsers);
        }
        if (isEndpointsUp(failedEndpoints)) {
            productionMonitorEventJson.put("isEndpointsUp", 1);
        } else {
            productionMonitorEventJson.put("isEndpointsUp", 0);
            productionMonitorEventJson.put("failedEndpoints", failedEndpoints);
        }
        new SplunkReporter().report(Enums.SplunkSourceTypes.ProductionMonitor, productionMonitorEventJson.toString());
    }

    private boolean isEndpointsUp(StringBuilder failedEndpoints) throws IOException {
        boolean result = true;
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        String endTime = new SimpleDateFormat("MM/dd/yyyy:HH:mm:ss").format(today);
        String startTime = new SimpleDateFormat("MM/dd/yyyy:HH:mm:ss").format(calendar.getTime());
        String query = "search starttime=\"" + startTime + "\" endtime=\"" + endTime + "\" data.Info.RequestType=GetUserInfo OR data.Info.RequestType=StartSession OR data.Info.RequestType=MatchExpectedOutputAsSession | rex field=data.Context.RequestUrl \"(?<domain>https://.*\\.applitools.com)\" | stats count by domain data.Site | where NOT LIKE(domain, \"https://test%\") | rename data.Site as site | table domain site";
        String theString = new SplunkReporter().search(query, "csv", 1000);
        theString = theString.replace("\"", "").replace("domain,site\n", "");
        String[] domainsSitesList = theString.split("\n");
        for (String domainSite : domainsSitesList) {
            String domain = domainSite.split(",")[0];
            String site = domainSite.split(",")[1];
            domain = domain + "/api/admin/userinfo";
            URL endpoint = new URL(domain);
            HttpURLConnection con = (HttpURLConnection) endpoint.openConnection();
            con.setRequestMethod("GET");
            try {
                int responseStatusCode = con.getResponseCode();
                if (responseStatusCode != 200 && responseStatusCode != 403) {
                    result = false;
                    failedEndpoints.append(domain).append("-").append(con.getResponseCode()).append(";");
                }
            } catch (Throwable t) {
                result = false;
                failedEndpoints.append(domain);
            }
        }
        return result;
    }

    private boolean isVGUp(StringBuilder failedVGBrowsers){
        SheetData vgStatusSheet = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.VisualGrid.value, Enums.VisualGridSheetTabsNames.Status.value));
        vgStatusSheet.getSheetData();
        int passedBrowsers = 0;
        int failedBrowsers = 0;
        for (String browser : vgStatusSheet.getColumnNames()) {
            if (vgStatusSheet.getSheetData().get(0).getAsJsonObject().get(browser).getAsString().equals(Enums.TestResults.Passed.value)) {
                passedBrowsers++;
            } else if (vgStatusSheet.getSheetData().get(0).getAsJsonObject().get(browser).getAsString().equals(Enums.TestResults.Passed.value)) {
                failedVGBrowsers.append(browser);
                failedBrowsers++;
            }
        }
        return passedBrowsers >= failedBrowsers;
    }

}
