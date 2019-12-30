package com.yarden.restServiceDemo.reportService;

import com.yarden.restServiceDemo.Logger;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class WriteEntireSheetsPeriodically extends TimerTask{
    public static boolean shouldClearSheets = false;
    private static boolean isRunning = false;
    private static Timer timer;

    public static void start() {
        if (!isRunning) {
            timer = new Timer("MyTimer");
            timer.scheduleAtFixedRate(new WriteEntireSheetsPeriodically(), 30, 5 * 1000 * 60);
            isRunning = true;
        }
    }

    public static void stop(){
        timer.cancel();
        isRunning = false;
    }

    @Override
    public void run() {
        if (shouldClearSheets) {
            try {
                Logger.info("Timer timeout. writing all sheets to google.");
                SheetData.writeAllTabsToSheet();
            } catch (IOException e) {
                e.printStackTrace();
            }
            cancel();
            return;
        }
        shouldClearSheets = true;
    }



}