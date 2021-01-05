package com.yarden.restServiceDemo.splunkService;

import com.splunk.*;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.Logger;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class SplunkReporter {

    private static Receiver receiver = null;
    private static Service service = null;

    public void report(Enums.SplunkSourceTypes sourcetype, String json){
        Args args = new Args();
        args.add("sourcetype", sourcetype.value);
        try {
            getReceiver().log("qualityevents", args, json);
        } catch (Throwable t) {
            Logger.warn("Retrying splunk log");
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
