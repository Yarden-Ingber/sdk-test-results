package com.yarden.restServiceDemo.kpis.kpiCalculators;

import com.google.gson.JsonElement;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.kpis.TicketStates;
import com.yarden.restServiceDemo.reportService.SheetData;
import com.yarden.restServiceDemo.reportService.SheetTabIdentifier;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class KpiCalculator {

    private SheetData rawSheetData = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.KPIS.value, Enums.KPIsSheetTabsNames.RawData.value));
    private SheetData kpiSheetData = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.KPIS.value, Enums.KPIsSheetTabsNames.KPIs.value));
    private KpiSheetEntries kpiSheetEntries = new KpiSheetEntries();

    @Test
    public void buildKpisSheet() throws IOException {
        calculate();
        dumpKpisToSheet();
        SheetData.writeAllTabsToSheet();
    }

    public void calculate(){
        for (JsonElement sheetEntry: rawSheetData.getSheetData()){
            for (KpisSheetEntryObject kpisSheetEntryObject : kpiSheetEntries.getEntries().values()) {
                if (sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.Team.value).getAsString().equals(kpisSheetEntryObject.team)
                    && sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.SubProject.value).getAsString().equals(kpisSheetEntryObject.subProject)) {
//                    addCountOfOpenTicket(kpisSheetEntryObject, sheetEntry);
//                    addToNumberOfTicketCreatedLastWeek(kpisSheetEntryObject, sheetEntry);
//                    addToNumberOfBugsCreatedLastWeek(kpisSheetEntryObject, sheetEntry);
//                    addToNumberOfTicketsMovedToDoneLastWeek(kpisSheetEntryObject, sheetEntry);
//                    addToNumberOfBugsMovedToDoneLastWeek(kpisSheetEntryObject, sheetEntry);
//                    addNumberOfTicketsMovedToWaitingForFieldInputLastWeek(kpisSheetEntryObject, sheetEntry);
//                    addTicketsTimeInHoursFromNewToDone(kpisSheetEntryObject, sheetEntry);
//                    addBugsTimeInHoursFromNewToDone(kpisSheetEntryObject, sheetEntry);
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
        Team("Team"), SubProject("Sub project"), IsOnlyBugs("Is only bugs"), OpenTickets("Open tickets"),
        NumberOfTicketsCreatedLastWeek("Number of tickets created last week"), NumberOfTicketsMovedToDoneLastWeek("Number of tickets moved to done last week"),
        AverageHoursInNew("Average hours in new"), AverageHoursInTryingToReproduce("Average hours in trying to reproduce"), AverageHoursInDoing("Average hours in doing"),
        AverageHoursInWaitingForFieldInput("Average hours in waiting for field input"), AverageHoursInWaitingForRD("Average hours in waiting for R&D"),
        AverageHoursInWaitingForFieldApproval("Average hours in waiting for field approval"), AverageHoursInWaitingForProduct("Average hours in waiting for product"),
        AverageHoursInMissingQuality("Average hours in missing quality"), AverageDaysFromNewToDone("Average days from new to done"),
        NumberOfTicketsMovedToWaitingForFieldInputLastWeek("Number of tickets moved to Waiting for field input last week");

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

    public static Date timestampToDate(String timestamp) throws ParseException {
        timestamp = timestamp.substring(0,timestamp.indexOf('.'));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return dateFormat.parse(timestamp);
    }

    private void addCountOfOpenTicket(KpisSheetEntryObject kpisSheetEntryObject, JsonElement sheetEntry) {
        if(sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.MovedToStateDone.value).getAsString().isEmpty()) {
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

//    private void addToNumberOfBugsCreatedLastWeek(KpisSheetEntryObject kpisSheetEntryObject, JsonElement sheetEntry) {
//        if (!isBug(sheetEntry)) {
//            return;
//        }
//        String creationDate = sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.CreationDate.value).getAsString();
//        if (creationDate.isEmpty()) {
//            return;
//        }
//        try {
//            if (isTimestampInLastWeek(creationDate)) {
//                addOneToTicketsCount(kpisSheetEntryObject, KpisColumns.NumberOfBugsCreatedLastWeek);
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void addToNumberOfTicketsMovedToDoneLastWeek(KpisSheetEntryObject kpisSheetEntryObject, JsonElement sheetEntry){
//        String movedToDoneDate = sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.MovedToStateDone.value).getAsString();
//        if (movedToDoneDate.isEmpty()) {
//            return;
//        }
//        try {
//            if (isTimestampInLastWeek(movedToDoneDate)) {
//                addOneToTicketsCount(kpisSheetEntryObject, KpisColumns.NumberOfTicketsMovedToDoneLastWeek);
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void addToNumberOfBugsMovedToDoneLastWeek(KpisSheetEntryObject kpisSheetEntryObject, JsonElement sheetEntry){
//        if (!isBug(sheetEntry)) {
//            return;
//        }
//        String movedToDoneDate = sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.MovedToStateDone.value).getAsString();
//        if (movedToDoneDate.isEmpty()) {
//            return;
//        }
//        try {
//            if (isTimestampInLastWeek(movedToDoneDate)) {
//                addOneToTicketsCount(kpisSheetEntryObject, KpisColumns.NumberOfBugsMovedToDoneLastWeek);
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void addNumberOfTicketsMovedToWaitingForFieldInputLastWeek(KpisSheetEntryObject kpisSheetEntryObject, JsonElement sheetEntry){
//        String movedToWaitingForFieldInputDate = sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.EnterForTimeCalculationState.value + TicketStates.WaitingForFieldInput.name()).getAsString();
//        if (movedToWaitingForFieldInputDate.isEmpty()) {
//            return;
//        }
//        try {
//            if (isTimestampInLastWeek(movedToWaitingForFieldInputDate)) {
//                addOneToTicketsCount(kpisSheetEntryObject, KpisColumns.NumberOfTicketsMovedToWaitingForFieldInputLastWeek);
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void addTicketsTimeInHoursFromNewToDone(KpisSheetEntryObject kpisSheetEntryObject, JsonElement sheetEntry){
//        String movedToDoneTimestamp = sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.MovedToStateDone.value).getAsString();
//        if (movedToDoneTimestamp.isEmpty()) {
//            return;
//        }
//        try {
//            Date creationDate = timestampToDate(sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.CreationDate.value).getAsString());
//            Date movedToDoneDate = timestampToDate(movedToDoneTimestamp);
//            addTimeCalculationToAvarageCalculator(kpisSheetEntryObject, KpisColumns.TicketsTimeInHoursFromNewToDone, creationDate, movedToDoneDate);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void addBugsTimeInHoursFromNewToDone(KpisSheetEntryObject kpisSheetEntryObject, JsonElement sheetEntry){
//        if (!isBug(sheetEntry)) {
//            return;
//        }
//        String movedToDoneTimestamp = sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.MovedToStateDone.value).getAsString();
//        if (movedToDoneTimestamp.isEmpty()) {
//            return;
//        }
//        try {
//            Date creationDate = timestampToDate(sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.CreationDate.value).getAsString());
//            Date movedToDoneDate = timestampToDate(movedToDoneTimestamp);
//            addTimeCalculationToAvarageCalculator(kpisSheetEntryObject, KpisColumns.BugsTimeInHoursFromNewToDone, creationDate, movedToDoneDate);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void addBugsTimeInHoursFromAcceptedToReproduced(KpisSheetEntryObject kpisSheetEntryObject, JsonElement sheetEntry){
//        String reproducedTimestamp = sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.ReproducedDate.value).getAsString();
//        String acceptedTimestamp = sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.AcceptedDate.value).getAsString();
//        if (reproducedTimestamp.isEmpty() || acceptedTimestamp.isEmpty()) {
//            return;
//        }
//        try {
//            Date acceptedDate = timestampToDate(acceptedTimestamp);
//            Date reproducedDate = timestampToDate(reproducedTimestamp);
//            addTimeCalculationToAvarageCalculator(kpisSheetEntryObject, KpisColumns.BugsTimeInHoursFromAcceptedToReproduced, acceptedDate, reproducedDate);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void addAverageTimeInHoursBugsWaitInWaitingForFieldApprovalLastWeek(KpisSheetEntryObject kpisSheetEntryObject, JsonElement sheetEntry){
//        String movedToDoneTimestamp = sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.MovedToDoneDate.value).getAsString();
//        String movedToWaitingForApprovalTimestamp = sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.MovedToWaitingForApprovalDate.value).getAsString();
//        if (movedToDoneTimestamp.isEmpty() || movedToWaitingForApprovalTimestamp.isEmpty()) {
//            return;
//        }
//        try {
//            Date movedToDoneDate = timestampToDate(movedToDoneTimestamp);
//            Date movedToWaitingForApprovalDate = timestampToDate(movedToWaitingForApprovalTimestamp);
//            addTimeCalculationToAvarageCalculator(kpisSheetEntryObject, KpisColumns.AverageTimeInHoursBugsWaitInWaitingForFieldApprovalLastWeek, movedToWaitingForApprovalDate, movedToDoneDate);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//    }

    private void addTimeCalculationToAvarageCalculator(KpisSheetEntryObject kpisSheetEntryObject, KpisColumns kpisColumns, Date start, Date end){
        Long calculatedTime = TimeUnit.MILLISECONDS.toHours(end.getTime() - start.getTime());
        AvarageCalculator avarageCalculator = (AvarageCalculator)kpisSheetEntryObject.kpisList.get(kpisColumns);
        if (avarageCalculator == null) {
            kpisSheetEntryObject.kpisList.put(kpisColumns, new AvarageCalculator());
            avarageCalculator = (AvarageCalculator)kpisSheetEntryObject.kpisList.get(kpisColumns);
        }
        avarageCalculator.members.add(calculatedTime);
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
