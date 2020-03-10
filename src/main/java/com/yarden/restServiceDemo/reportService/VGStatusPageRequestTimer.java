package com.yarden.restServiceDemo.reportService;

import com.yarden.restServiceDemo.Logger;
import com.yarden.restServiceDemo.RestCalls;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class VGStatusPageRequestTimer extends TimerTask {

    public static boolean isRequestReceived;
    private static boolean isRunning = false;
    private static Timer timer;

    public static synchronized void start() {
        if (!isRunning) {
            isRequestReceived = true;
            timer = new Timer("MyTimer");
            timer.scheduleAtFixedRate(new WriteEntireSheetsPeriodically(), 30, 12 * 1000 * 60);
            isRunning = true;
            Logger.info("VGStatusPageRequestTimer started");
        }
    }

    @Override
    public void run() {
        Logger.info("VGStatusPageRequestTimer saying: \"tick...\"");
        if (!isRequestReceived) {
            Logger.info("VGStatusPageRequestTimer timeout. adding empty row to VG status page.");
            String json = "{\"status\": []}";
            try {
                new VisualGridStatusPageService().postResults(json);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        isRequestReceived = false;
    }

}
