package com.yarden.restServiceDemo.kpis;

import com.google.gson.JsonElement;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.Logger;
import com.yarden.restServiceDemo.RestCalls;
import com.yarden.restServiceDemo.reportService.SheetData;
import com.yarden.restServiceDemo.reportService.SheetTabIdentifier;
import org.junit.Test;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

@Configuration
public class WriteKpisToSplunkPeriodically extends TimerTask{
    private static boolean isRunning = false;
    private static Timer timer;
    private static boolean isKpisDumped = false;

    @EventListener(ApplicationReadyEvent.class)
    public static synchronized void start() {
        if (!isRunning) {
            timer = new Timer("WriteKpisToSplunkPeriodically");
            timer.scheduleAtFixedRate(new WriteKpisToSplunkPeriodically(), 30, 1000 * 60 * 1);
            isRunning = true;
            Logger.info("WriteKpisToSplunkPeriodically started");
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
                try {
                    if (shouldDumpTickets()) {
                        periodicDumpTickets();
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean shouldDumpTickets() throws ParseException {
        Date date = Logger.timestampToDate(Logger.getTimaStamp());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        if (hour == 23 && minute > 50) {
            Logger.info("WriteKpisToSplunkPeriodically: Dump window is open. Don't restart server!!!!!!!!!!!!!!");
            if (!isKpisDumped) {
                isKpisDumped = true;
                return true;
            }
        }
        if (hour < 23) {
            isKpisDumped = false;
        }
        return false;
    }

    private void periodicDumpTickets() {
        TicketsStateChanger ticketsStateChanger = new TicketsStateChanger();
        String timeStamp = Logger.getTimaStamp();
        SheetData rawDataSheetData = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.KPIS.value, Enums.KPIsSheetTabsNames.RawData.value));
        for (JsonElement sheetEntry: rawDataSheetData.getSheetData()){
            TicketStates currentState = TicketStates.valueOf(sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.CurrentState.value).getAsString());
            if (!(currentState.equals(TicketStates.Done) || currentState.equals(TicketStates.NoState))) {
                try {
                    ticketsStateChanger.addCalculatedTimeInPreviousState(timeStamp, sheetEntry, currentState);
                    ticketsStateChanger.writeNewStateTimestamp(timeStamp, sheetEntry, currentState);
                } catch (Throwable t) {}
            }
        }
        for (JsonElement sheetEntry: rawDataSheetData.getSheetData()){
            TicketUpdateRequest ticketUpdateRequest = new TicketUpdateRequest();
            ticketUpdateRequest.setTeam(sheetEntry.getAsJsonObject().get("Team").getAsString());
            new KpiSplunkReporter(rawDataSheetData, ticketUpdateRequest).reportLatestState(sheetEntry);
        }
    }

}