package com.yarden.restServiceDemo.splunkService;

import com.splunk.*;
import com.yarden.restServiceDemo.Enums;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class SplunkReporter {

    private static Receiver receiver = null;
    private static Service service = null;
    private static AtomicReference<LinkedList<SplunkReportObject>> jsons = new AtomicReference<>();
    ExecutorService executor = Executors.newFixedThreadPool(1);
    private static Runnable runnableTask = null;
    private static final String lock = "lock";
    private static AtomicReference<Boolean> isRunnableRunning = new AtomicReference<>();

    public SplunkReporter() {
        synchronized (lock) {
            if (runnableTask == null) {
                runnableTask = () -> {
                    while (true) {
                        try {
                            Thread.sleep(500);
                            sendReportAsync();
                        } catch (Throwable t) {
                        }
                    }
                };
            }
            if (isRunnableRunning.get() == null) {
                isRunnableRunning.set(false);
            }
            if (jsons.get() == null) {
                jsons.set(new LinkedList<>());
            }
        }
    }

    public void report(Enums.SplunkSourceTypes sourcetype, String json){
        SplunkReportObject splunkReportObject = new SplunkReportObject(sourcetype, json);
        jsons.get().add(splunkReportObject);
        synchronized (lock) {
            if (!isRunnableRunning.get()) {
                executor.execute(runnableTask);
                isRunnableRunning.set(true);
            }
        }
    }

    private void sendReportAsync() {
        if (!jsons.get().isEmpty()) {
            SplunkReportObject reportObject = jsons.get().removeFirst();
            Args args = new Args();
            args.add("sourcetype", reportObject.sourcetype.value);
            try {
                getReceiver().log("qualityevents", args, reportObject.json);
            } catch (Throwable t) {
                System.out.println("Retrying splunk log");
                try {
                    resetSplunkConnection();
                    getReceiver().log("qualityevents", args, reportObject.json);
                } catch (Throwable t2) {
                    System.out.println("Failed logging to splunk: " + reportObject.json);
                }
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

    public class SplunkReportObject {
        Enums.SplunkSourceTypes sourcetype;
        String json;

        public SplunkReportObject(Enums.SplunkSourceTypes sourcetype, String json) {
            this.sourcetype = sourcetype;
            this.json = json;
        }
    }
}
