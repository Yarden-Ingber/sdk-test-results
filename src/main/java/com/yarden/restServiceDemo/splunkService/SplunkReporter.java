package com.yarden.restServiceDemo.splunkService;

import com.splunk.*;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.Logger;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SplunkReporter {

    private static Receiver receiver = null;
    private static Service service = null;

    public void report(Enums.SplunkSourceTypes sourcetype, String json){
        Args args = new Args();
        args.add("sourcetype", sourcetype.value);
        try {
            getReceiver().log("qualityevents", args, json);
        } catch (Throwable t) {
            Logger.warn("Retrying splunk lof");
            try {
                resetSplunkConnection();
                getReceiver().log("qualityevents", args, json);
            } catch (Throwable t2) {
                Logger.error("Failed logging to splunk: " + json);
            }
        }
    }

    public String search(String query, String outputMode, int resultsCount) {
        Job job = null;
        try {
            job = getService().getJobs().create(query);
        } catch (Exception e) {
            resetSplunkConnection();
            job = getService().getJobs().create(query);
        }
        while (!job.isDone()) {
            try {
                Thread.sleep(500);
            } catch (Throwable t) {}
        }
        CollectionArgs outputArgs = new CollectionArgs();
        outputArgs.setCount(resultsCount);
        outputArgs.put("output_mode", outputMode);
        InputStream stream = job.getResults(outputArgs);
        try {
            return IOUtils.toString(stream, Charset.defaultCharset());
        } catch (IOException e) {
            return "";
        }
    }

    @Test
    public void searchTest() throws IOException {
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        String endTime = new SimpleDateFormat("MM/dd/yyyy:HH:mm:ss").format(today);
        String startTime = new SimpleDateFormat("MM/dd/yyyy:HH:mm:ss").format(calendar.getTime());
        String query = "search starttime=\"" + startTime + "\" endtime=\"" + endTime + "\" data.Info.RequestType=GetUserInfo OR data.Info.RequestType=StartSession OR data.Info.RequestType=MatchExpectedOutputAsSession | rex field=data.Context.RequestUrl \"(?<domain>https://.*\\.applitools.com)\" | stats count by domain data.Site | where NOT LIKE(domain, \"https://test%\") | rename data.Site as site | table domain site";
        String theString = search(query, "csv", 1000);
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
                if (responseStatusCode == 200 || responseStatusCode == 403) {
                    System.out.println("pass " + domain);
                } else {
                    System.out.println("error " + con.getResponseCode() + " " + domain);
                }
            } catch (Throwable t) {
                System.out.println("error " + domain);
            }
        }
    }

    private void resetSplunkConnection(){
        receiver = null;
        service = null;
        System.gc();
    }

    private Service getService(){
        if (service == null) {
            ServiceArgs serviceArgs = new ServiceArgs();
            serviceArgs.setHost("applitools.splunkcloud.com");
            serviceArgs.setUsername(Enums.EnvVariables.SplunkUsername.value);
            serviceArgs.setPassword(Enums.EnvVariables.SplunkPassword.value);
            serviceArgs.setPort(8089);
            serviceArgs.setSSLSecurityProtocol(SSLSecurityProtocol.TLSv1_2);
            service = Service.connect(serviceArgs);
        }
        return service;
    }

    private Receiver getReceiver(){
        if (receiver == null) {
            return getService().getReceiver();
        }
        return receiver;
    }
}
