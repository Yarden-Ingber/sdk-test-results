package com.yarden.restServiceDemo.reportService;

import com.yarden.restServiceDemo.Logger;
import com.yarden.restServiceDemo.RestCalls;

import java.util.Timer;
import java.util.TimerTask;

public class WriteEntireSheetsPeriodically extends TimerTask{
    public static boolean shouldStopSheetWritingTimer = false;
    private static boolean isRunning = false;
    private static Timer timer;

    public static  synchronized void start() {
        if (!isRunning) {
            timer = new Timer("WriteEntireSheetsPeriodically");
            timer.scheduleAtFixedRate(new WriteEntireSheetsPeriodically(), 30, 1000 * 60 * 3);
            isRunning = true;
        }
    }

    public static synchronized void stop(){
        isRunning = false;
        timer.cancel();
    }

    @Override
    public void run() {
        try {
            synchronized (RestCalls.lock) {
                Logger.info("WriteEntireSheetsPeriodically saying: \"tick...\"");
                try {
                    SheetData.writeAllTabsToSheet();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                if (shouldStopSheetWritingTimer) {
                    Logger.info("Timer timeout");
                    stop();
                    return;
                }
                shouldStopSheetWritingTimer = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}