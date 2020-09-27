package com.yarden.restServiceDemo.kpis.kpiCalculators;

import com.google.gson.JsonElement;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.reportService.SheetData;
import com.yarden.restServiceDemo.reportService.SheetTabIdentifier;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class KpiCalculator {

    private SheetData rawSheetData = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.KPIS.value, Enums.KPIsSheetTabsNames.RawData.value));
    private SheetData kpiSheetData = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.KPIS.value, Enums.KPIsSheetTabsNames.KPIs.value));
    private KpiSheetEntries kpiSheetEntries = new KpiSheetEntries();

    public void calculate(){
        for (JsonElement sheetEntry: rawSheetData.getSheetData()){
            for (KpisSheetEntryObject kpisSheetEntryObject : kpiSheetEntries.getEntries().values()) {
                if (sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.Team.value).getAsString().equals(kpisSheetEntryObject.team)
                    && sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.SubProject.value).getAsString().equals(kpisSheetEntryObject.subProject)) {
                    addCountOfOpenTicket(kpisSheetEntryObject, sheetEntry);
                    addToNumberOfTicketCreatedLastWeek(kpisSheetEntryObject, sheetEntry);
                    addToNumberOfBugsCreatedLastWeek(kpisSheetEntryObject, sheetEntry);
                    addToNumberOfTicketsMovedToDoneLastWeek(kpisSheetEntryObject, sheetEntry);
                    addToNumberOfBugsMovedToDoneLastWeek(kpisSheetEntryObject, sheetEntry);
                    addNumberOfTicketsMovedToMissingInformationLastWeek(kpisSheetEntryObject, sheetEntry);
                    addNumberOfTicketsMovedToWaitingForFieldInputLastWeek(kpisSheetEntryObject, sheetEntry);
                    addNumberOfBugsMovedBackFromWaitingForFieldApprovalToWIPLastWeek(kpisSheetEntryObject, sheetEntry);
                    addTicketsTimeFromNewToDone(kpisSheetEntryObject, sheetEntry);
                    addBugsTimeFromNewToDone(kpisSheetEntryObject, sheetEntry);
                    addBugsTimeFromStartedInvestigationToReproduced(kpisSheetEntryObject, sheetEntry);
                }
            }
        }
    }

    public void dumpKpisToSheet(){
        for (KpisSheetEntryObject kpisSheetEntryObject : kpiSheetEntries.getEntries().values()) {
            Iterator iterator = kpisSheetEntryObject.kpisList.entrySet().iterator();
            JsonElementBuilder sheetEntryBuilder = new JsonElementBuilder();
            sheetEntryBuilder.addKeyValue(KpisColumns.Team.value, kpisSheetEntryObject.team);
            sheetEntryBuilder.addKeyValue(KpisColumns.SubProject.value, kpisSheetEntryObject.subProject);
            while (iterator.hasNext()) {
                Map.Entry mapEntry = (Map.Entry)iterator.next();
                sheetEntryBuilder.addKeyValue(((KpisColumns)mapEntry.getKey()).value, mapEntry.getValue().toString());
            }
            kpiSheetData.getSheetData().add(sheetEntryBuilder.buildJsonElement());
        }
    }

    public enum KpisColumns {
        Team("Team"), SubProject("Sub project"), OpenTickets("Open tickets"), NumberOfTicketsCreatedLastWeek("Number of tickets created last week"),
        NumberOfBugsCreatedLastWeek("Number of bugs created last week"), NumberOfTicketsMovedToDoneLastWeek("Number of tickets moved to done last week"),
        NumberOfBugsMovedToDoneLastWeek("Number of bugs moved to done last week"), TicketsTimeFromNewToDone("Tickets time from new to done"),
        BugsTimeFromNewToDone("Bugs time from new to done"), BugsTimeFromStartedInvestigationToReproduced("Bugs time from started investigation to reproduced"),
        NumberOfTicketsMovedToMissingInformationLastWeek("Number of tickets moved to Missing information last week"),
        NumberOfTicketsMovedToWaitingForFieldInputLastWeek("Number of tickets moved to Waiting for field input last week"),
        NumberOfBugsMovedBackFromWaitingForFieldApprovalToWIPLastWeek("Number of bugs moved back from Waiting for field approval to WIP last week"),
        AverageTimeBugsWaitInWaitingForFieldApprovalLastWeek("Average time bugs wait in Waiting for field approval last week");

        public final String value;

        KpisColumns(String value){
            this.value = value;
        }
    }

    private boolean isTimestampInLastWeek(String timestamp) throws ParseException {
        Date date = timestampToDate(timestamp);
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        return calendar.getTime().before(date);
    }

    private Date timestampToDate(String timestamp) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        return dateFormat.parse(timestamp);
    }

    private void addCountOfOpenTicket(KpisSheetEntryObject kpisSheetEntryObject, JsonElement sheetEntry) {
        if(sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.MovedToDoneDate.value).getAsString().isEmpty()) {
            addOneToTicketsCount(kpisSheetEntryObject, KpisColumns.OpenTickets);
        }
    }

    private void addToNumberOfTicketCreatedLastWeek(KpisSheetEntryObject kpisSheetEntryObject, JsonElement sheetEntry) {
        String ticketDate = sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.CreationDate.value).getAsString();
        if (ticketDate.isEmpty()) {
            return;
        }
        try {
            if (isTimestampInLastWeek(ticketDate)) {
                addOneToTicketsCount(kpisSheetEntryObject, KpisColumns.NumberOfTicketsCreatedLastWeek);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void addToNumberOfBugsCreatedLastWeek(KpisSheetEntryObject kpisSheetEntryObject, JsonElement sheetEntry) {
        if (!isBug(sheetEntry)) {
            return;
        }
        String creationDate = sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.CreationDate.value).getAsString();
        if (creationDate.isEmpty()) {
            return;
        }
        try {
            if (isTimestampInLastWeek(creationDate)) {
                addOneToTicketsCount(kpisSheetEntryObject, KpisColumns.NumberOfBugsCreatedLastWeek);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void addToNumberOfTicketsMovedToDoneLastWeek(KpisSheetEntryObject kpisSheetEntryObject, JsonElement sheetEntry){
        String movedToDoneDate = sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.MovedToDoneDate.value).getAsString();
        if (movedToDoneDate.isEmpty()) {
            return;
        }
        try {
            if (isTimestampInLastWeek(movedToDoneDate)) {
                addOneToTicketsCount(kpisSheetEntryObject, KpisColumns.NumberOfTicketsMovedToDoneLastWeek);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void addToNumberOfBugsMovedToDoneLastWeek(KpisSheetEntryObject kpisSheetEntryObject, JsonElement sheetEntry){
        if (!isBug(sheetEntry)) {
            return;
        }
        String movedToDoneDate = sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.MovedToDoneDate.value).getAsString();
        if (movedToDoneDate.isEmpty()) {
            return;
        }
        try {
            if (isTimestampInLastWeek(movedToDoneDate)) {
                addOneToTicketsCount(kpisSheetEntryObject, KpisColumns.NumberOfBugsMovedToDoneLastWeek);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void addNumberOfTicketsMovedToMissingInformationLastWeek(KpisSheetEntryObject kpisSheetEntryObject, JsonElement sheetEntry){
        String movedToMissingInformationDate = sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.MovedToMissingInformation.value).getAsString();
        if (movedToMissingInformationDate.isEmpty()) {
            return;
        }
        try {
            if (isTimestampInLastWeek(movedToMissingInformationDate)) {
                addOneToTicketsCount(kpisSheetEntryObject, KpisColumns.NumberOfTicketsMovedToMissingInformationLastWeek);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void addNumberOfTicketsMovedToWaitingForFieldInputLastWeek(KpisSheetEntryObject kpisSheetEntryObject, JsonElement sheetEntry){
        String movedToWaitingForFieldInputDate = sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.MovedToWaitingForFieldInput.value).getAsString();
        if (movedToWaitingForFieldInputDate.isEmpty()) {
            return;
        }
        try {
            if (isTimestampInLastWeek(movedToWaitingForFieldInputDate)) {
                addOneToTicketsCount(kpisSheetEntryObject, KpisColumns.NumberOfTicketsMovedToWaitingForFieldInputLastWeek);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void addNumberOfBugsMovedBackFromWaitingForFieldApprovalToWIPLastWeek(KpisSheetEntryObject kpisSheetEntryObject, JsonElement sheetEntry){
        String movedBackFromWaitingForFieldApprovalToWIPDate = sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.ReopenedAfterMovedToApproval.value).getAsString();
        if (movedBackFromWaitingForFieldApprovalToWIPDate.isEmpty()) {
            return;
        }
        try {
            if (isTimestampInLastWeek(movedBackFromWaitingForFieldApprovalToWIPDate)) {
                addOneToTicketsCount(kpisSheetEntryObject, KpisColumns.NumberOfBugsMovedBackFromWaitingForFieldApprovalToWIPLastWeek);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void addTicketsTimeFromNewToDone(KpisSheetEntryObject kpisSheetEntryObject, JsonElement sheetEntry){
        String movedToDoneTimestamp = sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.MovedToDoneDate.value).getAsString();
        if (movedToDoneTimestamp.isEmpty()) {
            return;
        }
        try {
            Date creationDate = timestampToDate(sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.CreationDate.value).getAsString());
            Date movedToDoneDate = timestampToDate(movedToDoneTimestamp);
            Long daysBetweenNewAndDone = TimeUnit.MILLISECONDS.toHours(movedToDoneDate.getTime() - creationDate.getTime());
            AvarageCalculator avarageCalculator = (AvarageCalculator)kpisSheetEntryObject.kpisList.get(KpisColumns.TicketsTimeFromNewToDone);
            if (avarageCalculator == null) {
                kpisSheetEntryObject.kpisList.put(KpisColumns.TicketsTimeFromNewToDone, new AvarageCalculator());
                avarageCalculator = (AvarageCalculator)kpisSheetEntryObject.kpisList.get(KpisColumns.TicketsTimeFromNewToDone);
            }
            avarageCalculator.members.add(daysBetweenNewAndDone);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void addBugsTimeFromNewToDone(KpisSheetEntryObject kpisSheetEntryObject, JsonElement sheetEntry){
        if (!isBug(sheetEntry)) {
            return;
        }
        String movedToDoneTimestamp = sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.MovedToDoneDate.value).getAsString();
        if (movedToDoneTimestamp.isEmpty()) {
            return;
        }
        try {
            Date creationDate = timestampToDate(sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.CreationDate.value).getAsString());
            Date movedToDoneDate = timestampToDate(movedToDoneTimestamp);
            Long hoursBetweenNewAndDone = TimeUnit.MILLISECONDS.toHours(movedToDoneDate.getTime() - creationDate.getTime());
            AvarageCalculator avarageCalculator = (AvarageCalculator)kpisSheetEntryObject.kpisList.get(KpisColumns.BugsTimeFromNewToDone);
            if (avarageCalculator == null) {
                kpisSheetEntryObject.kpisList.put(KpisColumns.BugsTimeFromNewToDone, new AvarageCalculator());
                avarageCalculator = (AvarageCalculator)kpisSheetEntryObject.kpisList.get(KpisColumns.BugsTimeFromNewToDone);
            }
            avarageCalculator.members.add(hoursBetweenNewAndDone);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void addBugsTimeFromStartedInvestigationToReproduced(KpisSheetEntryObject kpisSheetEntryObject, JsonElement sheetEntry){
        String reproducedTimestamp = sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.ReproducedDate.value).getAsString();
        if (reproducedTimestamp.isEmpty()) {
            return;
        }
        try {
            Date startedInvestigationDate = timestampToDate(sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.StartedInvestigationDate.value).getAsString());
            Date reproducedDate = timestampToDate(reproducedTimestamp);
            Long hoursBetweenStartedInvestigationAndReproduced = TimeUnit.MILLISECONDS.toHours(reproducedDate.getTime() - startedInvestigationDate.getTime());
            AvarageCalculator avarageCalculator = (AvarageCalculator)kpisSheetEntryObject.kpisList.get(KpisColumns.BugsTimeFromStartedInvestigationToReproduced);
            if (avarageCalculator == null) {
                kpisSheetEntryObject.kpisList.put(KpisColumns.BugsTimeFromStartedInvestigationToReproduced, new AvarageCalculator());
                avarageCalculator = (AvarageCalculator)kpisSheetEntryObject.kpisList.get(KpisColumns.BugsTimeFromStartedInvestigationToReproduced);
            }
            avarageCalculator.members.add(hoursBetweenStartedInvestigationAndReproduced);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void addOneToTicketsCount(KpisSheetEntryObject kpisSheetEntryObject, KpisColumns kpisColumns){
        if (kpisSheetEntryObject.kpisList.get(kpisColumns) == null) {
            kpisSheetEntryObject.kpisList.put(kpisColumns, "1");
        } else {
            int currentValue = Integer.parseInt((String)kpisSheetEntryObject.kpisList.get(kpisColumns));
            kpisSheetEntryObject.kpisList.put(kpisColumns, String.valueOf(currentValue + 1));
        }
    }

    private boolean isBug(JsonElement sheetEntry){
        return sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.TicketType.value).getAsString().equals("bug");
    }

}
