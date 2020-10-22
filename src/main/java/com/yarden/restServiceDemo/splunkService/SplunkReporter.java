package com.yarden.restServiceDemo.splunkService;

import com.splunk.*;
import com.yarden.restServiceDemo.Enums;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class SplunkReporter {

    private static Receiver receiver = null;
    private static Service service = null;

    public static void report(Enums.SplunkSourceTypes sourcetype, String json){
        Args args = new Args();
        args.add("sourcetype", sourcetype.value);
        getReceiver().log("qualityevents", args, json);
    }

    @Test
    public void search() throws IOException {
        Job job = getService().getJobs().create("search starttime=\"10/12/2020:15:58:00\" endtime=\"10/22/2020:15:59:50\" data.Info.RequestType=GetUserInfo OR data.Info.RequestType=StartSession OR data.Info.RequestType=MatchExpectedOutputAsSession | rex field=data.Context.RequestUrl \"(?<domain>https://.*\\.applitools.com)\" | stats count by domain data.Site | where NOT LIKE(domain, \"https://test%\") | rename data.Site as site | table domain site | head 1000");
        while (!job.isDone()) {
            try {
                Thread.sleep(500);
            } catch (Throwable t) {}
        }
        CollectionArgs outputArgs = new CollectionArgs();
        outputArgs.setCount(1000);
        outputArgs.put("output_mode", "csv");
        InputStream stream = job.getResults(outputArgs);
        String theString = IOUtils.toString(stream, Charset.defaultCharset());
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

    private static Service getService(){
        if (service == null) {
            ServiceArgs serviceArgs = new ServiceArgs();
            serviceArgs.setHost("applitools.splunkcloud.com");
            serviceArgs.setUsername("yardeningber");
            serviceArgs.setPassword("sderotjer136");
            serviceArgs.setPort(8089);
            serviceArgs.setSSLSecurityProtocol(SSLSecurityProtocol.TLSv1_2);
            service = Service.connect(serviceArgs);
        }
        return service;
    }

    private static Receiver getReceiver(){
        if (receiver == null) {
            return getService().getReceiver();
        }
        return receiver;
    }
}
