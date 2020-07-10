package com.yarden.restServiceDemo.reportService;

import com.yarden.restServiceDemo.Logger;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class WriteEntireSheetsPeriodically extends TimerTask{
    public static boolean shouldClearSheets = false;
    private static boolean isRunning = false;
    private static Timer timer;

    public static  synchronized void start() {
        if (!isRunning) {
            timer = new Timer("WriteEntireSheetsPeriodically");
            timer.scheduleAtFixedRate(new WriteEntireSheetsPeriodically(), 30, 3 * 1000 * 60);
            isRunning = true;
        }
    }

    public static synchronized void stop(){
        isRunning = false;
        timer.cancel();
    }

    @Override
    public void run() {
        Logger.info("WriteEntireSheetsPeriodically saying: \"tick...\"");
        if (shouldClearSheets) {
            try {
                Logger.info("Timer timeout. writing all sheets to google.");
                SheetData.writeAllTabsToSheet();
            } catch (IOException e) {
                e.printStackTrace();
            }
            stop();
            return;
        }
        shouldClearSheets = true;
    }



}